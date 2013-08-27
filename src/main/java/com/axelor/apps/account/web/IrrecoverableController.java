package com.axelor.apps.account.web;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.AxelorSettings;
import com.axelor.apps.account.db.Irrecoverable;
import com.axelor.apps.account.service.IrrecoverableService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class IrrecoverableController {

	@Inject 
	private IrrecoverableService is;

	private static final Logger LOG = LoggerFactory.getLogger(IrrecoverableController.class);

	public void getIrrecoverable(ActionRequest request, ActionResponse response)  {

		Irrecoverable irrecoverable = request.getContext().asType(Irrecoverable.class);
		irrecoverable = Irrecoverable.find(irrecoverable.getId());

		try {
			is.getIrrecoverable(irrecoverable);
			response.setReload(true);
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }	
	}

	public void createIrrecoverableReport(ActionRequest request, ActionResponse response)  {

		Irrecoverable irrecoverable = request.getContext().asType(Irrecoverable.class);
		irrecoverable = Irrecoverable.find(irrecoverable.getId());

		try {
			is.createIrrecoverableReport(irrecoverable);
			response.setReload(true);
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}

	public void passInIrrecoverable(ActionRequest request, ActionResponse response)  {

		Irrecoverable irrecoverable = request.getContext().asType(Irrecoverable.class);
		irrecoverable = Irrecoverable.find(irrecoverable.getId());

		try {
			int anomaly = is.passInIrrecoverable(irrecoverable);
			
			response.setReload(true);
			
			response.setFlash("Traitement terminé - "+anomaly+" anomalies générées");
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}

	public void printIrrecoverable(ActionRequest request, ActionResponse response)  {

		Irrecoverable irrecoverable = request.getContext().asType(Irrecoverable.class);

		if(irrecoverable.getExportTypeSelect() == null) {
			response.setFlash("Veuillez selectionner un type d'impression"); 
		} 
		else {
			AxelorSettings gieSettings = AxelorSettings.get();
			StringBuilder url = new StringBuilder();
			
			url.append(gieSettings.get("gie.report.engine", "")+"/frameset?__report=report/Irrecoverable.rptdesign&__format="+irrecoverable.getExportTypeSelect()+"&IrrecoverableID="+irrecoverable.getId()+gieSettings.get("gie.report.engine.datasource"));
			LOG.debug("URL : {}", url);

			Map<String,Object> mapView = new HashMap<String,Object>();
			mapView.put("title", "Passage en irrécouvrable "+irrecoverable.getName());
			mapView.put("resource", url);
			mapView.put("viewType", "html");
			response.setView(mapView);			
		}	
	}
}
