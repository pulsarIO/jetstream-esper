/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Dual licensed under the Apache 2.0 license and the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper.annotation.listener;

/**
 * - Listener class gets new stream name from ChangeStreamNameMetadata and injects into JetstreamEvent 
 * @author rmuthupandian
 */
import com.ebay.jestream.event.annotation.AnnotationListener;
import com.ebay.jestream.event.annotation.StatementAnnotationInfo;
import com.ebay.jetstream.event.EventSink;
import com.ebay.jetstream.event.JetstreamEvent;
import com.ebay.jetstream.event.processor.esper.annotation.OutputToWithNewStreamName;
import com.ebay.jetstream.event.processor.esper.annotation.metadata.OutputToNewStreamMetadata;

public class OutputToNewStreamListener implements AnnotationListener {

	private static final String ANNO_KEY = OutputToWithNewStreamName.class.getSimpleName();
    
	@Override
	public JetstreamEvent processMetaInformation(JetstreamEvent event,
			StatementAnnotationInfo annotationInfo) {
		
		OutputToNewStreamMetadata anntmetadata = (OutputToNewStreamMetadata) annotationInfo
				.getAnnotationInfo(ANNO_KEY);
		
		String newEventType = anntmetadata.getStreamName();
		
		event.setEventType(newEventType);
		
		EventSink[] sinks = anntmetadata.getSinks();
		if (sinks != null)
			event.put("sink.array", sinks);
		
		return event;
	}

	@Override
	public Class getAnnotationClass() {
		return OutputToWithNewStreamName.class;
	}

}
