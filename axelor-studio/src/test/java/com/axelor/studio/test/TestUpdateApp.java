package com.axelor.studio.test;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.axelor.common.FileUtils;
import com.axelor.test.GuiceModules;
import com.axelor.test.GuiceRunner;
import com.google.common.base.Joiner;
import com.google.common.io.Files;
import com.google.inject.Inject;


@RunWith(GuiceRunner.class)
@GuiceModules({ TestModule.class })
public class TestUpdateApp {
	
	@Test
	public void test() {
//		UpdateAppService updateapp = new UpdateAppService();
//		String[] result = updateapp.updateLive(true);
//		System.out.println(result[0]);
//		System.out.println(result[1]);
		try{
//			File dir = new File("/home/axelor/demo/axelor-demo/build/libs/");
//			FileFilter fileFilter = new WildcardFileFilter("*.war");
//			File[] files = dir.listFiles(fileFilter);
//			Files.copy(files[0], new File("/home/axelor/Desktop/abcd.war"));
//			FileUtils.deleteDirectory(new File("/o"));
			
//			ProcessBuilder processBuilder = new ProcessBuilder("/home/axelor/opt/apache-tomcat-7.0.39/bin/shutdown.sh && /home/axelor/opt/apache-tomcat-7.0.39/bin/startup.sh");
//			Process process = processBuilder.start();
//			process.waitFor();
//
//			System.out.println(process.exitValue());
//			
//			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//	
//	        String line = "";			
//			while ((line = reader.readLine())!= null) {
//				System.out.println(line);
//			}
//			
//			reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
//			
//			while ((line = reader.readLine())!= null) {
//				System.out.println(line);
//			}
			
			
			File tempDir = new File(System.getProperty("java.io.tmpdir"));
			File scriptFile = new File(tempDir,"RestartServesr.sh");
			
			scriptFile.setReadable(true);
			boolean executable = scriptFile.setExecutable(true);
			
			System.out.println(executable);
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
