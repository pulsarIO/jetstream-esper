/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Dual licensed under the Apache 2.0 license and the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.ebay.jetstream.config.AbstractNamedBean;
import com.ebay.jetstream.event.JetstreamEvent;
import com.ebay.jetstream.management.Management;
import com.ebay.jetstream.xmlser.Hidden;
import com.ebay.jetstream.xmlser.XSerializable;
import com.espertech.esper.client.EPRuntime;

@ManagedResource(objectName = "Event/Processor", description = "Esper Event Processor Langguage")
public class EPL extends AbstractNamedBean implements Serializable, XSerializable, InitializingBean {

  private static final long serialVersionUID = 1L;

  private final List<String> m_statements = new ArrayList<String>();
  
  private final List<String> m_statementsWithComments = new ArrayList<String>();
  
  private List<OnDemandQuery> m_onDemandQueries;

  public void afterPropertiesSet() throws Exception {
    Management.removeBeanOrFolder(getBeanName(), this);
    Management.addBean(getBeanName(), this);
  }

  public List<OnDemandQuery> getOnDemandQueries() {
    return m_onDemandQueries;
  }

  /**
   * Gets the EPL statements as a single string block, such as a CDATA block.
   *
   * @return the set of all statements, in order, each separated by semicolon and new-line.
   */
  
  public List<String> getEPLStatements() {
	  return Collections.unmodifiableList(m_statements);
  }

  /**
   * Gets the list of EPL statements. 
   *
   * @return the list of all statements.
   */
  @Hidden
  public List<String> getStatements() {
    return Collections.unmodifiableList(m_statements);
  }

  public void processOnDemandQueries(EPRuntime runtime, int workerId, JetstreamEvent originalEvent) {
    List<OnDemandQuery> onDemandQueries = getOnDemandQueries();
    if (onDemandQueries != null) {
      for (OnDemandQuery onDemandQuery : onDemandQueries) {
        onDemandQuery.process(runtime, workerId, originalEvent);
      }
    }
  }

  public void setOnDemandQueries(List<OnDemandQuery> onDemandQueries) {
    m_onDemandQueries = onDemandQueries;
    if (onDemandQueries != null) {
      for (OnDemandQuery onDemandQuery : onDemandQueries) {
        if (onDemandQuery == null) {
          throw new IllegalArgumentException("onDemandQueries bean must contain at least one 'OnDemandQuery' bean");
        }
        onDemandQuery.validate();
      }
    }
  }

  /**
   * Parses a single String containing a semicolon delimited list of EPL statements.
   *
   * @param statementBlock
   *          the String containing all statements, with each statement separated by a semicolon.
   */
  
  public void setStatementBlock(String statementBlock) {
	    // TODO: better stmt delimiter parsing (e.g. skip ';' in a string or comment)
	     m_statements.clear();
	     m_statementsWithComments.clear();
	     String noncomment = "";
	     
	     /**
	      * This pattern removes statements with comments. 
	      */
	     if(statementBlock.contains("/*")){
	    	 Pattern p = Pattern.compile("/\\*(.*?)\\*/", Pattern.MULTILINE | Pattern.DOTALL);
	    	 Matcher m = p.matcher(statementBlock);
	    	 while (m.find()) {
	    		 noncomment  = m.replaceAll("");
	    	 }
	     }else {
	    	 noncomment = statementBlock ;
	     }
	     
	     
		 String pattern = "(?s);(?=(?:(?:.*?(?<!\\\\)\"){2})*[^\"]*$)(?=(?:(?:.*?(?<!\\\\)'){2})*[^']*$)";
	     String[] stmts = noncomment.split(pattern , -1);
	     for (String statement :  stmts){
	    	String trimmed = statement.trim();
	      if (trimmed.length() > 0) {
	          m_statements.add(trimmed);
	      }
	    }
	  }

  /**
   * Sets the list of EPL statements.
   *
   * @param statements
   *          the list of EPL statements.
   */
  public void setStatements(List<String> statements) {
    m_statements.clear();
    m_statements.addAll(statements);
  }

}
