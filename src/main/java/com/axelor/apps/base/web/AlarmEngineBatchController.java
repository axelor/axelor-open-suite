package com.axelor.apps.base.web;

import java.util.HashMap;
import java.util.Map;

import com.axelor.apps.base.db.AlarmEngineBatch;
import com.axelor.apps.base.service.alarm.AlarmEngineBatchService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class AlarmEngineBatchController {

	@Inject
	private AlarmEngineBatchService alarmEngineBatchService;
	
	public void launch(ActionRequest request, ActionResponse response) {
		
		AlarmEngineBatch alarmEngineBatch = request.getContext().asType(AlarmEngineBatch.class);
		alarmEngineBatch = AlarmEngineBatch.find(alarmEngineBatch.getId());
		
		response.setFlash(alarmEngineBatchService.run(alarmEngineBatch).getComment());
		response.setReload(true);	
	}
	
	// WS
	public void run(ActionRequest request, ActionResponse response) throws AxelorException {
		
		AlarmEngineBatch alarmEngineBatch = AlarmEngineBatch.all().filter("self.code = ?1", request.getContext().get("code")).fetchOne();
		
		if (alarmEngineBatch == null) {
			TraceBackService.trace(new AxelorException("Batch d'alarme "+request.getContext().get("code"), 3));
		}
		else {
			Map<String,Object> mapData = new HashMap<String,Object>();
			mapData.put("anomaly", alarmEngineBatchService.run(alarmEngineBatch).getAnomaly());
			response.setData(mapData);
		}		
	}
}
