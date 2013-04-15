package com.axelor.apps.tool;

import org.junit.Assert;
import org.junit.Test;


public class TestStringTool {
	
	@Test
	public void testToFirstLower() {
		
		String actual = "Test";
		String result = "test";
		
		Assert.assertEquals(StringTool.toFirstLower(actual), result);
		
	}
	
	@Test
	public void testToFirstUpper() {
		
		String actual = "test";
		String result = "Test";
		
		Assert.assertEquals(StringTool.toFirstUpper(actual), result);
		
	}
	
	@Test
	public void testFillString() {
		
		String actual = "test";
		String resultRight = "test    ";
		String resultLeft  = "    test";
		
		Assert.assertEquals(StringTool.fillStringRight(actual, ' ', 8), resultRight);
		Assert.assertEquals(StringTool.fillStringRight(actual, ' ', 2), "te");
		
		Assert.assertEquals(StringTool.fillStringLeft(actual, ' ', 8), resultLeft);
		Assert.assertEquals(StringTool.fillStringLeft(actual, ' ', 2), "st");
		
		Assert.assertEquals(StringTool.fillStringLeft(resultRight, ' ', 4), "    ");
		
	}
	
}
