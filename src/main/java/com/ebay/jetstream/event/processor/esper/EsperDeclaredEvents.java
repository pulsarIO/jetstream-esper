/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Dual licensed under the Apache 2.0 license and the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import com.ebay.jetstream.config.AbstractNamedBean;
import com.ebay.jetstream.xmlser.XSerializable;

/**
 * @author trobison, derived from original work of msikes
 */
public final class EsperDeclaredEvents extends AbstractNamedBean implements Serializable, XSerializable {

	private static final long serialVersionUID = -8107344168985484754L;
	private final Collection<AbstractEventType> m_listEventTypes = new ArrayList<AbstractEventType>();

	/**
	 * Gets all registered Event Types with their fields
	 * 
	 * @return the collection of event types
	 */
	@SuppressWarnings("unchecked")
	public Collection<AbstractEventType> getEventTypes() {
		return (Collection<AbstractEventType>)((ArrayList<AbstractEventType>)m_listEventTypes).clone();
	}

	public void setEventTypes(Collection<AbstractEventType> eventTypes) {
		m_listEventTypes.clear();
		m_listEventTypes.addAll(eventTypes);
	}
}
