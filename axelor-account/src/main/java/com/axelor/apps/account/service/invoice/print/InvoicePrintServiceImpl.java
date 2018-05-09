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
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.inject.persist.Transactional;

/**
 * Implementation of the service printing invoices.
 */
public class InvoicePrintServiceImpl implements InvoicePrintService {

    @Override
    public String printInvoice(Invoice invoice) throws AxelorException, IOException {
        String fileName = getInvoiceFilesName(false);
        return PdfTool.getFileLinkFromPdfFile(printCopiesToFile(invoice), fileName);
    }

    @Override
    public File printCopiesToFile(Invoice invoice) throws AxelorException, IOException {
        File file = getPrintedInvoice(invoice);
        int copyNumber = invoice.getInvoicesCopySelect();
        return PdfTool.printCopiesToFile(file, copyNumber);
    }

    @Override
    @Transactional(rollbackOn = {AxelorException.class, IOException.class, RuntimeException.class})
    public File getPrintedInvoice(Invoice invoice) throws AxelorException {
        if (invoice.getStatusSelect() == InvoiceRepository.STATUS_VENTILATED
                && invoice.getPrintedPDF() != null) {
            Path path = MetaFiles.getPath(invoice.getPrintedPDF().getFileName());
            return path.toFile();
        }

        // if the invoice is not ventilated or missing a printing,
        // we generate and save it.
        return printAndSave(invoice);
    }

    public File printAndSave(Invoice invoice) throws AxelorException {

        ReportSettings reportSettings = prepareReportSettings(invoice);
        MetaFile metaFile;

        reportSettings.toAttach(invoice);
        File file = reportSettings.generate().getFile();

        try {
            MetaFiles metaFiles = Beans.get(MetaFiles.class);
            metaFile = metaFiles.upload(file);
            invoice.setPrintedPDF(metaFile);
            return MetaFiles.getPath(metaFile).toFile();
        } catch (IOException e) {
            throw new AxelorException(TraceBackRepository.TYPE_TECHNICAL,
                    I18n.get(IExceptionMessage.INVOICE_PRINTING_IO_ERROR)
                            + " "
                            + e.getLocalizedMessage());
        }
    }

    @Override
    public String printInvoices(List<Long> ids) throws IOException {
        List<File> printedInvoices = new ArrayList<>();
        ModelTool.apply(Invoice.class, ids, new ThrowConsumer<Invoice>() {
            @Override
            public void accept(Invoice invoice) throws Exception {
                printedInvoices.add(printCopiesToFile(invoice));
            }
        });
        String fileName = getInvoiceFilesName(true);
        return PdfTool.mergePdfToFileLink(printedInvoices, fileName);
    }


    @Override
    public ReportSettings prepareReportSettings(Invoice invoice) throws AxelorException {

        if (invoice.getPrintingSettings() == null) {
            throw new AxelorException(TraceBackRepository.CATEGORY_MISSING_FIELD,
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

        return reportSetting.addParam("InvoiceId", invoice.getId())
                .addParam("Locale", locale);
    }

    /**
     * Return the name for the printed invoice.
     * @param plural if there is one or multiple invoices.
     */
    protected String getInvoiceFilesName(boolean plural) {

        return I18n.get(plural ? "Invoices" : "Invoice")
                + " - "
                + Beans.get(AppBaseService.class).getTodayDate()
                .format(DateTimeFormatter.BASIC_ISO_DATE)
                + ".pdf";
    }
}
