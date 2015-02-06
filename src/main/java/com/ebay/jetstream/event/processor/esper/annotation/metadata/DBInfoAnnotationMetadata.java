/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Dual licensed under the Apache 2.0 license and the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper.annotation.metadata;

import java.util.Map;

public class DBInfoAnnotationMetadata {
	
	private Map<String, Object> mapMetaData;
		
	public Map<String, Object> getMapMetaData() {
		return mapMetaData;
	}
	public void setMapMetaData(Map<String, Object> mapMetaData) {
		this.mapMetaData = mapMetaData;
	}
	
}
