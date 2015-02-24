/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Licensed under the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper.annotation.metadata;

import java.util.LinkedList;
import java.util.List;

import com.ebay.jetstream.event.EventSink;

public class OutputToAnnotationMetadata {

	private List<EventSink> sinklist = new LinkedList<EventSink>();
	private String strStreamName = "";
	private EventSink[] sinks;
    
	public String getStreamName() {
		return strStreamName;
	}

	public void setStreamName(String strStreamName) {
		this.strStreamName = strStreamName;
	}

	public void addToSinkList(List<EventSink> theSinks) {
		sinklist.addAll(theSinks);
		sinks = new EventSink[sinklist.size()];
		sinks = sinklist.toArray(sinks);
	}

	public EventSink[] getSinks() {
		return sinks;
	}

}
