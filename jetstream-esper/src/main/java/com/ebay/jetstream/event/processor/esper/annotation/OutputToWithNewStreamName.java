/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Licensed under the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper.annotation;

/**
 *  - Annotation is to change the existing stream name to new name
 * @author rmuthupandian
 *
 */
public @interface OutputToWithNewStreamName {
	
	String sinks() default "";
	
	String newname() default "";

}
