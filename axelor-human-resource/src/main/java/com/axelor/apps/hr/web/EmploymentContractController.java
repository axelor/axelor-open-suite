package com.axelor.apps.hr.web;

import java.io.IOException;

import com.axelor.apps.hr.db.EmploymentContract;
import com.axelor.apps.hr.service.EmploymentContractService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class EmploymentContractController {
	@Inject
    private EmploymentContractService employmentContractService;

    public void addAmendment(ActionRequest request, ActionResponse response){

    	EmploymentContract employmentContract = request.getContext().asType(EmploymentContract.class);

		try {

			employmentContractService.addAmendment( employmentContractService.find( employmentContract.getId() ) );
	    	response.setFlash( String.format( "Contrat %s - avenant %s", employmentContract.getFullName(), employmentContract.getEmploymentContractVersion() ) );
			response.setReload(true);

		} catch (IOException e) {
			TraceBackService.trace(response, e);
		}

    }
}
