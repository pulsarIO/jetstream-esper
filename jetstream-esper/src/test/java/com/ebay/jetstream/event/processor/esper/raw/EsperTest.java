/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Licensed under the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper.raw;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Ignore;
import org.junit.Test;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPOnDemandQueryResult;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.time.CurrentTimeEvent;
import com.espertech.esper.event.map.MapEventBean;

/**
 * This is native Esper test using native XML configuration file
 *
 * @author snikolaev
 *
 */
public class EsperTest {

  static class EsperTestThreadFactory implements ThreadFactory {
    public Thread newThread(Runnable r) {
      Thread t = new Thread(r);
      t.setUncaughtExceptionHandler(new EsperTestUncaughtEH());
      return t;
    }
  }

  static class EsperTestUncaughtEH implements Thread.UncaughtExceptionHandler {
    public void uncaughtException(Thread t, Throwable e) {
      fail("Exception \'" + e.getMessage() + "\' in thread " + t.getId());
    }
  }

  private static final Log log = LogFactory.getLog(EsperTest.class);
  private static final int THREADS_NUM = 10;
  private static final int THREADS_NUM_AGGRTEST = 3;
  private static Map<Integer, Double> m_aggregationResults = new HashMap<Integer, Double>();
  private static Map<Integer, Double> m_aggregationAvgResults = new HashMap<Integer, Double>();

  static {
    org.apache.log4j.xml.DOMConfigurator.configure("src/test/java/com/ebay/jetstream/event/processor/esper/raw/log4j.xml");
  }

  protected static void doSendAggrEvent(EPRuntime runtime, long workerId, Map<String, ? extends Object> map) {
    runtime.sendEvent(new CurrentTimeEvent(System.nanoTime() / 1000));
    runtime.sendEvent(map, "EsperTestAggregationEvent");

    EPOnDemandQueryResult result = runtime
        .executeQuery("SELECT workerId, SUM(V) AS RESULT, guid FROM SW GROUP BY workerId, guid");

    for (EventBean row : result.getArray()) {
      Long receivedWorkerId = (Long) row.get("workerId");
      if (receivedWorkerId != workerId)
        continue;
      Integer guid = (Integer) row.get("guid");
      Double aggregationValue = (Double) row.get("RESULT");
      System.out.println("guid=" + guid + ", RESULT=" + aggregationValue); //KEEPME
      m_aggregationResults.put(guid, aggregationValue);
      MapEventBean outMap = (MapEventBean) row;
      runtime.sendEvent(outMap.getProperties(), "OutputEvent");
    }

    EPOnDemandQueryResult resultAvg = runtime
        .executeQuery("SELECT workerId, AVG(V) AS RESULT, guid FROM SW GROUP BY workerId, guid");

    for (EventBean row : resultAvg.getArray()) {
      Long receivedWorkerId = (Long) row.get("workerId");
      if (receivedWorkerId != workerId)
        continue;
      Integer guid = (Integer) row.get("guid");
      Double aggregationValue = (Double) row.get("RESULT");
      System.out.println("guid=" + guid + ", RESULT=" + aggregationValue); //KEEPME
      m_aggregationAvgResults.put(guid, aggregationValue);
      MapEventBean outMap = (MapEventBean) row;
      runtime.sendEvent(outMap.getProperties(), "OutputEvent");
    }

    // it has to go last
    runtime.route(map, "CleanupWindowEvent");

  }

  @Test
  public void aggregationTest() {
    Configuration configuration = new Configuration();
    configuration.configure(new File("src/test/java/com/ebay/jetstream/event/processor/esper/raw/EsperTestConfig.xml"));
    EPServiceProvider epService = EPServiceProviderManager.getProvider("EsperTest", configuration);
    EsperTestAggregationStatement esperStmt = new EsperTestAggregationStatement(epService.getEPAdministrator());
    EsperTestAggregationListener listener = new EsperTestAggregationListener();
    esperStmt.addListener(listener);

    ExecutorService threadPool = Executors.newCachedThreadPool(new EsperTestThreadFactory());
    EsperTestAggregationRunnable runnables[] = new EsperTestAggregationRunnable[THREADS_NUM_AGGRTEST];
    try {
      for (int i = 0; i < THREADS_NUM_AGGRTEST; i++) {
        runnables[i] = new EsperTestAggregationRunnable(epService, i);
        threadPool.submit(runnables[i]);
      }
      threadPool.shutdown();
      threadPool.awaitTermination(200, TimeUnit.SECONDS);
    }
    catch (InterruptedException e) {
      fail("InterruptedException: " + e.getMessage());
    }
    assertTrue("ExecutorService failed to shut down properly", threadPool.isShutdown());
    assertEquals(THREADS_NUM_AGGRTEST * 2, listener.getCount());
    assertEquals(THREADS_NUM_AGGRTEST, m_aggregationResults.size()); // only one result per oroginal event
    for (int i = 0; i < THREADS_NUM_AGGRTEST; i++) {
      assertEquals(11.0 + 4. * i, m_aggregationResults.get(i), 1.e-06);
    }
    assertEquals(THREADS_NUM_AGGRTEST, m_aggregationAvgResults.size()); // only one result per oroginal event
    for (int i = 0; i < THREADS_NUM_AGGRTEST; i++) {
      assertEquals((11.0 + 4. * i) / 4., m_aggregationAvgResults.get(i), 1.e-06);
    }
  }

  @Ignore
  public void multithreadingTest() {
    Configuration configuration = new Configuration();
    configuration.configure(new File("src/test/java/com/ebay/jetstream/event/processor/esper/raw/EsperTestConfig.xml"));
    EPServiceProvider epService = EPServiceProviderManager.getProvider("EsperTest", configuration);
    EsperTestStatement esperStmt = new EsperTestStatement(epService.getEPAdministrator());

    EsperTestSubscriber subscriber = new EsperTestSubscriber();
    EsperTestListener listener = new EsperTestListener();
    esperStmt.setSubscriber(subscriber);
    esperStmt.addListener(listener);

    ExecutorService threadPool = Executors.newCachedThreadPool(new EsperTestThreadFactory());
    EsperTestRunnable runnables[] = new EsperTestRunnable[THREADS_NUM];
    try {
      for (int i = 0; i < THREADS_NUM; i++) {
        runnables[i] = new EsperTestRunnable(epService, i);
        threadPool.submit(runnables[i]);
      }
      threadPool.shutdown();
      threadPool.awaitTermination(200, TimeUnit.SECONDS);
    }
    catch (InterruptedException e) {
      fail("InterruptedException: " + e.getMessage());
    }
    assertTrue("ExecutorService failed to shut down properly", threadPool.isShutdown());

    log.info("[" + subscriber.getIds().first() + "," + subscriber.getIds().last() + "]");
    assertEquals(THREADS_NUM, subscriber.getCount());

    log.info("[" + listener.getIds().first() + "," + listener.getIds().last() + "]");
    assertEquals(THREADS_NUM, listener.getCount());
    assertEquals(THREADS_NUM, listener.getNewCount());
    assertEquals(0, listener.getOldCount());
  }
}
