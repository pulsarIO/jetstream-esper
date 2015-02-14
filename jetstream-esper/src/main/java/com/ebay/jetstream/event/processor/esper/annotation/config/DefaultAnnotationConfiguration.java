/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Dual licensed under the Apache 2.0 license and the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper.annotation.config;

import java.util.List;

import com.ebay.jestream.event.annotation.AnnotationConfiguration;

/**
 * - Container class to hold list of AnnotationConfiguration.
 * @author rmuthupandian
 *
 */
public class DefaultAnnotationConfiguration {
	
	private List<AnnotationConfiguration> defaultAnnotationConfiguration;

	public List<AnnotationConfiguration> getDefaultAnnotationConfiguration() {
		return defaultAnnotationConfiguration;
	}

	public void setDefaultAnnotationConfiguration(
			List<AnnotationConfiguration> defaultAnnotationConfiguration) {
		this.defaultAnnotationConfiguration = defaultAnnotationConfiguration;
	}

}
