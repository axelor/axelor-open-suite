/**
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the “License”); you may not use
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
 * Software distributed under the License is distributed on an “AS IS”
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
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 */
package com.axelor.apps.message.service;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Template;
import com.axelor.apps.base.service.template.TemplateService;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.service.MessageService;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.tool.template.TemplateMaker;
import com.google.inject.Inject;

public class TemplateMessageService {

	private static final Logger LOG = LoggerFactory.getLogger(TemplateMessageService.class); 

	@Inject
	private TemplateService templateService;
	
	@Inject
	private MessageService messageService;
	
	
	
	
	public Message generateMessage(Object object, long objectId, String model, String tag, Template template) throws ClassNotFoundException, InstantiationException, IllegalAccessException  {
		
		
		System.out.println("model : "+model);
		System.out.println("tag : "+tag);
		System.out.println("object id : "+objectId);
		System.out.println("object : "+object);
		System.out.println("object.getClass().getSimpleName() : "+object.getClass().getSimpleName());
		
		//Init the maker
		TemplateMaker maker = new TemplateMaker(new Locale("fr"), '$', '$');
		//Set template
		maker.setTemplate(template.getContent());
		
		
		Class<? extends Model> myClass = (Class<? extends Model>) Class.forName( model );

		//Set context
		maker.setContext(JPA.find(myClass.newInstance().getClass(), objectId), tag);
		//Make it
		String content = maker.make();
		
		
		maker.setTemplate(template.getSubject());
		String subject = maker.make();
		
		Message message = messageService.createMessage(model, new Long(objectId).intValue(), subject, content);
		
		return message;
		
	}
	

	
}

