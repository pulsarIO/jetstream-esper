/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Dual licensed under the Apache 2.0 license and the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.esper.annotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationListener;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.ebay.jetstream.config.AbstractNamedBean;
import com.ebay.jetstream.config.ApplicationInformation;
import com.ebay.jetstream.config.Configuration;
import com.ebay.jetstream.config.RootConfiguration;
import com.ebay.jetstream.event.EventException;
import com.ebay.jetstream.event.EventSink;
import com.ebay.jetstream.event.JetstreamEvent;
import com.ebay.jetstream.event.JetstreamReservedKeys;
import com.ebay.jetstream.event.processor.esper.EPL;
import com.ebay.jetstream.event.processor.esper.EsperConfiguration;
import com.ebay.jetstream.event.processor.esper.EsperProcessor;
import com.ebay.jetstream.event.support.AbstractEventSource;
import com.ebay.jetstream.event.support.channel.PipelineFlowControl;

@ManagedResource(objectName = "ESPTest", description = "EsperEventProcessor Unit Test")
public class TestCustomAnnotation  extends AbstractEventSource implements ApplicationListener, InitializingBean, ApplicationEventPublisherAware {

  static class ESPTestThreadFactory implements ThreadFactory {

	  public Thread newThread(Runnable r) {
      Thread t = new Thread(r);
      t.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			public void uncaughtException(Thread t, Throwable e) {
				final Logger logger = LoggerFactory.getLogger("com.ebay.rdbd.queueworker");
				logger.error( "Exception \'" + e.getMessage() + "\' in thread " + t.getId());
			}
		});
      return t;
    }
  }

  private static final Logger testLogger = LoggerFactory.getLogger("com.ebay.jetstream.esper.annotations.TestCustomAnnotation");
  private static final int THREADS_NUM = 2;
  private static final Configuration s_springConfiguration = new RootConfiguration(new ApplicationInformation(
      "ESPTest", "0.0"), new String[] { "src/test/java/com/ebay/jetstream/esper/annotations/EsperAnnotationTest.xml" });

/*  static {
    org.apache.log4j.xml.DOMConfigurator.configure("src/test/java/com/ebay/jetstream/event/processor/esper/log4j.xml");
  }*/

  private final PipelineFlowControl m_flowHandler = new PipelineFlowControl(this);

  public TestCustomAnnotation() {
  }

  public void afterPropertiesSet() throws Exception {
    
  }


  private JetstreamEvent generateJetstreamEvent(int id) {
    HashMap<String, Object> map = new HashMap<String, Object>();
    String field1 = "123.0";
    map.put("id", id);
    map.put("field1", field1);
    return new JetstreamEvent("ESPTestEvent1", null, map);
  }
  
  private JetstreamEvent generateTestEvent(int id) {
	    HashMap<String, Object> map = new HashMap<String, Object>();
	    String field1 = "testing";
	    map.put("id", id);
	    map.put("field1", field1);
	    return new JetstreamEvent("TestEvent", null, map);
	  }

  private EPL getEpl(String name) {
    return (EPL) s_springConfiguration.getBean(name);
  }

  private EsperConfiguration getEsperConfiguration() {
    return (EsperConfiguration) s_springConfiguration.getBean("EsperConfiguration");
  }

  private EsperProcessor getProcessor(String name) {
    return (EsperProcessor) s_springConfiguration.getBean(name);
  }

  @Override
  public void onApplicationEvent(ApplicationEvent event) {
	  testLogger.warn( "ESPTest received " + event);
  }

  @Override
  public void pause() {
    // TODO Auto-generated method stub

  }

  @Override
  protected void processApplicationEvent(ApplicationEvent event) {
    // TODO Auto-generated method stub

  }

  @Override
  public void resume() {
    // TODO Auto-generated method stub

  }

  public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
    m_flowHandler.setApplicationEventPublisher(applicationEventPublisher);
    // Management.addBean(this.getClass().getSimpleName(), this);
  }


  @Test
  public void testFireAndForget() throws InterruptedException {
    EsperProcessor processor = getProcessor("TestProcessor1");
   
    processor.addEventSink(new EventSink() {
        public String getBeanName() {
          // TODO Auto-generated method stub
          return null;
        }

        public void sendEvent(JetstreamEvent event) throws EventException {
          System.out.println("Receive event " + event); //KEEPME
        }
      });
   
    List<JetstreamEvent> events = new ArrayList<JetstreamEvent>();
    for (int i = 0; i < THREADS_NUM; i++) {
      events.add(generateJetstreamEvent(i));
      events.add(generateTestEvent(i));

    }
    for (JetstreamEvent event : events) {
      TimeUnit.MILLISECONDS.sleep(1);
      processor.sendEvent(event);
    }
    Thread.sleep(1000);
    processor.stop();
    
    System.out.println(processor.getEsperEventListener().getEventReceived());
    assertEquals(8 , Integer.parseInt(processor.getEsperEventListener().getEventReceived().toString()));
    for(EventSink sink: processor.getEventSinks()){
    	if( sink instanceof SampleOMCEventSink )
    		assertFalse(((SampleOMCEventSink)sink).assertFailure);
    	if( sink instanceof SampleDBEventSink )
    		assertFalse(((SampleDBEventSink)sink).assertFailure);
    	if( sink instanceof SampleNewStreamSink )
    		assertFalse(((SampleNewStreamSink)sink).assertFailure);
    	
    }	
  }

  

static class SampleOMCEventSink extends AbstractNamedBean implements EventSink{

	public boolean assertFailure = false;
	
	public SampleOMCEventSink(){
		
	}

	@Override
	public void sendEvent(JetstreamEvent event) throws EventException {
		try{
			System.out.println("Event : " + event.toString());
			assertEquals("ESPTestEvent1", event.getEventType());
			assertEquals("sample/topic", event.getForwardingTopics()[0]);
			assertTrue(event.containsKey(JetstreamReservedKeys.MessageAffinityKey.toString()));
			assertTrue(Boolean.valueOf((String)event.getMetaData(JetstreamReservedKeys.EventBroadCast.toString())));
		}catch(Throwable t ){
			assertFailure = true;
			t.printStackTrace();
		}
		
	}
	
}


static class SampleNewStreamSink extends AbstractNamedBean implements EventSink{

	public boolean assertFailure = false;
	
	public SampleNewStreamSink(){
		
	}

	@Override
	public void sendEvent(JetstreamEvent event) throws EventException {
		try{
			System.out.println("Event : " + event.toString());
			assertEquals("NewStreamName", event.getEventType());
		}catch(Throwable t ){
			assertFailure = true;
			t.printStackTrace();
		}
		
	}
	
}



static class SampleDBEventSink extends AbstractNamedBean implements EventSink{

	public boolean assertFailure = false;
	
	public SampleDBEventSink(){
		
	}

	@Override
	public void sendEvent(JetstreamEvent event) throws EventException {
			try {
				Map<String, Object> mapMetaData = (Map<String, Object>)event.get(JetstreamReservedKeys.EventMetaData.toString());
				assertEquals("ESPTestEvent1", mapMetaData.get("table"));
			} catch (Throwable t) {
				assertFailure = true;
				t.printStackTrace();
			}
	
	}
	
}

}
