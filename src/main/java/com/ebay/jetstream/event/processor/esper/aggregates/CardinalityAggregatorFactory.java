/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Dual licensed under the Apache 2.0 license and the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper.aggregates;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.espertech.esper.client.hook.AggregationFunctionFactory;
import com.espertech.esper.epl.agg.aggregator.AggregationMethod;
import com.espertech.esper.epl.agg.service.AggregationValidationContext;

/**
 * @author shmurthy@ebay.com Esper aggregation Function factory for  
 * CardinalityAggregator
 * 
 * 
 */

public class CardinalityAggregatorFactory implements AggregationFunctionFactory {

	private static final Logger LOGGER = LoggerFactory.getLogger("com.ebay.jetstream.event.processor.esper.aggregates");
	
	
	@Override
	public void setFunctionName(String functionName) {
				
	}

	@Override
	public void validate(AggregationValidationContext validationContext) {
		if (validationContext.getParameterTypes().length != 1)
				LOGGER.error( "Cardinality Aggregation Function requires 1 parameter of type Double)");
			
	}

	@Override
	public AggregationMethod newAggregator() {
		
		return new CardinalityAggregator();
	}

	@Override
	public Class getValueType() {
		
		return Long.class;
	}

}
