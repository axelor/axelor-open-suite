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
package com.axelor.apps.production.web;

import java.io.IOException;
import java.util.List;

import org.eclipse.birt.core.exception.BirtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.exceptions.IExceptionMessage;
import com.axelor.apps.production.report.IReport;
import com.axelor.apps.production.service.ManufOrderService;
import com.axelor.apps.production.service.ManufOrderWorkflowService;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class ManufOrderController {

	
	private static final Logger LOG = LoggerFactory.getLogger(ManufOrderController.class);
	
	@Inject
	private ManufOrderWorkflowService manufOrderWorkflowService;
	
	@Inject
	private ManufOrderService manufOrderService;
	
	@Inject
	private ManufOrderRepository manufOrderRepo;
	
	
//	public void copyToConsume (ActionRequest request, ActionResponse response) {
//
//		ManufOrder manufOrder = request.getContext().asType( ManufOrder.class );
//
//		manufOrderService.copyToConsume(ManufOrder.find(manufOrder.getId()));
//		
//		response.setReload(true);
//		
//	}
	
	
//	public void copyToProduce (ActionRequest request, ActionResponse response) {
//	
//		ManufOrder manufOrder = request.getContext().asType( ManufOrder.class );
//
//		manufOrderService.copyToProduce(ManufOrder.find(manufOrder.getId()));
//		
//		response.setReload(true);
//		
//	}
	
	
	public void start (ActionRequest request, ActionResponse response) {
		
		Long manufOrderId = (Long)request.getContext().get("id");
		ManufOrder manufOrder = manufOrderRepo.find(manufOrderId);
		
		manufOrderWorkflowService.start(manufOrder);
		
		response.setReload(true);
		
	}
	
	public void pause (ActionRequest request, ActionResponse response) {
		
		Long manufOrderId = (Long)request.getContext().get("id");
		ManufOrder manufOrder = manufOrderRepo.find(manufOrderId);

		manufOrderWorkflowService.pause(manufOrder);
		
		response.setReload(true);
		
	}
	
	public void resume (ActionRequest request, ActionResponse response) {
		
		Long manufOrderId = (Long)request.getContext().get("id");
		ManufOrder manufOrder = manufOrderRepo.find(manufOrderId);

		manufOrderWorkflowService.resume(manufOrder);
		
		response.setReload(true);
		
	}
	
	public void finish (ActionRequest request, ActionResponse response) throws AxelorException {
		
		Long manufOrderId = (Long)request.getContext().get("id");
		ManufOrder manufOrder = manufOrderRepo.find(manufOrderId);

		manufOrderWorkflowService.finish(manufOrder);
		
		response.setReload(true);
		
	}
	
	public void cancel (ActionRequest request, ActionResponse response) throws AxelorException {
		
		Long manufOrderId = (Long)request.getContext().get("id");
		ManufOrder manufOrder = manufOrderRepo.find(manufOrderId);

		manufOrderWorkflowService.cancel(manufOrder);
		
		response.setReload(true);
		
	}
	
	public void plan (ActionRequest request, ActionResponse response) throws AxelorException {
		
		Long manufOrderId = (Long)request.getContext().get("id");
		ManufOrder manufOrder = manufOrderRepo.find(manufOrderId);

		manufOrderWorkflowService.plan(manufOrder);
		
		response.setReload(true);
		
	}
	
	
	
	/**
	 * Method that generate a Pdf file for an manufacturing order
	 *
	 * @param request
	 * @param response
	 * @return
	 * @throws BirtException 
	 * @throws IOException 
	 */
	public void print(ActionRequest request, ActionResponse response) throws AxelorException {

		ManufOrder manufOrder = request.getContext().asType( ManufOrder.class );
		String manufOrderIds = "";

		@SuppressWarnings("unchecked")
		List<Integer> lstSelectedManufOrder = (List<Integer>) request.getContext().get("_ids");
		if(lstSelectedManufOrder != null){
			for(Integer it : lstSelectedManufOrder) {
				manufOrderIds+= it.toString()+",";
			}
		}	
			
		if(!manufOrderIds.equals(""))  {
			manufOrderIds = manufOrderIds.substring(0, manufOrderIds.length()-1);	
			manufOrder = manufOrderRepo.find(new Long(lstSelectedManufOrder.get(0)));
		}else if(manufOrder.getId() != null)  {
			manufOrderIds = manufOrder.getId().toString();			
		}
		
		if(!manufOrderIds.equals(""))  {
			
			String name = I18n.get("Print");
			if(manufOrder.getManufOrderSeq() != null)  {
				name += lstSelectedManufOrder == null ? "OF "+manufOrder.getManufOrderSeq():"OFs";
			}
			
			String fileLink = ReportFactory.createReport(IReport.MANUF_ORDER, name+"-${date}")
					.addParam("Locale", manufOrderService.getLanguageToPrinting(manufOrder))
					.addParam("ManufOrderId", manufOrderIds)
					.generate()
					.getFileLink();

			LOG.debug("Printing "+name);
		
			response.setView(ActionView
					.define(name)
					.add("html", fileLink).map());
			
		}  
		else  {
			response.setFlash(I18n.get(IExceptionMessage.MANUF_ORDER_1));
		}	
	}
	
	
	public void preFillOperations (ActionRequest request, ActionResponse response) throws AxelorException {
		
		ManufOrder manufOrder = request.getContext().asType( ManufOrder.class );
		ManufOrderService moService = Beans.get(ManufOrderService.class);
		manufOrder  = manufOrderRepo.find(manufOrder.getId());
		moService.preFillOperations(manufOrder);
		response.setReload(true);
		
	}
	
}
