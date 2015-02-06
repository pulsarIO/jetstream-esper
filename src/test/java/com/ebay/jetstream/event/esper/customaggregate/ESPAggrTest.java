/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Dual licensed under the Apache 2.0 license and the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.esper.customaggregate;

import static org.junit.Assert.assertEquals;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationListener;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.ebay.jetstream.config.ApplicationInformation;
import com.ebay.jetstream.config.Configuration;
import com.ebay.jetstream.config.RootConfiguration;
import com.ebay.jetstream.event.EventException;
import com.ebay.jetstream.event.EventSink;
import com.ebay.jetstream.event.JetstreamEvent;
import com.ebay.jetstream.event.processor.esper.EPL;
import com.ebay.jetstream.event.processor.esper.EsperConfiguration;
import com.ebay.jetstream.event.processor.esper.EsperProcessor;
import com.ebay.jetstream.event.support.AbstractEventSource;
import com.ebay.jetstream.event.support.channel.PipelineFlowControl;

/**
 * This test needs 'JETSTREAM_HOME' variable to be set to something like D:\path\EventProcessor
 *
 * @author snikolaev
 *
 */
@ManagedResource(objectName = "ESPTest", description = "EsperEventProcessor Unit Test")
public class ESPAggrTest extends AbstractEventSource implements ApplicationListener, InitializingBean, ApplicationEventPublisherAware {

  static class ESPTestThreadFactory implements ThreadFactory {

	  public Thread newThread(Runnable r) {
      Thread t = new Thread(r);
      t.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			public void uncaughtException(Thread t, Throwable e) {
				final Logger logger = LoggerFactory.getLogger("com.ebay.rdbd.queueworker");
				logger.error( "Exception \'" + e.getMessage() + "\' in thread " + t.getId());
			}
		});
      return t;
    }
  }

  private static final Logger testLogger = LoggerFactory.getLogger("com.ebay.jetstream.event.esper.customaggregate.ESPAggrTest");
  private static final int THREADS_NUM = 2;
  private static final int THREADS_NUM_AGGREGATION = 30;
  private static final Configuration s_springConfiguration = new RootConfiguration(new ApplicationInformation(
      "ESPTest", "0.0"), new String[] { "src/test/java/com/ebay/jetstream/event/esper/customaggregate/ESPAggrTest.xml" });

  static {
    org.apache.log4j.xml.DOMConfigurator.configure("src/test/java/com/ebay/jetstream/event/processor/esper/log4j.xml");
  }

  private final PipelineFlowControl m_flowHandler = new PipelineFlowControl(this);

  public ESPAggrTest() {
  }

  public void afterPropertiesSet() throws Exception {
    
  }


  private JetstreamEvent generateJetstreamEvent(int id) {
    HashMap<String, Object> map = new HashMap<String, Object>();
    String field1 = "123.0";
    map.put("id", id);
    map.put("field1", field1);
    return new JetstreamEvent("ESPTestEvent1", null, map);
  }

  private EPL getEpl(String name) {
    return (EPL) s_springConfiguration.getBean(name);
  }

  private EsperConfiguration getEsperConfiguration() {
    return (EsperConfiguration) s_springConfiguration.getBean("EsperConfiguration");
  }

  private EsperProcessor getProcessor(String name) {
    return (EsperProcessor) s_springConfiguration.getBean(name);
  }

  @Override
  public void onApplicationEvent(ApplicationEvent event) {
	  testLogger.warn( "ESPTest received " + event);
  }

  @Override
  public void pause() {
    // TODO Auto-generated method stub

  }

  @Override
  protected void processApplicationEvent(ApplicationEvent event) {
    // TODO Auto-generated method stub

  }

  @Override
  public void resume() {
    // TODO Auto-generated method stub

  }

  public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
    m_flowHandler.setApplicationEventPublisher(applicationEventPublisher);
    // Management.addBean(this.getClass().getSimpleName(), this);
  }


  @Test
  public void testFireAndForget() throws InterruptedException {
    EsperProcessor processor = getProcessor("ESPTestProcessor1");
   
    processor.addEventSink(new EventSink() {
        public String getBeanName() {
          // TODO Auto-generated method stub
          return null;
        }

        public void sendEvent(JetstreamEvent event) throws EventException {
          System.out.println("Receive event " + event); //KEEPME
        }
      });
   
    List<JetstreamEvent> events = new ArrayList<JetstreamEvent>();
    for (int i = 0; i < THREADS_NUM; i++) {
      events.add(generateJetstreamEvent(i));

    }
    for (JetstreamEvent event : events) {
      TimeUnit.MILLISECONDS.sleep(1);
      processor.sendEvent(event);
    }
    Thread.sleep(2000);
    processor.stop();
    
    //System.out.println(sink.getCount());
    System.out.println(processor.getEsperEventListener().getEventReceived());
    assertEquals(4, Integer.parseInt(processor.getEsperEventListener().getEventReceived().toString()));
  }


}
