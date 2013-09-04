/**
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the “License”); you may not use
 * this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://license.axelor.com/.
 *
 * The License is based on the Mozilla Public License Version 1.1 but
 * Sections 14 and 15 have been added to cover use of software over a
 * computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be
 * consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an “AS IS”
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is part of "Axelor Business Suite", developed by
 * Axelor exclusively.
 *
 * The Original Developer is the Initial Developer. The Initial Developer of
 * the Original Code is Axelor.
 *
 * All portions of the code written by Axelor are
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 */
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
