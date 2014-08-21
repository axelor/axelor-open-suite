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
package com.axelor.apps.base.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.administration.ExportDbObjectService;
import com.axelor.db.JPA;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Joiner;
import com.google.inject.Inject;

public class GeneralController {
	
	private static final Logger LOG = LoggerFactory.getLogger(CurrencyService.class);
	
	@Inject
	private ExportDbObjectService eos;
	
	public Set<MetaField> setFields(String model) throws IOException {
		LOG.debug("Model: {}",model);
		Set<MetaField> fieldSet = new HashSet<MetaField>();
		List<String> fields = new ArrayList<String>();
		for(MetaField field :MetaField.all().filter("metaModel.fullName = ?1 AND (relationship = null OR relationship = 'ManyToOne')",model).fetch()){
			fieldSet.add(field);
			fields.add(field.getName());
		}
			
		LOG.debug("Fields set: {}",fields);
		return fieldSet;
	}
	
	@SuppressWarnings("unchecked")
	public void showDuplicate(ActionRequest request, ActionResponse response){
		String object = (String) request.getContext().get("object");
		LOG.debug("Duplicate record model: {}",object);
		List<String> joinList = new ArrayList<String>();
		joinList.add("m.id <> m1.id");
		for(HashMap<String,Object> field:(List<HashMap<String,Object>>) request.getContext().get("fieldsSet")){
			if((Boolean)field.get("selected"))
				joinList.add("m."+field.get("name")+" = m1."+field.get("name"));
			
		}
		if(joinList.size() > 1){
			LOG.debug("Duplicate record joinList: {}",joinList);
			Query query = JPA.em().createQuery("SELECT DISTINCT(m.id) FROM "+object+" m,"+object+" m1 WHERE "+Joiner.on(" AND ").join(joinList));
			String ids = Joiner.on(",").join(query.getResultList());
			if(ids.isEmpty())
				response.setFlash("No duplicate records found");
			else
				response.setView(ActionView
						  .define("Duplicate records")
						  .model(object)
						  .domain("self.id in ("+ids+")")
						  .map());
		}
		else 
			response.setFlash("Please select key fields to check duplicate");
	}
	
	public void exportObjects(ActionRequest request, ActionResponse response){
		MetaFile metaFile = eos.exportObject();
		if(metaFile == null){
			response.setFlash("Attachment directory OR Application source does not exist");
		}
		else {
			response.setView(ActionView
					  .define("Export Object")
					  .model("com.axelor.meta.db.MetaFile")
					  .add("form", "meta-files-form")
					  .add("grid", "meta-files-grid")
					  .param("forceEdit", "true")
					  .context("_showRecord", metaFile.getId().toString())
					  .map());
		}
	}	
}
