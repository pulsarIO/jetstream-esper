/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Dual licensed under the Apache 2.0 license and the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ebay.jetstream.event.JetstreamEvent;
import com.ebay.jetstream.event.JetstreamReservedKeys;
import com.ebay.jetstream.xmlser.XSerializable;
import com.espertech.esper.client.EPOnDemandQueryResult;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EventBean;

/**
 * On Demand query handler. See Esper docs for more details
 *
 * @author snikolaev
 *
 */
public class OnDemandQuery implements XSerializable {

  private static final String LOGGING_COMPONENT_NAME = "EsperEventListener";
  private static final Logger logger = LoggerFactory.getLogger("com.ebay.jetstream.event.processor.esper.OnDemandQuery");
  private static final EsperEventConverter DEFAULT_CONVERTER = new EsperWrappedEventConverter();

  public static EsperEventConverter getEventConverter() {
    return DEFAULT_CONVERTER;
  }

  private String m_query;
  private String m_cleanUpEvent;
  private String m_outputEvent;
  private String m_workerIdentifier;

  public String getCleanUpEvent() {
    return m_cleanUpEvent;
  }

  public String getOutputEvent() {
    return m_outputEvent;
  }

  public String getQuery() {
    return m_query;
  }

  public String getWorkerIdentifier() {
    return m_workerIdentifier;
  }

  /**
   * Query processor. It does 3 things: 1) It executes the query set by 'query' property 2) It takes a result and sends
   * it as a NEW event set by 'outputEvent' property 3) It routes a NEW cleanup event set by 'cleanUpEvent' property
   *
   * @param runtime
   * @param workerId
   * @param originalEvent
   */
  public void process(EPRuntime runtime, int workerId, JetstreamEvent originalEvent) {

  
    try {
      Object eventId = originalEvent.get(JetstreamReservedKeys.WorkerId.toString());
      if (eventId == null || !(eventId instanceof Integer)) {
        logger.error( "EPL error: OnDemandQuery processor failed to retrieve "
            + JetstreamReservedKeys.WorkerId.toString() + " field from original event " + originalEvent
            + " because it's either null or not of Integer type", LOGGING_COMPONENT_NAME);
      }
      else {
        final Integer eventWorkerId = (Integer) eventId;

        EPOnDemandQueryResult result = runtime.executeQuery(getQuery());
        for (EventBean row : result.getArray()) {
          Object id = row.get(getWorkerIdentifier());
          if (id == null || !(id instanceof Integer)) {
            logger.warn( "EPL error: OnDemandQuery processor failed to convert " + id + " into "
                + getWorkerIdentifier() + " for EventBean " + row, LOGGING_COMPONENT_NAME);
          }
          Integer receivedWorkerId = (Integer) id;
          if (receivedWorkerId != null) {
        	  if(receivedWorkerId != workerId) { 

        		  continue;
        	  }
          }
          JetstreamEvent jetstreamEvent = getEventConverter().getJetstreamEvent(row);
          if (jetstreamEvent == null) {
            logger.warn( "EPL error: OnDemandQuery processor ignored event of type "
                + row.getEventType() + ": " + row, LOGGING_COMPONENT_NAME);
          }
          else {
            if (logger.isDebugEnabled()) {
              logger.debug( "Sending " + getOutputEvent() + ": " + jetstreamEvent,
                  LOGGING_COMPONENT_NAME);
            }
            runtime.sendEvent(jetstreamEvent, getOutputEvent());
          }
        }
        Map<String, Object> cleanupEvent = new HashMap<String, Object>();
        cleanupEvent.put(getWorkerIdentifier(), eventWorkerId);
        if (logger.isDebugEnabled()) {
          logger.debug( "Routing " + getCleanUpEvent() + ": " + cleanupEvent, LOGGING_COMPONENT_NAME);
        }
        runtime.sendEvent(cleanupEvent, getCleanUpEvent());
      }
    }
    catch (Throwable e) {
    }
    finally {
    
    }
  }

  public void setCleanUpEvent(String cleanUpEvent) {
    m_cleanUpEvent = cleanUpEvent;
  }

  public void setOutputEvent(String outputEvent) {
    m_outputEvent = outputEvent;
  }

  public void setQuery(String query) {
    m_query = query;
  }

  public void setWorkerIdentifier(String workerIdentifier) {
    m_workerIdentifier = workerIdentifier;
  }

  public void validate() {
    String query = getQuery();
    if (query != null) {
      query = query.trim();
    }
    if (query == null || query.length() <= 0) {
      throw new IllegalArgumentException("No 'query' property for OnDemandQuery bean provided");
    }
    setQuery(query);

    String cleanUpEvent = getCleanUpEvent();
    if (cleanUpEvent != null) {
      cleanUpEvent = cleanUpEvent.trim();
    }
    if (cleanUpEvent == null || cleanUpEvent.length() <= 0) {
      throw new IllegalArgumentException("No 'cleanUpEvent' property for OnDemandQuery bean provided");
    }
    setCleanUpEvent(cleanUpEvent);

    String outputEvent = getOutputEvent();
    if (outputEvent != null) {
      outputEvent = outputEvent.trim();
    }
    if (outputEvent == null || outputEvent.length() <= 0) {
      throw new IllegalArgumentException("No 'outputEvent' property for OnDemandQuery bean provided");
    }
    setOutputEvent(outputEvent);

    String workerIdentifier = getWorkerIdentifier();
    if (workerIdentifier != null) {
      workerIdentifier = workerIdentifier.trim();
    }
    if (workerIdentifier == null || workerIdentifier.length() <= 0) {
      throw new IllegalArgumentException("No 'workerIdentifier' property for OnDemandQuery bean provided");
    }
    setWorkerIdentifier(workerIdentifier);
  }

}
