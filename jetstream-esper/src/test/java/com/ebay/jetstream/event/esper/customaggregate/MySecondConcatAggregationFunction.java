/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Licensed under the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.esper.customaggregate;

import com.espertech.esper.epl.agg.aggregator.AggregationMethod;

public class MySecondConcatAggregationFunction implements AggregationMethod {

	private static char DELIMITER = '2';
	private StringBuilder builder;
	private String delimiter;

	public MySecondConcatAggregationFunction() {
		builder = new StringBuilder();
		delimiter = "";
	}

	@Override
	public void enter(Object value) {
		if (value != null) {
			builder.append(delimiter);
			builder.append(value.toString());
			delimiter = Character.toString(DELIMITER);
		}
	}

	@Override
	public void leave(Object value) {
		if (value != null) {
			builder.setLength(0);
		}
	}

	@Override
	public Object getValue() {
		return builder.toString();
	}

	@Override
	public Class getValueType() {
		return String.class;
	}

	@Override
	public void clear() {
		builder = new StringBuilder();
		delimiter = "";
	}

}
