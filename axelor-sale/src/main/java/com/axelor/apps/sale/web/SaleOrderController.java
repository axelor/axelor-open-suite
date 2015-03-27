/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2014 Axelor (<http://axelor.com>).
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
package com.axelor.apps.sale.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.ReportSettings;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.report.IReport;
import com.axelor.apps.sale.service.SaleOrderService;
import com.axelor.apps.tool.net.URLService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class SaleOrderController {

	@Inject
	private SaleOrderService saleOrderService;

	private static final Logger LOG = LoggerFactory.getLogger(CopyOfSaleOrderController.class);

	public void compute(ActionRequest request, ActionResponse response)  {

		SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);

		try {
			saleOrderService.computeSaleOrder(saleOrderService.find(saleOrder.getId()));
			response.setReload(true);
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}


	/**
	 * Fonction appeler par le bouton imprimer
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	public void showSaleOrder(ActionRequest request, ActionResponse response) {

		SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);

		StringBuilder url = new StringBuilder();

		url.append(this.getURLSaleOrderPDF(saleOrder));

		LOG.debug("URL : {}", url);
		String urlNotExist = URLService.notExist(url.toString());

		if(urlNotExist == null) {

			LOG.debug("Impression du devis "+saleOrder.getSaleOrderSeq()+" : "+url.toString());

			String title = I18n.get("Devis");
			if(saleOrder.getSaleOrderSeq() != null)  {
				title += saleOrder.getSaleOrderSeq();
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
	}

	private String getURLSaleOrderPDF(SaleOrder saleOrder){
		String language="";
		try{
			language = saleOrder.getClientPartner().getLanguageSelect() != null? saleOrder.getClientPartner().getLanguageSelect() : saleOrder.getCompany().getPrintingSettings().getLanguageSelect() != null ? saleOrder.getCompany().getPrintingSettings().getLanguageSelect() : "en" ;
		}catch (NullPointerException e) {
			language = "en";
		}
		language = language.equals("")? "en": language;


		return new ReportSettings(IReport.SALES_ORDER, ReportSettings.FORMAT_PDF)
							.addParam("Locale", language)
							.addParam("__locale", "fr_FR")
							.addParam("SaleOrderId", saleOrder.getId().toString())
							.getUrl();
	}


	public void exportSaleOrderExcel(ActionRequest request, ActionResponse response) {

		SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);

		StringBuilder url = new StringBuilder();

		String language="";
		try{
			language = saleOrder.getClientPartner().getLanguageSelect() != null? saleOrder.getClientPartner().getLanguageSelect() : saleOrder.getCompany().getPrintingSettings().getLanguageSelect() != null ? saleOrder.getCompany().getPrintingSettings().getLanguageSelect() : "en" ;
		}catch (NullPointerException e) {
			language = "en";
		}
		language = language.equals("")? "en": language;

		url.append(
				new ReportSettings(IReport.SALES_ORDER, ReportSettings.FORMAT_XLS)
				.addParam("Locale", language)
				.addParam("__locale", "fr_FR")
				.addParam("SaleOrderId", saleOrder.getId().toString())
				.getUrl());

		LOG.debug("URL : {}", url);
		String urlNotExist = URLService.notExist(url.toString());

		if(urlNotExist == null) {

			LOG.debug("Impression du devis "+saleOrder.getSaleOrderSeq()+" : "+url.toString());

			String title = I18n.get("Devis");
			if(saleOrder.getSaleOrderSeq() != null)  {
				title += saleOrder.getSaleOrderSeq();
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
	}



	public void exportSaleOrderWord(ActionRequest request, ActionResponse response) {

		SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);

		StringBuilder url = new StringBuilder();

		String language="";
		try{
			language = saleOrder.getClientPartner().getLanguageSelect() != null? saleOrder.getClientPartner().getLanguageSelect() : saleOrder.getCompany().getPrintingSettings().getLanguageSelect() != null ? saleOrder.getCompany().getPrintingSettings().getLanguageSelect() : "en" ;
		}catch (NullPointerException e) {
			language = "en";
		}
		language = language.equals("")? "en": language;

		url.append(
				new ReportSettings(IReport.SALES_ORDER, ReportSettings.FORMAT_DOC)
				.addParam("Locale", language)
				.addParam("__locale", "fr_FR")
				.addParam("SaleOrderId", saleOrder.getId().toString())
				.getUrl());

		LOG.debug("URL : {}", url);
		String urlNotExist = URLService.notExist(url.toString());

		if(urlNotExist == null) {

			LOG.debug("Impression du devis "+saleOrder.getSaleOrderSeq()+" : "+url.toString());

			String title = I18n.get("Devis");
			if(saleOrder.getSaleOrderSeq() != null)  {
				title += saleOrder.getSaleOrderSeq();
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
	}

	public void setSequence(ActionRequest request, ActionResponse response) throws AxelorException {

		SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);

		if(saleOrder != null &&  saleOrder.getCompany() != null) {

			response.setValue("saleOrderSeq", saleOrderService.getSequence(saleOrder.getCompany()));

		}
	}

	public void validateCustomer(ActionRequest request, ActionResponse response) {

		SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);

		response.setValue("clientPartner", saleOrderService.validateCustomer(saleOrder));

	}

	public void setDraftSequence(ActionRequest request,ActionResponse response){
		SaleOrder saleOrder=request.getContext().asType(SaleOrder.class);
		if(saleOrder.getSaleOrderSeq()!=null){
			return;
		}
		response.setValue("saleOrderSeq","*"+saleOrder.getId().toString());
	}

	public void cancelSaleOrder(ActionRequest request, ActionResponse response) {

		SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);

		saleOrderService.cancelSaleOrder(saleOrderService.find(saleOrder.getId()));

		response.setFlash("The sale order was canceled");
		response.setCanClose(true);

	}

	public void saveSaleOrderPDFAsAttachment(ActionRequest request, ActionResponse response) throws IOException {

		SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);

		String birtReportURL = this.getURLSaleOrderPDF(saleOrder);

		saleOrderService.saveSaleOrderPDFAsAttachment(saleOrder, birtReportURL);

	}
}
