/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Licensed under the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper;

import java.util.List;

import com.espertech.esper.client.EventBean;

public class ESPTestListenerForStringUtilsTest implements EventListener {

  private int count = 0;
  private List<String> topicList;

  int getCount() {
    return count;
  }

  public List<String> getTopicList() {
    return topicList;
  }

  @SuppressWarnings("unchecked")
  public void update(EventBean[] newEvents, EventBean[] oldEvents) {
    count++;
    EventBean event = newEvents[0];
    topicList = (List<String>) event.get("TopicList");
  }
}
