package com.axelor.apps.account.web;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.account.db.BankOrder;
import com.axelor.apps.account.db.EbicsUser;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.db.repo.BankOrderRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.report.IReport;
import com.axelor.apps.account.service.bankOrder.BankOrderService;
import com.axelor.apps.base.db.Wizard;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class BankOrderController {
	
	private final Logger log = LoggerFactory.getLogger( getClass() );
	
	@Inject
	protected BankOrderService bankOrderService;
	
	@Inject
	protected BankOrderRepository bankOrderRepo;
	
	public void checkLines(ActionRequest request, ActionResponse response ) throws AxelorException{
		
		BankOrder bankOrder = request.getContext().asType(BankOrder.class);
		bankOrder = bankOrderRepo.find(bankOrder.getId());
		try {
			bankOrderService.checkLines(bankOrder);
			ActionViewBuilder confirmView = ActionView
					.define("Sign bank order")
					.model(Wizard.class.getName())
					.add("form", "bank-order-sign-wizard-form")
					.param("popup", "reload")
					.param("show-toolbar", "false")
					.param("popup-save", "false")
					.param("forceEdit", "true")
					.context("_contextBankOrder", bankOrder);
			
			response.setView(confirmView.map());
		} catch (Exception e) {
			TraceBackService.trace(response, e);
		}
	}
	
	public void updateAmount(ActionRequest request, ActionResponse response) throws AxelorException{
		
		BankOrder bankOrder = request.getContext().asType(BankOrder.class);
		try{
			response.setValue("amount", bankOrderService.updateAmount(bankOrder));
		} catch (Exception e) {
			TraceBackService.trace(response, e);
		}
	}
	
	public void send(ActionRequest request, ActionResponse response ) {

		try {
			BankOrder bankOrder = request.getContext().asType(BankOrder.class);
			bankOrder = bankOrderRepo.find(bankOrder.getId());
			if(bankOrder != null){ 
				bankOrderService.send(bankOrder);
				response.setReload(true);
			}
		} catch (Exception e) {
			TraceBackService.trace(response, e);
		}
	}
	
	public void sign(ActionRequest request, ActionResponse  response) throws AxelorException{
		
		BankOrder bankOrder  = JPA.em().find(BankOrder.class, new Long((Integer)((Map)request.getContext().get("_contextBankOrder")).get("id")));
		EbicsUser ebicsUser = new EbicsUser();
		String password = null;
		try {
			if (request.getContext().get("ebicsUser") == null) {
				response.setError(I18n.get(IExceptionMessage.EBICS_MISSING_NAME));
			}else	
			{
				ebicsUser = JPA.em().find(EbicsUser.class, new Long((Integer)((Map)request.getContext().get("ebicsUser")).get("id")));
				if (request.getContext().get("password") == null){
					response.setError(I18n.get(IExceptionMessage.EBICS_WRONG_PASSWORD));
				}
				else{
					password = (String)request.getContext().get("password");
					if(!ebicsUser.getPassword().equals(password)){
						response.setValue("password", "");
						response.setError(I18n.get(IExceptionMessage.EBICS_WRONG_PASSWORD));
					}
					else{
						bankOrderService.sign(bankOrder);
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
				.generate()
				.getFileLink();

		log.debug("Printing " + name);
	
		response.setView(ActionView
				.define(name)
				.add("html", fileLink).map());
		
	}
}
