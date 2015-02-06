/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Dual licensed under the Apache 2.0 license and the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;

import com.ebay.jetstream.config.ApplicationInformation;
import com.ebay.jetstream.config.Configuration;
import com.ebay.jetstream.config.RootConfiguration;
import com.ebay.jetstream.event.EventException;
import com.ebay.jetstream.event.EventSink;
import com.ebay.jetstream.event.JetstreamEvent;


public class ESPEventProcessorTest {
  protected static final Configuration s_springConfiguration = new RootConfiguration(new ApplicationInformation(
      "ESPTest", "0.0"), new String[] { Configuration.getClasspathContext(ESPTest.class, null) });

  private EsperProcessor m_eventProcessor = null;

  @Before
  public void setUp() throws Exception {
    m_eventProcessor = (EsperProcessor)s_springConfiguration.getBean("ESPTestProcessorXYZ");
    m_eventProcessor.addEventSink(new EventSink() {
      public String getBeanName() {
        // TODO Auto-generated method stub
        return null;
      }

      public void sendEvent(JetstreamEvent event) throws EventException {
        System.out.println("Receive event " + event); //KEEPME
      }
    });
  }

  @After
  public void tearDown() throws Exception {
    m_eventProcessor = null;
  }

  @Ignore
  public void testSendEvent() {
    HashMap<String, Object> map = new HashMap<String, Object>();
    String field1 = "Field 1 value";
    ArrayList<String> field2 = new ArrayList<String>();
    EsperTestEventField field3 = new EsperTestEventField();
    field2.add("Field 2, value 1");
    field2.add("Field 2, value 2");
    map.put("id", "001");
    map.put("field1", field1);
    map.put("field2", field2);
    map.put("field3", field3);
    JetstreamEvent event = new JetstreamEvent("ESPTestEvent1", null, map);
    m_eventProcessor.sendEvent(event);
  }

}
