/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Licensed under the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ebay.jetstream.event.JetstreamEvent;
import com.ebay.jetstream.event.MonitorableStatCollector;
import com.ebay.jetstream.event.processor.EventProcessRequest;
import com.ebay.jetstream.event.processor.esper.EsperProcessor.EsperInternals;

/**
 * @author shmurthy@ebay.com -  worker request to execute DB TXN
 */
public class ProcessEventRequest extends EventProcessRequest {

	private static final Logger LOGGER = LoggerFactory.getLogger("com.ebay.jetstream.event.processor.esper");
	private EsperInternals m_esperInternals;

	/**
	 */
	public ProcessEventRequest(EsperProcessor.EsperInternals esp, JetstreamEvent event, 
			MonitorableStatCollector parent) {
		super(event, parent);
		m_esperInternals = esp;
	}

	@Override
	protected void processEvent(JetstreamEvent event) throws Exception {
		m_esperInternals.processEvent(event);
	}
	
	protected EsperInternals getEsperInternals() {
		return m_esperInternals;
	}
}
