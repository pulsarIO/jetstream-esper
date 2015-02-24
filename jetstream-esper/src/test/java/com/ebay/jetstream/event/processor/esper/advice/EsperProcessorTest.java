/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Licensed under the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper.advice;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.BeforeClass;
import org.junit.Test;

import com.ebay.jetstream.config.ApplicationInformation;
import com.ebay.jetstream.config.Configuration;
import com.ebay.jetstream.config.RootConfiguration;
import com.ebay.jetstream.event.JetstreamEvent;
import com.ebay.jetstream.event.RetryEventCode;
import com.ebay.jetstream.event.advice.Advice;
import com.ebay.jetstream.event.processor.esper.EsperProcessor;
import com.ebay.jetstream.event.support.AbstractEventProcessor;

public class EsperProcessorTest {
	
	
 private static final Configuration s_springConfiguration = new RootConfiguration(new ApplicationInformation(
		      "EsperProcessorTest", "0.0"), new String[] { "src/test/java/com/ebay/jetstream/event/processor/esper/advice/EsperConfig.xml" });

 private static EsperProcessor processor = (EsperProcessor) s_springConfiguration.getBean("EsperProcessor1");
 
 private static AbstractEventProcessor espersink = (AbstractEventProcessor) s_springConfiguration.getBean("espersink"); 
 
 
 @BeforeClass
 public static void setUp(){
	 
 }
 
 
 public void sendEvent() {
	  processor.sendEvent(createEvent());
	  try {
		Thread.sleep(200);
	} catch (InterruptedException e) {
		e.printStackTrace();
	}
 }
 
 public void sendBadEvent() {
	  processor.sendEvent(createBadEvent());
	  try {
		Thread.sleep(200);
	} catch (InterruptedException e) {
		e.printStackTrace();
	}
}
 
 public void sendBadEvent(JetstreamEvent event) {
	  processor.sendEvent(event);
	  try {
		Thread.sleep(200);
	} catch (InterruptedException e) {
		e.printStackTrace();
	}
}
 
 @Test
 public void testEPLDynamicUpdate(){
	
	 
 }
 
 @Test
 public void testExceptionHandlerWithoutListner(){
	 assertNotNull(processor.getEsperExceptionHandler());
	 
	 processor.setAdviceListener(null); // removing adviceListener
	 
	 sendBadEvent();
	 JetstreamEvent event = createBadEvent();
	 sendBadEvent(event);
	 try {
		Thread.sleep(10000); // sleep for exception wait time window 
	} catch (InterruptedException e) {
		e.printStackTrace();
	}
	sendBadEvent(event);
	sendBadEvent(event);
	  
	 assertNotNull(processor.getEsperExceptionHandler().getLastException());
	 assertTrue(processor.getTotalEventsDropped() != 0);
	 
	 
 }
 
 
 @Test
 public void testExceptionHandlerWithAdviceLr(){
	 
	 processor.getEsperExceptionHandler().clearAlertStatus(); // This is needed , since previous testcases might have set the alert status.
	 
	 Advice mockListner = mock(Advice.class);
	 processor.setAdviceListener(mockListner);
	 
	 assertNotNull(processor.getEsperExceptionHandler());
	 assertNotNull(processor.getAdviceListener());
	 
	 JetstreamEvent event = createBadEvent();
	 sendBadEvent(event);
	 try {
		Thread.sleep(10000); // sleep for exception wait time window 
	} catch (InterruptedException e) {
		e.printStackTrace();
	}
	sendBadEvent(event);
	sendBadEvent(event);
	  
	 assertNotNull(processor.getEsperExceptionHandler().getLastException());
	 
	 //verify AdviseListener is called atleast once to post the msg
	 verify(mockListner, atLeastOnce()).retry(event, RetryEventCode.MSG_RETRY, processor.getEsperExceptionHandler().getLastException());
	 verify(mockListner, atLeastOnce()).stopReplay();
	 
 }
 

 
 private JetstreamEvent createEvent(){
	 JetstreamEvent event = new JetstreamEvent();
	 event.setEventType("TestEvent");
	 event.put("id", 11212);
	 event.put("cost", 11212);
	 return event;
 }
 
 
 private JetstreamEvent createBadEvent(){
	 JetstreamEvent event = new JetstreamEvent();
	 event.setEventType("BadEvent");
	 event.put("cost", 1234);
	 event.put("id", 12345678900644L);
	 return event;
 }
 


}
