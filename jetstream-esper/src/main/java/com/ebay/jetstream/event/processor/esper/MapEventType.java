/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Licensed under the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper;

import java.util.Collections;
import java.util.Map;

import com.ebay.jetstream.config.ConfigException;

/**
 * MappedEvent is used to declare an event based on a Map, such as a JetstreamEvent.
 * @author trobison, derived from original work by msikes
 */
public class MapEventType extends AbstractEventType {
	
	private static final long serialVersionUID = -5381179076717992566L;
	private Map<String, Object> m_mapEventFields;

	@SuppressWarnings("unchecked")
	public Map<String, Object> getEventFields() {
		return m_mapEventFields == null ? Collections.EMPTY_MAP : m_mapEventFields;
	}

	public void setEventFields(Map<String, Object> mapEventFields) throws ConfigException {
		validateFields(mapEventFields);
		m_mapEventFields = Collections.unmodifiableMap(mapEventFields);
	}

	@Override
	protected Map<String, Object> getEventDefinition() {
		return getEventFields();
	}

	@SuppressWarnings("unchecked")
	private void validateFields(Map<String, Object> mapFields) throws ConfigException {
		
		for (Map.Entry<String, Object> entry : mapFields.entrySet()) {
			Object objValue = entry.getValue();

			/*
			 * String must be promoted to Class<?> because Spring won't convert String 
			 * to Class.  Map values are validated recursively, null and Class<?> are OK,
			 * anything else is invalid.
			 */
			if (objValue == null || objValue instanceof Class<?>)
				continue;

			if (objValue instanceof String) {
				try {
					entry.setValue(Class.forName((String)objValue));
				} 
				catch (ClassNotFoundException e) {
					throw new ConfigException("Mapped field validation failed", e);
				}
			}
			else if (objValue instanceof Map)
				validateFields((Map<String, Object>)objValue);
			else
				throw new ConfigException("not a String, a Class, a Map or null: " + objValue);
		}
	}
}
