/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Licensed under the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.epl;

import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.SecureRandom;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.StringTokenizer;

import org.apache.commons.jxpath.JXPathContext;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ebay.jetstream.application.JetstreamApplication;
import com.ebay.jetstream.util.CommonUtils;
import com.ebay.jetstream.util.DateUtil;

public class EPLUtilities {

  private static final Logger logger = LoggerFactory.getLogger("com.ebay.jetstream.epl.EPLUtilities");
  private static final String LOGGING_COMPONENT_NAME = "EPLUtilities";
  private static final String EPL_STREAM_COLUMN1 = "COLUMN1";
  private static final String EPL_STREAM_COLUMN2 = "COLUMN2";
  private static final String EPL_STREAM_COLUMN3 = "COLUMN3";
  private static final String EPL_STREAM_NAME = "name";
  private static final String EPL_STREAM_VALUE = "value";
  private static final String SLICE_SEPARATOR = "|";

  private static boolean attributesAliasesBeanReported = false;
  private static Random m_random = new SecureRandom();

  /**
   * Array to List converter
   * 
   * @param T
   *          []
   * @return List<T>
   */
  public static <T> List<T> arrayToList(T[] array) {
    List<T> list = new ArrayList<T>(); // we return at least empty list for EPL
    if (array != null) {
      for (T element : array) {
        list.add(element); // we don't filter null out
      }
    }
    return list;
  }

  /**
   * Sometimes ELP's version of cast is misbehaving
   * 
   * @param object
   * @return
   */
  public static Double castToDouble(Object object) {
    if (object != null) {
      if (object instanceof Double)
        return (Double) object;
      else
        return Double.valueOf(object.toString());
    }
    return null;
  }

  /**
   * EPL has embedded cast method, but it doesn't cover arrays
   * 
   * @param array
   * @return
   */
  public static Double[] castToDoubleArray(Object[] array) {
    if (array != null) {
      Double[] retArray = new Double[array.length];
      int i = 0;
      for (Object element : array) {
        retArray[i] = element == null ? null : (Double) element;
        i++;
      }
      return retArray;
    }
    return null;
  }

  /**
   * Sometimes ELP's version of cast is misbehaving
   * 
   * @param object
   * @return
   */
  public static Integer castToInteger(Object object) {
    if (object != null) {
      if (object instanceof Integer)
        return (Integer) object;
      else
        return Integer.valueOf(object.toString());
    }
    return null;
  }

  /**
   * EPL has embedded cast method, but it doesn't cover arrays
   * 
   * @param array
   * @return
   */
  public static Integer[] castToIntegerArray(Object[] array) {
    if (array != null) {
      Integer[] retArray = new Integer[array.length];
      int i = 0;
      for (Object element : array) {
        retArray[i] = element == null ? null : (Integer) element;
        i++;
      }
      return retArray;
    }
    return null;
  }

  /**
   * Sometimes ELP's version of cast is misbehaving
   * 
   * @param object
   * @return
   */
  public static Long castToLong(Object object) {
    if (object != null) {
      if (object instanceof Long)
        return (Long) object;
      else
        return Long.valueOf(object.toString());
    }
    return null;
  }

  /**
   * EPL has embedded cast method, but it doesn't cover arrays
   * 
   * @param array
   * @return
   */
  public static Long[] castToLongArray(Object[] array) {
    if (array != null) {
      Long[] retArray = new Long[array.length];
      int i = 0;
      for (Object element : array) {
        retArray[i] = element == null ? null : (Long) element;
        i++;
      }
      return retArray;
    }
    return null;
  }

  /**
   * Sometimes ELP's version of cast is misbehaving
   * 
   * @param object
   * @return
   */
  public static String castToString(Object object) {
    return object == null ? null : object.toString();
  }

  /**
   * EPL has embedded cast method, but it doesn't cover arrays
   * 
   * @param array
   * @return
   */
  public static String[] castToStringArray(Object[] array) {
    if (array != null) {
      String[] retArray = new String[array.length];
      int i = 0;
      for (Object element : array) {
        retArray[i] = element == null ? null : element.toString();
        i++;
      }
      return retArray;
    }
    return null;
  }

  /**
   * findIntersection - Method Objects any object set1,set2 and if its a instance of Map, then findIntersectionMap will
   * called. * @param set1
   * 
   * @param set2
   * @return
   */
  @SuppressWarnings("unchecked")
  public static Map<String, Object> findIntersection(Object set1, Object set2) {
    if (set1 instanceof Map && set2 instanceof Map) {
      return findIntersectionMap((Map<String, Object>) set1, (Map<String, Object>) set2);
    }
    return null;
  }

  /**
   * findIntersectionMap - This method finds the intersection of set1 and set2 and upon finding the intersection further
   * filters the set elements by comparing the values. If the values of intersection match, that element is removed from
   * the resulting set. For Example, if set1 contains {a=1,b=2,c=3} set2 contains {a=3,b=2} resulting set would contain
   * {a=3}
   * 
   * 
   * @param set1
   * @param set2
   * @return
   */
  public static Map<String, Object> findIntersectionMap(Map<String, Object> set1, Map<String, Object> set2) {
    if (set1 != null && set2 != null) {
      for (Entry<String, Object> entry : set1.entrySet()) {
        String key = entry.getKey();
        Object value = entry.getValue();
        if (set2.get(key) != null && value != null) {
          if (value instanceof Double) {
            if (((Double) value).doubleValue() == ((Double) set2.get(key)).doubleValue()) {
              set2.remove(entry.getKey());
            }
          }
          else if (value instanceof Long) {
            if (((Long) value).longValue() == ((Long) set2.get(entry.getKey())).longValue()) {
              set2.remove(entry.getKey());
            }
          }
          else if (value instanceof Integer) {
            if (((Integer) value).intValue() == ((Long) set2.get(entry.getKey())).intValue()) {
              set2.remove(entry.getKey());
            }
          }
          else if (value instanceof Float) {
            if (((Float) value).floatValue() == ((Float) set2.get(entry.getKey())).floatValue()) {
              set2.remove(entry.getKey());
            }
          }
          else if (value instanceof String) {
            if (Double.parseDouble((String) value) == Double.parseDouble((String) set2.get(entry.getKey()))) {
              set2.remove(entry.getKey());
            }
          }
        }
      }
      return set2;
    }
    return null;
  }

  /**
   * generateRandomDouble - This method returns Random Double.
   * 
   * Note: This method is written for testing purpose only.
   * 
   * @return
   */
  public static Double generateRandomDouble(Object key) {
    return m_random.nextDouble();
  }

  /**
   * Since EPL doesn't support .get(i) syntax, we need another way to retrieve array elements (for example, zeroth one)
   * 
   * @param T
   *          [] array
   * @param int at
   * @return T
   */
  public static <T> T getArrayElement(T[] array, int at) {
    return array == null || array.length <= 0 ? null : array[at];
  }

  public static Object getAttribute(Object object, String key) {
    try {
      Object event = object;
      if (object instanceof String) {
        ObjectMapper mapper = new ObjectMapper();
        event = mapper.readValue(object.toString(), HashMap.class);
      }
      if (event != null) {
        JXPathContext context = JXPathContext.newContext(event);
        context.setLenient(true);
        return context.getValue(key);
      }
    }
    catch (Exception e) { // NOPMD
      return null;
    }
    return null;
  }

  /**
   * @deprecated getAttributeValue() alone is used by EPL, with a cast() operation. This allows many more types than
   *             double to be supported, and is consistent across them all.
   */
  @Deprecated
  public static Double getAttributeDoubleValue(List<Map<String, String>> attributes, String name) {
    Object objectValue = getAttributeValue(attributes, name);
    if (objectValue != null) {
      return java.lang.Double.valueOf(objectValue.toString());
    }
    return null;
  }

  /**
   * Gets attribute value by given name scanning given list of attributes. This list contains one or more entries like:
   * 
   * <map> <entry key="action"> <value>replace</value> </entry> <entry key="value"> <list> <value>null</value> </list>
   * </entry> <entry key="name"> <value>marketplace.currency</value> </entry> </map>
   * 
   * @param attributes
   * @param name
   * @return
   */
 
  public static Object getAttributeValue(List<Map<String, String>> attributes, String name) {
    if (attributes != null && name != null) {
      for (Map<String, String> attribute : attributes) {
        if (attribute != null && isNameOrAliasEqual(attribute, name)) {
          Object value = attribute.get("value");
          if (value == null) {
            value = attribute.get("v");
          }
          if (value != null && value instanceof List) {
            value = ((List) value).get(0);
          }
          if (value != null && value instanceof String) {
            return ((String) value).equals("null") ? null : value;
          }
          break;
        }
      }
    }
    return null;
  }

  /**
   * Gets attribute value by given name scanning given list of attributes. This list contains one or more entries like:
   * 
   * <map> <entry key="action"> <value>replace</value> </entry> <entry key="value"> <list> <value>null</value> </list>
   * </entry> <entry key="name"> <value>marketplace.currency</value> </entry> </map>
   * 
   * @param attributes
   * @param name
   * @return
   */
  @SuppressWarnings("unchecked")
  public static Object getAttributeValue(Map<String, Object> attrs, String name) {
    Map<String, Object> objMap = (Map<String, Object>) attrs.get("object");
    Map<String, Object> attrsmap = (Map<String, Object>) objMap.get("attributes");
    List<Map<String, String>> attributes = (List<Map<String, String>>) attrsmap.get("values");

    if (attributes != null && name != null) {
      for (Map<String, String> attribute : attributes) {
        if (attribute != null && isNameOrAliasEqual(attribute, name)) {
          Object value = attribute.get("value");
          if (value == null) {
            value = attribute.get("v");
          }
          if (value != null && value instanceof List) {
            value = ((List) value).get(0);
          }
          if (value != null && value instanceof String) {
            return ((String) value).equals("null") ? null : value;
          }
          break;
        }
      }

    }
    return null;
  }

  /**
   * Gets list of attribute values by given name scanning given list of attributes. This list contains one or more
   * entries like:
   * 
   * <map> <entry key="action"> <value>replace</value> </entry> <entry key="value"> <list> <value>null</value> </list>
   * </entry> <entry key="name"> <value>marketplace.currency</value> </entry> </map>
   * 
   * @param attributes
   * @param name
   * @return
   */

  public static List getAttributeValues(List<Map<String, String>> attributes, String name) {
    if (attributes != null && name != null) {
      for (Map<String, String> attribute : attributes) {
        if (attribute != null && isNameOrAliasEqual(attribute, name)) {
          Object value = attribute.get("value");
          if (value == null) {
            value = attribute.get("v");
          }
          if (value != null && value instanceof List) {
            return (List) value;
          }
          break;
        }
      }
    }
    return null;
  }

  /**
   * Expects and distinguishes map, array, list and plain bean. Also recognizes Jetstream attributes.
   * 
   * @param bean
   * @param name
   * @return
   * @throws SecurityException
   * @throws NoSuchMethodException
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   */
  @SuppressWarnings("unchecked")
  private static Object getBeanEntry(Object bean, String name) throws SecurityException, NoSuchMethodException,
      IllegalArgumentException, IllegalAccessException, InvocationTargetException {
    if (bean == null || name == null || name.length() < 1) {
      return null;
    }
    if (bean instanceof Map) {
      return ((Map) bean).get(name);
    }
    Class cls = bean.getClass();
    try {
      Integer index = Integer.valueOf(name);
      if (cls.isArray()) {
        return ((Object[]) bean)[index];
      }
      Method getter = cls.getMethod("get", int.class); // trying List.get
      return getter.invoke(bean, index);
    }
    catch (NumberFormatException ex) {
      // not integer, trying to get a bean property...
    }
    try {
      // some wsdl types don't follow bean specs and use direct naming
      Method getter = cls.getMethod(name);
      return getter.invoke(bean, (Object[]) null);
    }
    catch (NoSuchMethodException ex) {
    }

    // attributes retriever
    if (bean instanceof List) {
      List list = (List) bean;
      for (Object entry : list) {
        Object value = getBeanEntry(entry, name);
        if (value != null) {
          return value;
        }
      }
    }
    try {
      // trying a particular attribute having this name
      Object ret = cls.getMethod("getName").invoke(bean, (Object[]) null);
      if (ret instanceof String) {
        if (name.equals(ret)) {
          return cls.getMethod("getValue").invoke(bean, (Object[]) null);
        }
        if (logger.isDebugEnabled()) {
          logger.debug( "getBeanEntry skipped attribute '" + ret + "' for bean '" + bean
              + "' of class '" + cls.getName() + "'", LOGGING_COMPONENT_NAME);
        }
      }
    }
    catch (Throwable ex) {
      if (logger.isDebugEnabled()) {
        logger.debug( "getBeanEntry failed to retrieve potential attribute '" + name + "' for bean '"
            + bean + "' of class '" + cls.getName() + "'. Exception: " + ex, LOGGING_COMPONENT_NAME);
      }
    }

    // last chance: trying getter
    StringBuffer buf = new StringBuffer();
    buf.append("get");
    buf.append(name.substring(0, 1).toUpperCase());
    buf.append(name.substring(1));
    String methodName = buf.toString();
    try {
      return cls.getMethod(methodName).invoke(bean, (Object[]) null);
    }
    catch (Throwable ex) {
      if (logger.isDebugEnabled()) {
        logger.debug( "getBeanEntry failed to call method '" + methodName + "' for bean '" + bean
            + "' of class '" + cls.getName() + "'. Exception: " + ex, LOGGING_COMPONENT_NAME);
      }
    }
    return null;
  }

  /**
   * Returns size of the incoming list
   * 
   * @param list
   * @return
   */
  public static int getCollectionSize(Collection<?> collection) {
    if (collection == null)
      return 0;
    else
      return collection.size();
  }

 

  /**
   * @param key
   *          - should be a field from the incoming event. This parameter is required only because Esper calls a method
   *          taking no arguments or constant values for all arguments only once in the entire run. To make Esper call
   *          the method for every event, the argument passed in to the method must be a field in the event. Esper 3.0
   *          has a config option to turn off this behaviour completely. The only option available in Esper 2.3 is to
   *          pass an argument.
   * @return
   */
  public static String getCurrentDateInISO8601Format(Object key) {

    StringBuffer buf = new StringBuffer();
    DateTimeFormatter fmt = ISODateTimeFormat.basicDateTimeNoMillis();

    fmt.printTo(buf, System.currentTimeMillis());

    return buf.toString();
  }

  /**
   * Parses a specific ISO 8601 extended-format date-time string, format == "yyyy-MM-dd'T'HH:mm:ss" and returns the
   * resulting Date object.
   * 
   * FIX: In order to make EPL nested calls like:
   * com.ebay.jetstream.util.DateUtil.getMillisFromISO8601(com.ebay.jetstream.epl
   * .EPLUtilities.getAttributeValue(attributes.values, 'marketplace.transaction_date')) possible, we need this guy to
   * be able to accept Object which is a string actually
   * 
   * FIX for 5827 - we have support both 'T'-separated and ' '-separated formats
   * 
   * @param iso8601
   *          an extended format ISO 8601 date-time string.
   * 
   * @return the Date object for the ISO 8601 date-time string.
   * 
   * @throws ParseException
   *           if the date could not be parsed.
   */
  public static Date getDateFromISO8601(Object iso8601) throws ParseException {
    return DateUtil.getDateFromISO8601(iso8601);
  }

  public static Date getDateFromRFC822String(Object rfc822) throws ParseException {
    return DateUtil.getDateFromRFC822String(rfc822);
  }

  /**
   * Since EPL doesn't support .get(i) syntax, we need another way to retrieve list elements (for example, zeroth one)
   * 
   * @param List
   *          <T> list
   * @param int at
   * @return T
   */
  public static <T> T getListElement(List<T> list, int at) {
    return CommonUtils.isEmpty(list) ? null : list.get(at);
  }

  public static Long getMillisFromISO8601(Object iso8601) throws ParseException {
    return DateUtil.getMillisFromISO8601(iso8601);
  }

  public static Long getMillisFromRFC822String(Object rfc822) throws ParseException {
    return DateUtil.getMillisFromRFC822String(rfc822);
  }

  @SuppressWarnings("unchecked")
  static public String getNestedKeyValue(Map<String, Object> event, String key) {
    String result = getValue(event, key);
    if (result != null) {
      return result;
    }
    else {
      String[] keys = key.split("\\.");
      String keyName = "";
      for (int i = 0; i < keys.length; i++) {
        if (event.containsKey(keys[i])) {
          Map<String, Object> newEvent = (Map<String, Object>) event.get(keys[i]);
          for (int j = i + 1; j < keys.length; j++) {
            if (keyName.length() > 0) {
              keyName = keyName + "." + keys[j];
            }
            else {
              keyName = keys[j];
            }
          }
          result = getNestedKeyValue(newEvent, keyName);
          if (result != null) {
            return result;
          }
          newEvent.size();
        }
      }
    }

    return null;
  }

  /**
   * Current time in milliseconds
   * 
   * @return Long
   */
  public static Long getTimeInMillis() {
    return DateUtil.getTimeInMillis();
  }

  public static Long getTimeInNanos() {
    return System.nanoTime();
  }

  /**
   * 
   * @param event
   * @param key
   * @return
   */
  public static String getValue(Map<String, Object> event, String key) {
    if (event != null && key != null) {
      return (String) event.get(key);
    }
    return null;
  }

  
  public static boolean isContains(Object object, String key) {
    if (object instanceof List)
      return ((List) object).contains(key);

    return false;
  }

  /**
   * This method was added for Touchstone project where we tried to reduce attributes names for better network
   * performance. Here we check "old" or original name first and if it's not equal we give a chance to each alias. So,
   * "paypal.transaction_type" name has "TxnType" alias, thus we check attribute name against original name first then
   * we try the alias. If both checks fail we return false. Please note that we can have more than one alias (i.e. list
   * of them) - that's added for future extensions.
   * 
   * @param attribute
   * @param name
   * @return true/false
   */
  public static boolean isNameOrAliasEqual(Map<String, String> attribute, String name) {
    String attributeName = attribute.get("name");
    if (attributeName == null) {
      attributeName = attribute.get("n");
    }
    if (name.equals(attributeName)) {
      return true; // direct hit, no need to check aliases
    }
    AttributesAliasesConfiguration bean = null;
    final String beanId = AttributesAliasesConfiguration.getId();
    if (beanId != null && JetstreamApplication.getConfiguration().containsBean(beanId)) {
      bean = (AttributesAliasesConfiguration) JetstreamApplication.getConfiguration().getBean(beanId);
    }
    if (bean == null) {
      if (!attributesAliasesBeanReported) {
        attributesAliasesBeanReported = true; // report once as severe
        logger.error( 
            "Failed to retrieve Attributes Aliases bean, original names will be used only");
      }
      return false;
    }
    Map<String, List<String>> aliasesMap = bean.getAliasesMap();
    if (aliasesMap != null) {
      List<String> aliases = aliasesMap.get(name);
      if (aliases != null) {
        for (String alias : aliases) {
          if (alias.equals(attributeName)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  public static boolean isVersionEqualOrGreaterThan(Object versionBase, Object versionValue) {
    if (versionBase != null && versionValue != null) {
      try {
        Double baseValue = castToDouble(versionBase);
        Double value = castToDouble(versionValue);
        if (baseValue != null && value != null && value >= baseValue) {
          return true;
        }
      }
      catch (NumberFormatException e) {
      }
    }
    return false;
  }
 

  /**
   * Removes element from an array. It's faster that turning array into stream and then using WHERE clause.
   * 
   * @param array
   * @param value
   * @return
   */
  public static Object[] removeElement(Object[] array, Object entryToRemove) {
    if (array != null && entryToRemove != null) {
      List<Object> list = new ArrayList<Object>();
      for (Object entry : array) {
        if (entry == null || !entry.equals(entryToRemove)) {
          list.add(entry);
        }
      }
      return list.toArray();
    }
    return array;
  }

  /**
   * removeKeyAndConvertToListOfMaps - This method is written to meet the requirement for BML Indigo. Input would be
   * key/value pair which will be converted to list of Maps for each entry. Incoming Map's Key and value would become
   * separate entry. incoming Map's Key will associate with the keyname of "Name" String Value will associate with the
   * keyname "Value" String. Also incoming key is present in the Object, that entry will be removed and will be
   * converted to ListOfMaps
   * 
   * @param obj
   *          , Key
   * @return List of Maps
   */
  @SuppressWarnings("unchecked")
  public static List<Map<Object, Object>> removeKeyAndConvertToListOfMaps(Object obj, String key) {
    List<Map<Object, Object>> listofMaps = null;
    if (obj instanceof Map) {
      listofMaps = new ArrayList<Map<Object, Object>>();
      Map<Object, Object> map = (Map<Object, Object>) obj;
      if (map.containsKey(key))
        map.remove(key);
      for (Entry<Object, Object> mapEntry : map.entrySet()) {
        Map<Object, Object> listmap = new HashMap<Object, Object>();
        listmap.put(EPL_STREAM_NAME, mapEntry.getKey());
        listmap.put(EPL_STREAM_VALUE, mapEntry.getValue());
        listofMaps.add(listmap);
      }
    }
    obj = null;
    return listofMaps;
  }

  /**
   * Array of objects slicer returns array of values taken from each Object (either Map or POJO or List) for the key
   * provided. Key can have many levels (dot separated) like "Identity.guid". In this case we assume map nested to map.
   * 
   * @param mapsArray
   * @param key
   * @return
   * @throws InvocationTargetException
   * @throws IllegalAccessException
   * @throws NoSuchMethodException
   * @throws IllegalArgumentException
   * @throws SecurityException
   */
  public static Object[] sliceArray(Object[] objArray, String keys) throws SecurityException, IllegalArgumentException,
      NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    Object[] valuesArray = null;
    if (objArray != null) {
      valuesArray = new Object[objArray.length];
      int i = 0;
      for (Object entry : objArray) {
        valuesArray[i] = sliceObject(entry, keys);
        i++;
      }
    }
    return valuesArray;
  }

  /**
   * Object slicer returns entry taken from each Object (either Map or POJO or List) for the key and or index provided.
   * Key can have many levels (dot separated) like "Identity.guid.0". In this case we assume list nested to bean nested
   * to map.
   * 
   * @param obj
   * @param keys
   * @return
   * @throws SecurityException
   * @throws IllegalArgumentException
   * @throws NoSuchMethodException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   */
  public static Object sliceObject(Object obj, String keys) throws SecurityException, IllegalArgumentException,
      NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    if (keys != null && keys.length() > 0) {
      StringTokenizer st = new StringTokenizer(keys, SLICE_SEPARATOR);
      while (st.hasMoreTokens()) {
        obj = getBeanEntry(obj, st.nextToken().trim());
      }
    }
    return obj;
  }

  /**
   * Non-localized version, be careful
   * 
   * @param str
   * @return
   */
  public static String toLowerCase(String str) {
    if (str != null) {
      return str.toLowerCase();
    }
    return null;
  }

  /**
   * Non-localized version, be careful
   * 
   * @param str
   * @return
   */
  public static String[] toLowerCase(String[] strArray) {
    if (strArray != null) {
      String[] ret = new String[strArray.length];
      for (int i = 0; i < strArray.length; i++) {
        ret[i] = toLowerCase(strArray[i]);
      }
      return ret;
    }
    return null;
  }

  /**
   * The following act of white magic turns array into stream. Simple usage:
   * 
   * <code>
   * SELECT GuidSet FROM GUIDS, method:EPLUtilities.toStream1(GUIDS.Guids) AS GuidSet;
   * </code>
   * 
   * Secret ingredient here is in toStreamMetadata method below
   */
  @SuppressWarnings("unchecked")
  public static Map<String, Object>[] toStream1(Object[] array) {
    if (array != null) {
      Map<String, Object>[] mappedEvents = new HashMap[array.length];
      int i = 0;
      for (Object entry : array) {
        mappedEvents[i] = new HashMap<String, Object>();
        mappedEvents[i].put(EPL_STREAM_COLUMN1, entry);
        i++;
      }
      return mappedEvents;
    }
    return null;
  }

  public static Map<String, Class> toStream1Metadata() {
    Map<String, Class> propertyNames = new HashMap<String, Class>();
    propertyNames.put(EPL_STREAM_COLUMN1, Object.class);
    return propertyNames;
  }

  /**
   * The following act of white magic turns two arrays into stream of two columns. Simple usage:
   * 
   * <code>
   * SELECT COLUMN1 AS GuidSet FROM GUIDS, method:EPLUtilities.toStream2(GUIDS.Guids, GUIDS.b) WHERE cast(COLUMN2,string) = 'Value0';
   * </code>
   * 
   * Secret ingredient here is in toStreamMetadata method below
   */
  @SuppressWarnings("unchecked")
  public static Map<String, Object>[] toStream2(Object[] array1, Object[] array2) {
    if (array1 != null && array2 != null && array1.length == array2.length) {
      Map<String, Object>[] mappedEvents = new HashMap[array1.length];
      for (int i = 0; i < array1.length; i++) {
        mappedEvents[i] = new HashMap<String, Object>();
        mappedEvents[i].put(EPL_STREAM_COLUMN1, array1[i]);
        mappedEvents[i].put(EPL_STREAM_COLUMN2, array2[i]);
      }
      return mappedEvents;
    }
    return null;
  }

   public static Map<String, Class> toStream2Metadata() {
    Map<String, Class> propertyNames = new HashMap<String, Class>();
    propertyNames.put(EPL_STREAM_COLUMN1, Object.class);
    propertyNames.put(EPL_STREAM_COLUMN2, Object.class);
    return propertyNames;
  }

  /*
   * @SuppressWarnings("unchecked") public static boolean evaluateXPath(Object object, String key) { try { Object event
   * = null ; if (object instanceof String) { ObjectMapper mapper = new ObjectMapper(); event =
   * mapper.readValue(object.toString(), HashMap.class); } if (object instanceof List) { ObjectMapper mapper = new
   * ObjectMapper(); event = mapper.readValue(object.toString(), List.class); } if (object instanceof Map) {
   * ObjectMapper mapper = new ObjectMapper(); event = mapper.readValue(object.toString(), HashMap.class); } if (event
   * != null) { JXPathContext context = JXPathContext.newContext(event); context.setLenient(true);
   * System.out.println("Evaluate path" + event); return context.getValue("boolean(" + key +
   * ")").toString().equalsIgnoreCase("true"); } } catch (Exception e) { return false; } return false; }
   */

  /*
   * @SuppressWarnings("unchecked") public static Object extractXPath(Object object, String key) { try { Object event =
   * null; if (object instanceof String) { ObjectMapper mapper = new ObjectMapper(); event =
   * mapper.readValue(object.toString(), HashMap.class); } if (object instanceof List) { ObjectMapper mapper = new
   * ObjectMapper(); event = mapper.readValue(object.toString(), List.class); } if (object instanceof Map) {
   * ObjectMapper mapper = new ObjectMapper(); event = mapper.readValue(object.toString(), HashMap.class); }
   * 
   * if (event != null) { JXPathContext context = JXPathContext.newContext(event); context.setLenient(true);
   * System.out.println("Extracepath" + context.getValue(key)); return context.getValue(key); } } catch (Exception e) {
   * return null; } return null; }
   */

  /**
   * The following act of white magic turns three arrays into stream of two columns. Simple usage:
   * 
   * Secret ingredient here is in toStreamMetadata method below
   */
  @SuppressWarnings("unchecked")
  public static Map<String, Object>[] toStream3(Object[] array1, Object[] array2, Object[] array3) {
    if (array1 != null && array2 != null && array3 != null && array1.length == array2.length
        && array1.length == array3.length) {
      Map<String, Object>[] mappedEvents = new HashMap[array1.length];
      for (int i = 0; i < array1.length; i++) {
        mappedEvents[i] = new HashMap<String, Object>();
        mappedEvents[i].put(EPL_STREAM_COLUMN1, array1[i]);
        mappedEvents[i].put(EPL_STREAM_COLUMN2, array2[i]);
        mappedEvents[i].put(EPL_STREAM_COLUMN3, array3[i]);
      }
      return mappedEvents;
    }
    return null;
  }

  public static Map<String, Class> toStream3Metadata() {
    Map<String, Class> propertyNames = new HashMap<String, Class>();
    propertyNames.put(EPL_STREAM_COLUMN1, Object.class);
    propertyNames.put(EPL_STREAM_COLUMN2, Object.class);
    propertyNames.put(EPL_STREAM_COLUMN3, Object.class);
    return propertyNames;
  }

  /**
   * Non-localized version, be careful
   * 
   * @param str
   * @return
   */
  public static String toUpperCase(String str) {
    if (str != null) {
      return str.toUpperCase();
    }
    return null;
  }

  /**
   * Non-localized version, be careful
   * 
   * @param str
   * @return
   */
  public static String[] toUpperCase(String[] strArray) {
    if (strArray != null) {
      String[] ret = new String[strArray.length];
      for (int i = 0; i < strArray.length; i++) {
        ret[i] = toUpperCase(strArray[i]);
      }
      return ret;
    }
    return null;
  }

  /**
   * transformToJsonString - This method converts from incoming object to JSON string. Need/use of this method is, in
   * EPL we need to use pass Objects through window. But EPL allows primitives type alone. For that we 'll convert
   * Obejct to JsonString and pass it to the stream.
   * 
   * @param obj
   * @return
   */
  public static String transformToJsonString(Object obj) throws Exception {
    if (obj != null) {
      ObjectMapper mapper = new ObjectMapper();
      Writer writer = new StringWriter();
      mapper.writeValue(writer, obj);
      return writer.toString();
    }
    return null;
  }

  /**
   * transformToObject - This method converts incoming JsonString to HashMap. Need/use of this method is, in EPL we need
   * to use pass Objects through window. But EPL allows primitives type alone. For that we 'll convert Obejct to
   * JsonString and pass it to the stream. After that we need to convert back the Json String to original Object type.
   * 
   */
  @SuppressWarnings("unchecked")
  public static HashMap<String, Object> transformToObjectAndRemoveKey(String jsonStr, String key) throws Exception {
    if (jsonStr != null) {
      ObjectMapper mapper = new ObjectMapper();
      HashMap<String, Object> event = mapper.readValue(jsonStr, HashMap.class);
      if (key != null && !key.equals("")) {
        JXPathContext context = JXPathContext.newContext(event);
        context.setLenient(true);
        context.removePath(key);
      }
      return event;
    }
    return null;
  }

}
