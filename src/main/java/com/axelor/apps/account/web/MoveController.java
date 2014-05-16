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

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.service.MoveService;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class MoveController {
	
	@Inject
	private Provider<MoveService> moveService;
	
	@Inject
	private Provider<PeriodService> periodService;
	
	public void validate(ActionRequest request, ActionResponse response) {

		Move move = request.getContext().asType(Move.class);
		move = Move.find(move.getId());
		
		try {
			moveService.get().validate(move);
			response.setReload(true);
		}
		catch (Exception e){ TraceBackService.trace(response, e); }
	}
	
	public void getPeriod(ActionRequest request, ActionResponse response) {
		
		Move move = request.getContext().asType(Move.class);
	
		try {
			if(move.getDate() != null && move.getCompany() != null) {
				
				response.setValue("period", periodService.get().rightPeriod(move.getDate(), move.getCompany()));				
			}
			else {
				response.setValue("period", null);
			}
		}
		catch (Exception e){ TraceBackService.trace(response, e); }
	}
	
	public void generateReverse(ActionRequest request, ActionResponse response) {
		
		Move move = request.getContext().asType(Move.class);
		
		try {
			Move newMove = moveService.get().generateReverse(Move.find(move.getId()));
			Map<String, Object> viewMap = Maps.newHashMap();
			viewMap.put("title", "Account move");
			viewMap.put("resource", "com.axelor.apps.account.db.Move");
			viewMap.put("domain", "self.id = "+newMove.getId());
			response.setView(viewMap);
		}
		catch (Exception e){ TraceBackService.trace(response, e); }
	}
}
