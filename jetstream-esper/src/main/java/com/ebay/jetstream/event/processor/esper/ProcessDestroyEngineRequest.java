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
import com.ebay.jetstream.event.processor.esper.EsperProcessor.EsperInternals;


public class ProcessDestroyEngineRequest extends ProcessEventRequest {
	
	private static Logger logger = LoggerFactory.getLogger("com.ebay.jetstream.event.processor.esper.EsperProcessor");
	
	
	public ProcessDestroyEngineRequest(EsperInternals esp) {
		super(esp, null, null);
	}

	@Override
	protected void processEvent(JetstreamEvent event) throws Exception {
		getEsperInternals().getEsperService().getEPRuntime().getEventSender("EsperEndEvent").sendEvent(new Object());
		try {
			Thread.sleep(100);
		}catch (InterruptedException e) {
			logger.error( e.getLocalizedMessage() , e);
		}
		getEsperInternals().getEsperService().destroy();
		getEsperInternals().clear();
	}
	
	protected void afterEventProcessed(JetstreamEvent event, MonitorableStatCollector stats) {
		// do nothing but log
		logger.info( "Destroyed stale Esper engine");
	}
}
