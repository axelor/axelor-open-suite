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
package com.axelor.apps.production.web;
/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the “License”); you may not use
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
 * Software distributed under the License is distributed on an “AS IS”
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


import javax.inject.Inject;

import org.joda.time.Duration;

import com.axelor.apps.crm.db.Event;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.service.OperationOrderService;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.persist.Transactional;

public class OperationOrderController {

	@Inject
	OperationOrderService operationOrderService;
	
//	public void copyToConsume (ActionRequest request, ActionResponse response) {
//
//		OperationOrder operationOrder = request.getContext().asType( OperationOrder.class );
//
//		operationOrderService.copyToConsume(OperationOrder.find(operationOrder.getId()));
//		
//		response.setReload(true);
//		
//	}
	
	
//	public void copyToProduce (ActionRequest request, ActionResponse response) {
//
//		OperationOrder operationOrder = request.getContext().asType( OperationOrder.class );
//
//		operationOrderService.copyToProduce(OperationOrder.find(operationOrder.getId()));
//		
//		response.setReload(true);
//		
//	}
	
	
	public void computeDuration(ActionRequest request, ActionResponse response) {
		
		OperationOrder operationOrder = request.getContext().asType( OperationOrder.class );
		
		if(operationOrder.getPlannedStartDateT() != null && operationOrder.getPlannedEndDateT() != null) {
			response.setValue("plannedDuration", 
					operationOrderService.getDuration(
							operationOrderService.computeDuration(operationOrder.getPlannedStartDateT(), operationOrder.getPlannedEndDateT())));
		}
		
		if(operationOrder.getRealStartDateT() != null && operationOrder.getRealEndDateT() != null) {
			response.setValue("realDuration", 
					operationOrderService.getDuration(
							operationOrderService.computeDuration(operationOrder.getRealStartDateT(), operationOrder.getRealEndDateT())));
		}
	}
	
	public void finish (ActionRequest request, ActionResponse response) throws AxelorException {

		OperationOrder operationOrder = request.getContext().asType( OperationOrder.class );

		operationOrderService.finish(OperationOrder.find(operationOrder.getId()));
		
		response.setReload(true);
		
	}
	
	
	
	
//	TODO A SUPPRIMER UNE FOIS BUG FRAMEWORK CORRIGE
	public void saveOperationOrder(ActionRequest request, ActionResponse response) throws AxelorException {
		OperationOrder operationOrder = request.getContext().asType( OperationOrder.class );
		OperationOrder persistOperationOrder = OperationOrder.find(operationOrder.getId());
		persistOperationOrder.setStatusSelect(operationOrder.getStatusSelect());
		persistOperationOrder.setRealStartDateT(operationOrder.getRealStartDateT());
		persistOperationOrder.setRealEndDateT(operationOrder.getRealEndDateT());
		
		this.saveOperationOrder(persistOperationOrder);
	}
	
	
	@Transactional
	public void saveOperationOrder(OperationOrder operationOrder){
		operationOrder.save();
	}
}
