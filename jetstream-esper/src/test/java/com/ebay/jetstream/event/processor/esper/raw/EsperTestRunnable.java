/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Licensed under the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper.raw;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.ebay.jetstream.event.processor.esper.EsperTestEventField;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.time.CurrentTimeEvent;

public class EsperTestRunnable implements Runnable {
  private final EPServiceProvider m_engine;
  private final int m_id;

  public EsperTestRunnable(EPServiceProvider engine, int id) {
    m_engine = engine;
    m_id = id;
  }

  public void run() {
    Map<String, ? extends Object> map = generateNVP(m_id);
    EPRuntime runtime = m_engine.getEPRuntime();
    runtime.sendEvent(new CurrentTimeEvent(System.nanoTime() / 1000));
    runtime.sendEvent(map, "EsperTestEvent");
  }

  private Map<String, ? extends Object> generateNVP(int id) {
    HashMap<String, Object> map = new HashMap<String, Object>();
    String field1 = "Field 1 value";
    ArrayList<String> field2 = new ArrayList<String>();
    EsperTestEventField field3 = new EsperTestEventField();
    field2.add("Field 2, value 1");
    field2.add("Field 2, value 2");
    map.put("id", id);
    map.put("field0", new Double(id));
    map.put("field1", field1);
    map.put("field2", field2);
    map.put("field3", field3);
    return map;
  }
}
