/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Dual licensed under the Apache 2.0 license and the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper.raw;

import java.util.HashMap;
import java.util.Map;

import com.espertech.esper.client.EPServiceProvider;

public class EsperTestAggregationRunnable implements Runnable {

  private final EPServiceProvider m_engine;
  private final int m_id;

  public EsperTestAggregationRunnable(EPServiceProvider engine, int id) {
    m_engine = engine;
    m_id = id;
  }

  public void run() {
    Thread thread = Thread.currentThread();
    final long workerId = thread.getId();
    Map<String, ? extends Object> map = generateNVP(m_id, workerId);
    EsperTest.doSendAggrEvent(m_engine.getEPRuntime(), workerId, map);
  }

  private Map<String, ? extends Object> generateNVP(int id, long workerId) {
    HashMap<String, Object> map = new HashMap<String, Object>();
    map.put("workerId", workerId);
    map.put("guid", id);
    return map;
  }

}
