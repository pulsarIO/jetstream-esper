/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Licensed under the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.epl;

import java.util.Map;

import com.espertech.esper.client.hook.AggregationFunctionFactory;
import com.espertech.esper.epl.agg.aggregator.AggregationMethod;
import com.espertech.esper.epl.agg.service.AggregationValidationContext;

public class HistogramAggregatorFactory implements AggregationFunctionFactory {

	@Override
	public void setFunctionName(String functionName) {
		// nothing needed here
	}

	@Override
	public void validate(AggregationValidationContext validationContext) {
		// TODO ADD VALIDATAION
	}

	@Override
	public AggregationMethod newAggregator() {
		return new HistogramAggregator();
	}

	@Override
	public Class getValueType() {
		return Map.class;
	}
}






