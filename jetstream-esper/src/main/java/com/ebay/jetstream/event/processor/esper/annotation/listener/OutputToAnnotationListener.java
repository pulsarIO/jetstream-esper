/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Licensed under the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper.annotation.listener;

import com.ebay.jestream.event.annotation.AnnotationListener;
import com.ebay.jestream.event.annotation.StatementAnnotationInfo;
import com.ebay.jetstream.event.EventSink;
import com.ebay.jetstream.event.JetstreamEvent;
import com.ebay.jetstream.event.processor.esper.annotation.OutputTo;
import com.ebay.jetstream.event.processor.esper.annotation.metadata.OutputToAnnotationMetadata;

public class OutputToAnnotationListener implements AnnotationListener {
	private static final String ANNO_KEY = OutputTo.class.getSimpleName();
	
	@Override
	public JetstreamEvent processMetaInformation(JetstreamEvent event,
			StatementAnnotationInfo annotationInfo) {
		
		OutputToAnnotationMetadata anntmetadata = (OutputToAnnotationMetadata) annotationInfo.getAnnotationInfo(ANNO_KEY);
		if (anntmetadata.getStreamName() != null)
			event.setEventType(anntmetadata.getStreamName());
		
		EventSink[] sinks = anntmetadata.getSinks();
		if (sinks != null)
			event.put("sink.array", sinks);
		
		return event;

	}

	@Override
	public Class getAnnotationClass() {
		return OutputTo.class;
	}

}
