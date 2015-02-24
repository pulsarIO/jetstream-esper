/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Licensed under the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.ebay.jetstream.config.AbstractNamedBean;
import com.ebay.jetstream.counter.LongCounter;
import com.ebay.jetstream.management.Management;
import com.ebay.jetstream.xmlser.XSerializable;
import com.espertech.esper.client.hook.ExceptionHandler;
import com.espertech.esper.client.hook.ExceptionHandlerContext;

/**
 * This class will be called by EsperEngine when there is RuntimeException
 * thrown during EPL statement processing. Last Exception will be set for
 * debugging purposes. It also counts number of exceptions thrown per second
 * with EWMA counter. Based on the exception count, action can be taken.
 * 
 * @author rmuthupandian
 * 
 */

@ManagedResource(objectName = "Event/Processor", description = "Esper Exception Handler")
public class EsperExceptionHandler extends AbstractNamedBean implements
		ExceptionHandler, XSerializable, InitializingBean {

	private String lastException;
	private LongCounter num_exception = new LongCounter();
	private LongCounter exceptionTimeStamp = new LongCounter();
	private AtomicBoolean exceptionStatus = new AtomicBoolean(false);
	private AtomicBoolean alertPosted = new AtomicBoolean(false);
	DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss'Z'");
	Date m_date = new Date();
	private String m_lastExceptionTime;
	
	public String getLastExceptionTime() {
		return m_lastExceptionTime;
	}

	public void setLastExceptionTime(String m_lastExceptionTime) {
		this.m_lastExceptionTime = m_lastExceptionTime;
	}

	//This method is just for exposing to monitoring page
	public boolean getAlertPostedStatus() {
		return alertPosted.get();
	}

	public boolean isAlertPosted(){
		return alertPosted.get();
	}
	
	public void setAlertPostedStatus(boolean status){
		alertPosted.set(status);
	}
	
	public boolean IsInException(){
		return exceptionStatus.get();
	}
	
	//This method is just for exposing to monitoring page - it demands method name to start with getXX
	public boolean getIsInException(){
		return exceptionStatus.get();
	}
	
	public String getLastException() {
		return lastException;
	}

	public void setLastException(String exception) {
		lastException = exception;
	}

	@Override
	public void handle(ExceptionHandlerContext context) {
		num_exception.increment();
		m_date.setTime(System.currentTimeMillis());
		setLastExceptionTime(formatter.format(m_date));
		setLastException(context.getThrowable().getMessage() + " , EPL :" + context.getEpl());
		
		if (num_exception.get() == 1) {
		  exceptionTimeStamp.set( System.currentTimeMillis());
		}
		else {
		   if( (System.currentTimeMillis() -  exceptionTimeStamp.get()) > 10000) {
		     exceptionStatus.set(true);
		     alertPosted.set(false);
		     exceptionTimeStamp.set(0); 
		     num_exception.set(0);
		   }
		}
	}

	public LongCounter getNumExceptions() {
		return num_exception;
	}
	

	@Override
	public void afterPropertiesSet() throws Exception {
		Management.addBean(getBeanName(), this);
	}
	
	@ManagedOperation
	public void clearAlertStatus(){
		exceptionStatus.set(false);
		alertPosted.set(false);
        exceptionTimeStamp.set(0); 
        num_exception.set(0);
	}
	
	public void setExceptionStatus(boolean status) {
		exceptionStatus.set(status);
	}

}
