/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Licensed under the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper.annotation;


public @interface SendMail {
	String alertList() default "";
	String sendFrom() default "";
	String alertSeverity() default "SEVERE";
    String mailServer() default "";
    String eventFields() default "";
    String mailContent() default "Esper query needs your attention!!";
    String mailSubject() default "Notification from Esper query annotation..";
}
