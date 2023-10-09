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
import org.camunda.bpm.engine.impl.util.CollectionUtil;

public class ExpenseProofFileServiceImpl implements ExpenseProofFileService {

  protected PdfService pdfService;
  protected PdfSignatureService pdfSignatureService;
  protected AppBaseService appBaseService;

  @Inject
  public ExpenseProofFileServiceImpl(
      PdfService pdfService,
      PdfSignatureService pdfSignatureService,
      AppBaseService appBaseService) {
    this.pdfService = pdfService;
    this.pdfSignatureService = pdfSignatureService;
    this.appBaseService = appBaseService;
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void convertProofFilesInPdf(Expense expense) throws AxelorException {
    List<ExpenseLine> expenseLineList = expense.getGeneralExpenseLineList();
    if (CollectionUtil.isEmpty(expenseLineList)) {
      return;
    }

    for (ExpenseLine expenseLine : expenseLineList) {
      convertProofFileToPdf(expenseLine);
    }
  }

  @Override
  public void convertProofFileToPdf(ExpenseLine expenseLine) throws AxelorException {
    MetaFile metaFile = expenseLine.getJustificationMetaFile();
    if (metaFile == null || expenseLine.getIsJustificationFileDigitallySigned()) {
      return;
    }

    MetaFile result;
    MetaFile pdfToSign = pdfService.convertImageToPdf(metaFile);

    result = getSignedPdf(pdfToSign);
    expenseLine.setJustificationMetaFile(result);
    expenseLine.setIsJustificationFileDigitallySigned(true);
  }

  protected MetaFile getSignedPdf(MetaFile pdfToSign) throws AxelorException {
    AppBase appBase = appBaseService.getAppBase();
    PfxCertificate pfxCertificate = appBase.getPfxCertificate();
    if (pfxCertificate != null) {
      return pdfSignatureService.digitallySignPdf(
          pdfToSign, pfxCertificate.getCertificate(), pfxCertificate.getPassword(), "Expense");
    }
    return pdfToSign;
  }
}
