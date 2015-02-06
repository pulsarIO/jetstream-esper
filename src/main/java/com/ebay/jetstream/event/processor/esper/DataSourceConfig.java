/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Dual licensed under the Apache 2.0 license and the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper;


public class DataSourceConfig {

	private String m_strDsName;
	private String m_strEplRef;
	
	public void setDsName(String strName) {
		//m_strDsName = DalJdbcDriver.JDBC_PREFIX + strName;
	}
	
	public void setEplRefName(String strName) {
		m_strEplRef = strName;
	}
	
	String getDsName() {
		return m_strDsName;
	}
	
	String getEplRefName() {
		return m_strEplRef;
	}
}
