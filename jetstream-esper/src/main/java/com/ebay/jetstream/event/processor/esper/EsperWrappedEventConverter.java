/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Dual licensed under the Apache 2.0 license and the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper;

import com.ebay.jetstream.event.EventException;
import com.ebay.jetstream.event.JetstreamEvent;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.event.WrapperEventBean;
import com.espertech.esper.event.WrapperEventType;

/**
 * This converter serves cases like
 *
 * INSERT INTO metricUpdate SELECT Guid, EntityType, ate.* FROM metricsDb, applyTransaction ate GROUP BY Guid;
 *
 * SELECT * FROM metricUpdate;
 *
 * @author snikolaev
 *
 */
public class EsperWrappedEventConverter extends EsperDefaultEventConverter {

  @SuppressWarnings("unchecked")
  @Override
  public JetstreamEvent getJetstreamEvent(EventBean bean) throws EventException {
    JetstreamEvent event = super.getJetstreamEvent(bean);
    if (event == null && bean instanceof WrapperEventBean) { // trying to unwrap it
      WrapperEventBean wrappedBean = (WrapperEventBean) bean;
      WrapperEventType eventType = (WrapperEventType) wrappedBean.getEventType();
      JetstreamEvent nestedEvent = super.getJetstreamEvent(wrappedBean.getUnderlyingEvent());
      if (nestedEvent != null) {
        nestedEvent.putAll(wrappedBean.getUnderlyingMap()); // outer map has priority, i.e. it overrides similar fields
        event = new JetstreamEvent(eventType.getName(), null, nestedEvent); // and we keep original event type
      }
      else {
        event = new JetstreamEvent(eventType.getName(), null, wrappedBean.getUnderlyingMap());
      }
    }
    return event;
  }

}
