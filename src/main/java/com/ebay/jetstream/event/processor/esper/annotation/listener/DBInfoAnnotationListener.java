/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Dual licensed under the Apache 2.0 license and the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper.annotation.listener;

import com.ebay.jestream.event.annotation.AnnotationListener;
import com.ebay.jestream.event.annotation.StatementAnnotationInfo;
import com.ebay.jetstream.event.JetstreamEvent;
import com.ebay.jetstream.event.JetstreamReservedKeys;
import com.ebay.jetstream.event.processor.esper.annotation.DBInfo;
import com.ebay.jetstream.event.processor.esper.annotation.metadata.DBInfoAnnotationMetadata;

public class DBInfoAnnotationListener implements AnnotationListener {
	private static final String ANNO_KEY = DBInfo.class.getSimpleName();
    
	@Override
	public JetstreamEvent processMetaInformation(JetstreamEvent event,
			StatementAnnotationInfo annotationInfo) {
	
		DBInfoAnnotationMetadata anntmetadata = (DBInfoAnnotationMetadata) annotationInfo.getAnnotationInfo(ANNO_KEY);
		if (anntmetadata != null){
			event.put(JetstreamReservedKeys.EventMetaData.toString(), anntmetadata.getMapMetaData());
		}	

		return event;
	}

	@Override
	public Class getAnnotationClass() {
		return DBInfo.class;
	}
	
	

}
