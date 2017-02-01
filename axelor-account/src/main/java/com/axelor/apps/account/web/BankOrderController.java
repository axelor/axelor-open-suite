/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.account.db.BankOrder;
import com.axelor.apps.account.db.EbicsUser;
import com.axelor.apps.account.db.repo.BankOrderRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.report.IReport;
import com.axelor.apps.account.service.bankorder.BankOrderMergeService;
import com.axelor.apps.account.service.bankorder.BankOrderService;
import com.axelor.auth.AuthUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class BankOrderController {
	
	private final Logger log = LoggerFactory.getLogger( getClass() );
	
	@Inject
	protected BankOrderService bankOrderService;
	
	@Inject
	protected BankOrderRepository bankOrderRepo;
	
	public void sign(ActionRequest request, ActionResponse response ) throws AxelorException{
		
		BankOrder bankOrder = request.getContext().asType(BankOrder.class);
		bankOrder = bankOrderRepo.find(bankOrder.getId());
		try {
			if (bankOrder.getFileToSend() == null) {
				bankOrderService.checkLines(bankOrder);
			}
			
			ActionViewBuilder confirmView = ActionView
					.define("Sign bank order")
					.model(BankOrder.class.getName())
					.add("form", "bank-order-sign-wizard-form")
					.param("popup", "reload")
					.param("show-toolbar", "false")
					.param("show-confirm", "false")
					.param("popup-save", "false")
					.param("forceEdit", "true")
					.context("_showRecord", bankOrder.getId());
			
			response.setView(confirmView.map());
		} catch (Exception e) {
			TraceBackService.trace(response, e);
		}
	}
	
	public void confirm(ActionRequest request, ActionResponse response ) {

		try {
			BankOrder bankOrder = request.getContext().asType(BankOrder.class);
			bankOrder = bankOrderRepo.find(bankOrder.getId());
			if(bankOrder != null)  { 
				if (bankOrder.getFileToSend() == null) {
					bankOrderService.checkLines(bankOrder);
				}
				bankOrderService.confirm(bankOrder);
				response.setReload(true);
			}
		} catch (Exception e) {
			TraceBackService.trace(response, e);
		}
	}
	
	public void validateWithoutSign(ActionRequest request, ActionResponse response ) {

		try {
			BankOrder bankOrder = request.getContext().asType(BankOrder.class);
			bankOrder = bankOrderRepo.find(bankOrder.getId());
			if(bankOrder != null)  { 
				bankOrderService.validate(bankOrder);
				response.setReload(true);
			}
		} catch (Exception e) {
			TraceBackService.trace(response, e);
		}
	}
	
	public void validate(ActionRequest request, ActionResponse  response) throws AxelorException{
		
		Context context = request.getContext();
		
		BankOrder bankOrder  = context.asType(BankOrder.class);
		bankOrder = bankOrderRepo.find(bankOrder.getId());

		try {
			
			EbicsUser ebicsUser = bankOrder.getEbicsUser();
			
			if (ebicsUser == null) {
				response.setError(I18n.get(IExceptionMessage.EBICS_MISSING_NAME));
			}  
			else  {
				if (context.get("password") == null)  {
					response.setError(I18n.get(IExceptionMessage.EBICS_WRONG_PASSWORD));
				}
				else  {
					String password = (String)context.get("password");
					if(!ebicsUser.getPassword().equals(password)){
						response.setValue("password", "");
						response.setError(I18n.get(IExceptionMessage.EBICS_WRONG_PASSWORD));
					}
					else{
						bankOrderService.validate(bankOrder);
					}
				}
			}
		} catch (Exception e) {
			TraceBackService.trace(response, e);
		}
	}
	
	public void print(ActionRequest request, ActionResponse response) throws AxelorException{
		
		BankOrder bankOrder = request.getContext().asType(BankOrder.class);
		
		String name = I18n.get("Bank Order")+" "+ bankOrder.getBankOrderSeq();
		
		String fileLink = ReportFactory.createReport(IReport.BANK_ORDER, name + "-${date}")
				.addParam("BankOrderId", bankOrder.getId())
				.addParam("Locale", AuthUtils.getUser().getLanguage())
				.generate()
				.getFileLink();

		log.debug("Printing " + name);
	
		response.setView(ActionView
				.define(name)
				.add("html", fileLink).map());
		
	}
	
	//called to check if there is a linked invoice payment to validate
	public void validatePayment(ActionRequest request, ActionResponse response ) {

		try {
			BankOrder bankOrder = request.getContext().asType(BankOrder.class);
			bankOrder = bankOrderRepo.find(bankOrder.getId());
			if(bankOrder != null){ 
				bankOrderService.validatePayment(bankOrder);
			}
		} catch (Exception e) {
			TraceBackService.trace(response, e);
		}
	}
	
	//called to check if there is a linked invoice payment to cancel
	public void cancelPayment(ActionRequest request, ActionResponse response ) {

		try {
			BankOrder bankOrder = request.getContext().asType(BankOrder.class);
			bankOrder = bankOrderRepo.find(bankOrder.getId());
			if(bankOrder != null){ 
				bankOrderService.cancelPayment(bankOrder);
			}
		} catch (Exception e) {
			TraceBackService.trace(response, e);
		}
	}
	
	
	@SuppressWarnings("unchecked")
	public void merge(ActionRequest request, ActionResponse response ) {

		try {
			
			List<Integer> listSelectedBankOrder = (List<Integer>) request.getContext().get("_ids");
			
			List<BankOrder> bankOrderList = Lists.newArrayList();
			if(listSelectedBankOrder != null)  {
				for(Integer bankOrderId : listSelectedBankOrder)  {
					
					BankOrder bankOrder = bankOrderRepo.find(bankOrderId.longValue());
					
					if(bankOrder != null)  {
						bankOrderList.add(bankOrder);
					}
					
				}
				
				BankOrder bankOrder = Beans.get(BankOrderMergeService.class).mergeBankOrderList(bankOrderList);
				
				response.setView(ActionView
					.define(I18n.get("Bank Order"))
					.model(BankOrder.class.getName())
					.add("form", "bank-order-form")
					.param("forceEdit", "true")
					.context("_showRecord", String.valueOf(bankOrder.getId())).map());
			}
			
		} catch (Exception e) {
			TraceBackService.trace(response, e);
		}
		
		
	}
}
