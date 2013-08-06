package com.axelor.apps.supplychain.web;

import java.math.BigDecimal;

import com.axelor.apps.supplychain.db.SalesOrder;
import com.axelor.apps.supplychain.db.SalesOrderSubLine;
import com.axelor.apps.supplychain.service.SalesOrderSubLineService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class SalesOrderSubLineController {

	@Inject
	private SalesOrderSubLineService salesOrderSubLineService;

	public void compute(ActionRequest request, ActionResponse response){

		SalesOrderSubLine salesOrderSubLine = request.getContext().asType(SalesOrderSubLine.class);

		if(salesOrderSubLine != null) {
			BigDecimal exTaxTotal = BigDecimal.ZERO;

			try {
				if (salesOrderSubLine.getPrice() != null && salesOrderSubLine.getQty() != null) {
					exTaxTotal = salesOrderSubLineService.computeAmount(salesOrderSubLine.getQty(), salesOrderSubLine.getPrice());
				}
				response.setValue("exTaxTotal", exTaxTotal);
			}
			catch(Exception e)  {
				response.setFlash(e.getMessage());
			}
		}
	}

	public void getProductInformation(ActionRequest request, ActionResponse response){

		SalesOrderSubLine salesOrderSubLine = request.getContext().asType(SalesOrderSubLine.class);

		if(salesOrderSubLine != null) {
			SalesOrder salesOrder = null;

			if(salesOrderSubLine.getSalesOrderLine() != null && salesOrderSubLine.getSalesOrderLine().getSalesOrder() != null) {
				salesOrder = salesOrderSubLine.getSalesOrderLine().getSalesOrder();
			}
			if(salesOrder == null) {
				salesOrder = request.getContext().getParentContext().getParentContext().asType(SalesOrder.class);
			}

			if(salesOrder != null && salesOrderSubLine.getProduct() != null) {
				try  {
					response.setValue("vatLine", salesOrderSubLineService.getVatLine(salesOrder, salesOrderSubLine));
					response.setValue("price", salesOrderSubLineService.getUnitPrice(salesOrder, salesOrderSubLine));
					response.setValue("productName", salesOrderSubLine.getProduct().getName());
					response.setValue("unit", salesOrderSubLine.getProduct().getUnit());
				}
				catch(Exception e)  {
					response.setFlash(e.getMessage());
				}
			}
		}
	}
}
