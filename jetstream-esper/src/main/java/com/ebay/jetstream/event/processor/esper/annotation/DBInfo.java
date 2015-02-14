/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Dual licensed under the Apache 2.0 license and the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper.annotation;

public @interface DBInfo {

	public enum WriteMode {INSERT_ONLY, UPSERT, UPDATE_ONLY}
	String table() default "";
	String sumfields() default "";
	String minfields() default "";
	String maxfields() default "";
	WriteMode mode() default WriteMode.UPSERT;
	
}
