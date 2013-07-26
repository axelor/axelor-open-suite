package com.axelor.apps.base.web;

import javax.inject.Inject;

import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class SequenceController {
	@Inject
	private SequenceService sequenceService;

	public void resetAll(ActionRequest request, ActionResponse response){
		
		sequenceService.resetSequenceAll();
		response.setReload(true);
		
	}

	public void reset(ActionRequest request, ActionResponse response){
		
		Sequence sequence = request.getContext().asType(Sequence.class);
		sequenceService.resetSequence(sequence);
				
		response.setValue("nextNum", sequence.getNextNum());
		
	}
}
