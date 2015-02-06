/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Dual licensed under the Apache 2.0 license and the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper;

import com.ebay.jetstream.event.EventException;
import com.ebay.jetstream.event.JetstreamEvent;
import com.espertech.esper.client.EventBean;

/**
 * This interface allows pluggable Esper EventBean to JetstreamEvent conversion. Esper always sends events as EventBeans,
 * and supports several types of beans. Jetstream expects JetstreamEvents always, which is an extension of Map<String,
 * Object>.
 *
 * @author msikes
 */
public interface EsperEventConverter {

  /**
   * Convert from an EventBean to a JetstreamEvent. If the event bean isn't supported, null may be returned for the event
   * to be ignored: sub-classes may then attempt their own conversions.
   *
   * @param bean
   *          the event from Esper to be converted.
   * @return the event, converted to a JetstreamEvent.
   * @throws EventException
   *           if the event cannot be converted but the event should not be ignored. An exception is logged.
   */
  JetstreamEvent getJetstreamEvent(EventBean bean) throws EventException;
}
