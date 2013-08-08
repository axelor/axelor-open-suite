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
import com.axelor.apps.tool.net.URLService;
import com.axelor.auth.db.User;
import com.axelor.exception.service.TraceBackService;
import com.axelor.meta.db.MetaUser;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class CandidateController {

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
