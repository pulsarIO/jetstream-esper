/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Dual licensed under the Apache 2.0 license and the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper.raw;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Schema;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class EsperTestConfigurationValidator implements ErrorHandler {

	private static final Log log = LogFactory.getLog(EsperTestConfigurationValidator.class);

	private int warnings;
	private int errors;
	private int fatalErrors;


	public boolean validate(String instance, String schema) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        
		try {
			warnings = 0;
			errors = 0;
			fatalErrors = 0;
			try {
				//Set the validation feature
				factory.setFeature("http://xml.org/sax/features/validation",
				                  true);

				//Set the schema validation feature
				factory.setFeature("http://apache.org/xml/features/validation/schema",
				                  true);

				//Set schema full grammar checking
				factory.setFeature("http://apache.org/xml/features/validation/schema-full-checking",
				                  true);
                
				// If there is an external schema set it
				if (schema != null) {
                    SchemaFactory sf = 
                        SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                    Schema s = sf.newSchema(new StreamSource(schema));
                    factory.setSchema(s);
				}
				DocumentBuilder parser = factory.newDocumentBuilder();
				parser.setErrorHandler(this);
				// Parse and validate
				parser.parse(instance);
				// Return true if we made it this far with no errors
				return ((errors == 0) && (fatalErrors == 0));
			} catch (SAXException e) {
				log.error("Could not activate validation features - " + e.getMessage());
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return false;
	}

	public void warning(SAXParseException ex) {
		log.error("[Warning] " + ex.getMessage() + " line "
				+ ex.getLineNumber() + " column " + ex.getColumnNumber());
		warnings++;
	}

	public void error(SAXParseException ex) {
		log.error("[Error] " + ex.getMessage() + " line "
				+ ex.getLineNumber() + " column " + ex.getColumnNumber());
		errors++;
	}

	public void fatalError(SAXParseException ex) throws SAXException {
		log.error("[Fatal Error] " + ex.getMessage() + " line "
				+ ex.getLineNumber() + " column " + ex.getColumnNumber());
		fatalErrors++;
		throw ex;
	}

}
