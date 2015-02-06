/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Dual licensed under the Apache 2.0 license and the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper.aggregates;

import com.clearspring.analytics.stream.cardinality.HyperLogLog;
import com.clearspring.analytics.stream.cardinality.ICardinality;
import com.espertech.esper.epl.agg.aggregator.AggregationMethod;

/**
 * @author shmurthy@ebay.com Esper aggregation method for computing 
 * the cardinality of elements provided to this aggregator
 * 
 * Usage: select cardinality(element) as uniqueElement from stream
 */

public class CardinalityAggregator implements AggregationMethod {

	ICardinality m_card = new HyperLogLog(24);

	@Override
	public void enter(Object value) {

		m_card.offer(value);

	}

	@Override
	public void leave(Object value) {
		// nothing to do

	}

	@Override
	public Object getValue() {

		return m_card.cardinality();
	}

	@Override
	public Class getValueType() {

		return Long.class;
	}

	@Override
	public void clear() {
	}

	
}
