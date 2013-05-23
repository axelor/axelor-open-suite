package com.axelor.apps.base.web

import javax.inject.Inject

import com.axelor.apps.base.db.Sequence
import com.axelor.apps.base.service.administration.SequenceService
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse

class SequenceController {
	
	@Inject
	private SequenceService sequenceService;

	def void resetAll(ActionRequest request, ActionResponse response){
		
		sequenceService.resetSequenceAll()
		response.reload = true
		
	}

	def void reset(ActionRequest request, ActionResponse response){
		
		Sequence sequence = request.context as Sequence
		sequenceService.resetSequence(sequence)
				
		response.values = [
			"nextNum":sequence.nextNum
		]
		
		
	}
			
}
