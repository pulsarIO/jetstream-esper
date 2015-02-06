/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Dual licensed under the Apache 2.0 license and the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper;

import java.util.ArrayList;
import java.util.HashMap;

import com.ebay.jetstream.event.JetstreamEvent;

public class ESPTestRunnable implements Runnable {

  private final EsperProcessor m_processor;
  private final int m_id;

  public ESPTestRunnable(EsperProcessor processor, int id) {
    m_processor = processor;
    m_id = id;
  }

  public void run() {
    JetstreamEvent event = generateNVP();
    event.setEventType("ESPTestEvent1");
    m_processor.sendEvent(event);
  }

  private JetstreamEvent generateNVP() {
    HashMap<String, Object> map = new HashMap<String, Object>();
    String field1 = "456.789";
    ArrayList<String> field2 = new ArrayList<String>();
    EsperTestEventField field3 = new EsperTestEventField();
    field2.add("Field 2, value 1");
    field2.add("Field 2, value 2");
    map.put("id", m_id);
    map.put("field1", field1);
    map.put("field2", field2);
    map.put("field3", field3);
    return new JetstreamEvent(map);
  }

}
