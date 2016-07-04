package com.axelor.studio.test;

import java.io.File;
import java.io.IOException;

import org.apache.xmlbeans.impl.common.JarHelper;
import org.junit.Test;

public class TestJava {

	@Test
	public void test() {
		
		File file = new File("/home/axelor/Desktop/test");
		File warFile = new File("/home/axelor/studio/studio-app/build/libs/studio-app-1.0.0.war");
		String appName = warFile.getName();
		appName = appName.substring(0,appName.length()-4);
		File appDir = new File(file,appName);
		if(!appDir.exists()){
			appDir.mkdir();
		}
		JarHelper jarHelper = new JarHelper();
		try {
			jarHelper.unjarDir(warFile, appDir);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
