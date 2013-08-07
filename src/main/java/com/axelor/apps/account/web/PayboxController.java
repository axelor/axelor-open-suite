package com.axelor.apps.account.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.service.payment.PayboxService;
import com.axelor.apps.account.service.payment.PaymentVoucherService;
import com.axelor.apps.base.db.Partner;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class PayboxController {

	@Inject
	private Provider<PaymentVoucherService> pvs;
	
	@Inject
	private Provider<PayboxService> ps;
	
	private static final Logger LOG = LoggerFactory.getLogger(PayboxController.class);
	
	public void paybox(ActionRequest request, ActionResponse response)  {
		
		PaymentVoucher paymentVoucher = request.getContext().asType(PaymentVoucher.class);
		
		try {
			Partner partner = paymentVoucher.getPartner();
			
			if ((partner.getEmail() != null && !partner.getEmail().isEmpty()) ||
			    (paymentVoucher.getEmail() != null && !paymentVoucher.getEmail().isEmpty())  || 
				paymentVoucher.getDefaultEmailOk()) {
			
				String url = ps.get().paybox(paymentVoucher);
				
				Map<String,Object> mapView = new HashMap<String,Object>();
				mapView.put("title", "Paiement par Paybox");
				mapView.put("resource", url);
				mapView.put("viewType", "html");
				response.setView(mapView);
			}
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}
	
	public void addPayboxEmail(ActionRequest request, ActionResponse response)  {
		
		PaymentVoucher paymentVoucher = request.getContext().asType(PaymentVoucher.class);
		
		try {	
			ps.get().addPayboxEmail(paymentVoucher.getPartner(), paymentVoucher.getEmail(), paymentVoucher.getToSaveEmailOk());
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}
	
	/**
     * Lancer le batch à travers un web service.
     *
     * @param request
     * @param response
     */
	public void webServicePaybox(ActionRequest request, ActionResponse response) throws Exception {
        
        Context context = request.getContext();
        
        String idPaymentVoucher = (String) context.get("idPV");
        String operation = (String) context.get("retour");
        String signature = (String) context.get("sign");
        
        if (idPaymentVoucher != null && operation != null && signature != null) {
            
            LOG.debug("idPaymentVoucher :"+idPaymentVoucher);
            
            PaymentVoucher paymentVoucher = PaymentVoucher.find(Long.parseLong(idPaymentVoucher));
            LOG.debug("paymentVoucher :"+paymentVoucher);
            
            boolean verified = false;
            
            if(paymentVoucher != null && paymentVoucher.getCompany() != null && !paymentVoucher.getPayboxPaidOk()) {
                
                List<String> varList = new ArrayList<String>();
                
                String retourVars = paymentVoucher.getCompany().getPayboxRetour();
                String[] retours = retourVars.split(";");
                
                varList.add("idPV="+idPaymentVoucher);
                LOG.debug("idPV="+idPaymentVoucher);
                varList.add("retour="+operation);
                LOG.debug("retour="+operation);
                for(int i = 0; i < retours.length - 1 ; i++)  {
                    String variableName = retours[i].split(":")[0];
                    String varValue = (String) context.get(variableName);
                    String varBuilt = variableName+"="+varValue;
                    LOG.debug(varBuilt);
                    if(varValue != null)  {
                        varList.add(varBuilt);
                    }
                }
                verified = ps.get().checkPaybox(signature, varList, paymentVoucher.getCompany());
                LOG.debug("L'adresse URL est-elle correcte ? : {}", verified);
            }
            if(verified) {       
                if(operation == "1" && (String) context.get("idtrans") != null && (String) context.get("montant") != null ) {
                        pvs.get().authorizeConfirmPaymentVoucher(paymentVoucher, (String) context.get("idtrans"), (String) context.get("montant"));
                        response.setFlash("Paiement réalisé"); 
                        LOG.debug("Paiement réalisé");
                }
                else if(operation == "2") {
                        response.setFlash("Paiement échoué"); 
                        LOG.debug("Paiement échoué");
                }
                else if(operation == "3") {
                        response.setFlash("Paiement annulé");
                        LOG.debug("Paiement annulé");
                }
            }
            else  {
                response.setFlash("Retour d'information de Paybox erroné");
                LOG.debug("Retour d'information de Paybox erroné");
            }      
        }
    }
}
