/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Dual licensed under the Apache 2.0 license and the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper.annotation.listener;

import com.ebay.jestream.event.annotation.AnnotationListener;
import com.ebay.jestream.event.annotation.StatementAnnotationInfo;
import com.ebay.jetstream.event.JetstreamEvent;
import com.ebay.jetstream.event.processor.esper.annotation.PublishOn;
import com.ebay.jetstream.event.processor.esper.annotation.metadata.PublishOnAnnotationMetadata;

public class PublishOnAnnotationListener implements AnnotationListener {
	private static final String ANNO_KEY = PublishOn.class.getSimpleName();
	
	@Override
	public JetstreamEvent processMetaInformation(JetstreamEvent event,
			StatementAnnotationInfo annotationInfo) {

		PublishOnAnnotationMetadata anntmetadata = (PublishOnAnnotationMetadata) annotationInfo
				.getAnnotationInfo(ANNO_KEY);

		String[] aPublishTopics = anntmetadata.getPublishTopics();
		if (aPublishTopics != null)
			event.setForwardingTopics(aPublishTopics);

		String[] aPublishUrls = anntmetadata.getPublishUrls();
		if (aPublishUrls != null)
			event.setForwardingUrls(aPublishUrls);

		return event;
	}

	@Override
	public Class getAnnotationClass() {
		return PublishOn.class;
	}

}
