/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://license.axelor.com/.
 *
 * The License is based on the Mozilla Public License Version 1.1 but
 * Sections 14 and 15 have been added to cover use of software over a
 * computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be
 * consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is part of "Axelor Business Suite", developed by
 * Axelor exclusively.
 *
 * The Original Developer is the Initial Developer. The Initial Developer of
 * the Original Code is Axelor.
 *
 * All portions of the code written by Axelor are
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 */
package com.axelor.apps.message.service;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.app.AppSettings;
import com.axelor.apps.AxelorSettings;
import com.axelor.apps.base.db.BirtTemplate;
import com.axelor.apps.base.db.BirtTemplateParameter;
import com.axelor.apps.base.db.Template;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.template.TemplateService;
import com.axelor.apps.message.db.EmailAddress;
import com.axelor.apps.message.db.MailAccount;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.tool.net.URLService;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.tool.template.TemplateMaker;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class TemplateMessageService {

	private static final Logger LOG = LoggerFactory.getLogger(TemplateMessageService.class); 

	@Inject
	private TemplateService templateService;
	
	@Inject
	private MessageService messageService;
	
	@Inject
	private MailAccountService mailAccountService;
	
	
	
	public Message generateMessage(Object object, long objectId, String model, String tag, Template template) throws ClassNotFoundException, InstantiationException, IllegalAccessException, AxelorException  {
		
		LOG.debug("model : "+model);
		LOG.debug("tag : "+tag);
		LOG.debug("object id : "+objectId);
		LOG.debug("object : "+object);
		
		//Init the maker
		TemplateMaker maker = new TemplateMaker(new Locale("fr"), '$', '$');
		
		Class<? extends Model> myClass = (Class<? extends Model>) Class.forName( model );

		//Set context
		maker.setContext(JPA.find(myClass.newInstance().getClass(), objectId), tag);
		
		
		String content = "";
		String subject = "";
		String toRecipients = "";
		String ccRecipients = "";
		String bccRecipients = "";
		
		
		if(template.getContent() != null)  {
			//Set template
			maker.setTemplate(template.getContent());
			//Make it
			content = maker.make();
		}
		
		MailAccount mailAccount = mailAccountService.getDefaultMailAccount();
		content += messageService.getSignature(mailAccount);
		
		if(template.getSubject() != null)  {
			maker.setTemplate(template.getSubject());
			subject = maker.make();
		}
		
		if(template.getToRecipients() != null)  {
			maker.setTemplate(template.getToRecipients());
			toRecipients = maker.make();
		}
		
		if(template.getCcRecipients() != null)  {
			maker.setTemplate(template.getCcRecipients());
			ccRecipients = maker.make();
		}
		
		if(template.getBccRecipients() != null)  {
			maker.setTemplate(template.getBccRecipients());
			bccRecipients = maker.make();
		}
		
		String filePath = null;
		BirtTemplate birtTemplate = template.getBirtTemplate();
		if(birtTemplate != null)  {
			filePath = this.generatePdfFile(
					maker, 
					birtTemplate.getName(),
					birtTemplate.getTemplateLink(), 
					birtTemplate.getGeneratedFilePath(), 
					birtTemplate.getFormat(), 
					birtTemplate.getBirtTemplateParameterList());
					
		}
		if(filePath == null)  {
			filePath = template.getFilePath();
		}
		
		JPA.clear();
		Message message = messageService.createMessage(
				model, 
				new Long(objectId).intValue(), 
				subject, 
				content, 
				this.getEmailAddress(toRecipients),
				this.getEmailAddress(ccRecipients),
				this.getEmailAddress(bccRecipients),
				mailAccount,
				filePath
				);
		
		return message;
		
	}
	
	public List<EmailAddress> getEmailAddress(String recipients)  {
		
		List<EmailAddress> emailAddressList = Lists.newArrayList(); 
		
		if(recipients!=null && !recipients.isEmpty())  {
			String[] toTab = recipients.split(";");
			for(String s : toTab)  {
				EmailAddress emailAddress = EmailAddress.all().filter("address = ?1",s).fetchOne();
				if(emailAddress != null)  {
					emailAddressList.add(emailAddress);
				}
			}
		}
		
		return emailAddressList;
	}
	
	
	public String generatePdfFile(TemplateMaker maker, String name, String modelPath, String generatedFilePath, String format, List<BirtTemplateParameter> birtTemplateParameterList) throws AxelorException {

		AppSettings appSettings = AppSettings.get();
		
		if(modelPath != null && !modelPath.isEmpty())  {
			
			String parameters = "";
			for(BirtTemplateParameter birtTemplateParameter : birtTemplateParameterList)  {
				
				maker.setTemplate(birtTemplateParameter.getValue());
				String value = maker.make();
				parameters += "&"+birtTemplateParameter.getName()+"="+value;
			}
			
			
			String url = appSettings.get("axelor.report.engine", "")+"/frameset?__report=report/"+modelPath+"&__format="+format+parameters+AxelorSettings.getAxelorReportEngineDatasource();
			
			LOG.debug("URL : {}", url);
			
			String urlNotExist = URLService.notExist(url.toString());
			if (urlNotExist != null){
				throw new AxelorException(String.format("%s : Le chemin vers le template Birt est incorrect", 
						GeneralService.getExceptionMailMsg()), IException.CONFIGURATION_ERROR);
			}
			final int random = new Random().nextInt();
			String filePath = generatedFilePath;
			String fileName = name+"_"+random+"."+format;
			
			try {
				URLService.fileDownload(url, filePath, fileName);
			} catch (IOException e) {
				throw new AxelorException(String.format("%s : Erreur lors de l'édition du fichier : \n %s", 
						GeneralService.getExceptionMailMsg(), e), IException.CONFIGURATION_ERROR);
			}
			
			return filePath+fileName;
			
		}
		return "";
		
	}
	
}

