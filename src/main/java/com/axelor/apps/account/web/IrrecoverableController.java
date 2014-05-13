/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the "License"); you may not use
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
 * Software distributed under the License is distributed on an "AS IS"
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
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 */
package com.axelor.apps.account.web;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.ReportSettings;
import com.axelor.apps.account.db.IReport;
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
			StringBuilder url = new StringBuilder();
			
			url.append(new ReportSettings(IReport.REPORT_IRRECOVERABLE, irrecoverable.getExportTypeSelect())
						.addParam("IrrecoverableID", irrecoverable.getId().toString())
						.getUrl());
			
			LOG.debug("URL : {}", url);

			Map<String,Object> mapView = new HashMap<String,Object>();
			mapView.put("title", "Passage en irrécouvrable "+irrecoverable.getName());
			mapView.put("resource", url);
			mapView.put("viewType", "html");
			response.setView(mapView);			
		}	
	}
}
