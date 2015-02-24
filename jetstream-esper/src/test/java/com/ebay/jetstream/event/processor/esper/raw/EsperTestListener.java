/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Licensed under the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper.raw;

import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

public class EsperTestListener implements UpdateListener {

  private static final Log log = LogFactory.getLog(EsperTestListener.class);

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

  public void update(EventBean[] newEvents, EventBean[] oldEvents) {
    EventBean event = newEvents[0];
    Integer id = (Integer) event.get("id");

    ids.add(id);
    log.debug("id " + id);

    count++;
    if (newEvents != null)
      newCount += newEvents.length;
    if (oldEvents != null)
      oldCount += oldEvents.length;
  }

}
