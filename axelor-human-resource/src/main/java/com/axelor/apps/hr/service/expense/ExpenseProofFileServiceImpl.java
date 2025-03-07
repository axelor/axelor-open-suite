/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.hr.service.expense;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.PfxCertificate;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.pdf.PdfService;
import com.axelor.apps.base.service.pdf.PdfSignatureService;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.meta.db.MetaFile;
import com.axelor.studio.db.AppBase;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class ExpenseProofFileServiceImpl implements ExpenseProofFileService {

  protected PdfService pdfService;
  protected PdfSignatureService pdfSignatureService;
  protected AppBaseService appBaseService;
  protected ExpenseLineService expenseLineService;

  @Inject
  public ExpenseProofFileServiceImpl(
      PdfService pdfService,
      PdfSignatureService pdfSignatureService,
      AppBaseService appBaseService,
      ExpenseLineService expenseLineService) {
    this.pdfService = pdfService;
    this.pdfSignatureService = pdfSignatureService;
    this.appBaseService = appBaseService;
    this.expenseLineService = expenseLineService;
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void convertProofFilesInPdf(Expense expense) throws AxelorException {
    List<ExpenseLine> expenseLineList = expense.getGeneralExpenseLineList();
    if (CollectionUtils.isEmpty(expenseLineList)) {
      return;
    }

    for (ExpenseLine expenseLine : expenseLineList) {
      signJustificationFile(expenseLine);
    }
  }

  @Override
  public void signJustificationFile(ExpenseLine expenseLine) throws AxelorException {
    AppBase appBase = appBaseService.getAppBase();
    PfxCertificate pfxCertificate = appBase.getPfxCertificate();
    if (expenseLine.getIsJustificationFileDigitallySigned()
        && expenseLine.getJustificationMetaFile() != null) {
      return;
    }
    if (pfxCertificate != null) {
      convertProofFileToPdf(pfxCertificate, expenseLine);
      signPdf(pfxCertificate, expenseLine);
    }
  }

  @Override
  public void convertProofFileToPdf(PfxCertificate pfxCertificate, ExpenseLine expenseLine)
      throws AxelorException {
    MetaFile metaFile = expenseLine.getJustificationMetaFile();
    if (metaFile == null || pfxCertificate == null) {
      return;
    }

    expenseLine.setJustificationMetaFile(pdfService.convertImageToPdf(metaFile));
  }

  protected void signPdf(PfxCertificate pfxCertificate, ExpenseLine expenseLine)
      throws AxelorException {

    if (!expenseLineService.isFilePdf(expenseLine)
        || expenseLine.getIsJustificationFileDigitallySigned()) {
      return;
    }

    MetaFile signedPdf = getSignedPdf(pfxCertificate, expenseLine);
    expenseLine.setJustificationMetaFile(signedPdf);
  }

  protected MetaFile getSignedPdf(PfxCertificate pfxCertificate, ExpenseLine expenseLine)
      throws AxelorException {
    MetaFile pdfToSign = expenseLine.getJustificationMetaFile();

    if (pfxCertificate != null) {
      expenseLine.setIsJustificationFileDigitallySigned(true);
      return pdfSignatureService.digitallySignPdf(pdfToSign, pfxCertificate, "Expense");
    }
    return pdfToSign;
  }
}
