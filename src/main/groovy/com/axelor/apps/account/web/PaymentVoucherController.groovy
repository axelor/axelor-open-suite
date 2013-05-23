package com.axelor.apps.account.web

import groovy.util.logging.Slf4j

import com.axelor.apps.AxelorSettings
import com.axelor.apps.account.db.Invoice
import com.axelor.apps.account.db.Journal
import com.axelor.apps.account.db.PaymentVoucher
import com.axelor.apps.account.service.payment.PayboxService
import com.axelor.apps.account.service.payment.PaymentVoucherService
import com.axelor.apps.base.db.IAdministration
import com.axelor.apps.base.service.administration.SequenceService
import com.axelor.exception.service.TraceBackService
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import com.google.inject.Inject
import com.google.inject.Provider

@Slf4j
class PaymentVoucherController {
	
	@Inject
	private Provider<PaymentVoucherService> pvs
	
	@Inject
	private Provider<PayboxService> pbs
	
	@Inject
	private Provider<SequenceService> sgs
	
	//Called on onSave event
	def PaymentVoucher paymentVoucherSetNum(ActionRequest request, ActionResponse response){
		
		log.debug("In paymentVoucherSetNum ....")
		PaymentVoucher paymentVoucher = request.context as PaymentVoucher
		
		if (!paymentVoucher.ref){
			
			Journal journal = paymentVoucher.paymentMode.bankJournal
			if(!journal)  {
				response.flash = "Merci de paramétrer un journal pour le mode de paiement "+paymentVoucher.paymentMode.name
			}
			else  {
				
				String num = sgs.get().getSequence(IAdministration.PAYMENT_VOUCHER,paymentVoucher.company, journal, false)
				
				if (!num){
					
					response.flash = "Veuillez configurer une séquence de saisie paiement pour la société "+ paymentVoucher.company.name +" et le journal "+journal.name

				}
				else  {	
					
					response.values = ["ref":num]
					
				}
			}
		}
		
		log.debug("End paymentVoucherSetNum.")
	}
	
	
	// Loading move lines of the selected partner (1st O2M)
	def void loadMoveLines(ActionRequest request, ActionResponse response) {
		
		log.debug("In loadMoveLines ....")
		PaymentVoucher paymentVoucher = request.context as PaymentVoucher
		paymentVoucher = PaymentVoucher.find(paymentVoucher.id)
		
		try {
			
			pvs.get().loadMoveLines(paymentVoucher);
			response.reload = true;
			
		}
		catch(Exception e)  { TraceBackService.trace(response, e) }
		
		log.debug("End loadMoveLines.")
	}

	
	// Filling lines to pay (2nd O2M)
	def void loadSelectedLines(ActionRequest request, ActionResponse response) {
		
		log.debug("In loadSelectedLines ....")
		
		PaymentVoucher paymentVoucherContext = request.context as PaymentVoucher
		PaymentVoucher paymentVoucher = PaymentVoucher.find(paymentVoucherContext.id)
			
		try {
			
			PaymentVoucher pv = pvs.get().loadSelectedLines(paymentVoucher,paymentVoucherContext)

			response.reload = true
		}
		catch(Exception e)  { TraceBackService.trace(response, e) }
		
		log.debug("End loadSelectedLines.")
	}
	
	// Confirm the payment voucher
	def void confirmPaymentVoucher(ActionRequest request, ActionResponse response) {
		
		log.debug("In confirmPaymentVoucher ....")
		
		PaymentVoucher paymentVoucher = request.context as PaymentVoucher
		paymentVoucher = PaymentVoucher.find(paymentVoucher.id)
		
		try{			
			
			pvs.get().confirmPaymentVoucher(paymentVoucher)
			
			response.reload = true
			
		}
		catch(Exception e)  { TraceBackService.trace(response, e) }
		
		log.debug("End confirmPaymentVoucher.")
	}
	
	
	def void getPaymentScheduleLine(ActionRequest request, ActionResponse response)  {
		
		log.debug("In getPaymentScheduleLine")
		PaymentVoucher pv = request.context as PaymentVoucher
		
		if(pv.invoiceToPay != null)  {
			
			Invoice invoicetoPay = Invoice.find(pv.invoiceToPay.id)
			if(invoicetoPay.paymentSchedule != null)  {
				
				response.values = [
					"paymentScheduleToPay" : invoicetoPay.paymentSchedule,
					"scheduleToPay" : pvs.get().getPaymentScheduleLine(invoicetoPay.paymentSchedule)
				]
			}
			else  {
				
				response.values = [	"paymentScheduleToPay" : null,	"scheduleToPay" : null	]
				
			}
		}
		else  {
			
			response.values = [	"paymentScheduleToPay" : null,	"scheduleToPay" : null	]
			
		}
		
		log.debug("End getPaymentScheduleLine")
	}
	
	def void printPaymentVoucher(ActionRequest request, ActionResponse response) {
		
		PaymentVoucher paymentVoucher = request.context as PaymentVoucher
		StringBuilder url = new StringBuilder()
		AxelorSettings gieSettings = AxelorSettings.get()
		
		url.append("${gieSettings.get('gie.report.engine', '')}/frameset?__report=report/PaymentVoucher.rptdesign&__format=pdf&PaymentVoucherId=${paymentVoucher.id}${gieSettings.get('gie.report.engine.datasource')}")
		
		log.debug("url.."+url)

		response.view = [
			"title": "Reçu saisie paiement ${paymentVoucher.receiptNo}",
			"resource": url,
			"viewType": "html"
		]
	
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