package com.axelor.apps.organisation.web

import groovy.util.logging.Slf4j

import com.axelor.apps.organisation.db.Candidate
import com.axelor.apps.organisation.db.EvaluationLine
import com.axelor.apps.AxelorSettings
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.exception.AxelorException
import com.axelor.apps.tool.net.URLService
import com.axelor.exception.service.TraceBackService
import com.axelor.meta.db.MetaUser
import com.axelor.auth.db.User
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

	/**
	 * Fonction appeler par le bouton imprimer
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	def void showCandidate(ActionRequest request, ActionResponse response) {

		Candidate candidate = request.context as Candidate

		StringBuilder url = new StringBuilder()
		AxelorSettings axelorSettings = AxelorSettings.get()
		
		MetaUser metaUser = MetaUser.findByUser(request.context.get("__user__"))
		url.append("${axelorSettings.get('axelor.report.engine', '')}/frameset?__report=report/Candidate.rptdesign&__format=pdf&CandidateId=${candidate.id}&Locale=${metaUser.language}${axelorSettings.get('axelor.report.engine.datasource')}")

		log.debug("URL : {}", url)
		
		String urlNotExist = URLService.notExist(url.toString())
		if (urlNotExist == null){
		
			log.debug("Impression des informations sur le candidat ${candidate.name} ${candidate.firstName} : ${url.toString()}")
			
			response.view = [
				"title": "Candidat ${candidate.name} ${candidate.firstName}",
				"resource": url,
				"viewType": "html"
			]
		
		}
		else {
			response.flash = urlNotExist
		}
	}
}
