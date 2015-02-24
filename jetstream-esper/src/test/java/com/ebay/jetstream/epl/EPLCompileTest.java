/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Licensed under the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.epl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.ebay.jetstream.event.processor.esper.EPL;
import com.ebay.jetstream.event.processor.esper.EsperConfiguration;
import com.ebay.jetstream.event.processor.esper.EsperProcessor;
import com.espertech.esper.client.EPException;

public class EPLCompileTest  {
	
	  public static class TestEvents {
	        private long timemillis;
	        private String ck;

	        public TestEvents(String id, long time) {
	            this.ck = id;
	            this.timemillis = time;
	        }

	        public long getTimemillis() {
	            return timemillis;
	        }

	        public String getCk() {
	            return ck;
	        }
	    }
	
	@Test
	public void testMultipleStmtEPL(){
		String EPL1 = " select * from TestEvent; " +
	                    " Select timemillis from TestEvent; " +
	                    " @OutputTo(\"omc\") select * from TestEvent; ";

		EPL epl = new EPL();
		epl.setStatementBlock(EPL1);
		EsperProcessor proc = new EsperProcessor();
		EsperConfiguration config = new EsperConfiguration();
		config.addEventType("TestEvent",  TestEvents.class);
		config.addImport(com.ebay.jetstream.event.processor.esper.annotation.OutputTo.class);
		proc.setConfiguration(config);
		assertTrue(proc.compileEPL(epl));
		
   }
	

	  @Test
	 public void testcreateSchemaEPL(){
			String EPL1 = " CREATE SCHEMA SchemaEvent as ( key1 string, key2 string) ;" +
					        " select * from SchemaEvent; " +
		                    " Select key2 from SchemaEvent; " +
		                    " @OutputTo(\"omc\") select * from SchemaEvent; ";

			EPL epl = new EPL();
			epl.setStatementBlock(EPL1);
			EsperProcessor proc = new EsperProcessor();
			EsperConfiguration config = new EsperConfiguration();
			config.addImport(com.ebay.jetstream.event.processor.esper.annotation.OutputTo.class);
			proc.setConfiguration(config);
			assertTrue(proc.compileEPL(epl));
	   }
	  
	  @Test(expected=EPException.class)
	  public void testWrongAnnotationEPL(){
				String EPL1 = " select * from TestEvent; " +
	                    " Select timemillis from TestEvent; " +
	                    " @testing(\"omc\") select * from TestEvent; ";

				EPL epl = new EPL();
				epl.setStatementBlock(EPL1);
				EsperProcessor proc = new EsperProcessor();
				EsperConfiguration config = new EsperConfiguration();
				config.addEventType("TestEvent",  TestEvents.class);
				proc.setConfiguration(config);
				assertFalse(proc.compileEPL(epl));
		}
	  
	  @Test(expected=EPException.class)
	  public void testWrongEventTYpeEPL(){
			String EPL1 = " select * from TestEvent; " +
		                    " Select timemillis from TestEvent; " +
		                    " @OutputTo(\"omc\") select * from TestEvent; ";

			EPL epl = new EPL();
			epl.setStatementBlock(EPL1);
			EsperProcessor proc = new EsperProcessor();
			EsperConfiguration config = new EsperConfiguration();
			proc.setConfiguration(config);
			assertFalse(proc.compileEPL(epl));
			
	   }
	  
	  

	
	
}
