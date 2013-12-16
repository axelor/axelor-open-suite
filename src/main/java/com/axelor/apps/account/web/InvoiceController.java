/**
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the “License”); you may not use
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
 * Software distributed under the License is distributed on an “AS IS”
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
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 */
package com.axelor.apps.account.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.AxelorSettings;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.service.IrrecoverableService;
import com.axelor.apps.account.service.JournalService;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.tool.net.URLService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class InvoiceController {

	@Inject
	private Provider<InvoiceService> is;

	@Inject
	private Provider<IrrecoverableService> ics;
	
	@Inject
	private Provider<JournalService> js;
	
	private static final Logger LOG = LoggerFactory.getLogger(InvoiceController.class);
	
	/**
	 * Fonction appeler par le bouton calculer
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	public void compute(ActionRequest request, ActionResponse response) {

		Invoice invoice = request.getContext().asType(Invoice.class);
		invoice = Invoice.find(invoice.getId());

		try{
			is.get().compute(invoice);
			response.setReload(true);
		}
		catch(Exception e)  {
			TraceBackService.trace(response, e);
		}
	}
	
	/**
	 * Fonction appeler par le bouton valider
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	public void validate(ActionRequest request, ActionResponse response) {

		Invoice invoice = request.getContext().asType(Invoice.class);
		invoice = Invoice.find(invoice.getId());

		try{
			is.get().validate(invoice);
			response.setReload(true);
		}
		catch(Exception e)  {
			TraceBackService.trace(response, e);
		}
	}
	
	/**
	 * Fonction appeler par le bouton ventiler
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	public void ventilate(ActionRequest request, ActionResponse response) {

		Invoice invoice = request.getContext().asType(Invoice.class);
		invoice = Invoice.find(invoice.getId());

		try {
			is.get().ventilate(invoice);
			response.setReload(true);
		}
		catch(Exception e)  {
			TraceBackService.trace(response, e);
		}
	}
	
	/**
	 * Passe l'état de la facture à "annulée"
	 * @param request
	 * @param response
	 * @throws AxelorException 
	 */
	public void cancel(ActionRequest request, ActionResponse response) throws AxelorException {

		Invoice invoice = request.getContext().asType(Invoice.class);
		invoice = Invoice.find(invoice.getId());

		is.get().cancel(invoice);
		response.setFlash("Facture "+invoice.getStatus().getName());
		response.setReload(true);
	}
	
	/**
	 * Fonction appeler par le bouton générer un avoir.
	 * 
	 * @param request
	 * @param response
	 */
	public void createRefund(ActionRequest request, ActionResponse response) {

		Invoice invoice = request.getContext().asType(Invoice.class);

		try {
			is.get().createRefund(Invoice.find(invoice.getId()));
			response.setReload(true);
			response.setFlash("Avoir créé"); 
		}
		catch(Exception e)  {
			TraceBackService.trace(response, e);
		}
	}
	
	public void usherProcess(ActionRequest request, ActionResponse response) {

		Invoice invoice = request.getContext().asType(Invoice.class);
		invoice = Invoice.find(invoice.getId());

		try {
			is.get().usherProcess(invoice);
		}
		catch (Exception e){
			TraceBackService.trace(response, e);
		}
	}
	
	public void passInIrrecoverable(ActionRequest request, ActionResponse response)  {

		Invoice invoice = request.getContext().asType(Invoice.class);
		invoice = Invoice.find(invoice.getId());

		try  {
			ics.get().passInIrrecoverable(invoice, true);
			response.setReload(true);
		}
		catch(Exception e)  {
			TraceBackService.trace(response, e);
		}
	}
	
	public void notPassInIrrecoverable(ActionRequest request, ActionResponse response)  {

		Invoice invoice = request.getContext().asType(Invoice.class);
		invoice = Invoice.find(invoice.getId());

		try  {
			ics.get().notPassInIrrecoverable(invoice);
			response.setReload(true);
		}
		catch(Exception e)  {
			TraceBackService.trace(response, e);
		}
	}
	
	public void getJournal(ActionRequest request, ActionResponse response)  {
		
		Invoice invoice = request.getContext().asType(Invoice.class);

		try  {
			response.setValue("journal", js.get().getJournal(invoice));
		}
		catch(Exception e)  {
			TraceBackService.trace(response, e);
		}
	}
	
	
	
	/**
	 * Fonction appeler par le bouton imprimer
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	public void showInvoice(ActionRequest request, ActionResponse response) {

		Invoice invoice = request.getContext().asType(Invoice.class);
		String invoiceIds = "";

		@SuppressWarnings("unchecked")
		List<Integer> lstSelectedPartner = (List<Integer>) request.getContext().get("_ids");
		if(lstSelectedPartner != null){
			for(Integer it : lstSelectedPartner) {
				invoiceIds+= it.toString()+",";
			}
		}	
			
		if(!invoiceIds.equals("")){
			invoiceIds = "&InvoiceId="+invoiceIds.substring(0, invoiceIds.length()-1);	
			invoice = Invoice.find(new Long(lstSelectedPartner.get(0)));
		}else if(invoice.getId() != null){
			invoiceIds = "&InvoiceId="+invoice.getId();			
		}
		
		System.out.println("SS" +invoiceIds);
		if(!invoiceIds.equals("")){
			System.out.println("INvoice ids. "+ invoiceIds);
			StringBuilder url = new StringBuilder();			
			AxelorSettings axelorSettings = AxelorSettings.get();
			String language = invoice.getPartner().getLanguageSelect() != null? invoice.getPartner().getLanguageSelect() : invoice.getCompany().getPrintingSettings().getLanguageSelect() != null ? invoice.getCompany().getPrintingSettings().getLanguageSelect() : "en" ; 
	
			url.append(axelorSettings.get("axelor.report.engine", "")+"/frameset?__report=report/Invoice.rptdesign&__format=pdf&Locale="+language+invoiceIds+"&__locale=fr_FR"+axelorSettings.get("axelor.report.engine.datasource"));
	
			LOG.debug("URL : {}", url);
			
			String urlNotExist = URLService.notExist(url.toString());
			if (urlNotExist == null){
			
				LOG.debug("Impression de la facture "+invoice.getInvoiceId()+" : "+url.toString());
				
				String title = "Facture ";
				if(invoice.getInvoiceId() != null)  {
					title += invoice.getInvoiceId();
				}
				
				Map<String,Object> mapView = new HashMap<String,Object>();
				mapView.put("title", title);
				mapView.put("resource", url);
				mapView.put("viewType", "html");
				response.setView(mapView);		
			}
			else {
				response.setFlash(urlNotExist);
			}
		}else{
			response.setFlash("Please select the invoice(s) to print.");
		}	
	}
	
	
}
