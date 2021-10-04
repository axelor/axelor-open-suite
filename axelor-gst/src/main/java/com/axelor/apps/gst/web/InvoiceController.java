package com.axelor.apps.gst.web;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.base.db.repo.LanguageRepository;
import com.axelor.apps.gst.service.invoice.InvoiceGstPrintService;
import com.axelor.apps.gst.service.invoice.InvoiceGstServiceImpl;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.*;
import com.google.inject.Inject;

public class InvoiceController {

  @Inject InvoiceGstServiceImpl service;

  public void calculate(ActionRequest request, ActionResponse response) throws AxelorException {

    Invoice invoice = request.getContext().asType(Invoice.class);
    invoice = service.compute(invoice);

    response.setValue("invoiceLineList", invoice.getInvoiceLineList());
    response.setValue("exTaxTotal", invoice.getExTaxTotal());
    response.setValue("netIgst", invoice.getNetIgst());
    response.setValue("netSgst", invoice.getNetSgst());
    response.setValue("netCgst", invoice.getNetCgst());

    response.setValue("taxTotal", invoice.getTaxTotal());
    response.setValue("inTaxTotal", invoice.getInTaxTotal());
  }
  
  public void showInvoice(ActionRequest request, ActionResponse response) {
	    Context context = request.getContext();
	    String fileLink;
	    String title;

	    try {
	      if (!ObjectUtils.isEmpty(request.getContext().get("_ids"))) {
	        List<Long> ids =
	            (List)
	                (((List) context.get("_ids"))
	                    .stream()
	                        .filter(ObjectUtils::notEmpty)
	                        .map(input -> Long.parseLong(input.toString()))
	                        .collect(Collectors.toList()));
	        fileLink = Beans.get(InvoiceGstPrintService.class).printInvoices(ids);
	        title = I18n.get("GstInvoices");
	      } else if (context.get("id") != null) {
	        String format = context.get("format") != null ? context.get("format").toString() : "pdf";
	        Integer reportType =
	            context.get("reportType") != null
	                ? Integer.parseInt(context.get("reportType").toString())
	                : null;

	        Map languageMap =
	            reportType != null
	                    && (reportType == 1 || reportType == 3)
	                    && context.get("language") != null
	                ? (Map<String, Object>) request.getContext().get("language")
	                : null;
	        String locale =
	            languageMap != null && languageMap.get("id") != null
	                ? Beans.get(LanguageRepository.class)
	                    .find(Long.parseLong(languageMap.get("id").toString()))
	                    .getCode()
	                : null;

	        fileLink =
	            Beans.get(InvoiceGstPrintService.class)
	                .printInvoice(
	                    Beans.get(InvoiceRepository.class)
	                        .find(Long.parseLong(context.get("id").toString())),
	                    false,
	                    format,
	                    reportType,
	                    locale);
	        title = I18n.get("GstInvoice");
	      } else {
	        throw new AxelorException(
	            TraceBackRepository.CATEGORY_MISSING_FIELD, I18n.get(IExceptionMessage.INVOICE_3));
	      }
	      response.setView(ActionView.define(title).add("html", fileLink).map());
	    } catch (Exception e) {
	      TraceBackService.trace(response, e);
	    }
	  }
}
