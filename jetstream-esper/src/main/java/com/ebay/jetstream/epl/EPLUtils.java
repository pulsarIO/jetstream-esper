/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Licensed under the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.epl;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;
import java.util.Random;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ebay.jetstream.event.JetstreamEvent;
import com.ebay.jetstream.event.JetstreamReservedKeys;

public final class EPLUtils {

	private static final Random m_rand = new SecureRandom();
	private static final Logger LOGGER = LoggerFactory.getLogger("com.ebay.jetstream.epl.EPLUtils");
	private static volatile int m_nErrorCount;
	
	private EPLUtils() {
		// util class
	}
	
	public static String toJsonString(Object obj) throws JsonGenerationException, JsonMappingException, IOException {
		return EPLUtils.toJsonString(obj, false);
	}
	
	public static Timestamp toTimestamp(Object objInParam) {
		Timestamp ts = null;
		if (objInParam instanceof Timestamp)
			ts = (Timestamp)objInParam;
		else if (objInParam instanceof Number)
			ts = new Timestamp(((Number)objInParam).longValue());
		else if (objInParam instanceof Date)
			ts = new Timestamp(((Date)objInParam).getTime());
		else if (objInParam instanceof String) {
			if ("now".equalsIgnoreCase((String)objInParam))
				ts = new Timestamp(System.currentTimeMillis());
			else
				ts = Timestamp.valueOf((String)objInParam);
		}
		else if (objInParam == null)
			ts = new Timestamp(System.currentTimeMillis());
		return ts;
	}
	
	public static String toJsonString(Object obj, boolean bStripJsReservedWords) throws JsonGenerationException, JsonMappingException, IOException {
		String strResult = null;
		if (obj != null) {
			
			if (bStripJsReservedWords && obj instanceof JetstreamEvent)
				obj = ((JetstreamEvent)obj).getFilteredEvent();
			
			try {
				ObjectMapper mapper = new ObjectMapper();
				Writer writer = new StringWriter();
				mapper.writeValue(writer, obj);
				strResult = writer.toString();
			}
			catch (Throwable t) {
				if (m_nErrorCount++ % 10000 == 0 && LOGGER.isErrorEnabled())
					LOGGER.error( "", t); 
			}
		}
		return strResult;
	}
	
	public static long getRandomLong(Object objDontCache) {
		return Math.abs(m_rand.nextLong());
	}
	
	public static Class getClass(Object obj) {
		return obj != null ? obj.getClass() : null;
	}
	
	public static Map<String, Object> fromJsonString(String strJson) throws JsonParseException, IOException {
		Map<String, Object> mapOut = null;
		if (strJson != null) {
			ObjectMapper mapper = new ObjectMapper();
			mapOut = mapper.readValue(strJson, Map.class);
		}
		return mapOut;
	}
	
	public static long generateEventId(Map<String, Object> event) {
		long lEventId = 0;
		if (event != null) {
			Number numIpHost = (Number)event.get("host");
			if (numIpHost != null)
				lEventId = JetstreamSideEventIdGenerator.generateEventId(numIpHost.intValue());
		}
		if (lEventId == 0 && event.get("id") instanceof Number)
			lEventId = ((Number)event.get("id")).longValue();
		return lEventId;
	}
	
	public static String getEventOrigin(Map mapInput) {
		String strOrigin = null;
		if (mapInput != null)
			strOrigin = (String)mapInput.get(JetstreamReservedKeys.EventOrigin.toString());
		return strOrigin != null ? strOrigin : "unknown";
	}
	
	public static String getEventType(Map mapInput) {
		String strType = null;
		if (mapInput != null)
			strType = (String)mapInput.get(JetstreamReservedKeys.EventType.toString());
		return strType != null ? strType : "unknown";
	}
	
	public static String getHostName() {
		String host = "unknownhost";
		try {
			host = java.net.InetAddress.getLocalHost().getHostName();
		} catch (java.net.UnknownHostException e) {
			
		}
		return host;
	}
}
