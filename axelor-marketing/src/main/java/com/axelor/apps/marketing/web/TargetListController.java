package com.axelor.apps.marketing.web;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.crm.db.repo.LeadRepository;
import com.axelor.apps.marketing.db.TargetList;
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
	
	public void applyQuery(ActionRequest request, ActionResponse response) {
		
		TargetList targetList = request.getContext().asType(TargetList.class);
		
		String partnerFilters = filterService.getSqlFitlers(targetList.getPartnerFilterList());
		if (partnerFilters != null) {
			List<Partner> partners = partnerRepo.all().filter(partnerFilters).fetch();
			Set<Partner> partnerSet = new HashSet<Partner>();
			partnerSet.addAll(partners);
			response.setValue("partnerSet", partnerSet);
		}
		
		String leadFilers = filterService.getSqlFitlers(targetList.getLeadFilterList());
		if (leadFilers != null) {
			List<Lead> leads = leadRepo.all().filter(leadFilers).fetch();
			Set<Lead> leadSet = new HashSet<Lead>();
			leadSet.addAll(leads);
			response.setValue("leadSet", leadSet);
		}
	}
}
