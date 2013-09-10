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
package com.axelor.apps.message.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Template;
import com.axelor.apps.base.db.Wizard;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.service.TemplateMessageService;
import com.axelor.apps.tool.ObjectTool;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

public class GenerateMessageController {

	@Inject
	private TemplateMessageService salesOrderTemplateService;


	private static final Logger LOG = LoggerFactory.getLogger(GenerateMessageController.class);
	
	
	
	
	public void callMessageWizard(ActionRequest request, ActionResponse response)   {
		
		Object object = request.getContext().asType(Object.class);

		try {		
		
			long templateNumber = Template.all().filter("self.metaModel.fullName = ?1", 
					object.getClass().getCanonicalName()).count();
			
			LOG.debug("Template number : {} ", templateNumber);
			
			if(templateNumber > 1 || templateNumber == 0)  {

				Map<String,Object> context = new HashMap<String,Object>();
				context.put("_object", object);
				context.put("_tag", object.getClass().getSimpleName());
				context.put("_templateContextModel", object.getClass().getCanonicalName());

				Map<String, Object> map = Maps.newHashMap();
				map.put("name", "generate-so-message-wizard-form");
				map.put("type", "form");
				
				List<Object> items = Lists.newArrayList();
				items.add(map);
				
				Map<String,Object> view = new HashMap<String,Object>();
				view.put("title", "Select template ");
				view.put("resource", Wizard.class.getName());
				view.put("viewType", "form");
				view.put("views", items);
				view.put("name", "generate-so-message-wizard-form");
				view.put("context", context);

				response.setView(view);
			}
			else  {
				Object objectId = ObjectTool.getObject(object, "id");
				
				Long id = Long.parseLong(objectId.toString());
				
				response.setView(
						this.generateMessage(
								object, 
								id,
								object.getClass().getCanonicalName(),
								object.getClass().getSimpleName(),
								Template.all().filter("self.metaModel.fullName = ?1", 
										object.getClass().getCanonicalName()).fetchOne()));
			}
			
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}
	
	
	
	public void generateMessage(ActionRequest request, ActionResponse response)  {
		
		Context context = request.getContext();
		
		Map<String, Object> object = (Map<String, Object>) context.get("_object");
		
		Map<String, Object> templateContext = (Map<String, Object>) context.get("template");
		
		Integer objectId =  (Integer) object.get("id");
		
		String model = (String) context.get("_templateContextModel");
		
		String tag = (String) context.get("_tag");
		
		Template template = Template.find(((Integer)templateContext.get("id")).longValue());
		try {		
		
			response.setView(
					this.generateMessage(object, objectId.longValue(), model, tag, template));
			
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}
	
	
	public Map<String,Object> generateMessage(Object object, long objectId, String model, String tag, Template template) throws SecurityException, NoSuchFieldException, ClassNotFoundException, InstantiationException, IllegalAccessException  {
		
		LOG.debug("object : {} ", object);
		LOG.debug("template : {} ", template);
		LOG.debug("object id : {} ", objectId);
		LOG.debug("model : {} ", model);
		LOG.debug("tag : {} ", tag);
		
		Message message = salesOrderTemplateService.generateMessage(object, objectId, model, tag, template);

		Map<String,Object> context = new HashMap<String,Object>();
		context.put("_showRecord", message.getId());
		
		Map<String,Object> view = new HashMap<String,Object>();
		view.put("title", "Create message ");
		view.put("resource",Message.class.getName());
		view.put("viewType", "form");
		view.put("context", context);

		return view;
			
	}
	
	
}
