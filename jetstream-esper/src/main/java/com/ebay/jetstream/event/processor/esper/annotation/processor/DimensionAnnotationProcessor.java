/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Dual licensed under the Apache 2.0 license and the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper.annotation.processor;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.ebay.jestream.event.annotation.AnnotationConfiguration;
import com.ebay.jestream.event.annotation.AnnotationProcessor;
import com.ebay.jestream.event.annotation.EngineMetadata;
import com.ebay.jestream.event.annotation.StatementAnnotationInfo;
import com.ebay.jetstream.event.EventSink;
import com.ebay.jetstream.event.processor.esper.CreateDimensionGenerator;
import com.ebay.jetstream.event.processor.esper.EsperEngineAnnotationMetadata;
import com.ebay.jetstream.event.processor.esper.annotation.CreateDimension;
import com.ebay.jetstream.event.processor.esper.annotation.metadata.AffinityAnnotationMetadata;
import com.espertech.esper.client.soda.AnnotationPart;

public class DimensionAnnotationProcessor implements AnnotationProcessor {

	@Override
	public StatementAnnotationInfo process(String statement,
			EngineMetadata engineAnntMetadata,
			Map<String, AnnotationConfiguration> annotConfigMap,
			StatementAnnotationInfo stmtAnntInfo,
			Collection<EventSink> registeredSinkList) {

		Map<String, List<AnnotationPart>> partsMap = ((EsperEngineAnnotationMetadata) engineAnntMetadata)
				.getAnnotationPartsMap();
		List<AnnotationPart> affinityAnnotationParts = partsMap
				.get("CreateDimension");
		AffinityAnnotationMetadata metadata = new AffinityAnnotationMetadata();
		for (AnnotationPart part : affinityAnnotationParts) {
			metadata.addToKeyGenList(new CreateDimensionGenerator(part
					.getAttributes()));
		}

		return stmtAnntInfo;
	}

	@Override
	public Class getAnnotationClass() {
		return CreateDimension.class;
	}
}
