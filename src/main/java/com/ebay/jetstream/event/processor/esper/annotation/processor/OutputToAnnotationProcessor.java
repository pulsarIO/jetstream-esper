/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Dual licensed under the Apache 2.0 license and the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper.annotation.processor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ebay.jestream.event.annotation.AnnotationConfiguration;
import com.ebay.jestream.event.annotation.AnnotationProcessor;
import com.ebay.jestream.event.annotation.EngineMetadata;
import com.ebay.jestream.event.annotation.StatementAnnotationInfo;
import com.ebay.jetstream.event.EventSink;
import com.ebay.jetstream.event.processor.esper.EsperEngineAnnotationMetadata;
import com.ebay.jetstream.event.processor.esper.annotation.OutputTo;
import com.ebay.jetstream.event.processor.esper.annotation.metadata.OutputToAnnotationMetadata;
import com.espertech.esper.client.soda.AnnotationAttribute;
import com.espertech.esper.client.soda.AnnotationPart;
import com.espertech.esper.client.soda.EPStatementObjectModel;
import com.espertech.esper.client.soda.FromClause;

public class OutputToAnnotationProcessor implements AnnotationProcessor {
	private static final Pattern FROM_STREAM = Pattern
			.compile("(?im:\\sfrom\\s+(\\w+))");

	@Override
	public StatementAnnotationInfo process(String statement,
			EngineMetadata engineAnntMetadata,
			Map<String, AnnotationConfiguration> annotConfigMap,
			StatementAnnotationInfo stmtAnntInfo,
			Collection<EventSink> registeredSinkList) {

		Map<String, List<AnnotationPart>> partsMap = ((EsperEngineAnnotationMetadata) engineAnntMetadata)
				.getAnnotationPartsMap();
		EPStatementObjectModel model = ((EsperEngineAnnotationMetadata) engineAnntMetadata)
				.getStatementObjectModel();

		List<AnnotationPart> outputToAnnotationParts = partsMap
				.get(OutputTo.class.getSimpleName());
		String strParsedEventStream = null;
		OutputToAnnotationMetadata metadata = new OutputToAnnotationMetadata();

		FromClause fc = model.getFromClause();
		if (fc != null && fc.getStreams().size() == 1) {
			Matcher matcher = FROM_STREAM.matcher(statement);
			if (matcher.find()) {
				String strFoundStream = matcher.group(1);
				if (strFoundStream != null)
					strParsedEventStream = strFoundStream;
			}
		}

		for (AnnotationPart part : outputToAnnotationParts) {
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

		metadata.setStreamName(strParsedEventStream);
		stmtAnntInfo
				.addAnnotationInfo(OutputTo.class.getSimpleName(), metadata);
		return stmtAnntInfo;
	}

	@Override
	public Class getAnnotationClass() {
		return OutputTo.class;
	}
}
