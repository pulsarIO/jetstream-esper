/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Dual licensed under the Apache 2.0 license and the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.esper.customaggregate;


import com.espertech.esper.client.hook.AggregationFunctionFactory;
import com.espertech.esper.epl.agg.aggregator.AggregationMethod;
import com.espertech.esper.epl.agg.service.AggregationValidationContext;


public class MyConcatAggregationFunctionFactory implements AggregationFunctionFactory {

    public void validate(AggregationValidationContext validationContext) {
        if ((validationContext.getParameterTypes().length != 1) ||
            (validationContext.getParameterTypes()[0] != String.class)) {
            throw new IllegalArgumentException("Concat aggregation requires a single parameter of type String");
        }
    }

    public Class getValueType() {
        return String.class;
    }

    public void setFunctionName(String functionName) {
        // not required here
    }

    public AggregationMethod newAggregator() {
        return new MyConcatAggregationFunction();
    }
}