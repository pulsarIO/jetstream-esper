/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Licensed under the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper.annotation.listener;

import com.ebay.jestream.event.annotation.AnnotationListener;
import com.ebay.jestream.event.annotation.StatementAnnotationInfo;
import com.ebay.jetstream.event.JetstreamEvent;
import com.ebay.jetstream.event.JetstreamReservedKeys;
import com.ebay.jetstream.event.processor.esper.annotation.BroadCast;
import com.ebay.jetstream.event.processor.esper.annotation.metadata.BroadCastMetadata;

public class BroadCastAnnotationListener implements AnnotationListener{

	private static final String ANNO_KEY = BroadCast.class.getSimpleName();

	@Override
	public JetstreamEvent processMetaInformation(JetstreamEvent event,
			StatementAnnotationInfo annotationInfo) {
		
		BroadCastMetadata metadata = (BroadCastMetadata) annotationInfo.getAnnotationInfo(ANNO_KEY);
		if(metadata != null && metadata.isBroadcast())
			event.addMetaData(JetstreamReservedKeys.EventBroadCast.toString(), String.valueOf(true));
	
		return event;
	}

	@Override
	public Class getAnnotationClass() {
		return BroadCast.class;
	}

}
