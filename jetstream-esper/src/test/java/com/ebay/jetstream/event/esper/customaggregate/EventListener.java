/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Dual licensed under the Apache 2.0 license and the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.esper.customaggregate;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.context.ApplicationEvent;

import com.ebay.jetstream.event.EventException;
import com.ebay.jetstream.event.processor.esper.EsperEventConverter;
import com.ebay.jetstream.event.processor.esper.EsperEventListener;
import com.ebay.jetstream.event.processor.esper.EsperWrappedEventConverter;
import com.ebay.jetstream.xmlser.XSerializable;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.StatementAwareUpdateListener;

public class EventListener extends EsperEventListener implements StatementAwareUpdateListener, XSerializable{

	private final AtomicLong m_eventReceived = new AtomicLong(0);

	protected static final EsperEventConverter DEFAULT_CONVERTER = new EsperWrappedEventConverter();
	
	private EsperEventConverter m_eventConverter = DEFAULT_CONVERTER;
	
	public EsperEventConverter getEventConverter() {
		return m_eventConverter;
	}
	
	@Override
	public void update(EventBean[] newEvents, EventBean[] oldEvents,
			EPStatement statement, EPServiceProvider epServiceProvider) {
		
		System.out.println(newEvents);
		if(newEvents == null)
			return;

		m_eventReceived.incrementAndGet();
		for (EventBean bean : newEvents) {
			try {
				System.out.println(bean.get("conOutput"));
			}
			catch (EventException e) {
			}
		}

		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void processApplicationEvent(ApplicationEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}

}
