/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2014 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.web;

import java.util.List;
import java.util.Map;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.MoveService;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.collect.Maps;

public class MoveController {
	
	public void validate(ActionRequest request, ActionResponse response) {

		Move move = request.getContext().asType(Move.class);
		MoveService  moveService = Beans.get(MoveService.class);
		move = moveService.find(move.getId());
		
		try {
			moveService.validate(move);
			response.setReload(true);
		}
		catch (Exception e){ TraceBackService.trace(response, e); }
	}
	
	public void getPeriod(ActionRequest request, ActionResponse response) {
		
		Move move = request.getContext().asType(Move.class);
	
		try {
			if(move.getDate() != null && move.getCompany() != null) {
				
				response.setValue("period", Beans.get(PeriodService.class).rightPeriod(move.getDate(), move.getCompany()));				
			}
			else {
				response.setValue("period", null);
			}
		}
		catch (Exception e){ TraceBackService.trace(response, e); }
	}
	
	public void generateReverse(ActionRequest request, ActionResponse response) {
		
		Move move = request.getContext().asType(Move.class);
		MoveService  moveService = Beans.get(MoveService.class);
		
		try {
			Move newMove = moveService.generateReverse(moveService.find(move.getId()));
			Map<String, Object> viewMap = Maps.newHashMap();
			viewMap.put("title", "Account move");
			viewMap.put("resource", "com.axelor.apps.account.db.Move");
			viewMap.put("domain", "self.id = "+newMove.getId());
			response.setView(viewMap);
		}
		catch (Exception e){ TraceBackService.trace(response, e); }
	}
	
	@SuppressWarnings("unchecked")
	public void validateMultipleMoves(ActionRequest request, ActionResponse response){
		MoveService  moveService = Beans.get(MoveService.class);
		List<Long> moveIds = (List<Long>) request.getContext().get("_ids");
		if(!moveIds.isEmpty()){
			List<? extends Move> moveList = moveService.all().filter("self.id in ?1 AND self.state NOT IN ('validated','canceled')", moveIds).fetch();
			if(!moveList.isEmpty()){
				boolean error = moveService.validateMultiple(moveList);
				if(error)
					response.setFlash(I18n.get(IExceptionMessage.MOVE_VALIDATION_NOT_OK));
				else{
					response.setFlash(I18n.get(IExceptionMessage.MOVE_VALIDATION_OK));
					response.setReload(true);
				}
			}
			else response.setFlash(I18n.get(IExceptionMessage.NO_MOVES_SELECTED));
		}
		else response.setFlash(I18n.get(IExceptionMessage.NO_MOVES_SELECTED));
	}
}
