package com.axelor.apps.supplychain.web;

import java.math.BigDecimal;

import com.axelor.apps.supplychain.db.PurchaseOrder;
import com.axelor.apps.supplychain.db.PurchaseOrderLine;
import com.axelor.apps.supplychain.service.PurchaseOrderLineService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class PurchaseOrderLineController {

	@Inject
	private PurchaseOrderLineService purchaseOrderLineService;
	
	public void compute(ActionRequest request, ActionResponse response){
		
		PurchaseOrderLine purchaseOrderLine = request.getContext().asType(PurchaseOrderLine.class);
		BigDecimal exTaxTotal = BigDecimal.ZERO;
		
		try{
			if (purchaseOrderLine.getPrice() != null && purchaseOrderLine.getQty() != null){
				
				exTaxTotal = purchaseOrderLineService.computeAmount(purchaseOrderLine.getQty(), purchaseOrderLine.getPrice());
			}
			response.setValue("exTaxTotal", exTaxTotal);
		}
		catch(Exception e)  {
			response.setFlash(e.getMessage());
		}	
	}
	
	public void getProductInformation(ActionRequest request, ActionResponse response){
		
		PurchaseOrderLine purchaseOrderLine = request.getContext().asType(PurchaseOrderLine.class);
		
		PurchaseOrder purchaseOrder = purchaseOrderLine.getPurchaseOrder();
		
		if(purchaseOrder == null)  {
			purchaseOrder = request.getContext().getParentContext().asType(PurchaseOrder.class);
		}
			
		if(purchaseOrder != null && purchaseOrderLine.getProduct() != null)  {
			
			try  {
				response.setValue("vatLine", purchaseOrderLineService.getVatLine(purchaseOrder, purchaseOrderLine));
				response.setValue("price", purchaseOrderLineService.getUnitPrice(purchaseOrder, purchaseOrderLine));
				response.setValue("productName", purchaseOrderLine.getProduct().getName());
				response.setValue("unit", purchaseOrderLine.getProduct().getUnit());
			}
			catch(Exception e) {
				response.setFlash(e.getMessage());
			}
		}
	}
}
