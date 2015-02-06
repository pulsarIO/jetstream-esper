/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Dual licensed under the Apache 2.0 license and the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper.aggregates;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.espertech.esper.client.hook.AggregationFunctionFactory;
import com.espertech.esper.epl.agg.aggregator.AggregationMethod;
import com.espertech.esper.epl.agg.service.AggregationValidationContext;

/**
 * @author shmurthy@ebay.com Esper aggregation Function factory for  
 * TopKStringAggregator
 * 
 * 
 */

public class TopKAggregatorFactory implements AggregationFunctionFactory {

	private static final Logger LOGGER = LoggerFactory.getLogger("com.ebay.jetstream.event.processor.esper.aggregates");
	
	@Override
	public void setFunctionName(String functionName) {
		// nothing needed here
	}

	// @Override
	public void validate(AggregationValidationContext validationContext) {
		if (validationContext.getParameterTypes().length != 3)
			LOGGER.error( "TopK Aggregation Function requires 3 parameters viz. max capacity, # top elements and element - topN(maxCapacit, topElements, element)");
	}

	@Override
	public AggregationMethod newAggregator() {
		
		return new TopKAggregator();
	}

	@Override
	public Class getValueType() {
		
		return Map.class;
		
	}

	

}
