/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Dual licensed under the Apache 2.0 license and the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper;

import java.util.Map;

import org.junit.Ignore;

import com.ebay.jetstream.config.ApplicationInformation;
import com.ebay.jetstream.config.Configuration;
import com.ebay.jetstream.config.RootConfiguration;

/**
 * @author msikes
 *
 */
public class EsperStreamTest {
  private static final Configuration s_springConfiguration = new RootConfiguration(new ApplicationInformation(
      "EsperStreamTest", "1.0"), new String[] { Configuration.getClasspathContext(EsperStreamTest.class, null) });

  public static Map<String, Object> tagEvent(Map<String, Object> event, String name, Object value) {
    event.put(name, value);
    return event;
  }

  @Ignore
  public void test() throws Exception {
    s_springConfiguration.start();
    Thread.sleep(100000);
  }
}
