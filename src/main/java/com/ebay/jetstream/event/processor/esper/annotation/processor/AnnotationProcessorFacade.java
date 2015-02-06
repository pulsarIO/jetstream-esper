/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Dual licensed under the Apache 2.0 license and the GPL v2 license.  See LICENSE for full terms.
 */
package com.ebay.jetstream.event.processor.esper.annotation.processor;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ebay.jestream.event.annotation.AnnotationConfiguration;
import com.ebay.jestream.event.annotation.AnnotationProcessor;
import com.ebay.jestream.event.annotation.EngineMetadata;
import com.ebay.jestream.event.annotation.StatementAnnotationInfo;
import com.ebay.jetstream.event.EventSink;
import com.ebay.jetstream.event.processor.esper.EsperEngineAnnotationMetadata;
import com.espertech.esper.client.soda.AnnotationPart;
import com.espertech.esper.client.soda.EPStatementObjectModel;

public class AnnotationProcessorFacade {

	private Map<String, AnnotationConfiguration> annotationConfigMap = new HashMap<String, AnnotationConfiguration>();

	public StatementAnnotationInfo getStatementAnnotationInfo(
			List<AnnotationPart> annotationParts, String statement,
			EPStatementObjectModel model,
			Map<String, List<AnnotationPart>> partsMap,
			Collection<EventSink> registeredSinkList) throws Exception {

		StatementAnnotationInfo stmtAnntInfo = new StatementAnnotationInfo();
		EngineMetadata engineMetadata = new EsperEngineAnnotationMetadata();

		((EsperEngineAnnotationMetadata) engineMetadata)
				.setAnnotationPartsMap(partsMap);
		((EsperEngineAnnotationMetadata) engineMetadata)
				.setStatementObjectModel(model);

		for (AnnotationPart part : annotationParts) {
			if (annotationConfigMap.get(part.getName()) != null) {
				AnnotationProcessor annotationProcessor = annotationConfigMap
						.get(part.getName()).getProcessor();
				if (annotationProcessor == null) {
					continue;
				}
				stmtAnntInfo = annotationProcessor.process(statement,
						engineMetadata, annotationConfigMap, stmtAnntInfo,
						registeredSinkList);
			}
		}

		return stmtAnntInfo;

	}

	public void setAnnotationConfigurationMap(
			Map<String, AnnotationConfiguration> annotationConfigMap) {
		this.annotationConfigMap = annotationConfigMap;
	}

}
