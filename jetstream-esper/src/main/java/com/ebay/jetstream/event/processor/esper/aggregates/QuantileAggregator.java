/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Licensed under the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper.aggregates;

import com.clearspring.analytics.stream.quantile.TDigest;
import com.espertech.esper.epl.agg.aggregator.AggregationMethod;

/**
 * @author shmurthy@ebay.com Esper aggregation method for computing 
 * the Quantile of element of type double provided to this aggregator
 * 
 * Usage: select percentile(quantile, element) as percentile from stream
 * quantile must be between 0 - 1. 
 */

public class QuantileAggregator implements AggregationMethod {

	 TDigest m_digest;
	 double m_quantile;
	 double m_compression = 10.0;
	
	@Override
	public void enter(Object value) {

		Object[] params = (Object[]) value;
		
		if(m_digest == null) {
		  m_quantile = (Double) params[0];
		  m_digest = new TDigest(m_compression);
		}
				 
		m_digest.add((Double) params[1]);
		
	}

	@Override
	public void leave(Object value) {
				// nothing to do
	}

	@Override
	public Object getValue() {
		
		return m_digest.quantile(m_quantile);
	}

	@Override
	public Class getValueType() {
		
		return Double.class;
	}

	@Override
	public void clear() {
		m_digest = null;
		
	}

}
