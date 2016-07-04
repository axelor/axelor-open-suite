package com.axelor.studio.test;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.axelor.studio.service.builder.ActionBuilderService;
import com.axelor.test.GuiceModules;
import com.axelor.test.GuiceRunner;
import com.google.inject.Inject;

@RunWith(GuiceRunner.class)
@GuiceModules({ TestModule.class })
public class TestActionBuilder {
	
	@Inject
	private ActionBuilderService actionBuilderService;

	@Test
	public void testRecorder() {
//		actionBuilderService.build("/home/axelor/studio/studio-app/modules/axelor-custom");
	}

}
