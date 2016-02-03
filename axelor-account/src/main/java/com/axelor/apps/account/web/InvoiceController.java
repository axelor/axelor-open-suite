/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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

import java.io.IOException;
import java.util.List;

import org.eclipse.birt.core.exception.BirtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.report.IReport;
import com.axelor.apps.account.service.IrrecoverableService;
import com.axelor.apps.account.service.JournalService;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class InvoiceController {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Inject
	private InvoiceService invoiceService;
	
	@Inject
	private InvoiceRepository invoiceRepo;

	/**
	 * Fonction appeler par le bouton calculer
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	public void compute(ActionRequest request, ActionResponse response) {

		Invoice invoice = request.getContext().asType(Invoice.class);

		try{
			invoice = invoiceService.compute(invoice);
			response.setValues(invoice);
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
		invoice = invoiceRepo.find(invoice.getId());

		try{
			invoiceService.validate(invoice);
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
		invoice = invoiceRepo.find(invoice.getId());

		try {
			invoiceService.ventilate(invoice);
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
		invoice = invoiceRepo.find(invoice.getId());

		invoiceService.cancel(invoice);
		response.setFlash(I18n.get(IExceptionMessage.INVOICE_1));
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

			invoice = invoiceRepo.find(invoice.getId());
			Invoice refund = invoiceService.createRefund( invoice );
			response.setReload(true);
			response.setNotify(I18n.get(IExceptionMessage.INVOICE_2));

			response.setView ( ActionView.define( String.format(I18n.get(IExceptionMessage.INVOICE_4), invoice.getInvoiceId() ) )
			.model(Invoice.class.getName())
			.add("form", "invoice-form")
			.add("grid", "invoice-grid")
			.param("forceTitle", "true")
			.context("_showRecord", refund.getId().toString())
			.domain("self.originalInvoice.id = " + invoice.getId())
			.map() );
		}
		catch(Exception e)  {
			TraceBackService.trace(response, e);
		}
	}

	public void usherProcess(ActionRequest request, ActionResponse response) {

		Invoice invoice = request.getContext().asType(Invoice.class);
		invoice = invoiceRepo.find(invoice.getId());

		try {
			invoiceService.usherProcess(invoice);
		}
		catch (Exception e){
			TraceBackService.trace(response, e);
		}
	}

	public void passInIrrecoverable(ActionRequest request, ActionResponse response)  {

		Invoice invoice = request.getContext().asType(Invoice.class);
		invoice = invoiceRepo.find(invoice.getId());

		try  {
			Beans.get(IrrecoverableService.class).passInIrrecoverable(invoice, true);
			response.setReload(true);
		}
		catch(Exception e)  {
			TraceBackService.trace(response, e);
		}
	}

	public void notPassInIrrecoverable(ActionRequest request, ActionResponse response)  {

		Invoice invoice = request.getContext().asType(Invoice.class);
		invoice = invoiceRepo.find(invoice.getId());

		try  {
			Beans.get(IrrecoverableService.class).notPassInIrrecoverable(invoice);
			response.setReload(true);
		}
		catch(Exception e)  {
			TraceBackService.trace(response, e);
		}
	}

	public void getJournal(ActionRequest request, ActionResponse response)  {

		Invoice invoice = request.getContext().asType(Invoice.class);

		try  {
			response.setValue("journal", Beans.get(JournalService.class).getJournal(invoice));
		}
		catch(Exception e)  {
			TraceBackService.trace(response, e);
		}
	}



	/**
	 * Method to generate invoice as a Pdf
	 *
	 * @param request
	 * @param response
	 * @return
	 * @throws BirtException 
	 * @throws IOException 
	 */
	public void showInvoice(ActionRequest request, ActionResponse response) throws AxelorException {

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
			invoice = invoiceRepo.find(new Long(lstSelectedPartner.get(0)));
		}else if(invoice.getId() != null){
			invoiceIds = invoice.getId().toString();
		}

		if(!invoiceIds.equals("")){
			String language;
			try{
				language = invoice.getPartner().getLanguageSelect() != null? invoice.getPartner().getLanguageSelect() : invoice.getCompany().getPrintingSettings().getLanguageSelect() != null ? invoice.getCompany().getPrintingSettings().getLanguageSelect() : "en" ;
			}catch (NullPointerException e){
				language = "en";
			}

			String title = I18n.get("Invoice");
			if(invoice.getInvoiceId() != null)  {
				title += invoice.getInvoiceId();
			}
			
			String fileLink = ReportFactory.createReport(IReport.INVOICE, title+"-${date}")
					.addParam("InvoiceId", invoiceIds)
					.addParam("Locale", language)
					.addModel(invoice)
					.generate()
					.getFileLink();

			logger.debug("Printing "+title);
		
			response.setView(ActionView
					.define(title)
					.add("html", fileLink).map());
			
		}else{
			response.setFlash(I18n.get(IExceptionMessage.INVOICE_3));
		}
	}

	public void massValidation(ActionRequest request, ActionResponse response) {

		List<Integer> listSelectedInvoice = (List<Integer>) request.getContext().get("_ids");
		if(listSelectedInvoice != null){
			Invoice invoice = null;
			int count = 1;
			for(Integer invoiceId : listSelectedInvoice){
				invoice = invoiceRepo.find(invoiceId.longValue());
				if (invoice.getStatusSelect() != InvoiceRepository.STATUS_DRAFT){
					continue;
				}else{
					try {
						invoiceService.validate(invoice);
					} catch (AxelorException e) {
						TraceBackService.trace(e);
					} finally{
						if (count%10 == 0){
							JPA.clear();
						}
						count ++;
					}
				}
			}
		}
		response.setReload(true);

	}

	public void massVentilation(ActionRequest request, ActionResponse response) {

		List<Integer> listSelectedInvoice = (List<Integer>) request.getContext().get("_ids");
		if(listSelectedInvoice != null){
			Invoice invoice = null;
			int count = 1;
			for(Integer invoiceId : listSelectedInvoice){
				invoice = invoiceRepo.find(invoiceId.longValue());
				if (invoice.getStatusSelect() != InvoiceRepository.STATUS_VALIDATED){
					continue;
				}else{
					try {
						invoiceService.ventilate(invoice);
					} catch (AxelorException e) {
						TraceBackService.trace(e);
					} finally{
						if (count%10 == 0){
							JPA.clear();
						}
						count ++;
					}
				}
			}
		}
		response.setReload(true);

	}

}
