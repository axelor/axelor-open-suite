package com.axelor.apps.account.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.BankOrder;
import com.axelor.apps.account.service.bankOrder.BankOrderService;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class BankOrderController {
	
	private final Logger log = LoggerFactory.getLogger( getClass() );
	
	
	@Inject
	protected BankOrderService bankOrderService;
	
	
	public void validate(ActionRequest request, ActionResponse response ) throws AxelorException{
		
		BankOrder bankOrder = request.getContext().asType(BankOrder.class);
		
		log.debug("BANK ORDER LINES {}", bankOrder.getBankOrderLineList().size());
		log.debug("BANK ORDER TYPE {}", bankOrder.getOrderType());
		
		bankOrderService.validate(bankOrder);
		//TODO create sequence for bank order and bank order line
		//TODO validate fields
	}
}
