/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Licensed under the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper;

import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.espertech.esper.client.EventBean;

public class ESPTestListener implements EventListener {

  private static final Logger LOGGER = LoggerFactory.getLogger("com.ebay.jetstream.config");

  private final SortedSet<Integer> ids = new TreeSet<Integer>();
  private int count = 0;
  private int newCount = 0;
  private int oldCount = 0;

  int getCount() {
    return count;
  }

  public SortedSet<Integer> getIds() {
    return ids;
  }

  int getNewCount() {
    return newCount;
  }

  int getOldCount() {
    return oldCount;
  }

  @SuppressWarnings("unchecked")
  public void update(EventBean[] newEvents, EventBean[] oldEvents) {
    EventBean event = newEvents[0];
    Integer id = (Integer) event.get("id");
    ids.add(id);

    LOGGER.info( "**Listener id " + id);

    count++;
    if (newEvents != null)
      newCount += newEvents.length;
    if (oldEvents != null)
      oldCount += oldEvents.length;

    HashMap<String, Object> hm = (HashMap<String, Object>) event.getUnderlying();
    if (!(hm.get("field1") instanceof Double))
      throw new IllegalStateException("expecting a Double");
  }

}
