/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://license.axelor.com/.
 *
 * The License is based on the Mozilla Public License Version 1.1 but
 * Sections 14 and 15 have been added to cover use of software over a
 * computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be
 * consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is part of "Axelor Business Suite", developed by
 * Axelor exclusively.
 *
 * The Original Developer is the Initial Developer. The Initial Developer of
 * the Original Code is Axelor.
 *
 * All portions of the code written by Axelor are
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 */
package com.axelor.apps.account.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.service.payment.PayboxService;
import com.axelor.apps.account.service.payment.paymentvoucher.PaymentVoucherPayboxService;
import com.axelor.apps.base.db.Partner;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class PayboxController {

	@Inject
	private Provider<PaymentVoucherPayboxService> paymentVoucherPayboxProvider;
	
	@Inject
	private Provider<PayboxService> ps;
	
	private static final Logger LOG = LoggerFactory.getLogger(PayboxController.class);
	
	public void paybox(ActionRequest request, ActionResponse response)  {
		
		PaymentVoucher paymentVoucher = request.getContext().asType(PaymentVoucher.class);
		
		try {
			Partner partner = paymentVoucher.getPartner();
			
			if ((partner.getEmailAddress().getAddress() != null && !partner.getEmailAddress().getAddress().isEmpty()) ||
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
                
                String retourVars = paymentVoucher.getCompany().getAccountConfig().getPayboxConfig().getPayboxRetour();
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
                        paymentVoucherPayboxProvider.get().authorizeConfirmPaymentVoucher(paymentVoucher, (String) context.get("idtrans"), (String) context.get("montant"));
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
