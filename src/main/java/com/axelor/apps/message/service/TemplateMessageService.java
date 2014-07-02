/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2012-2014 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.message.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.ReportSettings;
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
		String addressBlock= "";
		int mediaTypeSelect;
		
		if(template.getContent() != null)  {
			//Set template
			maker.setTemplate(template.getContent());
			//Make it
			content = maker.make();
		}
		
		
		if(template.getAddressBlock() != null)  {
			maker.setTemplate(template.getAddressBlock());
			//Make it
			addressBlock = maker.make();
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
		
		mediaTypeSelect=template.getMediaTypeSelect();
		
		String filePath = null;
		BirtTemplate birtTemplate = template.getBirtTemplate();
		if(birtTemplate != null)  {
			filePath = this.generatePdfFromBirtTemplate(maker, birtTemplate, "filePath");
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
				filePath,
				addressBlock,
				mediaTypeSelect
				);
		
		return message;
		
	}
	
	public List<EmailAddress> getEmailAddress(String recipients)  {
		
		List<EmailAddress> emailAddressList = Lists.newArrayList(); 
		
		if(recipients!=null && !recipients.isEmpty())  {
			String[] toTab = recipients.split(";");
			for(String s : toTab)  {
				EmailAddress emailAddress = EmailAddress.findByAddress(s);
				if(emailAddress != null)  {
					emailAddressList.add(emailAddress);
				}
			}
		}
		
		return emailAddressList;
	}
	
	
	public Map<String,String> generatePdfFile(TemplateMaker maker, String name, String modelPath, String generatedFilePath, String format, List<BirtTemplateParameter> birtTemplateParameterList) throws AxelorException {
		Map<String,String> result = new HashMap<String,String>();
		if(modelPath != null && !modelPath.isEmpty())  {
			
			ReportSettings reportSettings = new ReportSettings(modelPath, format);
			
			for(BirtTemplateParameter birtTemplateParameter : birtTemplateParameterList)  {
				
				maker.setTemplate(birtTemplateParameter.getValue());

				reportSettings.addParam(birtTemplateParameter.getName(), maker.make());
			}
			
			String url = reportSettings.getUrl();
			
			LOG.debug("URL : {}", url);
			String urlNotExist = URLService.notExist(url.toString());
			if (urlNotExist != null){
				throw new AxelorException(String.format("%s : Le chemin vers le template Birt est incorrect", 
						GeneralService.getExceptionMailMsg()), IException.CONFIGURATION_ERROR);
			}
			result.put("url",url);
			final int random = new Random().nextInt();
			String filePath = generatedFilePath;
			String fileName = name+"_"+random+"."+format;
			
			try {
				URLService.fileDownload(url, filePath, fileName);
			} catch (IOException e) {
				throw new AxelorException(String.format("%s : Erreur lors de l'édition du fichier : \n %s", 
						GeneralService.getExceptionMailMsg(), e), IException.CONFIGURATION_ERROR);
			}
			
			result.put("filePath",filePath+fileName);
			
		}
		return result;
	}
	
	public String generatePdfFromBirtTemplate(TemplateMaker maker, BirtTemplate birtTemplate, String value) throws AxelorException{
		Map<String,String> result =  this.generatePdfFile(
				maker, 
				birtTemplate.getName(),
				birtTemplate.getTemplateLink(), 
				birtTemplate.getGeneratedFilePath(), 
				birtTemplate.getFormat(), 
				birtTemplate.getBirtTemplateParameterList());
		if(result != null)
			return result.get(value);
		return null;
	}
	
}
