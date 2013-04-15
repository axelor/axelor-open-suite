package com.axelor.apps.tool.net;

import org.junit.Assert;
import org.junit.Test;


public class TestURLService {
	
	@Test
	public void testNotExist() {
		
		Assert.assertNull(URLService.notExist("http://www.google.com"));
		Assert.assertEquals("Problème de format de l'URL", URLService.notExist("www.google.com"));
		Assert.assertEquals("Ce document n'existe pas", URLService.notExist("http://www.testtrgfgfdg.com/"));
	}
	
}
