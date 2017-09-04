package com.axelor.apps.account.web;

import java.util.List;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.SubrogationRelease;
import com.axelor.apps.account.db.repo.SubrogationReleaseRepository;
import com.axelor.apps.account.service.SubrogationReleaseService;
import com.axelor.apps.base.db.Company;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class SubrogationReleaseController {

	private SubrogationReleaseRepository subrotationReleaseRepo;
	private SubrogationReleaseService subrogationReleaseService;

	@Inject
	public SubrogationReleaseController(SubrogationReleaseRepository subrotationReleaseRepo, SubrogationReleaseService subrogationReleaseService) {
		this.subrotationReleaseRepo = subrotationReleaseRepo;
		this.subrogationReleaseService = subrogationReleaseService;
	}

	public void retrieveInvoices(ActionRequest request, ActionResponse response) {
		SubrogationRelease subrogationRelease = request.getContext().asType(SubrogationRelease.class);
		Company company = subrogationRelease.getCompany();
		List<Invoice> releaseDetails = subrogationReleaseService.retrieveInvoices(company);
		response.setValue("releaseDetails", releaseDetails);
	}
	
	public void transmitRelease(ActionRequest request, ActionResponse response) {
		SubrogationRelease subrogationRelease = request.getContext().asType(SubrogationRelease.class);
		subrogationRelease = subrotationReleaseRepo.find(subrogationRelease.getId());
		subrogationReleaseService.transmitRelease(subrogationRelease);
		response.setReload(true);
	}

}
