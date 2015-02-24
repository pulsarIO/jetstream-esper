/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Licensed under the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper.annotation.listener;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ebay.jestream.event.annotation.AnnotationListener;
import com.ebay.jestream.event.annotation.StatementAnnotationInfo;
import com.ebay.jetstream.event.JetstreamEvent;
import com.ebay.jetstream.event.processor.esper.annotation.SendMail;
import com.ebay.jetstream.event.processor.esper.annotation.metadata.SendMailAnnotationMetadata;

public class SendMailAnnotationListener implements AnnotationListener {
	private static final String ANNO_KEY = SendMail.class.getSimpleName();
    
	private static final Logger s_logger = LoggerFactory.getLogger("com.ebay.jetstream.notification.MailSender");
	Properties properties;

	@Override
	public JetstreamEvent processMetaInformation(JetstreamEvent event,
			StatementAnnotationInfo annotationInfo) {
		
		SendMailAnnotationMetadata anntmetadata = (SendMailAnnotationMetadata) annotationInfo.getAnnotationInfo(ANNO_KEY);
		properties = new Properties();
		properties.setProperty("mail.smtp.host", anntmetadata.getMailServer());

	      Session session = Session.getInstance(properties, null); 
	      try{
	          MimeMessage message = new MimeMessage(session);
	          message.addHeader("Content-type", "text/HTML; charset=UTF-8");
	          
	          message.setFrom(new InternetAddress(anntmetadata.getSendFrom()));
	          
	          message.setRecipients(Message.RecipientType.TO,
	        		  InternetAddress.parse(anntmetadata.getAlertList(), false));
	          
	          String[] fieldList = anntmetadata.getEventFields().split(",");
			  StringBuffer sb = new StringBuffer();
			  sb.append(anntmetadata.getMailContent()).append(".\n");
	          for(int i=0; i<fieldList.length;i++){
	        	  sb.append(fieldList[i]).append(": ").append(event.get(fieldList[i])).append(",\n");
	          }
			  message.setText(sb.toString(), "UTF-8");	
			  
			  StringBuffer subject = new StringBuffer();
			  if(anntmetadata.getAlertSeverity() != null)
				  subject.append(anntmetadata.getAlertSeverity()).append(" alert for Jetstream Event Type").append(event.getEventType()).append(": ");
			  if(anntmetadata.getMailSubject() != "")
				  subject.append(anntmetadata.getMailSubject());
			  message.setSubject(subject.toString(), "UTF-8");

	          Transport.send(message);
	       }catch (Throwable mex) {
	          s_logger.warn( mex.getLocalizedMessage());
	       }
		return event;
	}
	
	@Override
	public Class getAnnotationClass() {
		return SendMail.class;
	}

}
