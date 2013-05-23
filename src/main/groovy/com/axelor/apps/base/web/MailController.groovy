package com.axelor.apps.base.web

import groovy.util.logging.Slf4j

import com.axelor.apps.AxelorSettings
import com.axelor.apps.base.db.Mail
import com.axelor.apps.base.db.MailModel
import com.axelor.apps.tool.net.URLService
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
@Slf4j
class MailController {
	/**
	 * Fonction appeler par le bouton imprimer
	 * Permet d'ouvrir le mail généré au format pdf 
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	def void openMail(ActionRequest request, ActionResponse response) {

		Mail mail = request.context as Mail

		StringBuilder url = new StringBuilder()
		
		MailModel mailModel = mail.mailModel
		
		if(mail.pdfFilePath == null || mail.pdfFilePath.isEmpty())  {
			response.flash = "Aucun modèle de Courrier/Email de défini"
		}
		
		url.append(mail.pdfFilePath)
		
		log.debug("URL : {}", url)
		
		String urlNotExist = URLService.notExist(url.toString())
		if (urlNotExist == null){
		
			log.debug("Impression du mail ${mail.code} : ${url.toString()}")
		
			response.view = [
				"title": "Courrier/Email ${mail.code}",
				"resource": url,
				"viewType": "html"
			]
		
		}
		else {
			response.flash = urlNotExist
		}
	}
	
	
	/**
	 * Fonction appeler par le bouton imprimer
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	def void printMail(ActionRequest request, ActionResponse response) {

		Mail mail = request.context as Mail

		StringBuilder url = new StringBuilder()
		AxelorSettings gieSettings = AxelorSettings.get()
		
		MailModel mailModel = mail.mailModel
		
		if(mailModel == null )  {
			response.flash = "Aucun modèle de Courrier/Email de défini"
		}
		
		String pdfName = mailModel.pdfModelPath
		
		if(pdfName == null || pdfName.isEmpty())  {
			response.flash = "Aucun modèle d'impression Birt de défini dans le modèle de Courrier/Email"
		}
		
		url.append("${gieSettings.get('gie.report.engine', '')}/frameset?__report=report/${pdfName}&__format=pdf&MailId=${mail.id}&__locale=fr_FR${gieSettings.get('gie.report.engine.datasource')}")
		
		log.debug("URL : {}", url)
		
		String urlNotExist = URLService.notExist(url.toString())
		if (urlNotExist == null){
		
			log.debug("Impression du mail ${mail.code} : ${url.toString()}")
		
			response.view = [
				"title": "Courrier/Email ${mail.code}",
				"resource": url,
				"viewType": "html"
			]
		
		}
		else {
			response.flash = urlNotExist
		}
	}
	
}
