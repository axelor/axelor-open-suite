package com.axelor.apps.organisation.web

import groovy.util.logging.Slf4j

import com.axelor.apps.organisation.db.Candidate
import com.axelor.apps.organisation.db.EvaluationLine
import com.axelor.exception.service.TraceBackService
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse

@Slf4j
class CandidateController {
	
	def void onSave(ActionRequest request, ActionResponse response)  {
		
		Candidate candidate = request.context as Candidate
		log.info("Saving {}", candidate.name)
		
		try {
			candidate.toolKeywordSet*.typeSelect = "0"
			candidate.fieldKeywordSet*.typeSelect = "1"
			candidate.jobKeywordSet*.typeSelect = "2"
			
			log.debug("toolKeywordSet= {}", candidate.toolKeywordSet)
			log.debug("fieldKeywordSet= {}", candidate.fieldKeywordSet)
			log.debug("jobKeywordSet= {}", candidate.jobKeywordSet)
			response.values = [toolKeywordSet : candidate.toolKeywordSet,
				fieldKeywordSet : candidate.fieldKeywordSet,
				jobKeywordSet : candidate.jobKeywordSet]
			
			
		}
		catch(Exception e)  { TraceBackService.trace(response, e) }
		
	}

	def void onNew(ActionRequest request, ActionResponse response)  {
		
		Candidate candidate = request.context as Candidate
		log.info("New candidate")
		
		try {

			
			def elList = [
				new EvaluationLine(label : "Technique", coefficient : 3),
				new EvaluationLine(label : "Anglais", coefficient : 2),
				new EvaluationLine(label : "Communication", coefficient : 2),
				new EvaluationLine(label : "Maturité", coefficient : 2),
				new EvaluationLine(label : "Potentiel", coefficient : 2),
				new EvaluationLine(label : "Management", coefficient : 2),
				new EvaluationLine(label : "Dynamisme", coefficient : 1)
			] 
			
			log.debug("elList= {}", elList)
			response.values = [evaluationLineList : elList]
		}
		catch(Exception e)  { TraceBackService.trace(response, e) }
		
	}

	
}
