/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Dual licensed under the Apache 2.0 license and the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ebay.jestream.event.annotation.EngineMetadata;
import com.espertech.esper.client.soda.AnnotationPart;
import com.espertech.esper.client.soda.EPStatementObjectModel;

public class EsperEngineAnnotationMetadata implements EngineMetadata {
	
	private Map<String, List<AnnotationPart>> partsMap = new HashMap<String, List<AnnotationPart>>();
	private EPStatementObjectModel model ;
	
	public void setAnnotationPartsMap(Map<String, List<AnnotationPart>> annotPartsMap){
		this.partsMap = annotPartsMap;
	}
	
	public Map<String, List<AnnotationPart>> getAnnotationPartsMap(){
		return partsMap;
	}
	
	public void setStatementObjectModel(EPStatementObjectModel model){
		this.model = model;
	}
	
	public EPStatementObjectModel getStatementObjectModel(){
		return model;
	}
	
	

}
