/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Dual licensed under the Apache 2.0 license and the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.epl;

import java.util.HashMap;
import java.util.Map;

import com.espertech.esper.epl.agg.aggregator.AggregationMethod;

public class HistogramAggregator implements AggregationMethod {

	private String[] m_fields;
	private Map<Object, Integer>[] m_maps;
	
	@Override
	public void enter(Object value) {
		Object[] aParts = (Object[])value;
		if (m_maps == null) {
			m_fields = new String[aParts.length / 2];
			m_maps = new Map[aParts.length / 2];
			for (int i = 0, j = 0; i < m_fields.length; i++, j++) {
				m_maps[i] = new HashMap<Object, Integer>();
				m_fields[i] = (String)aParts[i + j];
			}
		}
		
		for (int i = 0, j = 1; i < m_maps.length; i++, j++) {
			Object objValue = aParts[i + j];
			if (objValue != null) {
				Integer num = m_maps[i].get(objValue);
				if (num == null)
					num = Integer.valueOf(0);
				m_maps[i].put(objValue, Integer.valueOf(num + 1));
			}
		}
	}

	@Override
	public void leave(Object value) {
	
	}

	@Override
	public Map getValue() {
		Map<String, Object> event = new HashMap<String, Object>();
		for (int i = 0; i < m_fields.length; i++)
			event.put(m_fields[i], m_maps[i]);
		return event;
	}

	@Override
	public Class getValueType() {
		return Map.class;
	}

	@Override
	public void clear() {
		m_fields = null;
		m_maps = null;
	}

}
