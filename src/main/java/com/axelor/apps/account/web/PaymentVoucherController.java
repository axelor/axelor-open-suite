package com.axelor.apps.account.web;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.AxelorSettings;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.service.payment.PayboxService;
import com.axelor.apps.account.service.payment.PaymentVoucherService;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class PaymentVoucherController {

	@Inject
	private Provider<PaymentVoucherService> pvs;
	
	@Inject
	private Provider<PayboxService> pbs;
	
	@Inject
	private Provider<SequenceService> sgs;
	
	private static final Logger LOG = LoggerFactory.getLogger(PaymentVoucherController.class);
	
	//Called on onSave event
	public void paymentVoucherSetNum(ActionRequest request, ActionResponse response){
		
		LOG.debug("In paymentVoucherSetNum ....");
		PaymentVoucher paymentVoucher = request.getContext().asType(PaymentVoucher.class);
		
		if (paymentVoucher.getRef() == null || paymentVoucher.getRef().isEmpty()){
			
			Journal journal = paymentVoucher.getPaymentMode().getBankJournal();
			if(journal == null)  {
				response.setFlash("Merci de paramétrer un journal pour le mode de paiement "+paymentVoucher.getPaymentMode().getName());
			}
			else  {
				
				String num = sgs.get().getSequence(IAdministration.PAYMENT_VOUCHER,paymentVoucher.getCompany(), journal, false);
				
				if (num == null || num.isEmpty()){
					response.setFlash("Veuillez configurer une séquence de saisie paiement pour la société "+ paymentVoucher.getCompany().getName() +" et le journal "+journal.getName());
				}
				else  {	
					response.setValue("ref", num);					
				}
			}
		}
		LOG.debug("End paymentVoucherSetNum.");
	}
	
	// Loading move lines of the selected partner (1st O2M)
	public void loadMoveLines(ActionRequest request, ActionResponse response) {
		
		LOG.debug("In loadMoveLines ....");
		PaymentVoucher paymentVoucher = request.getContext().asType(PaymentVoucher.class);
		paymentVoucher = PaymentVoucher.find(paymentVoucher.getId());
		
		try {
			pvs.get().loadMoveLines(paymentVoucher);
			response.setReload(true);
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
		
		LOG.debug("End loadMoveLines.");
	}
	
	// Filling lines to pay (2nd O2M)
	public void loadSelectedLines(ActionRequest request, ActionResponse response) {
		
		LOG.debug("In loadSelectedLines ....");
		
		PaymentVoucher paymentVoucherContext = request.getContext().asType(PaymentVoucher.class);
		PaymentVoucher paymentVoucher = PaymentVoucher.find(paymentVoucherContext.getId());
			
		try {
			pvs.get().loadSelectedLines(paymentVoucher,paymentVoucherContext);
			response.setReload(true);
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
		
		LOG.debug("End loadSelectedLines.");
	}
	
	// Confirm the payment voucher
	public void confirmPaymentVoucher(ActionRequest request, ActionResponse response) {
		
		LOG.debug("In confirmPaymentVoucher ....");
		
		PaymentVoucher paymentVoucher = request.getContext().asType(PaymentVoucher.class);
		paymentVoucher = PaymentVoucher.find(paymentVoucher.getId());
		
		try{				
			pvs.get().confirmPaymentVoucher(paymentVoucher, false);
			
			response.setReload(true);	
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
		
		LOG.debug("End confirmPaymentVoucher.");
	}
	
	public void getPaymentScheduleLine(ActionRequest request, ActionResponse response)  {
		
		LOG.debug("In getPaymentScheduleLine");
		PaymentVoucher pv = request.getContext().asType(PaymentVoucher.class);
		
		if(pv.getInvoiceToPay() != null)  {
			
			Invoice invoicetoPay = Invoice.find(pv.getInvoiceToPay().getId());
			if(invoicetoPay.getPaymentSchedule() != null)  {
				response.setValue("paymentScheduleToPay", invoicetoPay.getPaymentSchedule());
				response.setValue("scheduleToPay", pvs.get().getPaymentScheduleLine(invoicetoPay.getPaymentSchedule()));
			}
			else  {
				response.setValue("paymentScheduleToPay", null);
				response.setValue("scheduleToPay", null);			
			}
		}
		else  {
			response.setValue("paymentScheduleToPay", null);
			response.setValue("scheduleToPay", null);			
		}
		
		LOG.debug("End getPaymentScheduleLine");
	}
	
	public void printPaymentVoucher(ActionRequest request, ActionResponse response) {
		
		PaymentVoucher paymentVoucher = request.getContext().asType(PaymentVoucher.class);
		StringBuilder url = new StringBuilder();
		AxelorSettings gieSettings = AxelorSettings.get();
		url.append(gieSettings.get("gie.report.engine","")+"/frameset?__report=report/PaymentVoucher.rptdesign&__format=pdf&PaymentVoucherId="+paymentVoucher.getId()+gieSettings.get("gie.report.engine.datasource"));
		//url.append("${gieSettings.get('gie.report.engine', '')}/frameset?__report=report/PaymentVoucher.rptdesign&__format=pdf&PaymentVoucherId=${paymentVoucher.id}${gieSettings.get('gie.report.engine.datasource')}")
		
		LOG.debug("url.."+url);
		
		Map<String,Object> mapView = new HashMap<String,Object>();
		mapView.put("title", "Reçu saisie paiement "+paymentVoucher.getReceiptNo());
		mapView.put("resource", url);
		mapView.put("viewType", "html");
		mapView.put("_showRecord", 6);
		response.setView(mapView);	
	}
	
//	// Confirm the payment voucher
//	def void isDebitToPay(ActionRequest request, ActionResponse response) {
//		
//		log.debug("In confirmPaymentVoucher ....")
//		
//		PaymentVoucher paymentVoucher = request.context as PaymentVoucher
//		paymentVoucher = PaymentVoucher.find(paymentVoucher.id)
//		
//		try{
//			
//			pvs.get().isDebitToPay(paymentVoucher)
//			
//			response.reload = true
//			
//		}
//		catch(Exception e)  { TraceBackService.trace(response, e) }
//		
//		log.debug("End confirmPaymentVoucher.")
//	}
	
}
