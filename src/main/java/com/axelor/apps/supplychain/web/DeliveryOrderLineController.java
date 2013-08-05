package com.axelor.apps.supplychain.web;

import com.axelor.apps.supplychain.db.DeliveryOrder;
import com.axelor.apps.supplychain.db.DeliveryOrderLine;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class DeliveryOrderLineController {

	public void getProductInformation(ActionRequest request, ActionResponse response){
		
		DeliveryOrderLine deliveryOrderLine = request.getContext().asType(DeliveryOrderLine.class);
		
		DeliveryOrder deliveryOrder = deliveryOrderLine.getDeliveryOrder();
		
		if(deliveryOrder == null)  {
			deliveryOrder = request.getContext().getParentContext().asType(DeliveryOrder.class);
		}
			
		if(deliveryOrder != null && deliveryOrderLine.getProduct() != null)  {
			
			try  {
				response.setValue("productName", deliveryOrderLine.getProduct().getName());
				response.setValue("unit", deliveryOrderLine.getProduct().getUnit());
			}
			catch(Exception e)  {
				response.setFlash(e.getMessage());
			}
		}
	}
}
