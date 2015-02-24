/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Licensed under the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper;

public class StringEventType extends AbstractEventType {

	private static final long serialVersionUID = 1784746590670293787L;
	private String m_strEventClassName;

	public String getEventClassName() {
		return m_strEventClassName;
	}

	public void setEventClassName(String strEventClassName) {
		m_strEventClassName = strEventClassName;
	}

	@Override
	protected Object getEventDefinition() {
		return getEventClassName();
	}
}
