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
import java.util.List;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.exception.AxelorException;

/**
 * Interface of the service managing the printing of invoices.
 */
public interface InvoicePrintService {


    /**
     * Print an invoice with the desired number of copies
     * and return the file link.
     *
     * @param invoice the invoice to print
     * @return the file link to the printed invoice.
     * @throws AxelorException
     */
    String printInvoice(Invoice invoice) throws AxelorException, IOException;

    /**
     * Print the invoice with the desired number of copies and return the file.
     *
     * @param invoice
     * @return the generated file.
     * @throws AxelorException
     * @throws IOException
     */
    File printCopiesToFile(Invoice invoice) throws AxelorException, IOException;

    /**
     * Take the stored report on the invoice, if the invoice
     * is ventilated and the file is available, else print it.
     *
     * @param invoice an invoice.
     * @return a file with the invoice as PDF.
     */
    File getPrintedInvoice(Invoice invoice) throws AxelorException;

    /**
     * Print an invoice, then save the generated file
     * in this invoice.
     *
     * @param invoice an invoice
     * @return a metafile of the invoice printing.
     * @throws AxelorException
     */
    File printAndSave(Invoice invoice) throws AxelorException;

    /**
     * Print a list of invoices in the same output.
     *
     * @param ids ids of the invoice.
     * @return the link to the generated file.
     * @throws IOException
     */
    String printInvoices(List<Long> ids) throws IOException;



    /**
     * Prepare report settings for one invoice.
     *
     * @param invoice an invoice
     * @return the report settings to print the given invoice
     * @throws AxelorException
     */
    ReportSettings prepareReportSettings(Invoice invoice) throws AxelorException;
}
