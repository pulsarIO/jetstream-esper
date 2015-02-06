/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Dual licensed under the Apache 2.0 license and the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper;

import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.springframework.jmx.export.annotation.ManagedResource;

import com.ebay.jestream.event.annotation.AnnotationConfiguration;
import com.ebay.jetstream.event.processor.QueuedEventProcessorConfiguration;
import com.ebay.jetstream.event.processor.esper.annotation.config.AnnotationConfigLoader;
import com.ebay.jetstream.event.processor.esper.annotation.config.DefaultAnnotationConfiguration;
import com.ebay.jetstream.event.processor.esper.annotation.processor.AnnotationProcessorFacade;
import com.ebay.jetstream.management.Management;
import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.ConfigurationDBRef;
import com.espertech.esper.client.ConfigurationDBRef.ConnectionLifecycleEnum;
import com.espertech.esper.client.ConfigurationDBRef.MetadataOriginEnum;
import com.espertech.esper.client.ConfigurationEngineDefaults;
import com.espertech.esper.client.ConfigurationEventTypeLegacy;
import com.espertech.esper.client.ConfigurationEventTypeMap;
import com.espertech.esper.client.ConfigurationEventTypeObjectArray;
import com.espertech.esper.client.ConfigurationEventTypeXMLDOM;
import com.espertech.esper.client.ConfigurationException;
import com.espertech.esper.client.ConfigurationInformation;
import com.espertech.esper.client.ConfigurationMethodRef;
import com.espertech.esper.client.ConfigurationOperations;
import com.espertech.esper.client.ConfigurationPlugInAggregationFunction;
import com.espertech.esper.client.ConfigurationPlugInAggregationMultiFunction;
import com.espertech.esper.client.ConfigurationPlugInEventRepresentation;
import com.espertech.esper.client.ConfigurationPlugInEventType;
import com.espertech.esper.client.ConfigurationPlugInPatternObject;
import com.espertech.esper.client.ConfigurationPlugInSingleRowFunction;
import com.espertech.esper.client.ConfigurationPlugInSingleRowFunction.FilterOptimizable;
import com.espertech.esper.client.ConfigurationPlugInSingleRowFunction.ValueCache;
import com.espertech.esper.client.ConfigurationPlugInView;
import com.espertech.esper.client.ConfigurationPlugInVirtualDataWindow;
import com.espertech.esper.client.ConfigurationPluginLoader;
import com.espertech.esper.client.ConfigurationRevisionEventType;
import com.espertech.esper.client.ConfigurationVariable;
import com.espertech.esper.client.ConfigurationVariantStream;
import com.espertech.esper.client.EventType;



@ManagedResource(objectName = "Event/Processor", description = "Esper Event Processor Configuration")
public class EsperConfiguration extends QueuedEventProcessorConfiguration implements ConfigurationOperations, ConfigurationInformation, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7564564473976128410L;
	private boolean m_bIsTimeSourceNano;
	private EsperDeclaredEvents m_declaredEvents;
	private boolean m_bTimeSourceNano;
	private Configuration m_esperconfig = new Configuration();
	private Map<String, AnnotationConfiguration> annotationConfigMap = new HashMap<String, AnnotationConfiguration>();
	private List<AnnotationConfiguration> extendedAnnotationConfiguration;
	private AnnotationProcessorFacade annotationProcessor = new AnnotationProcessorFacade();
	
	@Override
	public void afterPropertiesSet() throws Exception {
		Management.removeBeanOrFolder(getBeanName(), this);
		Management.addBean(getBeanName(), this);
		
		
		// load Default Annotations
		AnnotationConfigLoader loader = new AnnotationConfigLoader();
		DefaultAnnotationConfiguration defaultannconfig = loader
				.load_annotation_config();
		if (defaultannconfig != null) {
			for (AnnotationConfiguration config : defaultannconfig
					.getDefaultAnnotationConfiguration()) {
				annotationConfigMap.put(config.getClassName().getSimpleName(),
						config);
			}
		}
		
		//load extended Annotations
		if(getAnnotationConfig() != null){
			for(AnnotationConfiguration config : extendedAnnotationConfiguration){
				annotationConfigMap.put(config.getClassName().getSimpleName(), config);
			}
		}
		annotationProcessor.setAnnotationConfigurationMap(annotationConfigMap);
	}
	
	public AnnotationProcessorFacade getAnnotationProcesssor() {
		return annotationProcessor;
	}
	
	public EsperDeclaredEvents getDeclaredEvents() {
		return m_declaredEvents;
	}
	
	protected Configuration getEsperConfiguration() {
		return m_esperconfig;
	}
	
	/**
	 * INSERT INTO clause timeout in milliseconds (Esper's internal default is 100)
	 * 
	 * @return long
	 */
	public long getInsertIntoDispatchTimeout() {
		return getEngineDefaults().getThreading().getInsertIntoDispatchTimeout();
	}

	/**
	 * Listener timeout in milliseconds (Esper's internal default is 1000)
	 * 
	 * @return long
	 */
	public long getListenerDispatchTimeout() {
		return getEngineDefaults().getThreading().getListenerDispatchTimeout();
	}

	/**
	 * Gets the timer resolution in milliseconds, see s_engineSettings property
	 * 
	 * @return the timer resolution in milliseconds
	 */
	public long getMsecResolution() {
		return getEngineDefaults().getThreading().getInternalTimerMsecResolution();
	}
	
	/**
	 * INSERT INTO clause order preservation (Esper's default is true, but for better multithreading we use false)
	 * 
	 * @return boolean
	 */
	public boolean isInsertIntoDispatchPreserveOrder() {
		return getEngineDefaults().getThreading().isInsertIntoDispatchPreserveOrder();
	}

	/**
	 * Returns true if internal timer is enabled, see s_engineSettings property
	 * 
	 * @return true if internal timer enabled
	 */
	public boolean isInternalTimerEnabled() {
		return getEngineDefaults().getThreading().isInternalTimerEnabled();
	}

	/**
	 * Listener order preservation (Esper's default is true, but for better multithreading we use false)
	 * 
	 * @return boolean
	 */
	public boolean isListenerDispatchPreserveOrder() {
		return getEngineDefaults().getThreading().isListenerDispatchPreserveOrder();
	}
	

	public boolean isTimeSourceNano() {
		return m_bIsTimeSourceNano;
	}

	/**
	 * Sets a list of packages to import
	 * 
	 * @param autoImportList
	 */
	public void setAutoImport(List<String> autoImportList) {
		for (String autoImport : autoImportList) {
			addImport(autoImport.trim());
		}
	}
	
	/**
	 * Sets the declared event structures, and registers those structures for use.
	 * 
	 * @param declaredEvents
	 *          the declared event structures
	 */
	public void setDeclaredEvents(EsperDeclaredEvents declaredEvents) {
		m_declaredEvents = declaredEvents;
		for (AbstractEventType eventType : declaredEvents.getEventTypes())
			addEventType(eventType);	
	}

	/**
	 * Turns Esper internal EXECUTION logging on/off
	 * 
	 * @param enabled
	 */
	public void setExecutionLogging(boolean enabled) {
		getEngineDefaults().getLogging().setEnableExecutionDebug(enabled);
	}

	/**
	 * Sets insert into clause order preservation (Esper's default is true, but for better multithreading we use false)
	 * 
	 * @param enabled
	 */
	public void setInsertIntoDispatchPreserveOrder(boolean enabled) {
		getEngineDefaults().getThreading().setInsertIntoDispatchPreserveOrder(enabled);
	}

	/**
	 * Sets INSERT INTO clause timeout in milliseconds (Esper's internal default is 100)
	 * 
	 * @param timeout
	 */
	public void setInsertIntoDispatchTimeout(long timeout) {
		getEngineDefaults().getThreading().setInsertIntoDispatchTimeout(timeout);
	}

	/**
	 * Internal timer state. We disable it to set 1 ms granularity
	 * 
	 * @param enabled
	 */
	public void setInternalTimerEnabled(boolean enabled) {
		getEngineDefaults().getThreading().setInternalTimerEnabled(enabled);
	}

	/**
	 * Sets listener order preservation (Esper's default is true, but for better multithreading we use false)
	 * 
	 * @param enabled
	 */
	public void setListenerDispatchPreserveOrder(boolean enabled) {
		getEngineDefaults().getThreading().setListenerDispatchPreserveOrder(enabled);
	}

	/**
	 * Sets listener timeout in milliseconds (Esper's internal default is 1000)
	 * 
	 * @param timeout
	 */
	public void setListenerDispatchTimeout(long timeout) {
		getEngineDefaults().getThreading().setListenerDispatchTimeout(timeout);
	}

	/**
	 * Internal timer resolution in milliseconds. Esper's default is 100, but we better use 1 finer granularity or even
	 * switch to nanos
	 * 
	 * @param resolution
	 */
	public void setMsecResolution(long resolution) {
		getEngineDefaults().getThreading().setInternalTimerMsecResolution(resolution);
	}

	/**
	 * Turns Esper internal TIMER logging on/off
	 * 
	 * @param enabled
	 */
	public void setTimerLogging(boolean enabled) {
		getEngineDefaults().getLogging().setEnableTimerDebug(enabled);
	}

	public void setTimeSourceNano(boolean bTimeSourceNano) {
		if (m_bTimeSourceNano != bTimeSourceNano) {
			m_bTimeSourceNano = bTimeSourceNano;
			getEngineDefaults().getTimeSource().setTimeSourceType(m_bTimeSourceNano ? 
					ConfigurationEngineDefaults.TimeSourceType.NANO : ConfigurationEngineDefaults.TimeSourceType.MILLI);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void addEventType(AbstractEventType type) {
		String strAlias = type.getEventAlias();
		Object objDefinition = type.getEventDefinition();
		if (objDefinition instanceof Map<?, ?>)
			addEventType(strAlias, (Map<String, Object>)objDefinition);
		else if (objDefinition instanceof String)
			addEventType(strAlias, (String)objDefinition);
		else if (objDefinition instanceof Class<?>)
			addEventType(strAlias, (Class<?>)objDefinition);
		else
			throw new IllegalArgumentException("Unknown event type: " + 
					(objDefinition != null ? objDefinition.getClass().getName() : "null"));
	}

	@Override
	public Map<String, ConfigurationDBRef> getDatabaseReferences() {
		return m_esperconfig.getDatabaseReferences();
	}

	public void setDatasources(List<DataSourceConfig> src) {
		if (src != null) {
			for (DataSourceConfig ds : src) {
				ConfigurationDBRef configDB = new ConfigurationDBRef();
				configDB.setDriverManagerConnection("com.ebay.jetstream.dal.jdbc.DalJdbcDriver", "_" + ds.getDsName(), null);
				//configDB.setDriverManagerConnection(, "_" + ds.getDsName(), null);
				configDB.setMetadataOrigin(MetadataOriginEnum.SAMPLE);
				configDB.setConnectionLifecycleEnum(ConnectionLifecycleEnum.POOLED);
				getDatabaseReferences().put(ds.getEplRefName(), configDB);
			}
		}
	}
	
	@Override
	public String getEPServicesContextFactoryClassName() {
		return m_esperconfig.getEPServicesContextFactoryClassName();
	}

	@Override
	public ConfigurationEngineDefaults getEngineDefaults() {
		return m_esperconfig.getEngineDefaults();
	}

	@Override
	public Set<String> getEventTypeAutoNamePackages() {
		return m_esperconfig.getEventTypeAutoNamePackages();
	}

	@Override
	public Map<String, String> getEventTypeNames() {
		return m_esperconfig.getEventTypeNames();
	}

	@Override
	public Map<String, ConfigurationEventTypeLegacy> getEventTypesLegacy() {
		return m_esperconfig.getEventTypesLegacy();
	}

	@Override
	public Map<String, Properties> getEventTypesMapEvents() {
		return m_esperconfig.getEventTypesMapEvents();
	}

	@Override
	public Map<String, Map<String, Object>> getEventTypesNestableMapEvents() {
		return m_esperconfig.getEventTypesNestableMapEvents();
	}

	@Override
	public Map<String, Map<String, Object>> getEventTypesNestableObjectArrayEvents() {
		return m_esperconfig.getEventTypesNestableObjectArrayEvents();
	}

	@Override
	public Map<String, ConfigurationEventTypeXMLDOM> getEventTypesXMLDOM() {
		return m_esperconfig.getEventTypesXMLDOM();
	}

	@Override
	public List<String> getImports() {
		return m_esperconfig.getImports();
	}

	@Override
	public Map<String, ConfigurationEventTypeMap> getMapTypeConfigurations() {
		return m_esperconfig.getMapTypeConfigurations();
	}

	@Override
	public Map<String, ConfigurationMethodRef> getMethodInvocationReferences() {
		return m_esperconfig.getMethodInvocationReferences();
	}

	@Override
	public Map<String, ConfigurationEventTypeObjectArray> getObjectArrayTypeConfigurations() {
		return m_esperconfig.getObjectArrayTypeConfigurations();
	}

	@Override
	public List<ConfigurationPlugInAggregationFunction> getPlugInAggregationFunctions() {
		return m_esperconfig.getPlugInAggregationFunctions();
	}

	@Override
	public Map<URI, ConfigurationPlugInEventRepresentation> getPlugInEventRepresentation() {
		return m_esperconfig.getPlugInEventRepresentation();
	}

	@Override
	public URI[] getPlugInEventTypeResolutionURIs() {
		
		return m_esperconfig.getPlugInEventTypeResolutionURIs();
	}

	@Override
	public Map<String, ConfigurationPlugInEventType> getPlugInEventTypes() {
		return m_esperconfig.getPlugInEventTypes();
	}

	@Override
	public List<ConfigurationPlugInPatternObject> getPlugInPatternObjects() {
		return m_esperconfig.getPlugInPatternObjects();
	}

	@Override
	public List<ConfigurationPlugInSingleRowFunction> getPlugInSingleRowFunctions() {
		
		return m_esperconfig.getPlugInSingleRowFunctions();
	}

	@Override
	public List<ConfigurationPlugInView> getPlugInViews() {
		
		return m_esperconfig.getPlugInViews();
	}

	@Override
	public List<ConfigurationPlugInVirtualDataWindow> getPlugInVirtualDataWindows() {
		
		return m_esperconfig.getPlugInVirtualDataWindows();
	}

	@Override
	public List<ConfigurationPluginLoader> getPluginLoaders() {
		
		return m_esperconfig.getPluginLoaders();
	}

	@Override
	public Map<String, ConfigurationRevisionEventType> getRevisionEventTypes() {
		
		return m_esperconfig.getRevisionEventTypes();
	}

	@Override
	public Map<String, ConfigurationVariable> getVariables() {
		
		return m_esperconfig.getVariables();
	}

	@Override
	public Map<String, ConfigurationVariantStream> getVariantStreams() {
		
		return m_esperconfig.getVariantStreams();
	}

	@Override
	public void addEventType(Class eventClass) {
		m_esperconfig.addEventType(eventClass);
	}

	@Override
	public void addEventType(String eventTypeName, String eventClassName)
			throws ConfigurationException {
		m_esperconfig.addEventType(eventTypeName, eventClassName);
	}

	@Override
	public void addEventType(String arg0, Class arg1)
			throws ConfigurationException {
		m_esperconfig.addEventType(arg0, arg1);
	}

	@Override
	public void addEventType(String arg0, Properties arg1)
			throws ConfigurationException {
		m_esperconfig.addEventType(arg0, arg1);
		
	}

	@Override
	public void addEventType(String arg0, Map<String, Object> arg1)
			throws ConfigurationException {
		m_esperconfig.addEventType(arg0, arg1);
		
	}

	@Override
	public void addEventType(String arg0, ConfigurationEventTypeXMLDOM arg1)
			throws ConfigurationException {
		m_esperconfig.addEventType(arg0, arg1);
	}

	@Override
	public void addEventType(String arg0, String[] arg1, Object[] arg2)
			throws ConfigurationException {
		m_esperconfig.addEventType(arg0, arg1, arg2);
	}

	@Override
	public void addEventType(String arg0, Map<String, Object> arg1,
			String[] arg2) throws ConfigurationException {
		m_esperconfig.addEventType(arg0, arg1, arg2);
	}

	@Override
	public void addEventType(String arg0, Map<String, Object> arg1,
			ConfigurationEventTypeMap arg2) throws ConfigurationException {
		m_esperconfig.addEventType(arg0, arg1, arg2);
	}

	@Override
	public void addEventType(String arg0, String arg1,
			ConfigurationEventTypeLegacy arg2) {
		m_esperconfig.addEventType(arg0, arg1, arg2);
	}

	@Override
	public void addEventType(String arg0, String[] arg1, Object[] arg2,
			ConfigurationEventTypeObjectArray arg3)
			throws ConfigurationException {
		m_esperconfig.addEventType(arg0, arg1, arg2, arg3);
	}

	@Override
	public void addEventTypeAutoName(String packageName) {
		m_esperconfig.addEventTypeAutoName(packageName);
		
	}

	public void setAutoName(List<String> aAutoNames) {
		if (aAutoNames != null) {
			for (String strItem : aAutoNames)
				addEventTypeAutoName(strItem);
		}
	}
	
	@Override
	public void addImport(String arg0) throws ConfigurationException {
		m_esperconfig.addImport(arg0);
	}

	@Override
	public void addImport(Class arg0) throws ConfigurationException {
		m_esperconfig.addImport(arg0);
	}
	
	public void setPluginAggregationFunctions(List<ConfigurationPlugInAggregationFunction> aggregationfunctions){
		for(ConfigurationPlugInAggregationFunction aggrefn:aggregationfunctions){
			if( aggrefn.getFactoryClassName() != null)
				m_esperconfig.addPlugInAggregationFunctionFactory(aggrefn.getName(), aggrefn.getFactoryClassName().toString());
		}
	}
	
	
	public void setPlugInAggregationMultiFunction(List<ConfigurationPlugInAggregationMultiFunction> aggregatnmultifunctns){
		for(ConfigurationPlugInAggregationMultiFunction aggrefn:aggregatnmultifunctns){
			m_esperconfig.addPlugInAggregationMultiFunction(aggrefn);
		}
	}
	
	@Override
	public void addPlugInAggregationFunctionFactory(String arg0, String arg1)
			throws ConfigurationException {
		m_esperconfig.addPlugInAggregationFunctionFactory(arg0, arg1);
		
	}

	@Override
	public void addPlugInEventType(String arg0, URI[] arg1, Serializable arg2) {
		m_esperconfig.addPlugInEventType(arg0, arg1, arg2);
		
	}

	@Override
	public void addPlugInSingleRowFunction(String arg0, String arg1, String arg2)
			throws ConfigurationException {
		m_esperconfig.addPlugInSingleRowFunction(arg0, arg1, arg2);
		
	}

	@Override
	public void addPlugInSingleRowFunction(String arg0, String arg1,
			String arg2, ValueCache arg3) throws ConfigurationException {
		m_esperconfig.addPlugInSingleRowFunction(arg0, arg1, arg2, arg3);
	}

	@Override
	public void addPlugInSingleRowFunction(String arg0, String arg1,
			String arg2, FilterOptimizable arg3) throws ConfigurationException {
		m_esperconfig.addPlugInSingleRowFunction(arg0, arg1, arg2, arg3);
	}

	@Override
	public void addPlugInView(String namespace, String name, String viewFactoryClass) {
		m_esperconfig.addPlugInView(namespace, name, viewFactoryClass);
	}

	public void setPluginSingleRowFunction(List<Map<String, String>> listFunctions)  {
		for (Map<String, String> fcn : listFunctions)
			addPlugInSingleRowFunction(fcn.get("eplname"), fcn.get("classname"), fcn.get("javaname"));
	}
	
	@Override
	public void addRevisionEventType(String arg0,
			ConfigurationRevisionEventType arg1) {
		m_esperconfig.addRevisionEventType(arg0, arg1);
	}

	@Override
	public void addVariable(String arg0, Class arg1, Object arg2)
			throws ConfigurationException {
		m_esperconfig.addVariable(arg0, arg1, arg2);
	}

	@Override
	public void addVariable(String arg0, String arg1, Object arg2)
			throws ConfigurationException {
		m_esperconfig.addVariable(arg0, arg1, arg2);
	}

	@Override
	public void addVariable(String arg0, String arg1, Object arg2, boolean arg3)
			throws ConfigurationException {
		m_esperconfig.addVariable(arg0, arg1, arg2, arg3);
	}

	@Override
	public void addVariantStream(String arg0, ConfigurationVariantStream arg1) {
		m_esperconfig.addVariantStream(arg0, arg1);
	}
	
	@Override
	public EventType getEventType(String arg0) {
		
		return m_esperconfig.getEventType(arg0);
	}

	@Override
	public Set<String> getEventTypeNameUsedBy(String arg0) {
		
		return m_esperconfig.getEventTypeNameUsedBy(arg0);
	}

	@Override
	public EventType[] getEventTypes() {
		
		return m_esperconfig.getEventTypes();
	}

	@Override
	public Set<String> getVariableNameUsedBy(String arg0) {
		
		return m_esperconfig.getVariableNameUsedBy(arg0);
	}

	@Override
	public boolean isEventTypeExists(String arg0) {
		
		return m_esperconfig.isEventTypeExists(arg0);
	}

	@Override
	public boolean isVariantStreamExists(String arg0) {
		
		return m_esperconfig.isVariantStreamExists(arg0);
	}

	@Override
	public boolean removeEventType(String arg0, boolean arg1)
			throws ConfigurationException {
		
		return m_esperconfig.removeEventType(arg0, arg1);
	}

	@Override
	public boolean removeVariable(String arg0, boolean arg1)
			throws ConfigurationException {
		
		return m_esperconfig.removeVariable(arg0, arg1);
	}

	@Override
	public void replaceXMLEventType(String arg0,
			ConfigurationEventTypeXMLDOM arg1) throws ConfigurationException {
		m_esperconfig.replaceXMLEventType(arg0, arg1);
	}

	@Override
	public void setMetricsReportingDisabled() throws ConfigurationException {
		m_esperconfig.setMetricsReportingDisabled();
		
	}

	@Override
	public void setMetricsReportingEnabled() throws ConfigurationException {
		m_esperconfig.setMetricsReportingEnabled();
		
	}

	@Override
	public void setMetricsReportingInterval(String arg0, long arg1)
			throws ConfigurationException {
		m_esperconfig.setMetricsReportingInterval(arg0, arg1);
		
	}

	@Override
	public void setMetricsReportingStmtDisabled(String arg0)
			throws ConfigurationException {
		m_esperconfig.setMetricsReportingStmtDisabled(arg0);
		
	}

	@Override
	public void setMetricsReportingStmtEnabled(String arg0)
			throws ConfigurationException {
		m_esperconfig.setMetricsReportingStmtEnabled(arg0);
	}

	@Override
	public void setPatternMaxSubexpressions(Long arg0) {
		m_esperconfig.setPatternMaxSubexpressions(arg0);
	}

	@Override
	public void setPlugInEventTypeResolutionURIs(URI[] arg0) {
		m_esperconfig.setPlugInEventTypeResolutionURIs(arg0);
	}

	@Override
	public void updateMapEventType(String arg0, Map<String, Object> arg1)
			throws ConfigurationException {
		m_esperconfig.updateMapEventType(arg0, arg1);
	}

	@Override
	public void updateObjectArrayEventType(String arg0, String[] arg1,
			Object[] arg2) {
		m_esperconfig.updateObjectArrayEventType(arg0, arg1, arg2);
		
	}

	@Override
	public List<ConfigurationPlugInAggregationMultiFunction> getPlugInAggregationMultiFunctions() {
		return m_esperconfig.getPlugInAggregationMultiFunctions();
	}

	@Override
	public void addPlugInAggregationMultiFunction(ConfigurationPlugInAggregationMultiFunction config)
			throws ConfigurationException {
		m_esperconfig.addPlugInAggregationMultiFunction(config);
	}
	
	

	@Override
	public void addPlugInSingleRowFunction(String functionName, String className, String methodName, ValueCache valueCache,
			FilterOptimizable filterOptimizable, boolean rethrowExceptions) throws ConfigurationException {
		m_esperconfig.addPlugInSingleRowFunction(functionName, className, methodName, valueCache,
				filterOptimizable, rethrowExceptions);
	}
	
	public void setThreadPoolSize(int threadPoolSize) {
		super.setThreadPoolSize(1); // forcing thread pool size to 1 for Esper.
	}
	
	public void setExceptionHandlerFactoryClass(Class exceptionHandlerFactoryClass){
		getEngineDefaults().getExceptionHandling().addClass(exceptionHandlerFactoryClass);
	}

	public List<AnnotationConfiguration> getAnnotationConfig() {
		return extendedAnnotationConfiguration;
	}

	public void setAnnotationConfig(List<AnnotationConfiguration> annotationConfig) {
		this.extendedAnnotationConfiguration = annotationConfig;
	}
	
	public Map<String, AnnotationConfiguration> getAnnotationConfigMap() {
		return annotationConfigMap;
	}
	
	
}
