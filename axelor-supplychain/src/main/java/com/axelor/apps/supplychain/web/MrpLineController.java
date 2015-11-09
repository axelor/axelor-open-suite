package com.axelor.apps.supplychain.web;

import com.axelor.apps.supplychain.db.MrpLine;
import com.axelor.apps.supplychain.db.repo.MrpLineRepository;
import com.axelor.apps.supplychain.service.MrpLineService;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class MrpLineController {
	
	@Inject
	protected MrpLineService mrpLineService;
	
	@Inject
	protected MrpLineRepository mrpLineRepository;
	
	
	public void generateProposal(ActionRequest request, ActionResponse response) throws AxelorException  {
		MrpLine mrpLine = request.getContext().asType(MrpLine.class);
		mrpLineService.generateProposal(mrpLineRepository.find(mrpLine.getId()));
		response.setReload(true);
	}
	
}
