/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Dual licensed under the Apache 2.0 license and the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper.aggregates;



import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.clearspring.analytics.stream.Counter;
import com.clearspring.analytics.stream.StreamSummary;
import com.espertech.esper.epl.agg.aggregator.AggregationMethod;
import com.espertech.esper.epl.agg.service.AggregationValidationContext;

/**
 * @author shmurthy@ebay.com Esper aggregation method for computing 
 * the topN elements of the elements provided to this aggregator
 * 
 * Usage: select topN(<maxCapacity>, <topNValue>, element) as uniqueElement from stream
 */


public class TopKAggregator implements AggregationMethod {

	StreamSummary<Object> m_counter;
	Integer m_capacity; 
	Integer m_topElementCnt = new Integer(10);
	
	@Override
	public void enter(Object value) {
		
		Object[] params = (Object[]) value;
		
		if(m_counter == null) {
		  m_capacity = (Integer) params[0];
		  m_topElementCnt = (Integer) params[1];
		  m_counter = new StreamSummary<Object>(m_capacity.intValue());
		}
				 
		
		m_counter.offer(params[2]);
		
	}

	@Override
	public void leave(Object value) {
				
	}

	@Override
	public Object getValue() {
		
			
		Map<Object, Long> topN = new HashMap<Object, Long>();
		
		if ((m_counter == null))
			return topN;
			
		List<Counter<Object>> topCounters = m_counter.topK(m_topElementCnt);
		
		for (Counter<Object> counter : topCounters) {
			topN.put(counter.getItem(), counter.getCount());
		}
		return topN;
	}

	@Override
	public Class getValueType() {
		
		return Map.class;
	}

	@Override
	public void clear() {
		m_counter = null;
		
	}

	// @Override
	public void validate(AggregationValidationContext validationContext) {
		
		
	}

}
