/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Dual licensed under the Apache 2.0 license and the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper.annotation.listener;

import com.ebay.jestream.event.annotation.AnnotationListener;
import com.ebay.jestream.event.annotation.StatementAnnotationInfo;
import com.ebay.jetstream.event.JetstreamEvent;
import com.ebay.jetstream.event.processor.esper.AffinityKeyGenerator;
import com.ebay.jetstream.event.processor.esper.annotation.ClusterAffinityTag;
import com.ebay.jetstream.event.processor.esper.annotation.metadata.AffinityAnnotationMetadata;

public class AffinityAnnotationListener implements AnnotationListener {
	private static final String ANNO_KEY = ClusterAffinityTag.class.getSimpleName();

	@Override
	public JetstreamEvent processMetaInformation(JetstreamEvent event,
			StatementAnnotationInfo annotationInfo) {
		
		AffinityAnnotationMetadata metadata = (AffinityAnnotationMetadata) annotationInfo
				.getAnnotationInfo(ANNO_KEY);
		
		for (AffinityKeyGenerator gen : metadata.getAffinityKeyGen())
			gen.setEventFields(event);

		return event;
	}

	@Override
	public Class getAnnotationClass() {
		return ClusterAffinityTag.class;
	}

	

}
