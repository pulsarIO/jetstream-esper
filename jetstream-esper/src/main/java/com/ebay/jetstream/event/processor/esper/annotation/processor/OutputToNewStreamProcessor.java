/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Licensed under the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper.annotation.processor;

/**
 *  - Processor class parses name from EPL statement @ChangeStreamName and create metadata and injects into StatementAnnotationInfo 
 * @author rmuthupandian
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.ebay.jestream.event.annotation.AnnotationConfiguration;
import com.ebay.jestream.event.annotation.AnnotationProcessor;
import com.ebay.jestream.event.annotation.EngineMetadata;
import com.ebay.jestream.event.annotation.StatementAnnotationInfo;
import com.ebay.jetstream.event.EventSink;
import com.ebay.jetstream.event.processor.esper.EsperEngineAnnotationMetadata;
import com.ebay.jetstream.event.processor.esper.annotation.OutputToWithNewStreamName;
import com.ebay.jetstream.event.processor.esper.annotation.metadata.OutputToNewStreamMetadata;
import com.espertech.esper.client.soda.AnnotationAttribute;
import com.espertech.esper.client.soda.AnnotationPart;

public class OutputToNewStreamProcessor implements AnnotationProcessor {

	@Override
	public Class getAnnotationClass() {
		return OutputToWithNewStreamName.class;
	}

	@Override
	public StatementAnnotationInfo process(String statement,
			EngineMetadata engineAnntMetadata,
			Map<String, AnnotationConfiguration> annotConfigMap,
			StatementAnnotationInfo stmtAnntInfo,
			Collection<EventSink> registeredSinkList){
		
		Map<String, List<AnnotationPart>> partsMap = ((EsperEngineAnnotationMetadata) engineAnntMetadata).getAnnotationPartsMap();
		List<AnnotationPart> annotationParts = partsMap
				.get(OutputToWithNewStreamName.class.getSimpleName());
		
		OutputToNewStreamMetadata metadata = new OutputToNewStreamMetadata();
		for (AnnotationPart part : annotationParts) {
			if (OutputToWithNewStreamName.class.getSimpleName().equals(part.getName())) {
				for (AnnotationAttribute attr : part
						.getAttributes()) {
							metadata.setStreamName((String)attr.getValue());
				}
			}	
		}
		
		for (AnnotationPart part : annotationParts) {
			List<AnnotationAttribute> attributes = part.getAttributes();
			if (attributes.size() > 0) {
				String[] aSinks = ((String) attributes.get(0).getValue())
						.replaceAll(" ", "").split(",");
				List<EventSink> sinklist = new ArrayList<EventSink>();
				for (String strSinkName : aSinks) {
					boolean bFound = false;
					for (EventSink eventsink : registeredSinkList) {
						if (eventsink.getBeanName().equals(strSinkName)) {
							sinklist.add(eventsink);
							bFound = true;
							break;
						}
					}
					if (!bFound) {
						throw new IllegalArgumentException("Output Bean '"
								+ strSinkName + "' is was not found.");
					}
				}

				if (sinklist.size() > 0)
					metadata.addToSinkList(sinklist);
			}

		}
		stmtAnntInfo.addAnnotationInfo(OutputToWithNewStreamName.class.getSimpleName(), metadata);
		return stmtAnntInfo;
	}

}
