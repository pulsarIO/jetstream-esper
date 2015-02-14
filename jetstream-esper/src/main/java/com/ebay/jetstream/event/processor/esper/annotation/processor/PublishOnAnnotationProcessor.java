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
import com.ebay.jetstream.event.processor.esper.EsperEngineAnnotationMetadata;
import com.ebay.jetstream.event.processor.esper.annotation.PublishOn;
import com.ebay.jetstream.event.processor.esper.annotation.metadata.PublishOnAnnotationMetadata;
import com.espertech.esper.client.soda.AnnotationAttribute;
import com.espertech.esper.client.soda.AnnotationPart;

public class PublishOnAnnotationProcessor implements AnnotationProcessor {

	@Override
	public StatementAnnotationInfo process(String statement,
			EngineMetadata engineAnntMetadata,
			Map<String, AnnotationConfiguration> annotConfigMap,
			StatementAnnotationInfo stmtAnntInfo,
			Collection<EventSink> registeredSinkList) {

		String strTopics = null;
		String strUrls = null;
		Map<String, List<AnnotationPart>> partsMap = ((EsperEngineAnnotationMetadata) engineAnntMetadata)
				.getAnnotationPartsMap();
		List<AnnotationPart> affinityAnnotationParts = partsMap
				.get(PublishOn.class.getSimpleName());
		PublishOnAnnotationMetadata metadata = new PublishOnAnnotationMetadata();
		for (AnnotationPart part : affinityAnnotationParts) {
			for (AnnotationAttribute attr : part.getAttributes()) {
				if (strTopics == null && "topics".equals(attr.getName())) {
					strTopics = (String) attr.getValue();
					metadata.setTopics(strTopics);
				}
				if (strUrls == null && "urls".equals(attr.getName())) {

					strUrls = (String) attr.getValue();
					metadata.setTopics(strUrls);
				}
			}
		}
		stmtAnntInfo.addAnnotationInfo(PublishOn.class.getSimpleName(),
				metadata);
		return stmtAnntInfo;
	}

	@Override
	public Class getAnnotationClass() {
		return PublishOn.class;
	}
}
