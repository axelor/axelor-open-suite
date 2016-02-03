/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.test.templateRule;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.axelor.apps.base.db.TemplateRule;
import com.axelor.apps.base.db.TemplateRuleLine;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.template.TemplateRuleService;
import com.axelor.apps.base.test.TestModule;
import com.axelor.apps.message.db.Template;
import com.axelor.db.Model;
import com.axelor.meta.db.repo.MetaModelRepository;
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
	
	@Inject
	private PartnerRepository partnerRepo;
	
	@Inject
	private MetaModelRepository metaModelRepository;
	
	@Before
	public void before() {
		tr = new TemplateRule();
		tr.setMetaModel(metaModelRepository.findByName("Partner"));
		
		TemplateRuleLine line = new TemplateRuleLine();
		line.setSequence(1);
		line.setTemplate(new Template("Template 1"));
		tr.addTemplateRuleLineListItem(line);
		
		TemplateRuleLine line2 = new TemplateRuleLine();
		line2.setSequence(2);
		line2.setTemplate(new Template("Template 2"));
		tr.addTemplateRuleLineListItem(line2);
		
		bean = partnerRepo.find(Long.valueOf("1"));
	}
	
	@Test
	public void test1() {
		trs.getTemplate(bean, tr);
	}
	
}
