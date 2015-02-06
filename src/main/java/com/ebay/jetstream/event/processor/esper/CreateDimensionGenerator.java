/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Dual licensed under the Apache 2.0 license and the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import com.ebay.jetstream.event.JetstreamEvent;
import com.espertech.esper.client.soda.AnnotationAttribute;

public class CreateDimensionGenerator extends AffinityKeyGenerator {

	private String[] m_aFields;
	
	public CreateDimensionGenerator(List<AnnotationAttribute> attributes) {
		super(attributes);
		for (AnnotationAttribute attr : attributes) {
			if ("dimensionspan".equals(attr.getName())) {
				String strSpan = (String)attr.getValue();
				if (strSpan != null && strSpan.length() != 0)	
					m_aFields = strSpan.split(",");
			}
		}
		
		if (m_aFields == null)
			throw new IllegalArgumentException("@CreateDimension.dimensionspan must be defined");
		
		if (getNameAttribute() == null || getNameAttribute().length() == 0)
			throw new IllegalArgumentException("@CreateDimension.name must be defined");
	}

	String getFieldName() {
		return getNameAttribute();
	}
	
	String getValue(JetstreamEvent event) {
		
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
		} 
		catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		for (String strField : m_aFields) {
			Object objValue = event.get(strField);
			if (objValue != null)
				md.update(objValue.toString().getBytes());
		}
		BigInteger bi = new BigInteger(1, md.digest());
		return bi.toString(16);
	}
}
