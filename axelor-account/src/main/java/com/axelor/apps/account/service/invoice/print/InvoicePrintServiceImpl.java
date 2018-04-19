/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.invoice.print;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.report.IReport;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.apps.tool.ModelTool;
import com.axelor.apps.tool.ThrowConsumer;
import com.axelor.apps.tool.file.PdfTool;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;

public class InvoicePrintServiceImpl implements InvoicePrintService {

    @Override
    public ReportSettings printInvoice(Invoice invoice, boolean toAttach) throws AxelorException {
        if (invoice.getPrintingSettings() == null) {
            throw new AxelorException(IException.MISSING_FIELD,
                    String.format(I18n.get(IExceptionMessage.INVOICE_MISSING_PRINTING_SETTINGS), invoice.getInvoiceId()),
                    invoice
            );
        }
        String locale = ReportSettings.getPrintingLocale(invoice.getPartner());

        String title = I18n.get("Invoice");
        if (invoice.getInvoiceId() != null) {
            title += " " + invoice.getInvoiceId();
        }

        ReportSettings reportSetting = ReportFactory.createReport(IReport.INVOICE, title + " - ${date}");
        if (toAttach) {
            reportSetting.toAttach(invoice);
        }

        return reportSetting.addParam("InvoiceId", invoice.getId().toString())
                .addParam("Locale", locale)
                .addParam("InvoicesCopy", invoice.getInvoicesCopySelect())
                .generate();
    }

    @Override
    public String printInvoices(List<Long> ids) throws IOException {
        List<File> printedInvoices = new ArrayList<>();
        ModelTool.apply(Invoice.class, ids, new ThrowConsumer<Invoice>() {
            @Override
            public void accept(Invoice invoice) throws Exception {
                printedInvoices.add(getPrintedInvoice(invoice));
            }
        });
        String fileName = I18n.get("Invoices") + " - "
                + Beans.get(AppBaseService.class).getTodayDate()
                .format(DateTimeFormatter.BASIC_ISO_DATE)
                + ".pdf";
        return PdfTool.mergePdf(printedInvoices, fileName);
    }


    /**
     * Get the printed invoice from
     *
     * @param invoice
     * @return a String with the correct file link.
     */
    @Override
    public File getPrintedInvoice(Invoice invoice) throws AxelorException {
        return printInvoice(invoice, false).getFile();
    }

}
