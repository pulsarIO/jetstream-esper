/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Licensed under the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper.annotation.metadata;

import com.ebay.jetstream.notification.MailConfiguration;

public class SendMailAnnotationMetadata extends MailConfiguration {
	private String eventFields;
	private String mailContent;
	private String mailSubject;

	public String getEventFields() {
		return eventFields;
	}

	public void setEventFields(String eventFields) {
		this.eventFields = eventFields;
	}

	public String getMailSubject() {
		return mailSubject;
	}

	public void setMailSubject(String mailSubject) {
		this.mailSubject = mailSubject;
	}

	public String getMailContent() {
		return mailContent;
	}

	public void setMailContent(String mailContent) {
		this.mailContent = mailContent;
	}

}
