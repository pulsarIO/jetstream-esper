/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Dual licensed under the Apache 2.0 license and the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.epl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.BeforeClass;
import org.junit.Test;

import com.ebay.jetstream.event.processor.esper.EPL;
import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;

public class EPLParserTest  {
	
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
	
	static EPAdministrator epAdmin  ;
	@BeforeClass
	public static void setUp(){
		EPServiceProvider engine = EPServiceProviderManager.getDefaultProvider();
	    engine.getEPAdministrator().getConfiguration().addEventType(TestEvents.class);
	    epAdmin = engine.getEPAdministrator();
	 
	}
	

	public static void main(String[] args) throws Exception {
	
		InputStream stream = new FileInputStream(new File("src//test//java//com/ebay//jetstream//epl//sampleEPL.txt"));
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		StringBuffer cntns = new StringBuffer();
		String s ;
		try{
			while(( s = reader.readLine()) != null ){
				//System.out.println(s);
				cntns.append(s);
			}
			//String str ="\n select field_0, count(field_0) as googlebotcount from RTD where field_0 = 'Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; WOW64; Trident/6.0)' group  by field_0 output  snapshot when terminated order by count(field_0) desc;" ;
			
			System.out.println(cntns.toString());
			   String[] split = cntns.toString().split(";(?=(?:(?:[^']*'){2})*[^']*$)" , -1);
			 for(String spl : split) {
		          System.out.println(spl);
		        }
		}finally{
			if(reader != null)	
				reader.close();
		}
   }
	
	
	@Test
	public void testSingleStmtEPL(){
		String EPL1 = " select * from TestEvent;";

		EPL epl = new EPL();
		epl.setStatementBlock(EPL1);
		for (String stmt : epl.getStatements()) {
			epAdmin.compileEPL(stmt);
		}
   }
	
	
	@Test
	public void testMultipleStmtEPL(){
		String EPL1 = " select * from TestEvent; " +
	                    " Select timemillis from TestEvent; ";

		EPL epl = new EPL();
		epl.setStatementBlock(EPL1);
		for (String stmt : epl.getStatements()) {
			epAdmin.compileEPL(stmt);
		}
   }
	
	@Test
	public void testSemiColonBtwSingleQuotes(){
		String EPL1 = " select * from TestEvent;  " +
	                    " Select timemillis from TestEvent where ck='test;123'; ";

		EPL epl = new EPL();
		epl.setStatementBlock(EPL1);
		for (String stmt : epl.getStatements()) {
			epAdmin.compileEPL(stmt);
		}
   }
	
	
	@Test
	public void testMultipleSemiColonBtwSingleQuotes(){
		String EPL1 = " select * from TestEvent;  " +
	                    " Select timemillis from TestEvent where ck='test;1 test;  again ;23'; ";

		EPL epl = new EPL();
		epl.setStatementBlock(EPL1);
		for (String stmt : epl.getStatements()) {
			epAdmin.compileEPL(stmt);
		}
   }
	
	
	@Test
	public void testSemiColonBtwSingleQuotesWithEscapedQuote(){
		String EPL1 = " select * from TestEvent;  " +
	                    " Select timemillis from TestEvent where ck='test1 test \\\'again ;23'; ";

		EPL epl = new EPL();
		epl.setStatementBlock(EPL1);
		for (String stmt : epl.getStatements()) {
			epAdmin.compileEPL(stmt);
		}
   }
	
	@Test
	public void testSemiColonBtwSingleQuotesWithComments(){
		String EPL1 = " select * from TestEvent;  " +
	                    " /*This stmt is to test ck value ; */ Select timemillis from TestEvent where ck='test;123'; ";

		EPL epl = new EPL();
		epl.setStatementBlock(EPL1);
		for (String stmt : epl.getStatements()) {
			epAdmin.compileEPL(stmt);
		}
   }
	
	@Test
	public void testSemiColonBtwSingleQuotesWithCommentsInNL(){
		String EPL1 = " select * from TestEvent;  " +
	                    " /*This stmt is to test ck value*/  \n " + 
	                    " Select timemillis from TestEvent where ck='test;123'; ";

		EPL epl = new EPL();
		epl.setStatementBlock(EPL1);
		for (String stmt : epl.getStatements()) {
			epAdmin.compileEPL(stmt);
		}
   }
	
	@Test
	public void testSemiColonBtwSingleQuotesNL(){
		String EPL1 = " select * from TestEvent;  \n" +
	                    " Select timemillis from TestEvent where ck='test;123'; ";

		EPL epl = new EPL();
		epl.setStatementBlock(EPL1);
		for (String stmt : epl.getStatements()) {
			epAdmin.compileEPL(stmt);
		}
   }
	
	@Test
	public void testSemiColonBtwSingleQuotesTab(){
		String EPL1 = " select * from TestEvent;  \t" +
	                    " Select timemillis from TestEvent where ck='test;123'; ";

		EPL epl = new EPL();
		epl.setStatementBlock(EPL1);
		for (String stmt : epl.getStatements()) {
			epAdmin.compileEPL(stmt);
		}
   }
	
	@Test
	public void testSemiColonBtwSingleQuotesNLTab(){
		String EPL1 = " select * from TestEvent;  \n\t" +
	                    " Select timemillis from TestEvent where ck='test;123'; ";

		EPL epl = new EPL();
		epl.setStatementBlock(EPL1);
		for (String stmt : epl.getStatements()) {
			epAdmin.compileEPL(stmt);
		}
   }
	
	
	
	@Test
	public void testSemiColonBtwDoubleQuotes(){
		String EPL1 = " select * from TestEvent; " +
	                    " Select timemillis from TestEvent(ck=\"test;123\"); ";

		EPL epl = new EPL();
		epl.setStatementBlock(EPL1);
		for (String stmt : epl.getStatements()) {
			epAdmin.compileEPL(stmt);
		}
   }
	
	@Test
	public void testMultipleSemiColonBtwDoubleQuotes(){
		String EPL1 = " select * from TestEvent; " +
	                    " Select timemillis from TestEvent(ck=\"test;12 test; why;3\"); ";

		EPL epl = new EPL();
		epl.setStatementBlock(EPL1);
		for (String stmt : epl.getStatements()) {
			epAdmin.compileEPL(stmt);
		}
   }
	
	
	@Test
	public void testSemiColonBtwDoubleQuotesNL(){
		String EPL1 = " select * from TestEvent; \n" +
	                    " Select timemillis from TestEvent(ck=\"test;123\"); ";

		EPL epl = new EPL();
		epl.setStatementBlock(EPL1);
		for (String stmt : epl.getStatements()) {
			epAdmin.compileEPL(stmt);
		}
   }
	

	@Test
	public void testSemiColonBtwDoubleQuotesTab(){
		String EPL1 = " select * from TestEvent; \t" +
	                    " Select timemillis from TestEvent(ck=\"test;123\"); ";

		EPL epl = new EPL();
		epl.setStatementBlock(EPL1);
		for (String stmt : epl.getStatements()) {
			epAdmin.compileEPL(stmt);
		}
   }
	
	
	@Test
	public void testSemiColonBtwDoubleQuotesTabNL(){
		String EPL1 = " select * from TestEvent; \n\t" +
	                    " Select timemillis from TestEvent(ck=\"test;123\"); ";

		EPL epl = new EPL();
		epl.setStatementBlock(EPL1);
		for (String stmt : epl.getStatements()) {
			epAdmin.compileEPL(stmt);
		}
   }
	
	@Test
	public void testSemiColonBtwDoubleQuotesTabNLMultipleStmt(){
		String EPL1 = "  select * from TestEvent; \n\t" +
	                    " Select timemillis from TestEvent(ck=\"test;123\");  " +
	                    " select * from TestEvent;";

		EPL epl = new EPL();
		epl.setStatementBlock(EPL1);
		for (String stmt : epl.getStatements()) {
			epAdmin.compileEPL(stmt);
		}
   }
	
	
	@Test
	public void testSemiColonBtwDoubleQuotesTabNLEscaped(){
		String EPL1 = "  select * from TestEvent; \n\t" +
	                    " Select timemillis from TestEvent(ck=\"test;123 \\\" contd \");  " +
	                    " select * from TestEvent;";

		EPL epl = new EPL();
		epl.setStatementBlock(EPL1);
		for (String stmt : epl.getStatements()) {
			epAdmin.compileEPL(stmt);
		}
   }
	
	
	@Test
	public void testSemiColonBtwDoubleQuotesWithComments(){
		String EPL1 = " select * from TestEvent; \n " +
	                    " /*This stmt is to test ck value */ Select timemillis from TestEvent where ck=\"test;123\"; ";

		EPL epl = new EPL();
		epl.setStatementBlock(EPL1);
		for (String stmt : epl.getStatements()) {
			epAdmin.compileEPL(stmt);
		}
   }
	
	
	
}
