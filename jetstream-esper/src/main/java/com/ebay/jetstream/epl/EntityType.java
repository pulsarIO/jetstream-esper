/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Dual licensed under the Apache 2.0 license and the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.epl;

/**
 * The EntityType indicates whether the guid is for an identity in TIS. In case of link we identify it by pair of guids.
 */
public enum EntityType {
  IDENTITY, /* GraphType values: */USER, ASSET, TRANSACTION, COMMUNICATION
}