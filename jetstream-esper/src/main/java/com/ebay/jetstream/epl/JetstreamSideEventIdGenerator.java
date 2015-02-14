/*
Pulsar
Copyright (C) 2013-2015 eBay Software Foundation
Dual licensed under the Apache 2.0 license and the GPL v2 license.  See LICENSE for full terms.
*/
package com.ebay.jetstream.epl;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.ebay.jetstream.util.InetAddressParser;

public final class JetstreamSideEventIdGenerator {

	private static final Map<Integer, EventIdInfo> EVENT_DATA = new ConcurrentHashMap<Integer, EventIdInfo>();

	private JetstreamSideEventIdGenerator() {
		// utility class, no need to construct
	}

	public static long generateEventId(int nHostIp) {
		
		EventIdInfo info = EVENT_DATA.get(nHostIp);
		if (info == null) {
			info = new EventIdInfo(nHostIp);
			EVENT_DATA.put(nHostIp, info);
		}
		
		byte[] IP_BYTES = info.m_address;
		long nRandomMask = info.m_rand.nextInt(16) & 0xffff;
		long lEventId = (System.nanoTime() << 48) | (nRandomMask << 32) | ((long)(info.m_sequence.getAndIncrement() & 0xff) << 24) | ((IP_BYTES[1] & 0xff) << 16) | ((IP_BYTES[2] & 0xff) << 8) | (IP_BYTES[3] & 0xff);
		return lEventId < 0 ? -lEventId : lEventId;
	}

	private static final class EventIdInfo {
		private byte[] m_address;
		private final AtomicByte m_sequence = new AtomicByte();
		private final Random m_rand = new Random();
		EventIdInfo(int nHostIp) {
			m_address = InetAddressParser.intToAddressBytes(nHostIp);
		}
	}

	private static final class Random extends SecureRandom {
		public int nextInt(int bits) {
			return super.next(bits);
		}
	};


	private static final class AtomicByte extends Number {
		private byte m_bValue;

		public synchronized byte get() {                                    
			return m_bValue;
		}

		public synchronized byte getAndIncrement() {                          
			byte current = m_bValue++;                                                                                                              //IBM-perf_AtomicLong
			return current;                                                         
		}

		@Override
		public int intValue() {
			return (int)get();
		}

		@Override
		public long longValue() {
			return (long)get();
		}

		@Override
		public float floatValue() {
			return (float)get();
		}

		@Override
		public double doubleValue() {
			return (double)get();
		}
	}
}
