package com.axelor.apps.qms.service;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class QMSDocumentVersionServiceImplTest {
	private QMSDocumentVersionServiceImpl service;

	@Before
	public void setUp() {
		service = new QMSDocumentVersionServiceImpl();
	}

	@Test
	public void testIndexToString() {
		assertEquals("A", service.integerToVersionIndex(0));
		assertEquals("Z", service.integerToVersionIndex(25));
		assertEquals("AA", service.integerToVersionIndex(26));
		assertEquals("BA", service.integerToVersionIndex(52));
	}

	@Test
	public void testStringToIndex() {
		assertEquals(0, service.versionIndexToInteger("A"));
		assertEquals(26, service.versionIndexToInteger("AA"));
		assertEquals(52, service.versionIndexToInteger("BA"));
	}

}
