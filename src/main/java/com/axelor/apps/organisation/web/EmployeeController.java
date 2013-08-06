package com.axelor.apps.organisation.web;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.AxelorSettings;
import com.axelor.apps.organisation.db.Employee;
import com.axelor.apps.tool.net.URLService;
import com.axelor.auth.db.User;
import com.axelor.meta.db.MetaUser;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class EmployeeController {

	private static final Logger LOG = LoggerFactory.getLogger(EmployeeController.class);
	
	/**
	 * Fonction appeler par le bouton imprimer
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	public void showEmployee(ActionRequest request, ActionResponse response) {

		Employee employee = request.getContext().asType(Employee.class);

		StringBuilder url = new StringBuilder();
		AxelorSettings axelorSettings = AxelorSettings.get();
		
		MetaUser metaUser = MetaUser.findByUser((User) request.getContext().get("__user__"));
		url.append(axelorSettings.get("axelor.report.engine", "")+"/frameset?__report=report/Employee.rptdesign&__format=pdf&EmployeeId="+employee.getId()+"&Locale="+metaUser.getLanguage()+axelorSettings.get("axelor.report.engine.datasource"));

		LOG.debug("URL : {}", url);
		
		String urlNotExist = URLService.notExist(url.toString());
		if (urlNotExist == null){
		
			LOG.debug("Impression des informations sur l'employe "+employee.getName()+" "+employee.getFirstName()+" : "+url.toString());
			
			Map<String,Object> mapView = new HashMap<String,Object>();
			mapView.put("title", "Employee "+employee.getName()+" "+employee.getFirstName());
			mapView.put("resource", url);
			mapView.put("viewType", "html");
			response.setView(mapView);		
		}
		else {
			response.setFlash(urlNotExist);
		}
	}
}
