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
package com.axelor.apps.account.web;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.service.IrrecoverableService;
import com.axelor.apps.account.service.move.MoveLineService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Injector;

public class MoveLineController {
	
	@Inject
	private Injector injector;
	
	@Inject
	private MoveLineService moveLineService;
	
	@Inject
	private MoveLineRepository moveLineRepo;
	
	
	public void computeAnalyticDistribution(ActionRequest request, ActionResponse response){
		MoveLine moveLine = request.getContext().asType(MoveLine.class);
		Move move = moveLine.getMove();
		if(move == null){
			move = request.getContext().getParentContext().asType(Move.class);
			moveLine.setMove(move);
		}
		if(Beans.get(GeneralService.class).getGeneral().getManageAnalyticAccounting()){
			moveLine = moveLineService.computeAnalyticDistribution(moveLine);
			response.setValue("analyticDistributionLineList", moveLine.getAnalyticDistributionLineList());
		}
	}
	
	public void usherProcess(ActionRequest request, ActionResponse response) {
		
		MoveLine moveLine = request.getContext().asType(MoveLine.class);
		moveLine = moveLineRepo.find(moveLine.getId());
		
		MoveLineService mls = injector.getInstance(MoveLineService.class);
		
		try {
			mls.usherProcess(moveLine);
		}
		catch (Exception e){ TraceBackService.trace(response, e); }
	}
	
	public void passInIrrecoverable(ActionRequest request, ActionResponse response)  {
		
		MoveLine moveLine = request.getContext().asType(MoveLine.class);
		moveLine = moveLineRepo.find(moveLine.getId());
		
		IrrecoverableService is = injector.getInstance(IrrecoverableService.class);
		
		try  {
			is.passInIrrecoverable(moveLine, true, true);
			response.setReload(true);
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}
	
	public void notPassInIrrecoverable(ActionRequest request, ActionResponse response)  {
		
		MoveLine moveLine = request.getContext().asType(MoveLine.class);
		moveLine = moveLineRepo.find(moveLine.getId());
		
		IrrecoverableService is = injector.getInstance(IrrecoverableService.class);
		
		try  {
			is.notPassInIrrecoverable(moveLine, true);
			response.setReload(true);
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}
}
