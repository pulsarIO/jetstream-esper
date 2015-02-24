/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Licensed under the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper;

/**
 * ClassEvent is used to declare an event that is an instance of an arbitrary java class, that is, a POJO.
 * 
 * @author msikes
 */
public class ClassEventType extends StringEventType {
	
	private static final long serialVersionUID = 1L;

	@Override
	protected Class<?> getEventDefinition() {
		Class<?> clazz = null;
		try {
			clazz = Class.forName(getEventClassName());
		} 
		catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("Unknown class: " + getEventClassName(), e);
		}
		return clazz;
	}
}
