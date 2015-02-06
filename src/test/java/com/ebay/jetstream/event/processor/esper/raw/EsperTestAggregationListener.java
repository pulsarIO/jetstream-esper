/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Dual licensed under the Apache 2.0 license and the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper.raw;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

public class EsperTestAggregationListener implements UpdateListener {

  private int count = 0;

  public EsperTestAggregationListener() {
  }

  int getCount() {
    return count;
  }

  public void update(EventBean[] newEvents, EventBean[] oldEvents) {
    EventBean event = newEvents[0];
    count++;
    System.out.println("*** event: " + event + " guid: " + event.get("guid") + ", RESULT: " + event.get("RESULT")); //KEEPME
  }

}
