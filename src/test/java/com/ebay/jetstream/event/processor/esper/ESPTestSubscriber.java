/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Dual licensed under the Apache 2.0 license and the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ebay.jetstream.event.processor.esper.raw.EsperTestListener;

public class ESPTestSubscriber {
  private static final Log testLogger = LogFactory.getLog(EsperTestListener.class);
  private final SortedSet<Integer> ids = new TreeSet<Integer>();
  private int count = 0;

  public ESPTestSubscriber() {
    testLogger.info("subscribed");
  }

  public int getCount() {
    return count;
  }

  public SortedSet<Integer> getIds() {
    return ids;
  }

  public synchronized void update(Integer id, Double field1, List<String> field2, EsperTestEventField field3) {
    count++;
    if (id == null) {
      testLogger.error("Id is null!");
    }
    else {
      ids.add(id);
    }
  }

}
