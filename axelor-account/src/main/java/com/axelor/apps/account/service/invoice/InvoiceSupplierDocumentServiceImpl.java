/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.PfxCertificate;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.pdf.PdfService;
import com.axelor.apps.base.service.pdf.PdfSignatureService;
import com.axelor.meta.db.MetaFile;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;

public class InvoiceSupplierDocumentServiceImpl implements InvoiceSupplierDocumentService {

  protected PdfService pdfService;
  protected PdfSignatureService pdfSignatureService;
  protected AppBaseService appBaseService;

  @Inject
  public InvoiceSupplierDocumentServiceImpl(
      PdfService pdfService,
      PdfSignatureService pdfSignatureService,
      AppBaseService appBaseService) {
    this.pdfService = pdfService;
    this.pdfSignatureService = pdfSignatureService;
    this.appBaseService = appBaseService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void convertAndSignPrintedPdf(Invoice invoice) throws AxelorException {
    if (!InvoiceToolService.isPurchase(invoice)) {
      return;
    }
    if (Boolean.TRUE.equals(invoice.getIsPrintedPDFDigitallySigned())
        || invoice.getPrintedPDF() == null) {
      return;
    }
    PfxCertificate pfxCertificate = appBaseService.getAppBase().getPfxCertificate();
    if (pfxCertificate == null) {
      return;
    }
    MetaFile converted = pdfService.convertImageToPdf(invoice.getPrintedPDF());
    MetaFile signed = pdfSignatureService.digitallySignPdf(converted, pfxCertificate, "Invoice");
    invoice.setPrintedPDF(signed);
    invoice.setIsPrintedPDFDigitallySigned(true);
  }
}
