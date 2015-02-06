/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Dual licensed under the Apache 2.0 license and the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper;

import static org.junit.Assert.assertEquals;

import com.ebay.jetstream.event.EventException;
import com.ebay.jetstream.event.EventSink;
import com.ebay.jetstream.event.JetstreamEvent;

public class ESPTestAggregationSink implements EventSink {

  private int count = 0;

  public String getBeanName() {
    // TODO Auto-generated method stub
    return null;
  }

  int getCount() {
    return count;
  }

  public void sendEvent(JetstreamEvent event) throws EventException {

    Integer id = (Integer) event.get("field_id");
    Double result = (Double) event.get("AggregatedResult");

    // AVG of 3 values: id and id^2 and id^3
    assertEquals("For field_id=" + id, (id + id * id + id * id * id) / 3., result, 1.e-06);

    count++;

  }

}
