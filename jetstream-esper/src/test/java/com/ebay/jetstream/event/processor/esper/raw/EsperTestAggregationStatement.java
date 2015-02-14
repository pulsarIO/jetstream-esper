/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Dual licensed under the Apache 2.0 license and the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper.raw;

import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.UpdateListener;

public class EsperTestAggregationStatement {

  private final EPStatement statement;

  public EsperTestAggregationStatement(EPAdministrator admin) {
    admin.createEPL("INSERT INTO S SELECT 1.1+guid AS V, workerId, guid FROM EsperTestAggregationEvent");
    admin.createEPL("INSERT INTO S SELECT 2.2+guid AS V, workerId, guid FROM EsperTestAggregationEvent");
    admin.createEPL("INSERT INTO S SELECT 3.3+guid AS V, workerId, guid FROM EsperTestAggregationEvent");
    admin.createEPL("INSERT INTO S SELECT 4.4+guid AS V, workerId, guid FROM EsperTestAggregationEvent");
    admin.createEPL("CREATE WINDOW SW.win:keepall() AS (V double, workerId long, guid int)");
    admin.createEPL("INSERT INTO SW SELECT V, workerId, guid FROM S");
    admin.createEPL("ON CleanupWindowEvent DELETE FROM SW WHERE SW.workerId = CleanupWindowEvent.workerId");
    statement = admin.createEPL("SELECT * FROM OutputEvent");
  }

  public void addListener(UpdateListener listener) {
    statement.addListener(listener);
  }

}
