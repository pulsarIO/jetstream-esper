/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Dual licensed under the Apache 2.0 license and the GPL v2 license.  See LICENSE for full terms.
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
