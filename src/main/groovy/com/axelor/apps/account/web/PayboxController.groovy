package com.axelor.apps.account.web

import groovy.util.logging.Slf4j

import com.axelor.apps.account.db.PaymentVoucher
import com.axelor.apps.account.service.payment.PayboxService
import com.axelor.apps.account.service.payment.PaymentVoucherService
import com.axelor.apps.base.db.Partner
import com.axelor.exception.service.TraceBackService
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import com.google.inject.Inject
import com.google.inject.Provider
import com.axelor.rpc.Context


@Slf4j
class PayboxController {
	
	@Inject
	private Provider<PaymentVoucherService> pvs
	
	@Inject
	private Provider<PayboxService> ps

	
	def void paybox(ActionRequest request, ActionResponse response)  {
		
		PaymentVoucher paymentVoucher = request.context as PaymentVoucher
		
		try {
			Partner partner = paymentVoucher.partner
			
			if ((partner.email != null && !partner.email.isEmpty()) ||
			    (paymentVoucher.email != null && !paymentVoucher.email.isEmpty())  || 
				paymentVoucher.defaultEmailOk) {
			
				String url = ps.get().paybox(paymentVoucher)
				response.view = [
					"title": "Paiement par Paybox",
					"resource": url,
					"viewType": "html"
				]
			}
		}
		catch(Exception e)  { TraceBackService.trace(response, e) }
	}
	
	
	def void addPayboxEmail(ActionRequest request, ActionResponse response)  {
		
		PaymentVoucher paymentVoucher = request.context as PaymentVoucher
		
		try {
			
			ps.get().addPayboxEmail(paymentVoucher.partner, paymentVoucher.email, paymentVoucher.toSaveEmailOk)
			
		}
		catch(Exception e)  { TraceBackService.trace(response, e) }
	}
	
	
	 /**
     * Lancer le batch à travers un web service.
     *
     * @param request
     * @param response
     */
    def void webServicePaybox(ActionRequest request, ActionResponse response)  {
        
        Context context = request.context
        
        String idPaymentVoucher = context["idPV"] as String
        String operation = context["retour"] as String
        String signature = context["sign"] as String
        
        if ( idPaymentVoucher && operation && signature )  {
            
            log.debug("idPaymentVoucher :"+idPaymentVoucher)
            
            PaymentVoucher paymentVoucher = PaymentVoucher.find(Long.parseLong(idPaymentVoucher))
            log.debug("paymentVoucher :"+paymentVoucher)
            
            boolean verified = false
            
            if(paymentVoucher != null && paymentVoucher.company != null && !paymentVoucher.payboxPaidOk)  {
                
                List<String> varList = new ArrayList<String>()
                
                String retourVars = paymentVoucher.company.payboxRetour
                String[] retours = retourVars.split(";")
                
                varList.add("idPV=${idPaymentVoucher}")
                log.debug("idPV=${idPaymentVoucher}")
                varList.add("retour=${operation}")
                log.debug("retour=${operation}")
                for(int i = 0; i < retours.length - 1 ; i++)  {
                    String variableName = retours[i].split(":")[0]
                    String varValue = context.get(variableName)
                    String varBuilt = "${variableName}=${varValue}"
                    log.debug(varBuilt)
                    if(varValue != null)  {
                        varList.add(varBuilt)
                    }
                }
                
                verified = ps.get().checkPaybox(signature, varList, paymentVoucher.company)
                log.debug("L'adresse URL est-elle correcte ? : {}", verified)
            }
            if(verified)  {
                
                if ( operation == "1" && context["idtrans"] as String && context["montant"] as String )  {
                        pvs.get().authorizeConfirmPaymentVoucher(paymentVoucher, context["idtrans"] as String, context["montant"] as String)
                        response.flash = "Paiement réalisé"
                        log.debug("Paiement réalisé")
                }
                else if(operation == "2")  {
                        response.flash = "Paiement échoué"
                        log.debug("Paiement échoué")
                }
                else if(operation == "3")  {
                        response.flash = "Paiement annulé"
                        log.debug("Paiement annulé")
                }
            }
            else  {
                response.flash = "Retour d'information de Paybox erroné"
                log.debug("Retour d'information de Paybox erroné")
            }
            
            
        }
        
    }
	
}
