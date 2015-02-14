/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Dual licensed under the Apache 2.0 license and the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper;

import static org.junit.Assert.assertEquals;

import com.espertech.esper.client.EventBean;

public class ESPTestAggregationListener implements EventListener {

  private int count = 0;
  private int newCount = 0;
  private int oldCount = 0;

  int getCount() {
    return count;
  }

  int getNewCount() {
    return newCount;
  }

  int getOldCount() {
    return oldCount;
  }

  public void update(EventBean[] newEvents, EventBean[] oldEvents) {
    EventBean event = newEvents[0];
    Integer id = (Integer) event.get("field_id");
    Double result = (Double) event.get("AggregatedResult");

    // AVG of 3 values: id and id^2 and id^3
    assertEquals("For field_id=" + id, (id + id * id + id * id * id) / 3., result, 1.e-06);

    count++;
    if (newEvents != null)
      newCount += newEvents.length;
    if (oldEvents != null)
      oldCount += oldEvents.length;
  }

}
