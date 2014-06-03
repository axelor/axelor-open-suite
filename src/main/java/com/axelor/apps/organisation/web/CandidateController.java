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
package com.axelor.apps.organisation.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.ReportSettings;
import com.axelor.apps.base.db.Keyword;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.organisation.db.Candidate;
import com.axelor.apps.organisation.db.EvaluationLine;
import com.axelor.apps.organisation.db.RecuitmentProcessAdvancement;
import com.axelor.apps.organisation.report.IReport;
import com.axelor.apps.organisation.service.EmployeeService;
import com.axelor.apps.tool.net.URLService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.persist.Transactional;

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
		
		User user = AuthUtils.getUser();
		String language = user != null? (user.getLanguage() == null || user.getLanguage().equals(""))? "en" : user.getLanguage() : "en"; 

		url.append(
				new ReportSettings(IReport.CANDIDATE)
				.addParam("Locale", language)
				.addParam("CandidateId", candidate.getId().toString())
				.getUrl());
		
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
	
	@Transactional
	public void createRecruitmentProcessAdvancementLine(ActionRequest request, ActionResponse response) {

		Candidate candidate = request.getContext().asType(Candidate.class);
		
		if(candidate != null) {
			
			if(candidate.getRecruitmentProcessAdvancementList() == null) {
				candidate.setRecruitmentProcessAdvancementList(new ArrayList<RecuitmentProcessAdvancement>());
			}
			RecuitmentProcessAdvancement recruitmentProcessAdvancement = new RecuitmentProcessAdvancement();
			recruitmentProcessAdvancement.setCandidate(candidate);
			recruitmentProcessAdvancement.setStatusSelect(candidate.getRecruitmentStatusSelect());
			recruitmentProcessAdvancement.setDateT(GeneralService.getTodayDateTime().toLocalDateTime());
			recruitmentProcessAdvancement.setRecruitmentDate(candidate.getRecruitementDate());
			recruitmentProcessAdvancement.setNote(candidate.getNote());
			candidate.getRecruitmentProcessAdvancementList().add(recruitmentProcessAdvancement);
			candidate.save();
			response.setReload(true);
		}
	}
}
