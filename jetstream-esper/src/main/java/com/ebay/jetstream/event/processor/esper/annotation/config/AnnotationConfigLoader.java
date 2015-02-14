/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Dual licensed under the Apache 2.0 license and the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper.annotation.config;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;

import com.ebay.jestream.event.annotation.AnnotationConfiguration;
import com.ebay.jestream.event.annotation.AnnotationListener;
import com.ebay.jestream.event.annotation.AnnotationProcessor;


/**
 * - loads Annotations and its corresponding processors and listeners classes from the given package name.
 * 
 * @author rmuthupandian
 *
 */
public class AnnotationConfigLoader {

	private String listener_package = "com.ebay.jetstream.event.processor.esper.annotation.listener";
	private String processor_package = "com.ebay.jetstream.event.processor.esper.annotation.processor";

	public DefaultAnnotationConfiguration load_annotation_config() {
		DefaultAnnotationConfiguration defaultconfig = new DefaultAnnotationConfiguration();
		List<AnnotationConfiguration> configList = new ArrayList<AnnotationConfiguration>();

		loadProcessors();
		loadListeners();
		Map<Class<?>, AnnotationProcessor> processors = loadProcessors();
		Map<Class<?>, AnnotationListener> listeners = loadListeners();

		for (Map.Entry<Class<?>, AnnotationProcessor> procEntry : processors
				.entrySet()) {
			AnnotationConfiguration config = new AnnotationConfiguration();
			config.setAnnotation(procEntry.getKey().getSimpleName());
			config.setClassName(procEntry.getKey());
			config.setProcessor(procEntry.getValue());
			if (listeners.get(procEntry.getKey()) != null)
				config.setListener(listeners.get(procEntry.getKey()));

			configList.add(config);
		}

		defaultconfig.setDefaultAnnotationConfiguration(configList);

		return defaultconfig;
	}

	private Map<Class<?>, AnnotationProcessor> loadProcessors() {

		Reflections reflections = new Reflections(processor_package);
		Set<Class<? extends AnnotationProcessor>> proc_classes = reflections
				.getSubTypesOf(AnnotationProcessor.class);

		Map<Class<?>, AnnotationProcessor> procMap = new HashMap<Class<?>, AnnotationProcessor>();
		for (Class<?> procClass : proc_classes) {
			try {
				Class<?>[] clzargs = null ;
				Method m = procClass.getDeclaredMethod(
						"getAnnotationClass", clzargs );
				AnnotationProcessor processor = (AnnotationProcessor) procClass
						.newInstance();
				Object[] objargs = null ;
				Class<?> clz = (Class<?>) m.invoke(processor, objargs );
				if (clz != null)
					procMap.put(clz, processor);

			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		return procMap;

	}

	private Map<Class<?>, AnnotationListener> loadListeners() {

		Reflections reflections = new Reflections(listener_package);
		Set<Class<? extends AnnotationListener>> listener_classes = reflections
				.getSubTypesOf(AnnotationListener.class);

		Map<Class<?>, AnnotationListener> listenerMap = new HashMap<Class<?>, AnnotationListener>();
		for (Class<?> listenerClass : listener_classes) {
			try {
				
				Class<?>[] clzargs = null ;
				Method m = listenerClass.getDeclaredMethod(
						"getAnnotationClass", clzargs );
				AnnotationListener listener = (AnnotationListener) listenerClass
						.newInstance();
				Object[] objargs = null ;
				Class<?> clz = (Class<?>) m.invoke(listenerClass.newInstance(), objargs );
				if (clz != null)
					listenerMap.put(clz, listener);

			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		return listenerMap;

	}

}
