/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Dual licensed under the Apache 2.0 license and the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper;

import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ebay.jetstream.event.EventException;
import com.ebay.jetstream.event.EventSink;
import com.ebay.jetstream.event.JetstreamEvent;

public class ESPTestSink implements EventSink {
  private static final Log testLogger = LogFactory.getLog(ESPTestSink.class);

  private final SortedSet<Integer> ids = new TreeSet<Integer>();
  private int count = 0;

  public String getBeanName() {
    // TODO Auto-generated method stub
    return null;
  }

  public int getCount() {
    return count;
  }

  public SortedSet<Integer> getIds() {
    return ids;
  }

  public void sendEvent(JetstreamEvent event) throws EventException {
    count++;
    Integer id = (Integer) event.get("id");
    if (id != null) {
      ids.add(id);
    }
    testLogger.debug("id " + id);

  }
}
