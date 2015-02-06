/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Dual licensed under the Apache 2.0 license and the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
import com.ebay.jetstream.event.EventSink;
import com.ebay.jetstream.event.EventSinkList;
import com.ebay.jetstream.event.JetstreamEvent;
import com.ebay.jetstream.event.support.AbstractEventSource;
import com.ebay.jetstream.event.support.channel.PipelineFlowControl;

/**
 * This test needs 'JETSTREAM_HOME' variable to be set to something like D:\path\EventProcessor
 *
 * @author snikolaev
 *
 */
@ManagedResource(objectName = "ESPTest", description = "EsperEventProcessor Unit Test")
public class ESPTest extends AbstractEventSource implements ApplicationListener, InitializingBean,
    ApplicationEventPublisherAware {

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

  private static final Logger testLogger = LoggerFactory.getLogger("com.ebay.jetstream.event.processor.esper.ESPTest");
  private static final int THREADS_NUM = 2;
  private static final int THREADS_NUM_AGGREGATION = 30;
  private static final Configuration s_springConfiguration = new RootConfiguration(new ApplicationInformation(
      "ESPTest", "0.0"), new String[] { "src/test/java/com/ebay/jetstream/event/processor/esper/ESPTest.xml" });

  static {
    org.apache.log4j.xml.DOMConfigurator.configure("src/test/java/com/ebay/jetstream/event/processor/esper/log4j.xml");
  }

  private final PipelineFlowControl m_flowHandler = new PipelineFlowControl(this);

  public ESPTest() {
  }

  public void afterPropertiesSet() throws Exception {
    // TODO Auto-generated method stub
  }

  private JetstreamEvent generateESPAggregationTestEvent(int id) {
    HashMap<String, Object> map = new HashMap<String, Object>();
    map.put("field_id", id);
    map.put("field", new Double(id));
    return new JetstreamEvent("ESPAggregationTestEvent", null, map);
  }

  private JetstreamEvent generateJetstreamEvent(int id) {
    HashMap<String, Object> map = new HashMap<String, Object>();
    String field1 = "123.0";
    ArrayList<String> field2 = new ArrayList<String>();
    EsperTestEventField field3 = new EsperTestEventField();
    field2.add("Field 2, value 1");
    field2.add("Field 2, value 2");
    map.put("id", id);
    map.put("field1", field1);
    map.put("field2", field2);
    map.put("field3", field3);
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

  // @Test
  public void testConfiguration() {
    EsperConfiguration esperConfiguration = getEsperConfiguration();
    Collection<AbstractEventType> eventTypes = esperConfiguration.getDeclaredEvents().getEventTypes();
    assertEquals("ESPTestEvent1", eventTypes.iterator().next().getEventAlias());
    assertEquals(100, esperConfiguration.getMsecResolution());
    assertEquals(true, esperConfiguration.isInternalTimerEnabled());
    assertEquals(true, esperConfiguration.isTimeSourceNano());
  }

  @Test
  public void testEpl() {
    EPL epl = getEpl("ESPTestEPL1");
    List<String> stmts = epl.getStatements();
    assertEquals(stmts.size(), 1);
    assertEquals("select id, cast(field1, double) as field1, field2, field3 from ESPTestEvent1", stmts.get(0));
    assertNotNull(epl.getEPLStatements());
    epl = getEpl("ESPTestEPL2");
    stmts = epl.getStatements();
    assertEquals(stmts.size(), 1);
    assertEquals("select id, cast(field1, double) as field1, field2, field3 from ESPTestEvent1", stmts.get(0));
    assertNotNull(epl.getEPLStatements());
  }

  // @Test
  public void testFireAndForget() throws InterruptedException {
    EsperProcessor processor = getProcessor("ESPTestProcessor");
    ESPTestSink sink = new ESPTestSink();
    processor.addEventSink(sink);

    List<JetstreamEvent> events = new ArrayList<JetstreamEvent>();
    for (int i = 0; i < THREADS_NUM; i++) {
      events.add(generateJetstreamEvent(i));

    }
    for (JetstreamEvent event : events) {
      TimeUnit.MILLISECONDS.sleep(1);
      processor.sendEvent(event);
    }
    processor.stop();
    Thread.sleep(2000);
    assertEquals(THREADS_NUM, sink.getCount());
    testLogger.info("sink first, last = [" + sink.getIds().first() + "," + sink.getIds().last() + "]");

    // verify start works after stop
    // TODO:  start not exposed - processor.start();
    for (JetstreamEvent event : events) {
      processor.sendEvent(event);
    }
    processor.stop();
    Thread.sleep(2000);
    // first run + second run == THREADS_NUM * 2
    assertEquals(THREADS_NUM * 2, sink.getCount());
  }

  // @Test
  public void testFireAndForgetOneEnginePerThread() throws InterruptedException {
    EsperProcessor processor = getProcessor("ESPTestProcessorOneEnginePerThread");
    ESPTestSink sink = new ESPTestSink();
    if ( ((EventSinkList)processor.getEventSinks()).getSinks().size() <= 0) {
      processor.addEventSink(sink);
    }
    List<JetstreamEvent> events = new ArrayList<JetstreamEvent>();
    for (int i = 0; i < THREADS_NUM; i++) {
      events.add(generateJetstreamEvent(i));
    }
    for (JetstreamEvent event : events) {
      processor.sendEvent(event);
    }
    // processor.stop();
    Thread.sleep(2000);
    assertEquals(THREADS_NUM, sink.getCount());
    testLogger.info("sink first, last = [" + sink.getIds().first() + "," + sink.getIds().last() + "]");

    // verify start works after stop
    // processor.start();
    for (JetstreamEvent event : events) {
      processor.sendEvent(event);
    }

    // processor.stop();
    Thread.sleep(2000);
    // first run + second run == THREADS_NUM * 2
    assertEquals(THREADS_NUM * 2, sink.getCount());
  }

  // @Test
  public void testOnDemandQuery() throws InterruptedException {
    EsperProcessor processor = getProcessor("ESPTestProcessorOnDemandQuery");
    ESPTestAggregationSink sink = new ESPTestAggregationSink();
    processor.addEventSink(sink);

    // processor.start(); // it was stopped while running previous test
    for (int i = 0; i < THREADS_NUM_AGGREGATION; i++) {
      processor.sendEvent(generateESPAggregationTestEvent(i));
    }
    // processor.stop();
    Thread.sleep(3000);

    assertEquals(THREADS_NUM_AGGREGATION, sink.getCount());
  }

  // @Test
  public void testProcessor() {
    EsperProcessor processor = getProcessor("ESPTestProcessor");
    ESPTestSink sink = new ESPTestSink();
    List<EventSink> sinks = new ArrayList<EventSink>();
    sinks.add(sink);
    processor.setEventSinks(sinks);
    // TODO: start not exposed - processor.start(); // it was stopped while running previous test

    ExecutorService threadPool = Executors.newCachedThreadPool(new ESPTestThreadFactory());
    Runnable runnables[] = new ESPTestRunnable[THREADS_NUM];
    try {
      for (int i = 0; i < THREADS_NUM; i++) {
        runnables[i] = new ESPTestRunnable(processor, i);
        threadPool.submit(runnables[i]);
      }
      threadPool.shutdown();
      threadPool.awaitTermination(10, TimeUnit.SECONDS);
    }
    catch (InterruptedException e) {
      fail("InterruptedException: " + e.getMessage());
    }
    assertTrue("ExecutorService failed to shut down properly", threadPool.isShutdown());

    // processor.stop();
    try {
      Thread.sleep(3000);
    }
    catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    assertEquals(THREADS_NUM, sink.getCount());
    testLogger.info("sink first, last = [" + sink.getIds().first() + "," + sink.getIds().last() + "]");
  }

  // @Test
  public void testStringUtils() {
    EsperProcessor processor = getProcessor("ESPTestProcessorStringUtils");
    ESPTestListenerForStringUtilsTest listener = new ESPTestListenerForStringUtilsTest();
    ESPTestSink sink = new ESPTestSink();
    // TODO Fix, statements not exposed, processor.getStatements().get(0).addListener(listener);
    processor.addEventSink(sink);

    JetstreamEvent event = generateJetstreamEvent(0);
    processor.sendEvent(event);
    assertEquals(1, listener.getCount());
    assertEquals(1, sink.getCount());
    List<String> topicList = listener.getTopicList();
    assertEquals(3, topicList.size());
    assertEquals("One", topicList.get(0));
    assertEquals("Two", topicList.get(1));
    assertEquals("Three", topicList.get(2));
  }

  // @Test
  public void testSynchronousSender() {
    EsperProcessor processor = getProcessor("ESPTestSyncProcessor");
    ESPTestSink sink = new ESPTestSink();
    processor.addEventSink(sink);
    JetstreamEvent event = generateJetstreamEvent(0);
    processor.sendEvent(event);
    assertEquals(1, sink.getCount());
  }
}
