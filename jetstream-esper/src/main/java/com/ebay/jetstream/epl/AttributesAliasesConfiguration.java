/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Dual licensed under the Apache 2.0 license and the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.epl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NamedBean;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.ebay.jetstream.management.Management;
import com.ebay.jetstream.xmlser.XSerializable;
import com.espertech.esper.client.Configuration;

@ManagedResource(objectName = "Event/Processor", description = "Attributes Aliases Configuration")
public class AttributesAliasesConfiguration extends Configuration implements NamedBean, BeanNameAware, XSerializable,
    InitializingBean {

  private static final long serialVersionUID = -8212117594421994155L;
  private static String id;

  public static String getId() {
    return id;
  }

  private String name;
  private Map<String, List<String>> aliasesMap;

  public AttributesAliasesConfiguration() {
  }

  public void afterPropertiesSet() throws Exception {
    Management.removeBeanOrFolder(getBeanName(), this);
    Management.addBean(getBeanName(), this);
  }

  public Map<String, List<String>> getAliasesMap() {
    return aliasesMap;
  }

  public String getBeanName() {
    return name;
  }

  public void setAliasesMap(Map<String, List<String>> aliasesMap) {
    this.aliasesMap = aliasesMap;
  }

  public void setBeanName(String name) {
    this.name = name;
    id = name;
  }
}
