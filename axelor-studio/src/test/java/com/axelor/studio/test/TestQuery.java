package com.axelor.studio.test;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.axelor.meta.db.MetaView;
import com.axelor.meta.db.repo.MetaViewRepository;
import com.axelor.test.GuiceModules;
import com.axelor.test.GuiceRunner;
import com.google.inject.Inject;

@RunWith(GuiceRunner.class)
@GuiceModules(TestModule.class)
public class TestQuery {
	
	@Inject
	private MetaViewRepository metaViewRepo;
	
	@Test
	public void test() {
		
		String viewNames = "message-grid,message-form";
		List<MetaView> views = metaViewRepo.all().filter("self.name in (?1)", 
				Arrays.asList(viewNames.split(","))).fetch();
	
	}

}
