package com.axelor.apps.account.web;

import java.util.HashMap;
import java.util.Map;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLineReport;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class MoveLineReportControllerSimple {

	public void showMoveExported(ActionRequest request, ActionResponse response) {
		
		MoveLineReport moveLineReport = request.getContext().asType(MoveLineReport.class);
		Map<String,Object> mapView = new HashMap<String,Object>();
		mapView.put("title", "Ecritures exportées");
		mapView.put("resource", Move.class.getName());
		mapView.put("domain", "self.moveLineReport.id = "+moveLineReport.getId());
		response.setView(mapView);		
	}
}
