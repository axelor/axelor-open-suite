package com.axelor.apps.supplychain.web;

import java.math.BigDecimal;

import com.axelor.apps.supplychain.db.SalesOrder;
import com.axelor.apps.supplychain.db.SalesOrderLine;
import com.axelor.apps.supplychain.service.SalesOrderLineService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class SalesOrderLineController {

	@Inject
	private SalesOrderLineService salesOrderLineService;

	public void compute(ActionRequest request, ActionResponse response) {

		SalesOrderLine salesOrderLine = request.getContext().asType(SalesOrderLine.class);
		
		if(salesOrderLine != null) {
			BigDecimal exTaxTotal = BigDecimal.ZERO;

			try{
				if (salesOrderLine.getPrice() != null && salesOrderLine.getQty() != null) {
					if(salesOrderLine.getSalesOrderSubLineList() == null || salesOrderLine.getSalesOrderSubLineList().isEmpty()) {
						exTaxTotal = salesOrderLineService.computeAmount(salesOrderLine.getQty(), salesOrderLine.getPrice());
					}
				}
				response.setValue("exTaxTotal", exTaxTotal);
			}
			catch(Exception e) {
				response.setFlash(e.getMessage()); 
			}
		}
	}

	public void getProductInformation(ActionRequest request, ActionResponse response) {

		SalesOrderLine salesOrderLine = request.getContext().asType(SalesOrderLine.class);	

		if(salesOrderLine != null) {
			SalesOrder salesOrder = salesOrderLine.getSalesOrder();
			if(salesOrder == null)  {
				salesOrder = request.getContext().getParentContext().asType(SalesOrder.class);
			}

			if(salesOrder != null && salesOrderLine.getProduct() != null) {

				try  {
					response.setValue("vatLine", salesOrderLineService.getVatLine(salesOrder, salesOrderLine));
					response.setValue("price", salesOrderLineService.getUnitPrice(salesOrder, salesOrderLine));
					response.setValue("productName", salesOrderLine.getProduct().getName());
					response.setValue("saleSupplySelect", salesOrderLine.getProduct().getSaleSupplySelect());
					response.setValue("unit", salesOrderLine.getProduct().getUnit());
				}
				catch(Exception e)  {
					response.setFlash(e.getMessage()); 
				}
			}
		}
	}
}
