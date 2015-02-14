/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Dual licensed under the Apache 2.0 license and the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper;

import java.util.Map;

import com.ebay.jetstream.event.EventException;
import com.ebay.jetstream.event.JetstreamEvent;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.event.bean.BeanEventType;
import com.espertech.esper.event.map.MapEventType;

/**
 * @author msikes
 */
public class EsperDefaultEventConverter implements EsperEventConverter {

  /*
   * Default conversion from MapEventBean to JetstreamEvent. No other event beans are supported: if any is given, null is
   * returned.
   *
   * @see
   * com.ebay.jetstream.event.processor.esper.EsperEventConverter#getJetstreamEvent(com.espertech.esper.event.EventBean)
   */
  @SuppressWarnings("unchecked")
  public JetstreamEvent getJetstreamEvent(EventBean bean) throws EventException {
    JetstreamEvent jetstreamEvent = null;
    EventType eventType = bean.getEventType();
    if (eventType instanceof MapEventType) {
      MapEventType meType = (MapEventType) eventType;
      jetstreamEvent = new JetstreamEvent(meType.getName(), null, (Map<String, Object>) bean.getUnderlying());
    }
    //
    // Consider this:
    // INSERT INTO FDL SELECT
    // com.ebay.jetstream.identity.client.IdentityClient.findDirectLinks(metricUpdate.Guid, 'user') AS FindDirectLinks
    // FROM metricUpdate;
    // /*$OUTPUT*/ SELECT FindDirectLinks.* FROM FDL;
    //
    else if (eventType instanceof BeanEventType) {
      BeanEventType beType = (BeanEventType) eventType;
      jetstreamEvent = new JetstreamEvent(beType.getName(), null, (Map<String, Object>) bean.getUnderlying());
    }
    return jetstreamEvent;
  }
}
