/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Dual licensed under the Apache 2.0 license and the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper.annotation.metadata;

public class PublishOnAnnotationMetadata {
	
	private String urls;
	private String topics;
	private String[] m_aUrls;
	private String[] m_aTopics;

	
	public void setUrls(String urls) {
		this.urls = urls;
		this.m_aUrls = ( urls != null) ?  urls.split(",") : null;
	
	}
	public void setTopics(String topics) {
		this.topics = topics;
		m_aTopics = (topics != null) ? topics.split(",") : null;
	}
	
	public String[] getPublishUrls() {
		return m_aUrls;
	}
	
	public String[] getPublishTopics() {
		return m_aTopics;
	}

}
