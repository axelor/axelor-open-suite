package com.axelor.apps.organisation.web

import groovy.util.logging.Slf4j

import com.axelor.apps.organisation.db.Employee
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
class EmployeeController {

	/**
	 * Fonction appeler par le bouton imprimer
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	def void showEmployee(ActionRequest request, ActionResponse response) {

		Employee employee = request.context as Employee

		StringBuilder url = new StringBuilder()
		AxelorSettings axelorSettings = AxelorSettings.get()
		
		MetaUser metaUser = MetaUser.findByUser(request.context.get("__user__"))
		url.append("${axelorSettings.get('axelor.report.engine', '')}/frameset?__report=report/Employee.rptdesign&__format=pdf&EmployeeId=${employee.id}&Locale=${metaUser.language}${axelorSettings.get('axelor.report.engine.datasource')}")

		log.debug("URL : {}", url)
		
		String urlNotExist = URLService.notExist(url.toString())
		if (urlNotExist == null){
		
			log.debug("Impression des informations sur l'employe ${employee.name} ${employee.firstName} : ${url.toString()}")
			
			response.view = [
				"title": "Employee ${employee.name} ${employee.firstName}",
				"resource": url,
				"viewType": "html"
			]
		
		}
		else {
			response.flash = urlNotExist
		}
	}
}
