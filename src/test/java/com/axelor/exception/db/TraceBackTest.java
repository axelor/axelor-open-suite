package com.axelor.exception.db;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.axelor.exception.TestModule;
import com.axelor.test.GuiceModules;
import com.axelor.test.GuiceRunner;

@RunWith(GuiceRunner.class)
@GuiceModules({ TestModule.class })
public class TraceBackTest {

	@Test
	public void testPersist() {
	    
		System.out.println("Test");
		String str = "test.test1.test2";
		String tab[] = str.split("\\."); 
		System.out.println(tab.length);
		System.out.println(tab[0]);
		System.out.println(tab[1]);
		System.out.println(tab[2]);
	}
}
