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
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.report.IReport;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.apps.tool.ModelTool;
import com.axelor.apps.tool.ThrowConsumer;
import com.axelor.apps.tool.file.PdfTool;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.inject.persist.Transactional;

public class InvoicePrintServiceImpl implements InvoicePrintService {

    private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

    @Override
    public MetaFile printAndAttach(Invoice invoice) throws AxelorException {
        ReportSettings reportSettings = prepareReportSettings(invoice);
        MetaFile metaFile = null;

        reportSettings.toAttach(invoice);
        File file = reportSettings.generate().getFile();

        MetaFiles metaFiles = Beans.get(MetaFiles.class);
        try {
            metaFile = metaFiles.upload(file);
            invoice.setPrintedPDF(metaFile);
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage());
            TraceBackService.trace(e);
        }
        return metaFile;
    }

    @Override
    public ReportSettings printInvoice(Invoice invoice) throws AxelorException {
        return prepareReportSettings(invoice).generate();
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


    @Override
    @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
    public File getPrintedInvoice(Invoice invoice) throws AxelorException {
        if (invoice.getStatusSelect() == InvoiceRepository.STATUS_VENTILATED) {
            Path path = null;
            if (invoice.getPrintedPDF() != null) {
                path = MetaFiles.getPath(invoice.getPrintedPDF().getFileName());
            }
            if (path == null) {
                // if the invoice is ventilated and is missing a printing,
                // we generate and save it.
                path = MetaFiles.getPath(printAndAttach(invoice));
            }

            if (path != null) {
                return path.toFile();
            }

        }
        return printInvoice(invoice).getFile();
    }

    @Override
    public ReportSettings prepareReportSettings(Invoice invoice) throws AxelorException {

        if (invoice.getPrintingSettings() == null) {
            throw new AxelorException(IException.MISSING_FIELD,
                    String.format(I18n.get(
                            IExceptionMessage.INVOICE_MISSING_PRINTING_SETTINGS),
                            invoice.getInvoiceId()),
                    invoice
            );
        }
        String locale = ReportSettings.getPrintingLocale(invoice.getPartner());

        String title = I18n.get("Invoice");
        if (invoice.getInvoiceId() != null) {
            title += " " + invoice.getInvoiceId();
        }

        ReportSettings reportSetting = ReportFactory.createReport(IReport.INVOICE, title + " - ${date}");

        return reportSetting.addParam("InvoiceId", invoice.getId().toString())
                .addParam("Locale", locale)
                .addParam("InvoicesCopy", invoice.getInvoicesCopySelect());
    }
}
