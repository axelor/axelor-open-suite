/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.service.pdf.PdfService;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.repo.ExpenseLineRepository;
import com.axelor.apps.hr.service.app.AppHumanResourceService;
import com.axelor.meta.db.MetaFile;
import com.axelor.studio.db.AppExpense;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.List;
import org.camunda.bpm.engine.impl.util.CollectionUtil;

@Singleton
public class ExpenseLineServiceImpl implements ExpenseLineService {

  protected ExpenseLineRepository expenseLineRepository;
  protected PdfService pdfService;
  protected AppHumanResourceService appHumanResourceService;

  @Inject
  public ExpenseLineServiceImpl(
      ExpenseLineRepository expenseLineRepository,
      PdfService pdfService,
      AppHumanResourceService appHumanResourceService) {
    this.expenseLineRepository = expenseLineRepository;
    this.pdfService = pdfService;
    this.appHumanResourceService = appHumanResourceService;
  }

  @Override
  public List<ExpenseLine> getExpenseLineList(Expense expense) {
    List<ExpenseLine> expenseLineList = new ArrayList<>();
    if (expense.getGeneralExpenseLineList() != null) {
      expenseLineList.addAll(expense.getGeneralExpenseLineList());
    }
    if (expense.getKilometricExpenseLineList() != null) {
      expenseLineList.addAll(expense.getKilometricExpenseLineList());
    }
    return expenseLineList;
  }

  @Override
  public void completeExpenseLines(Expense expense) {
    List<ExpenseLine> expenseLineList =
        expenseLineRepository
            .all()
            .filter("self.expense.id = :_expenseId")
            .bind("_expenseId", expense.getId())
            .fetch();
    List<ExpenseLine> kilometricExpenseLineList = expense.getKilometricExpenseLineList();
    List<ExpenseLine> generalExpenseLineList = expense.getGeneralExpenseLineList();

    // removing expense from one O2M also remove the link
    for (ExpenseLine expenseLine : expenseLineList) {
      if (!kilometricExpenseLineList.contains(expenseLine)
          && !generalExpenseLineList.contains(expenseLine)) {
        expenseLine.setExpense(null);
        expenseLineRepository.remove(expenseLine);
      }
    }

    // adding expense in one O2M also add the link
    if (kilometricExpenseLineList != null) {
      for (ExpenseLine kilometricLine : kilometricExpenseLineList) {
        if (!expenseLineList.contains(kilometricLine)) {
          kilometricLine.setExpense(expense);
        }
      }
    }
    if (generalExpenseLineList != null) {
      for (ExpenseLine generalExpenseLine : generalExpenseLineList) {
        if (!expenseLineList.contains(generalExpenseLine)) {
          generalExpenseLine.setExpense(expense);
        }
      }
    }
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

  protected void convertProofFileToPdf(ExpenseLine expenseLine) throws AxelorException {
    MetaFile metaFile = expenseLine.getJustificationMetaFile();
    if (metaFile == null || expenseLine.getIsJustificationFileDigitallySigned()) {
      return;
    }

    MetaFile result;
    MetaFile pdfToSign = convertImageToPdf(metaFile);

    result = getSignedPdf(pdfToSign);
    expenseLine.setJustificationMetaFile(result);
    expenseLine.setIsJustificationFileDigitallySigned(true);
  }

  protected MetaFile convertImageToPdf(MetaFile metaFile) throws AxelorException {
    MetaFile pdfToSign = null;
    String fileType = metaFile.getFileType();

    if (fileType.startsWith("image")) {
      pdfToSign = pdfService.convertImageToPdf(metaFile);
    }

    if (fileType.contains("pdf")) {
      pdfToSign = metaFile;
    }
    return pdfToSign;
  }

  protected MetaFile getSignedPdf(MetaFile pdfToSign) throws AxelorException {
    AppExpense appExpense = appHumanResourceService.getAppExpense();
    PfxCertificate pfxCertificate = appExpense.getPfxCertificate();
    MetaFile signatureLogo = appExpense.getSignatureLogo();
    if (pfxCertificate != null) {
      return pdfService.digitallySignPdf(
          pdfToSign,
          pfxCertificate.getCertificate(),
          pfxCertificate.getPassword(),
          signatureLogo,
          "Expense",
          "France");
    }
    return pdfToSign;
  }
}
