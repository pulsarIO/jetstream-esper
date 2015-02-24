/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Licensed under the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper;

import java.io.Serializable;

import com.ebay.jetstream.xmlser.XSerializable;

/**
 * Events need to be declared for Esper processors to recognize them. 
 * AbstractEventType is a base for declared events.
 * 
 * @author trobison, derived from original work by msikes
 */
public abstract class AbstractEventType implements Serializable, XSerializable {
	
	private static final long serialVersionUID = -1348054045746281953L;
	private String m_strEventAlias;

	public String getEventAlias() {
		return m_strEventAlias;
	}

	public void setEventAlias(String strEventAlias) {
		m_strEventAlias = strEventAlias;
	}

	protected abstract Object getEventDefinition();
}
