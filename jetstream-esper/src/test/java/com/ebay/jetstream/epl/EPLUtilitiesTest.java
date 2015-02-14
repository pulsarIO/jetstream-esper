/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Dual licensed under the Apache 2.0 license and the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.epl;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * @author rmuthupandian
 *
 */
public class EPLUtilitiesTest {

  public void clearSets(Map<String, Object> map1, Map<String, Object> map2) {
    map1.clear();
    map2.clear();
  }

  @Test
  public void testFindIntersection() {
    Map<String, Object> map1 = new HashMap<String, Object>();
    Map<String, Object> map2 = new HashMap<String, Object>();

    // Double test
    map1.put("score1", 0.0888676767676767676767676767);
    map2.put("score1", 0.0888676767676767676767676767);
    
    System.out.println(EPLUtilities.findIntersection(map1, map2));

    assertEquals(EPLUtilities.findIntersection(map1, map2).size(), 0); // No Change

    map1.put("score1", 0.0);
    map2.put("score1", 0.2);
    assertEquals(EPLUtilities.findIntersection(map1, map2).size(), 1); // Value Change

    clearSets(map1, map2);
    map1.put("score1", 0.0);
    map2.put("score1", 0.1);
    map2.put("score2", 0.2);
    assertEquals(EPLUtilities.findIntersection(map1, map2).size(), 2); // Value Change + Addional Key

    // String Test
    clearSets(map1, map2);
    map1.put("score1", "234");
    map2.put("score1", "234");
    assertEquals(EPLUtilities.findIntersection(map1, map2).size(), 0); // No change

    clearSets(map1, map2);
    map1.put("score1", "234");
    map2.put("score1", "234.4");
    assertEquals(EPLUtilities.findIntersection(map1, map2).size(), 1); // Value Change

    clearSets(map1, map2);
    map1.put("score1", "234");
    map2.put("score1", "234.4");
    map2.put("score2", "234.4");
    assertEquals(EPLUtilities.findIntersection(map1, map2).size(), 2); // Value Change + Additional Key

    // Long Test
    clearSets(map1, map2);
    map1.put("score1", 234L);
    map2.put("score1", 234L);
    assertEquals(EPLUtilities.findIntersection(map1, map2).size(), 0); // No change

    clearSets(map1, map2);
    map1.put("score1", 234L);
    map2.put("score1", 235L);
    assertEquals(EPLUtilities.findIntersection(map1, map2).size(), 1); // Value Change

    clearSets(map1, map2);
    map1.put("score1", 234L);
    map2.put("score1", 235L);
    map2.put("score2", 4444L);
    assertEquals(EPLUtilities.findIntersection(map1, map2).size(), 2); // Value Change + Additional Key

    // Float Test

    clearSets(map1, map2);
    map1.put("score1", 234.5565f);
    map2.put("score1", 234.5565f);
    assertEquals(EPLUtilities.findIntersection(map1, map2).size(), 0); // No change

    clearSets(map1, map2);
    map1.put("score1", 234.5f);
    map2.put("score1", 235f);
    assertEquals(EPLUtilities.findIntersection(map1, map2).size(), 1); // Value Change

    clearSets(map1, map2);
    map1.put("score1", 234.5f);
    map2.put("score1", 235f);
    map2.put("score2", 666f);
    assertEquals(EPLUtilities.findIntersection(map1, map2).size(), 2); // Value Change + Additional Key

  }
}
