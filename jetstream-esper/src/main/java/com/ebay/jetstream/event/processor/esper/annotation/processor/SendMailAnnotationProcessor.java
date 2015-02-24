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
import com.ebay.jetstream.event.processor.esper.EsperEngineAnnotationMetadata;
import com.ebay.jetstream.event.processor.esper.annotation.SendMail;
import com.ebay.jetstream.event.processor.esper.annotation.metadata.SendMailAnnotationMetadata;
import com.espertech.esper.client.soda.AnnotationAttribute;
import com.espertech.esper.client.soda.AnnotationPart;

public class SendMailAnnotationProcessor implements AnnotationProcessor {

	@Override
	public Class getAnnotationClass() {
		return SendMail.class;
	}

	@Override
	public StatementAnnotationInfo process(String statement,
			EngineMetadata engineAnntMetadata,
			Map<String, AnnotationConfiguration> annotConfigMap,
			StatementAnnotationInfo stmtAnntInfo,
			Collection<EventSink> registeredSinkList) {

		Map<String, List<AnnotationPart>> partsMap = ((EsperEngineAnnotationMetadata) engineAnntMetadata)
				.getAnnotationPartsMap();
		List<AnnotationPart> affinityAnnotationParts = partsMap
				.get(SendMail.class.getSimpleName());
		SendMailAnnotationMetadata metadata = new SendMailAnnotationMetadata();
		for (AnnotationPart part : affinityAnnotationParts) {
			for (AnnotationAttribute attr : part.getAttributes()) {
				if ("alertList".equals(attr.getName())) {
					metadata.setAlertList((String) attr.getValue());
				} else if ("mailServer".equals(attr.getName())) {
					metadata.setMailServer((String) attr.getValue());
				} else if ("alertSeverity".equals(attr.getName())) {
					metadata.setAlertSeverity((String) attr.getValue());
				} else if ("sendFrom".equals(attr.getName())) {
					metadata.setSendFrom((String) attr.getValue());
				} else if ("eventFields".equals(attr.getName())) {
					String fields = attr.getValue().toString().replace(" ", "");
					metadata.setEventFields(fields);
				} else if ("mailContent".equals(attr.getName())) {
					metadata.setMailContent((String) attr.getValue());
				} else if ("mailSubject".equals(attr.getName())) {
					metadata.setMailSubject((String) attr.getValue());
				}
			}
		}
		stmtAnntInfo
				.addAnnotationInfo(SendMail.class.getSimpleName(), metadata);
		return stmtAnntInfo;
	}

}
