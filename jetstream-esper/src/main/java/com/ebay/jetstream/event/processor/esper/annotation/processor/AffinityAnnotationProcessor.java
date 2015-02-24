/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Licensed under the GPL v2 license.  See LICENSE for full terms.
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
import com.ebay.jetstream.event.processor.esper.AffinityKeyGenerator;
import com.ebay.jetstream.event.processor.esper.EsperEngineAnnotationMetadata;
import com.ebay.jetstream.event.processor.esper.annotation.ClusterAffinityTag;
import com.ebay.jetstream.event.processor.esper.annotation.metadata.AffinityAnnotationMetadata;
import com.espertech.esper.client.soda.AnnotationPart;

public class AffinityAnnotationProcessor implements AnnotationProcessor
{
	
	public StatementAnnotationInfo process(String statement,
			EngineMetadata engineAnntMetadata,
			Map<String, AnnotationConfiguration> annotConfigMap,
			StatementAnnotationInfo stmtAnntInfo,
			Collection<EventSink> registeredSinkList){
	
		Map<String, List<AnnotationPart>> partsMap = ((EsperEngineAnnotationMetadata) engineAnntMetadata).getAnnotationPartsMap();
		AffinityAnnotationMetadata metadata = new AffinityAnnotationMetadata();
		List<AnnotationPart> affinityAnnotationParts = partsMap.get("ClusterAffinityTag");
		for(AnnotationPart part : affinityAnnotationParts)
		{	
			metadata.addToKeyGenList(new AffinityKeyGenerator(part.getAttributes()));
		}
		
		stmtAnntInfo.addAnnotationInfo(ClusterAffinityTag.class.getSimpleName(), metadata);
		
		return stmtAnntInfo;
	}

	@Override
	public Class getAnnotationClass() {
		return ClusterAffinityTag.class;
	}

	
}
