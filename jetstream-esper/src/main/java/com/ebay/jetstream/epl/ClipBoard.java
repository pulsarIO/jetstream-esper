/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Licensed under the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.epl;

import java.util.HashMap;
import java.util.Map;

/**
 * @author shmurthy
 * 
 *         This class is intended for use in EPL as a temporary storage for building maps. The post() and get() methods
 *         are intended for use with in EPL. The erase method must be called by the component sending an event to Esper.
 *         it must be called immediately after the sendEvent() method of Esper returns. This class manages one or more
 *         named clip boards. Each named ClipBoard is a hash map that contains key value pairs. The Named ClipBoards are
 *         stored in a HashMap and the HashMap is erased upon every event is processed.
 * 
 */

public class ClipBoard {

  private static ThreadLocal<Map<Object, Object>> m_clipBoardMap = new ThreadLocal<Map<Object, Object>>();

  /**
   * erase() - is called to make sure that all the named clip boards are removed subsequent to the event being
   * processed. This way ownership of the named clip boards get passed to a downstream listener. This method is intended
   * to be called after the sendEvent() method for Esper runtime returns for every event submitted to the Esper runtime.
   */

  public static void erase() {

    HashMap<Object, Object> walls = (HashMap<Object, Object>) m_clipBoardMap.get();

    if (walls != null) {
      walls.clear();
    }
  }

  /**
   * get() method is used to get a key value pair to a named ClipBoard. The workspace key is a dummy argument and must
   * be a field in the incoming event to Esper. Without this Esper will not execute the post method for more than one
   * event.
   * 
   * @param eventKey
   * @param clipBoardName
   * @return a HashMap containing the key value pairs posted to the specified clip board
   */

  @SuppressWarnings("unchecked")
  public static Map<Object, Object> get(Object wallid, String clipBoardName) {

    String wall = null;
    if (wallid instanceof Long) {
      wall = wallid.toString();
    }
    else if (wallid instanceof String)
      wall = wallid.toString();

    HashMap<Object, Object> walls = (HashMap<Object, Object>) m_clipBoardMap.get();

    if (walls == null) {
      walls = new HashMap<Object, Object>();
      m_clipBoardMap.set(walls);
    }

    HashMap<Object, Object> wallClipBoards = (HashMap<Object, Object>) walls.get(wall);

    if (wallClipBoards == null) {
      wallClipBoards = new HashMap<Object, Object>();
      walls.put(wall, wallClipBoards);
    }

    HashMap<Object, Object> namedClipBoard = (HashMap<Object, Object>) wallClipBoards.get(clipBoardName);

    if (namedClipBoard == null) {
      namedClipBoard = new HashMap<Object, Object>();
      wallClipBoards.put(clipBoardName, namedClipBoard);
    }

    return namedClipBoard;

  }

  /**
   * get() method is used to get a entry of a named ClipBoard entries by providing a keyName. The workspace key is a
   * dummy argument and must be a field in the incoming event to Esper. Without this Esper will not execute the post
   * method for more than one event.
   * 
   * @param eventKey
   * @param clipBoardName
   * @param keyname
   * @return a HashMap containing the key value pairs posted to the specified clip board
   */

  @SuppressWarnings("unchecked")
  public static Map<Object, Object> get(Object wallid, String clipBoardName, String keyName) {
	  return (Map<Object, Object>) get(wallid, clipBoardName).get(keyName);
  }

  public static Object getAndRemovekey(Object wallid, String clipBoardName, String keyName) {
    HashMap<Object, Object> namedClipBoard = (HashMap<Object, Object>) get(wallid, clipBoardName);
    Object value = namedClipBoard.get(keyName);
    namedClipBoard.remove(keyName);
    return value;

  }

  /**
   * Post() method is used to post a key value pair to a named ClipBoard. The workspace key is a dummy argument and must
   * be a field in the incoming event to Esper. Without this Esper will not execute the post method for more than one
   * event.
   * 
   * @param eventKey
   * @param clipBoardName
   * @param key
   * @param value
   * @return
   */

  @SuppressWarnings("unchecked")
  public static Map<Object, Object> post(Object wallid, String clipBoardName, String key, Object value) {

    // concept is we will have one or more walls containing one or more clipboards
    // first we will get the outer most HashMap which will hold one or more walls

    HashMap<Object, Object> walls = (HashMap<Object, Object>) m_clipBoardMap.get();

    String wall = null;
    if (wallid instanceof Long) {
      wall = wallid.toString();
    }
    else if (wallid instanceof String)
      wall = wallid.toString();

    if (walls == null) {
      walls = new HashMap<Object, Object>();
      m_clipBoardMap.set(walls);
    }

    HashMap<Object, Object> wallClipBoards = (HashMap<Object, Object>) walls.get(wall);

    if (wallClipBoards == null) {
      wallClipBoards = new HashMap<Object, Object>();
      walls.put(wall, wallClipBoards);
    }

    // Now get the specified clipboard from the specified wall
    HashMap<Object, Object> namedClipBoard = (HashMap<Object, Object>) wallClipBoards.get(clipBoardName);

    if (namedClipBoard == null) {
      namedClipBoard = new HashMap<Object, Object>();
      wallClipBoards.put(clipBoardName, namedClipBoard);
    }

    namedClipBoard.put(key, value);
    return namedClipBoard;
  }
}
