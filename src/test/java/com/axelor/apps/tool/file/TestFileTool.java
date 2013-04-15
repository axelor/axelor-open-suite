package com.axelor.apps.tool.file;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;

public class TestFileTool {

	@Test
	public void create() throws IOException {
		
		String destinationFolder = System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + "tata/titi/toto";
		String fileName = "toto.txt";
		Assert.assertTrue(FileTool.create(destinationFolder, fileName).createNewFile());
		
	}
	
	@Test
	public void create2() throws IOException {
		
		String fileName = System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + "tata2/titi2/toto2/toto.txt";
		Assert.assertTrue(FileTool.create(fileName).createNewFile());
		
	}
}
