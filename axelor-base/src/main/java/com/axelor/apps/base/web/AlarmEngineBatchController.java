/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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

import java.util.HashMap;
import java.util.Map;

import com.axelor.apps.base.db.AlarmEngineBatch;
import com.axelor.apps.base.db.repo.AlarmEngineBatchRepository;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.service.alarm.AlarmEngineBatchService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class AlarmEngineBatchController {

	@Inject
	private AlarmEngineBatchService alarmEngineBatchService;

	@Inject
	private AlarmEngineBatchRepository alarmEngineBatchRepo;

	public void launch(ActionRequest request, ActionResponse response) {

		AlarmEngineBatch alarmEngineBatch = request.getContext().asType(AlarmEngineBatch.class);
		alarmEngineBatch = alarmEngineBatchRepo.find(alarmEngineBatch.getId());

		response.setFlash(alarmEngineBatchService.run(alarmEngineBatch).getComments());
		response.setReload(true);
	}

	// WS
	public void run(ActionRequest request, ActionResponse response) throws AxelorException {

		AlarmEngineBatch alarmEngineBatch = alarmEngineBatchRepo.findByCode("code");

		if (alarmEngineBatch == null) {
			TraceBackService.trace(new AxelorException(I18n.get(IExceptionMessage.ALARM_ENGINE_BATCH_5)+" "+request.getContext().get("code"), 3));
		}
		else {
			Map<String,Object> mapData = new HashMap<String,Object>();
			mapData.put("anomaly", alarmEngineBatchService.run(alarmEngineBatch).getAnomaly());
			response.setData(mapData);
		}
	}
}
