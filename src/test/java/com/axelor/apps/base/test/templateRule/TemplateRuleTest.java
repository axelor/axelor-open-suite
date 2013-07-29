package com.axelor.apps.base.test.templateRule;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Template;
import com.axelor.apps.base.db.TemplateRule;
import com.axelor.apps.base.db.TemplateRuleLine;
import com.axelor.apps.base.service.template.TemplateRuleService;
import com.axelor.apps.base.test.TestModule;
import com.axelor.db.Model;
import com.axelor.meta.db.MetaModel;
import com.axelor.test.GuiceModules;
import com.axelor.test.GuiceRunner;
import com.google.inject.Inject;

@RunWith(GuiceRunner.class)
@GuiceModules({ TestModule.class })
public class TemplateRuleTest {
	
	@Inject
	private TemplateRuleService trs;
	
	private TemplateRule tr;
	private Model bean;
	
	@Before
	public void before() {
		tr = new TemplateRule();
		tr.setMetaModel(MetaModel.findByName("Partner"));
		
		TemplateRuleLine line = new TemplateRuleLine();
		line.setSequence(1);
		line.setTemplate(new Template("Template 1"));
		tr.addTemplateRuleLineListItem(line);
		
		TemplateRuleLine line2 = new TemplateRuleLine();
		line2.setSequence(2);
		line2.setTemplate(new Template("Template 2"));
		tr.addTemplateRuleLineListItem(line2);
		
		bean = Partner.find(Long.valueOf("1"));
	}
	
	@Test
	public void test1() {
		trs.getTemplate(bean, tr);
	}
	
}
