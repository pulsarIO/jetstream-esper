/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Licensed under the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper.raw;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ebay.jetstream.event.processor.esper.EsperTestEventField;

public class EsperTestSubscriber {

  private static final Log log = LogFactory.getLog(EsperTestSubscriber.class);
  private static SortedSet<Integer> ids = new TreeSet<Integer>();
  private int count = 0;

  public int getCount() {
    return count;
  }

  public SortedSet<Integer> getIds() {
    return ids;
  }

  // yes, unlike listener's update this one has to be synchronized
  // TODO check this for future Esper versions
  public synchronized void update(Integer id, String field1, List<String> field2, EsperTestEventField field3) {
    count++;
    if (id == null) {
      log.error("Id is null!");
    }
    else {
      ids.add(id);
    }
  }
}
