/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Licensed under the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper.annotation.metadata;

import java.util.LinkedList;
import java.util.List;

import com.ebay.jetstream.event.EventSink;

/**
 * - Metadata class holds new name for the stream to change.
 * @author rmuthupandian
 *
 */
public class OutputToNewStreamMetadata {
	
	public String name ;
	private List<EventSink> sinklist = new LinkedList<EventSink>();
	private EventSink[] sinks;
    
	public void addToSinkList(List<EventSink> theSinks) {
		sinklist.addAll(theSinks);
		sinks = new EventSink[sinklist.size()];
		sinks = sinklist.toArray(sinks);
	}

	public EventSink[] getSinks() {
		return sinks;
	}
	
	public String getStreamName(){
		return this.name; 
	}
	
	public void setStreamName(String newName){
		this.name = newName;
	}

}
