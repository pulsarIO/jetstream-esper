/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Dual licensed under the Apache 2.0 license and the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationEvent;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.ebay.jestream.event.annotation.AnnotationConfiguration;
import com.ebay.jestream.event.annotation.StatementAnnotationInfo;
import com.ebay.jetstream.common.NameableThreadFactory;
import com.ebay.jetstream.config.ContextBeanChangedEvent;
import com.ebay.jetstream.counter.LongEWMACounter;
import com.ebay.jetstream.event.EventException;
import com.ebay.jetstream.event.EventSink;
import com.ebay.jetstream.event.EventSinkList;
import com.ebay.jetstream.event.JetstreamEvent;
import com.ebay.jetstream.event.RetryEventCode;
import com.ebay.jetstream.event.processor.AbstractQueuedEventProcessor;
import com.ebay.jetstream.event.processor.EventProcessRequest;
import com.ebay.jetstream.management.Management;
import com.ebay.jetstream.messaging.MessageServiceTimer;
import com.ebay.jetstream.notification.AlertListener;
import com.ebay.jetstream.notification.AlertListener.AlertStrength;
import com.ebay.jetstream.xmlser.Hidden;
import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPException;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventType;
import com.espertech.esper.client.soda.AnnotationPart;
import com.espertech.esper.client.soda.EPStatementObjectModel;


@ManagedResource(objectName = "Event/Processor", description = "Esper Processor")
public class EsperProcessor extends AbstractQueuedEventProcessor {

	Logger logger = LoggerFactory.getLogger("com.ebay.jetstream.event.processor.esper.EsperProcessor");
	private Random m_random = new SecureRandom();
	private AtomicInteger m_unknownEventCount = new AtomicInteger();
	
	private EPL m_epl;
	private EsperEventListener m_esperEventListener;
	private CopyOnWriteArrayList<EsperInternals> esperEngineHolder = new CopyOnWriteArrayList<EsperInternals>();
	private final LongEWMACounter m_avgSendEventExeNanos = new LongEWMACounter(60, MessageServiceTimer.sInstance().getTimer());
	private final Map<String, JetstreamEvent> m_recentEvents = new ConcurrentHashMap<String, JetstreamEvent>();
	private final Map<String, JetstreamEvent> m_recentEventsFail = new ConcurrentHashMap<String, JetstreamEvent>();
	private final Set<String> m_setKnownEvent = new HashSet<String>();
	private EsperExceptionHandler m_esperExceptionHandler;
	private String engineURI = null;
	private ScheduledExecutorService m_watchDog = Executors.newScheduledThreadPool(1, new NameableThreadFactory("EsperWatchDog"));
	private int m_checkIntervalInSeconds = 50 * 60;

	@ManagedAttribute
	public int getCheckIntervalInSeconds() {
		return m_checkIntervalInSeconds;
	}

	public void setCheckIntervalInSeconds(int checkIntervalInSeconds) {
		this.m_checkIntervalInSeconds = checkIntervalInSeconds;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (getAlertListener() != null) {
			getAlertListener().sendAlert(this.getBeanName(), "INIT", AlertListener.AlertStrength.GREEN);
		}
		Management.addBean(getBeanName(), this);
		for (EventSink eventSink : ((EventSinkList)getEventSinks()).getSinks()) {
			if (!m_esperEventListener.getEventSinks().contains(eventSink))
				m_esperEventListener.addEventSink(eventSink);
		}
		m_esperEventListener.setAdviceListener(getAdviceListener());
		m_esperEventListener.setPropagateEventOnFailure(getConfiguration().isPropagateEventOnFailure());
		m_esperEventListener.setAnnotationConfig(getConfiguration().getAnnotationConfigMap());

		// check the exception status at the intervals if it's in exception, submit stop replay command;
		m_watchDog.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					if (getEsperExceptionHandler() != null) {
						if (getEsperExceptionHandler().IsInException()) {
							getAdviceListener().stopReplay();
						}
					}
				} catch (Exception ex) {
					logger.error( ex.getLocalizedMessage() , ex);
				}
			}
		}, 5, m_checkIntervalInSeconds, TimeUnit.SECONDS);

		super.afterPropertiesSet();
		
		/* if it is placed in the base class, then we may end up no update if the 
		 * subclass does not report status. so leave it here 
		 */
		if (getAlertListener() != null) {
			getAlertListener().sendAlert(this.getBeanName(),  "OK", AlertListener.AlertStrength.GREEN);
		}
	}

	public long getAverageExeNanosToEsper() {
		return m_avgSendEventExeNanos.get();
	}
	
	public String getRegisteredEvents() {
		StringBuilder bldr = new StringBuilder();
		for (String strEvent : m_setKnownEvent) {
			if (bldr.length() > 0)
				bldr.append(",");
			bldr.append(strEvent);
		}
		return bldr.toString();
	}
	
	public EsperEventListener getEsperEventListener() {
		return m_esperEventListener;
	}

	public int getUnknownEventCount() {
		return m_unknownEventCount.get();
	}
	
	public Map<String, String> getLastEventsSuccess() {
		Map<String, String> mapCopy = new HashMap<String, String>();
		for (String strEventType : m_recentEvents.keySet())
			mapCopy.put(strEventType, m_recentEvents.get(strEventType).toString());
		return mapCopy;
	}
	
	public void registerError(Throwable t, JetstreamEvent event) {
		if (event != null)
			m_recentEventsFail.put(event.getEventType(), event);
		super.registerError(t, event);
	}
	
	public Map<String, String> getLastEventsFailure() {
		Map<String, String> mapCopy = new HashMap<String, String>();
		for (String strEventType : m_recentEventsFail.keySet())
			mapCopy.put(strEventType, m_recentEventsFail.get(strEventType).toString());
		return mapCopy;
	}
	
	@Required
	public void setEpl(EPL epl) {
		m_epl = epl;
	}

	public void setEsperEventListener(EsperEventListener listener) {
		m_esperEventListener = listener;
	}
	
	@Override
	public void stop() {
		super.stop();
		esperEngineHolder.get(0).clear();
		if (m_watchDog != null) {
			m_watchDog.shutdownNow();
		}
	}

	@Override
	public void shutDown() {
		pausePublisher("Application getting gracefulShutdown");
		while (getQueuedEventCount() != 0) {
			try {
				Thread.sleep(100);
			}catch (InterruptedException e) {
				logger.error( e.getLocalizedMessage() , e);
			}
		}
		//If the queue is empty, flush Esper context
		esperEngineHolder.get(0).getEsperService().getEPRuntime().getEventSender("EsperEndEvent").sendEvent(new Object());
		try {
			Thread.sleep(100);
		}catch (InterruptedException e) {
			logger.error( e.getLocalizedMessage() , e);
		}
		stop(true);
		esperEngineHolder.get(0).clear();
		
		logger.warn( getBeanName() + " Shutdown has been completed");
		if (m_watchDog != null) {
			m_watchDog.shutdownNow();
		}
	}

	@Override
	protected String getComponentName() {
		return "EsperProcessor";
	}

	@SuppressWarnings("unchecked")
	@Hidden
	@Override
	protected EsperConfiguration getConfiguration() {
		return super.getConfiguration();
	}

	@Override
	protected EventProcessRequest getProcessEventRequest(JetstreamEvent event) {
		return new ProcessEventRequest(getEsperEngine(), event, this);
	}

	@Override
	protected void init() {
		engineURI = getBeanName() + m_random.nextInt();
		EsperInternals esper = new EsperInternals(engineURI);
		boolean success = esper.init();
		if (!success) {
			if (getEsperExceptionHandler() != null) {
				getEsperExceptionHandler().setExceptionStatus(true);
			}
			if(getAdviceListener() != null) {
				getAdviceListener().stopReplay();
			}
		} 
		
		addEsperEngine(esper);
		
		if (success) {
			if(getAdviceListener() != null) {
				getAdviceListener().startReplay();
			}
		}
	}
	
	protected boolean reinit() {
		String newEgineURI = getBeanName() + m_random.nextInt();
		//set new EngineURI to the processor. if init is not successful, will revert back to old engineURI
		String oldEngineURI = engineURI;
		engineURI = newEgineURI;
		EsperInternals esper = new EsperInternals(newEgineURI);
		boolean success = esper.init();
		if (success) {
			addEsperEngine(esper);
		} else {
			//reinit is not successful so reverting engineuri to old one.
			engineURI = oldEngineURI;
			esper.getEsperService().destroy();
			esper.clear();
		}
		
		return success;
	}

	protected void fireSendEvent(JetstreamEvent event) {
		// no-op, since it's the EsperListener that sends events to sinks
	}
	
	@Override
	public void processApplicationEvent(ApplicationEvent event) {

		if (event instanceof ContextBeanChangedEvent) {
			
			ContextBeanChangedEvent bcInfo = (ContextBeanChangedEvent)event;
			 
			
			if (bcInfo.isChangedBean((EventSinkList) getEventSinks())) {
					//update sinklist to EsperEventListener.
					m_esperEventListener.setEventSinks((EventSinkList)bcInfo.getChangedBean());
			}
			
			// update event definition bean
			if(bcInfo.isChangedBean(getConfiguration().getDeclaredEvents())){
				getConfiguration().setDeclaredEvents((EsperDeclaredEvents)bcInfo.getChangedBean());
				// known events will be updated when EPL update happens
				
				if(getAdviceListener() != null) 
					getAdviceListener().startReplay();
			}	

			if (bcInfo.getBeanName().equals(getEpl().getBeanName())) {
				EPL oldEPL = getEpl();

				logger.info( 
						"Received dynamic notification to apply new EPLs");
						

				try {
					Object objChangeBean = bcInfo.getChangedBean();
					EPL epl = (objChangeBean instanceof EPL) ? (EPL) objChangeBean
							: getEpl();
					setEpl(epl);

					try {
						if (reinit()) {
							// resetting alert status
							if (getEsperExceptionHandler() != null) {
								getEsperExceptionHandler().clearAlertStatus();
							}

							// post advice to start replay
							if (getAdviceListener() != null) {
								getAdviceListener().startReplay();
							}
							
							queueEvent(new ProcessDestroyEngineRequest(
									esperEngineHolder.remove(1)));
							logger
									.info( 
											"Received dynamic notification: Applied new EPLs");
											

							
							Management.removeBeanOrFolder(oldEPL.getBeanName(), oldEPL);
							Management.addBean(epl.getBeanName(), epl);
						} else {
							  rollback(oldEPL, epl);
						}
						
					} catch (RuntimeException rte) {
						rollback(oldEPL, epl);
					}
				} catch (Exception e) {
					setEpl(oldEPL);
					logger.error( e.getLocalizedMessage(), e);
				}
			}
		}
		super.processApplicationEvent(event);
	}

	private void rollback(EPL oldEPL, EPL epl) {
		setEpl(oldEPL);
		Management.removeBeanOrFolder(epl.getBeanName(), epl);
		Management.addBean(oldEPL.getBeanName(), oldEPL);
		logger
				.error( "New EPL is not Working out...So reverting to OLD EPL");
		/*** send out an alert and update the dashboard */
		if (getAlertListener() != null) {
			getAlertListener().sendAlert(this.getBeanName(), "New EPL is not Working out...So reverting to OLD EPL",
					AlertListener.AlertStrength.RED);

		}
	}
	
	/**
	 * Just to validate the given EPL. 
	 * @param epl
	 * @return
	 * @throws EPException
	 */
	public boolean compileEPL(EPL epl) throws EPException {
		EsperInternals esperInternals = null;
		try {
			if (epl != null) {
				esperInternals = new EsperInternals("compileEngine", epl);
				esperInternals.compile(false);
				
				return true;
			}
			return false;
		} finally {
			if (esperInternals != null) {
				esperInternals.getEsperService().destroy();
				esperInternals.clear();
			}
		}

	}

	/**
	 * - When Exception reaches beyond threshold, don't submit event to queue. Drop the event.
	 */
	protected void queueEvent(JetstreamEvent event) throws EventException {
		
		if (getEsperExceptionHandler() != null) {
			if (getEsperExceptionHandler().IsInException()) {

				if (!getEsperExceptionHandler().isAlertPosted()) {
					postAlert(getEsperExceptionHandler().getLastException(),
							AlertStrength.RED);
					getEsperExceptionHandler().setAlertPostedStatus(true);
					if (logger.isDebugEnabled())
							logger
							.debug( 
									"Esper Exception threshold reached...Sending Alert..");
									
						
					// if we have an advice listener we will advice them to stop replay
					// they will be adviced to start replay when new EPL is deployed

					if(getAdviceListener() != null)
						getAdviceListener().stopReplay();
				} 
					
				if(getAdviceListener() != null){
						getAdviceListener().retry(event, RetryEventCode.MSG_RETRY,getEsperExceptionHandler().getLastException());
						
				}else{
					//AdviseListener not available. no further submission to engine... drop it..
					if (logger.isDebugEnabled())
						logger.debug( 
								"Esper Exception threshold reached...Dropping events");
					incrementEventDroppedCounter();
				}	
			} else {
				queueEventInternal(event);
			}
		} else {
			queueEventInternal(event);
		}

	}
	
	private void queueEventInternal(JetstreamEvent event){
		
		if (m_setKnownEvent.contains(event.getEventType())){
			super.queueEvent(event);
		}	
		else {
			m_recentEventsFail.put(event.getEventType(), event);
			m_unknownEventCount.incrementAndGet();
		}
		
	}
	
	public String getEngineURI() {
		return engineURI;
	}
	
	private void addEsperEngine(EsperInternals esperInternals){
		esperEngineHolder.add(0, esperInternals);
	}

	private EPL getEpl() {
		return m_epl;
	}

	private EsperInternals getEsperEngine(){
		return esperEngineHolder.get(0);
	}

	class EsperInternals {
		
		private EPServiceProvider m_esperService;
		private String m_strName;
		private EPL m_suppliedEpl;
		private EPAdministrator m_epAdmin;
		
		EsperInternals(String strName) {
			m_strName = strName;
		}
		
		EsperInternals(String strName, EPL epl) {
			m_strName = strName;
			m_suppliedEpl = epl;
		}
		
		public EPServiceProvider getEsperService() {
			return m_esperService;
		}

		void clear() {
			// nothing yet
		}
		
		void  compile(boolean checkSink) throws EPException{
			
			m_esperService = EPServiceProviderManager.getProvider(m_strName, getConfiguration().getEsperConfiguration());
			
			m_epAdmin = m_esperService.getEPAdministrator(); // 6908 fix - getAdmin caused recursion here
			
			for(Map.Entry<String, AnnotationConfiguration> annotConfig : getConfiguration().getAnnotationConfigMap().entrySet()){
				m_epAdmin.getConfiguration().addImport(annotConfig.getValue().getClassName());
			}
			m_epAdmin.getConfiguration().addImport(com.ebay.jetstream.epl.EPLUtilities.class);
			m_epAdmin.getConfiguration().addImport(com.ebay.jetstream.epl.EPLUtils.class);
			m_epAdmin.getConfiguration().addPlugInSingleRowFunction("toJson", "com.ebay.jetstream.epl.EPLUtils", "toJsonString");
			m_epAdmin.getConfiguration().addPlugInSingleRowFunction("fromJson", "com.ebay.jetstream.epl.EPLUtils", "fromJsonString");
			m_epAdmin.getConfiguration().addPlugInSingleRowFunction("generateEventId", "com.ebay.jetstream.epl.EPLUtils", "generateEventId");
			m_epAdmin.getConfiguration().addEventType("EsperStartup", Object.class);
			m_epAdmin.getConfiguration().addEventType("EsperEndEvent", Object.class);
			m_epAdmin.getConfiguration().addPlugInAggregationFunctionFactory("histogram", "com.ebay.jetstream.epl.HistogramAggregatorFactory");
			m_epAdmin.getConfiguration().addPlugInAggregationFunctionFactory("distinctcount", "com.ebay.jetstream.event.processor.esper.aggregates.CardinalityAggregatorFactory");
			m_epAdmin.getConfiguration().addPlugInAggregationFunctionFactory("percentile", "com.ebay.jetstream.event.processor.esper.aggregates.QuantileAggregatorFactory");
			m_epAdmin.getConfiguration().addPlugInAggregationFunctionFactory("topN", "com.ebay.jetstream.event.processor.esper.aggregates.TopKAggregatorFactory");
			
			
			m_suppliedEpl = m_suppliedEpl == null ? getEpl() : m_suppliedEpl;  
			
			for(String statement : m_suppliedEpl.getStatements()) {

				EPStatementObjectModel model = m_epAdmin.compileEPL(statement);
				
				List<AnnotationPart> annots = model.getAnnotations();
				if (getConfiguration().getAnnotationProcesssor() != null) {
					Map<String, List<AnnotationPart>> annotationPartsMapList = new HashMap<String, List<AnnotationPart>>();
					List<AnnotationPart> parts = null;
					for (AnnotationPart part : annots) {
						parts = annotationPartsMapList.get(part.getName());

						if (parts == null) {
							parts = new LinkedList<AnnotationPart>();
							annotationPartsMapList.put(part.getName(),
									parts);
						}
						parts.add(part);
					}
					EPStatement epStmt = null;
					if (annotationPartsMapList.size() == 0) {
						epStmt = m_epAdmin.create(model, null,
								new StatementAnnotationInfo());
					} else {
						StatementAnnotationInfo annotationInfo;
						try {
							annotationInfo = getConfiguration()
									.getAnnotationProcesssor()
									.getStatementAnnotationInfo(
											annots,
											statement,
											model,
											annotationPartsMapList,
											((EventSinkList) getEventSinks())
													.getSinks());
						} catch (Exception e) {
							throw new EPException("Exception from Annotation processing.." , e);
						}
						epStmt = m_epAdmin
								.create(model, null, annotationInfo);
						if(m_esperEventListener != null)
							epStmt.addListener(m_esperEventListener);
					}

				}
				
			}
			
		}
		
		boolean init() {
			try{
				
				compile(true);
				for (EventType type : m_epAdmin.getConfiguration().getEventTypes())
					m_setKnownEvent.add(type.getName());
				
				m_esperService.getEPRuntime().getEventSender("EsperStartup").sendEvent(new Object());
				
				return true;
			}
			catch (Throwable e) {
				if (getAlertListener() != null) {
					getAlertListener().sendAlert(getBeanName(), " Esper Processor: init failed with e=" +e, AlertListener.AlertStrength.RED);
				}
				if (getEsperExceptionHandler() != null) {
					getEsperExceptionHandler().setLastException(e.getMessage());
				}
				registerError(e);
				e.printStackTrace(System.err);
			
				logger.error( "Processor " + getBeanName()
						+ " failed to prepare. The application will be shut down immediately. Exception: " + e.getMessage());
						
				
				return false;
			}
		}

		void processEvent(JetstreamEvent event) {
			long lBeforeNanos = System.nanoTime();
			String eventType = event.getEventType();
			m_esperService.getEPRuntime().sendEvent(event, eventType);
			m_avgSendEventExeNanos.add(System.nanoTime() - lBeforeNanos);
			m_recentEvents.put(eventType, event);
		}

	}
	
	@Override
	public int getPendingEvents() {
		return 0;
	}
	
	public EsperExceptionHandler getEsperExceptionHandler() {
		return m_esperExceptionHandler;
	}

	public void setEsperExceptionHandler(
			EsperExceptionHandler m_esperExceptionHandler) {
		this.m_esperExceptionHandler = m_esperExceptionHandler;
	}

}
