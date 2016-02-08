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
package com.axelor.apps.sale.web;

import java.io.IOException;

import org.eclipse.birt.core.exception.BirtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.SaleOrderService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class SaleOrderController {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Inject
	private SaleOrderService saleOrderService;
	
	@Inject
	private SaleOrderRepository saleOrderRepo;
	
	public void compute(ActionRequest request, ActionResponse response)  {

		SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
		
		try {
			saleOrder = saleOrderService.computeSaleOrder(saleOrder);
			response.setValues(saleOrder);
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}


	/**
	 * Method that print the sale order as a Pdf
	 *
	 * @param request
	 * @param response
	 * @return
	 * @throws BirtException 
	 * @throws IOException 
	 */
	public void showSaleOrder(ActionRequest request, ActionResponse response) throws AxelorException {

		SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);

		String language = saleOrderService.getLanguageForPrinting(saleOrder);
		
		String name = saleOrderService.getFileName(saleOrder);
		
		String fileLink = saleOrderService.getReportLink(saleOrder, name, language, ReportSettings.FORMAT_PDF); 

		logger.debug("Printing "+name);
	
		response.setView(ActionView
				.define(name)
				.add("html", fileLink).map());
	}

	public void exportSaleOrderExcel(ActionRequest request, ActionResponse response) throws AxelorException {

		SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);

		String language = saleOrderService.getLanguageForPrinting(saleOrder);

		String name = saleOrderService.getFileName(saleOrder);
		
		String fileLink = saleOrderService.getReportLink(saleOrder, name, language, ReportSettings.FORMAT_XLS); 

		logger.debug("Printing "+name);

		response.setView(ActionView
				.define(name)
				.add("html", fileLink).map());
	}



	public void exportSaleOrderWord(ActionRequest request, ActionResponse response) throws AxelorException {

		SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);

		String language = saleOrderService.getLanguageForPrinting(saleOrder);

		String name = saleOrderService.getFileName(saleOrder);
		
		String fileLink = saleOrderService.getReportLink(saleOrder, name, language, ReportSettings.FORMAT_DOC);
		

		logger.debug("Printing "+name);

		response.setView(ActionView
				.define(name)
				.add("html", fileLink).map());
	}

	public void cancelSaleOrder(ActionRequest request, ActionResponse response) {

		SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);

		saleOrderService.cancelSaleOrder(saleOrderRepo.find(saleOrder.getId()));

		response.setFlash("The sale order was canceled");
		response.setCanClose(true);

	}

	public void finalizeSaleOrder(ActionRequest request, ActionResponse response) throws Exception {

		SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);

		saleOrderService.finalizeSaleOrder(saleOrderRepo.find(saleOrder.getId()));

		response.setReload(true);

	}
	
	public void confirmSaleOrder(ActionRequest request, ActionResponse response) throws Exception {

		SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);

		saleOrderService.confirmSaleOrder(saleOrderRepo.find(saleOrder.getId()));

		response.setReload(true);

	}

	public void generateViewSaleOrder(ActionRequest request, ActionResponse response){
		SaleOrder context = request.getContext().asType(SaleOrder.class);
		context = saleOrderRepo.find(context.getId());
		response.setView(ActionView
	            .define("Sale Order")
	            .model(SaleOrder.class.getName())
	            .add("form", "sale-order-form-wizard")
	            .context("_idCopy", context.getId().toString())
	            .map());
	}

	public void generateViewTemplate(ActionRequest request, ActionResponse response){
		SaleOrder context = request.getContext().asType(SaleOrder.class);
		context = saleOrderRepo.find(context.getId());
		response.setView(ActionView
	            .define("Template")
	            .model(SaleOrder.class.getName())
	            .add("form", "sale-order-template-form-wizard")
	            .context("_idCopy", context.getId().toString())
	            .map());
	}

	public void createSaleOrder(ActionRequest request, ActionResponse response)  {
		SaleOrder origin = saleOrderRepo.find(Long.parseLong(request.getContext().get("_idCopy").toString()));
		SaleOrder copy = saleOrderService.createSaleOrder(origin);
		response.setValues(copy);
	}

	public void createTemplate(ActionRequest request, ActionResponse response)  {
		SaleOrder origin = saleOrderRepo.find(Long.parseLong(request.getContext().get("_idCopy").toString()));
		SaleOrder copy = saleOrderService.createTemplate(origin);
		response.setValues(copy);
	}

	public void computeEndOfValidityDate(ActionRequest request, ActionResponse response)  {

		SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);

		try {
			saleOrder = saleOrderService.computeEndOfValidityDate(saleOrder);
			response.setValue("endOfValidityDate", saleOrder.getEndOfValidityDate());
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}
	
	
}
