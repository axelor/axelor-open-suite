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
package com.axelor.apps.message.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Template;
import com.axelor.apps.base.db.Wizard;
import com.axelor.apps.base.db.repo.TemplateRepository;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.service.TemplateMessageService;
import com.axelor.apps.tool.ObjectTool;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

public class GenerateMessageController {

	@Inject
	private TemplateMessageService templateMessageService;
	
	@Inject
	private TemplateRepository templateRepo;

	private static final Logger LOG = LoggerFactory.getLogger(GenerateMessageController.class);
	
	public void callMessageWizard(ActionRequest request, ActionResponse response)   {
		
		Object object = request.getContext().asType(Object.class);

		try {		
		
			long templateNumber = templateRepo.all().filter("self.metaModel.fullName = ?1", 
					object.getClass().getCanonicalName()).count();
			
			LOG.debug("Template number : {} ", templateNumber);
			
			if(templateNumber == 0)  {
				response.setFlash("Veuillez configurer un template");
			}
			else if(templateNumber > 1 || templateNumber == 0)  {

				Map<String,Object> context = new HashMap<String,Object>();
				context.put("_object", object);
				context.put("_tag", object.getClass().getSimpleName());
				context.put("_templateContextModel", object.getClass().getCanonicalName());

				Map<String, Object> map = Maps.newHashMap();
				map.put("name", "generate-message-wizard-form");
				map.put("type", "form");
				
				List<Object> items = Lists.newArrayList();
				items.add(map);
				
				Map<String,Object> view = new HashMap<String,Object>();
				view.put("title", "Select template ");
				view.put("resource", Wizard.class.getName());
				view.put("viewType", "form");
				view.put("views", items);
				view.put("name", "generate-message-wizard-form");
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
								templateRepo.all().filter("self.metaModel.fullName = ?1", 
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
		
		Template template = templateRepo.find(((Integer)templateContext.get("id")).longValue());
		try {		
		
			response.setView(
					this.generateMessage(object, objectId.longValue(), model, tag, template));
			
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}
	
	
	public Map<String,Object> generateMessage(Object object, long objectId, String model, String tag, Template template) throws SecurityException, NoSuchFieldException, ClassNotFoundException, InstantiationException, IllegalAccessException, AxelorException  {
		
		LOG.debug("object : {} ", object);
		LOG.debug("template : {} ", template);
		LOG.debug("object id : {} ", objectId);
		LOG.debug("model : {} ", model);
		LOG.debug("tag : {} ", tag);
		
		Message message = templateMessageService.generateMessage(object, objectId, model, tag, template);

		Map<String,Object> context = new HashMap<String,Object>();
		context.put("_showRecord", message.getId());
		
		Map<String, Object> map = Maps.newHashMap();
		map.put("name", "message-form");
		map.put("type", "form");
		
		List<Object> items = Lists.newArrayList();
		items.add(map);
		
		Map<String,Object> view = new HashMap<String,Object>();
		view.put("title", "Create message ");
		view.put("resource",Message.class.getName());
		view.put("viewType", "form");
		view.put("views", items);
		view.put("name", "message-form");
		view.put("context", context);

		return view;
			
	}
	
	
}
