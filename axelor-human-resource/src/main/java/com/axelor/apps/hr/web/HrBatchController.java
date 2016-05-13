package com.axelor.apps.hr.web;

import com.axelor.apps.base.db.Batch;
import com.axelor.apps.hr.db.HrBatch;
import com.axelor.apps.hr.db.repo.HrBatchRepository;
import com.axelor.apps.hr.service.batch.HrBatchService;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class HrBatchController {

	@Inject
	HrBatchService hrBatchService;
	@Inject
	HrBatchRepository hrBatchRepo;
	
	
	/**
	 * Lancer le batch d'ajout de congés
	 *
	 * @param request
	 * @param response
	 * @throws AxelorException 
	 */
	public void actionLeaveManagement(ActionRequest request, ActionResponse response) throws AxelorException{

		HrBatch hrBatch = request.getContext().asType(HrBatch.class);

		Batch batch = hrBatchService.run(hrBatchRepo.find(hrBatch.getId()));

		if(batch != null)
			response.setFlash(batch.getComments());
		response.setReload(true);
	}
}
