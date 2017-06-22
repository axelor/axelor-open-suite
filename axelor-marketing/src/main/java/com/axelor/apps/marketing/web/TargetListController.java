/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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
package com.axelor.apps.marketing.web;

import java.util.List;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.crm.db.repo.LeadRepository;
import com.axelor.apps.marketing.db.TargetList;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.studio.service.FilterService;
import com.google.inject.Inject;

public class TargetListController {
	
	@Inject
	private FilterService filterService;
	
	@Inject
	private PartnerRepository partnerRepo;
	
	@Inject
	private LeadRepository leadRepo;
	
	public void openFilteredLeads(ActionRequest request, ActionResponse response) {
		
		TargetList targetList = request.getContext().asType(TargetList.class);
		
		String leadFilers = filterService.getJpqlFilters(targetList.getLeadFilterList());
		if (leadFilers != null) {
			List<Lead> leads = leadRepo.all().filter(leadFilers).fetch();
			response.setView(ActionView.define(I18n.get("Leads"))
					.model(Lead.class.getName())
					.add("grid", "lead-grid")
					.add("form", "lead-form")
					.domain("self in (:_leads)")
					.context("_leads", leads)
					.map());
		}
	}
	
	public void openFilteredPartners(ActionRequest request, ActionResponse response) {
		
		TargetList targetList = request.getContext().asType(TargetList.class);
		
		String partnerFilters = filterService.getJpqlFilters(targetList.getPartnerFilterList());
		if (partnerFilters != null) {
			List<Partner> partners = partnerRepo.all().filter(partnerFilters).fetch();
			response.setView(ActionView.define(I18n.get("Partners"))
					.model(Partner.class.getName())
					.add("grid", "partner-grid")
					.add("form", "partner-form")
					.domain("self in (:_partners)")
					.context("_partners", partners)
					.map());
		}
		
	}
}
