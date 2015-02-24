/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Licensed under the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper.raw;

import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.UpdateListener;

public class EsperTestStatement {
    private EPStatement statement;

    public EsperTestStatement(EPAdministrator admin) {
    	String stmt = "select id, field1, field2, field3 from EsperTestEvent";
        statement = admin.createEPL(stmt);
    }

    public void addListener(UpdateListener listener) {
        statement.addListener(listener);
    }

    public void setSubscriber (EsperTestSubscriber subscriber) {
    	statement.setSubscriber(subscriber);
    }
}
