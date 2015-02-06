/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Dual licensed under the Apache 2.0 license and the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper;

import java.util.List;

import com.ebay.jetstream.event.JetstreamEvent;
import com.ebay.jetstream.event.JetstreamReservedKeys;
import com.ebay.jetstream.event.processor.esper.annotation.CreateDimension;
import com.espertech.esper.client.soda.AnnotationAttribute;
import com.espertech.esper.client.soda.AnnotationPart;

public class AffinityKeyGenerator {
	
	private String m_strNameAttribute;
	private AffinityKeyGenerator m_gen;
	
	public AffinityKeyGenerator(List<AnnotationAttribute> attributes) {
		for (AnnotationAttribute attr : attributes) {
			if ("colname".equals(attr.getName()) || "name".equals(attr.getName()))
				m_strNameAttribute = (String)attr.getValue();
			
			if ("dimension".equals(attr.getName())) {
				Object objDimension = attr.getValue();
				if (objDimension instanceof AnnotationPart) {
					AnnotationPart annnoDimension = (AnnotationPart)objDimension;
					if (CreateDimension.class.getSimpleName().equals(annnoDimension.getName())) {
						m_gen = new CreateDimensionGenerator(annnoDimension.getAttributes());
					}
				}
			}
		}
	}
	
	public void setEventFields(JetstreamEvent event) {
		if (m_gen == null)
			event.put(getFieldName(), getValue(event));
		else {
			m_gen.setEventFields(event);
			event.put(getFieldName(), event.get(m_gen.getFieldName()));
		}
	}
	
	String getValue(JetstreamEvent event) {
		String strValue = null;
		Object objValue = event.get(getNameAttribute());
		if (objValue != null)
			strValue = objValue.toString();
		return strValue;
	}
	
	String getFieldName() {
		return JetstreamReservedKeys.MessageAffinityKey.toString();
	}
	
	String getNameAttribute() {
		return m_strNameAttribute;
	}
}
