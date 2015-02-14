/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Dual licensed under the Apache 2.0 license and the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.event.processor.esper;

import java.util.Map;

import com.ebay.jetstream.config.RootConfiguration;
import com.espertech.esper.client.hook.ExceptionHandler;
import com.espertech.esper.client.hook.ExceptionHandlerFactory;
import com.espertech.esper.client.hook.ExceptionHandlerFactoryContext;

/**
 * This factory class serves Exceptionhandler instance to the Engine to capture
 * RuntimeExceptions from EPL processing. Since multiple EsperPorcessors are
 * available, all EsperProcessors are fetched from RootConfiguration. Each
 * EsperProcessor holds ExceptionHandler.
 * 
 * @author rmuthupandian
 * 
 */

public class JetstreamExceptionHandlerFactory implements
		ExceptionHandlerFactory {

	Map<String, EsperProcessor> esperprocessors = RootConfiguration
			.getConfiguration().getBeansOfType(EsperProcessor.class);

	@Override
	public ExceptionHandler getHandler(ExceptionHandlerFactoryContext context) {
		for (EsperProcessor proc : esperprocessors.values()) {
			if (proc.getEngineURI() != null
					&& proc.getEngineURI().equals(context.getEngineURI())) {
				return proc.getEsperExceptionHandler();
			}
		}
		return null;
	}

}
