/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Licensed under the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper.annotation.processor;

import java.util.Collection;
import java.util.HashMap;
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
import com.ebay.jetstream.event.processor.esper.annotation.DBInfo;
import com.ebay.jetstream.event.processor.esper.annotation.metadata.DBInfoAnnotationMetadata;
import com.espertech.esper.client.soda.AnnotationAttribute;
import com.espertech.esper.client.soda.AnnotationPart;
import com.espertech.esper.client.soda.EPStatementObjectModel;
import com.espertech.esper.client.soda.FromClause;

public class DBInfoAnnotationProcessor implements AnnotationProcessor {

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
		Map<String, Object> mapMeta = new HashMap<String, Object>();
		boolean bAddDbMap = false;
		String strParsedEventStream = null;
		DBInfoAnnotationMetadata metadata = new DBInfoAnnotationMetadata();
		if (partsMap.get(DBInfo.class.getSimpleName()) != null) {
			bAddDbMap = true;

			// Parse out event stream name
			FromClause fc = model.getFromClause();
			if (fc != null && fc.getStreams().size() == 1) {
				Matcher matcher = FROM_STREAM.matcher(statement);
				if (matcher.find()) {
					String strFoundStream = matcher.group(1);
					if (strFoundStream != null)
						strParsedEventStream = strFoundStream;
				}
			}

			List<AnnotationPart> dbInfoAnnoParts = partsMap.get(DBInfo.class
					.getSimpleName());
			for (AnnotationPart annotationPart : dbInfoAnnoParts) {
				for (AnnotationAttribute attr : annotationPart.getAttributes()) {
					mapMeta.put(attr.getName(), attr.getValue());
				}
			}
		}
		if (bAddDbMap) {
			if (strParsedEventStream != null && mapMeta.get("table") == null) {
				mapMeta.put("table", strParsedEventStream);
			}
			mapMeta.put("refresh", Boolean.TRUE);
		} else {
			mapMeta = null;
		}

		metadata.setMapMetaData(mapMeta);
		stmtAnntInfo.addAnnotationInfo(DBInfo.class.getSimpleName(), metadata);

		return stmtAnntInfo;

	}

	@Override
	public Class getAnnotationClass() {
		return DBInfo.class;
	}

}
