package com.asentinel.common.orm;

import static org.junit.Assert.assertEquals;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Test;

public class QueryUtilsTestCase {
	private final static Logger log = LoggerFactory.getLogger(QueryUtilsTestCase.class);
	
	@Test
	public void testTableAliasSequence() {
		QueryUtils.resetDescriptorId();
		for (int i=0; i < 2 * QueryUtils.MAX_DESCRIPTOR_ID + 1; i++) {
			int r = i % QueryUtils.MAX_DESCRIPTOR_ID;
			String alias = QueryUtils.nextTableAlias();
			if (r == QueryUtils.MAX_DESCRIPTOR_ID - 1) {
				log.debug("testTableAliasSequence - i: " + i);
				log.debug("testTableAliasSequence - alias: " + alias);
			}
			if (r == 0) {
				log.debug("testTableAliasSequence - i: " + i);
				log.debug("testTableAliasSequence - alias: " + alias);
				
			}
			assertEquals("a" + Integer.toHexString(r), alias);
		}
	}

}
