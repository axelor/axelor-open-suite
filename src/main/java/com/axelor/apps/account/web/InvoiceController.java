/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2012-2014 Axelor (<http://axelor.com>).
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.ReportSettings;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.report.IReport;
import com.axelor.apps.account.service.IrrecoverableService;
import com.axelor.apps.account.service.JournalService;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.tool.net.URLService;
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
		response.setFlash("Facture annulée");
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
			invoiceIds = invoiceIds.substring(0, invoiceIds.length()-1);	
			invoice = Invoice.find(new Long(lstSelectedPartner.get(0)));
		}else if(invoice.getId() != null){
			invoiceIds = invoice.getId().toString();			
		}
		
		System.out.println("SS" +invoiceIds);
		if(!invoiceIds.equals("")){
			System.out.println("INvoice ids. "+ invoiceIds);
			StringBuilder url = new StringBuilder();			
			String language;
			try{
				language = invoice.getPartner().getLanguageSelect() != null? invoice.getPartner().getLanguageSelect() : invoice.getCompany().getPrintingSettings().getLanguageSelect() != null ? invoice.getCompany().getPrintingSettings().getLanguageSelect() : "en" ;
			}catch (NullPointerException e){
				language = "en";
			}	 
			
			url.append(new ReportSettings(IReport.INVOICE)
						.addParam("InvoiceId", invoiceIds)
						.addParam("Locale", language)
						.addParam("__locale", "fr_FR")
						.getUrl());
			
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
				mapView.put("resource", "http://www.axelor.com");
				mapView.put("viewType", "html");
				mapView.put("target", "new");
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
