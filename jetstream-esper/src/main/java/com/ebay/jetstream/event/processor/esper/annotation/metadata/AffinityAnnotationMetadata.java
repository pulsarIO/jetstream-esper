/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Licensed under the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper.annotation.metadata;

import java.util.LinkedList;
import java.util.List;

import com.ebay.jetstream.event.processor.esper.AffinityKeyGenerator;

public class AffinityAnnotationMetadata {
	
	private List<AffinityKeyGenerator> keygen = new LinkedList<AffinityKeyGenerator>();
	
	public void setKeygen(List<AffinityKeyGenerator> keygen) {
		this.keygen = keygen;
	}
	
	public void addToKeyGenList(AffinityKeyGenerator keygen){
		this.keygen.add(keygen);
	}
	
	public List<AffinityKeyGenerator> getAffinityKeyGen(){
		return this.keygen;
	}

}
