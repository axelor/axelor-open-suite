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
package com.axelor.apps.organisation.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.AxelorSettings;
import com.axelor.apps.base.db.Keyword;
import com.axelor.apps.organisation.db.Candidate;
import com.axelor.apps.organisation.db.EvaluationLine;
import com.axelor.apps.organisation.service.EmployeeService;
import com.axelor.apps.tool.net.URLService;
import com.axelor.auth.db.User;
import com.axelor.exception.service.TraceBackService;
import com.axelor.meta.db.MetaUser;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class CandidateController {

	@Inject
	private Provider<EmployeeService> employeeService;
	
	
	private static final Logger LOG = LoggerFactory.getLogger(CandidateController.class);
	
	public void updateKeywordSet(Set<Keyword> keywordSet, String typeSelect) {
		
		Iterator<Keyword> it = keywordSet.iterator();
		while(it.hasNext()) {
			
			Keyword k = it.next();
			k.setTypeSelect(typeSelect);
		}
	}
	
	public void onSave(ActionRequest request, ActionResponse response)  {
		
		Candidate candidate = request.getContext().asType(Candidate.class);
		LOG.info("Saving {}", candidate.getName());
		
		try {
			updateKeywordSet(candidate.getToolKeywordSet(), "0");
			updateKeywordSet(candidate.getFieldKeywordSet(), "1");
			updateKeywordSet(candidate.getJobKeywordSet(), "2");
			
			LOG.debug("toolKeywordSet= {}", candidate.getToolKeywordSet());
			LOG.debug("fieldKeywordSet= {}", candidate.getFieldKeywordSet());
			LOG.debug("jobKeywordSet= {}", candidate.getJobKeywordSet());
			
			response.setValue("toolKeywordSet", candidate.getToolKeywordSet());
			response.setValue("fieldKeywordSet", candidate.getFieldKeywordSet());
			response.setValue("jobKeywordSet", candidate.getJobKeywordSet());
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}
	
	public EvaluationLine createEvaluationLine(String label, int coefficient) {
		
		EvaluationLine e = new EvaluationLine();
		e.setLabel(label);
		e.setCoefficient(coefficient);
		return e;
	}
	
	public void onNew(ActionRequest request, ActionResponse response)  {
				
		try {
			List<EvaluationLine> evaluationLineList = new ArrayList<EvaluationLine>();
			evaluationLineList.add(createEvaluationLine("Technique", 3));
			evaluationLineList.add(createEvaluationLine("Anglais", 2));
			evaluationLineList.add(createEvaluationLine("Communication", 2));
			evaluationLineList.add(createEvaluationLine("Maturité", 2));
			evaluationLineList.add(createEvaluationLine("Potentiel", 2));
			evaluationLineList.add(createEvaluationLine("Management", 2));
			evaluationLineList.add(createEvaluationLine("Dynamisme", 1));
			
			LOG.debug("elList= {}", evaluationLineList);
			response.setValue("evaluationLineList", evaluationLineList);
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}
	
	
	public void transformInEmployee(ActionRequest request, ActionResponse response) {
		
		Candidate candidate = request.getContext().asType(Candidate.class);
		
		employeeService.get().createEmployee(Candidate.find(candidate.getId()));
		
		response.setReload(true);
	}
	
	
	/**
	 * Fonction appeler par le bouton imprimer
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	public void showCandidate(ActionRequest request, ActionResponse response) {

		Candidate candidate = request.getContext().asType(Candidate.class);

		StringBuilder url = new StringBuilder();
		AxelorSettings axelorSettings = AxelorSettings.get();
		
		MetaUser metaUser = MetaUser.findByUser((User) request.getContext().get("__user__"));
		url.append(axelorSettings.get("axelor.report.engine", "")+"/frameset?__report=report/Candidate.rptdesign&__format=pdf&CandidateId="+candidate.getId()+"&Locale="+metaUser.getLanguage()+axelorSettings.get("axelor.report.engine.datasource"));
		
		LOG.debug("URL : {}", url);
		
		String urlNotExist = URLService.notExist(url.toString());
		if (urlNotExist == null){
		
			LOG.debug("Impression des informations sur le candidat "+candidate.getName()+" "+candidate.getFirstName()+" : "+url.toString());
			
			Map<String,Object> mapView = new HashMap<String,Object>();
			mapView.put("title", "Candidat "+candidate.getName()+" "+candidate.getFirstName());
			mapView.put("resource", url);
			mapView.put("viewType", "html");
			response.setView(mapView);		
		}
		else {
			response.setFlash(urlNotExist);
		}
	}
}
