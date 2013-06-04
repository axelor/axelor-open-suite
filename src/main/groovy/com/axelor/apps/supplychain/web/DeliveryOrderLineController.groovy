package com.axelor.apps.supplychain.web

import groovy.util.logging.Slf4j

import com.axelor.apps.supplychain.db.DeliveryOrder
import com.axelor.apps.supplychain.db.DeliveryOrderLine
import com.axelor.exception.service.TraceBackService

import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

class DeliveryOrderLineController {
	
	
	def void getProductInformation(ActionRequest request, ActionResponse response){
		
		DeliveryOrderLine deliveryOrderLine = request.context as DeliveryOrderLine
		
		DeliveryOrder deliveryOrder = deliveryOrderLine.deliveryOrder
		
		if(deliveryOrder == null)  {
			deliveryOrder = request.context.parentContext as DeliveryOrder
		}
			
		if(deliveryOrder != null && deliveryOrderLine.product != null)  {
			
			try  {
				response.values = ["productName" : deliveryOrderLine?.product?.name,
					"unit" : deliveryOrderLine?.product?.unit]
			}
			catch(Exception e)  {
				response.flash = e
			}
		}
	}
}
