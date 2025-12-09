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
package com.axelor.apps.account.service.analytic;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.apps.account.db.repo.AnalyticLine;
import com.axelor.apps.account.db.repo.InvoiceLineRepository;
import com.axelor.apps.account.db.repo.MoveLineMassEntryRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.model.AnalyticLineModel;
import com.axelor.apps.base.AxelorException;
import com.axelor.inject.Beans;
import com.axelor.rpc.Context;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.util.Optional;

public class AnalyticMoveLineParentServiceImpl implements AnalyticMoveLineParentService {

  protected AnalyticLineService analyticLineService;
  protected MoveLineRepository moveLineRepository;
  protected InvoiceLineRepository invoiceLineRepository;
  protected MoveLineMassEntryRepository moveLineMassEntryRepository;

  @Inject
  public AnalyticMoveLineParentServiceImpl(
      AnalyticLineService analyticLineService,
      MoveLineRepository moveLineRepository,
      InvoiceLineRepository invoiceLineRepository,
      MoveLineMassEntryRepository moveLineMassEntryRepository) {
    this.analyticLineService = analyticLineService;
    this.moveLineRepository = moveLineRepository;
    this.invoiceLineRepository = invoiceLineRepository;
    this.moveLineMassEntryRepository = moveLineMassEntryRepository;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void refreshAxisOnParent(AnalyticMoveLine analyticMoveLine) throws AxelorException {
    MoveLine moveLine = analyticMoveLine.getMoveLine();
    InvoiceLine invoiceLine = analyticMoveLine.getInvoiceLine();
    MoveLineMassEntry moveLineMassEntry = analyticMoveLine.getMoveLineMassEntry();
    if (moveLine != null) {
      analyticLineService.setAnalyticAccount(
          moveLine,
          Optional.of(moveLine).map(MoveLine::getMove).map(Move::getCompany).orElse(null));
      moveLineRepository.save(moveLine);
    } else if (invoiceLine != null) {
      analyticLineService.setAnalyticAccount(
          invoiceLine,
          Optional.of(invoiceLine)
              .map(InvoiceLine::getInvoice)
              .map(Invoice::getCompany)
              .orElse(null));
      invoiceLineRepository.save(invoiceLine);
    } else if (moveLineMassEntry != null) {
      analyticLineService.setAnalyticAccount(
          moveLineMassEntry,
          Optional.of(moveLineMassEntry)
              .map(MoveLineMassEntry::getMove)
              .map(Move::getCompany)
              .orElse(null));
      moveLineMassEntryRepository.save(moveLineMassEntry);
    }
  }

  @Override
  public AnalyticLineModel getModelUsingAnalyticMoveLine(
      AnalyticMoveLine analyticMoveLine, Context context) throws AxelorException {
    AnalyticLineModel analyticLineModel =
        getAnalyticLineModelFromParentContext(
            context,
            Optional.of(context)
                .map(Context::getParent)
                .map(Context::getContextClass)
                .orElse(null));

    if (analyticLineModel != null) {
      return analyticLineModel;
    } else {
      return searchWithAnalyticMoveLine(analyticMoveLine, context);
    }
  }

  protected AnalyticLineModel getAnalyticLineModelFromParentContext(
      Context context, Class<?> parentClass) throws AxelorException {
    if (context == null
        || context.getParent() == null
        || parentClass == null
        || !AnalyticLine.class.isAssignableFrom(parentClass)) {
      return null;
    }

    return searchWithParentContext(parentClass, context.getParent());
  }

  protected AnalyticLineModel searchWithParentContext(Class<?> parentClass, Context parentContext)
      throws AxelorException {
    if (MoveLine.class.equals(parentClass)) {
      return AnalyticLineModelInitAccountService.castAsAnalyticLineModel(
          parentContext.asType(MoveLine.class), null);
    } else if (InvoiceLine.class.equals(parentClass)) {
      return AnalyticLineModelInitAccountService.castAsAnalyticLineModel(
          parentContext.asType(InvoiceLine.class), null);
    }

    return null;
  }

  protected AnalyticLineModel searchWithAnalyticMoveLine(
      AnalyticMoveLine analyticMoveLine, Context context) throws AxelorException {
    if (analyticMoveLine.getMoveLine() != null) {
      return AnalyticLineModelInitAccountService.castAsAnalyticLineModel(
          analyticMoveLine.getMoveLine(), null);
    } else if (analyticMoveLine.getInvoiceLine() != null) {
      return AnalyticLineModelInitAccountService.castAsAnalyticLineModel(
          analyticMoveLine.getInvoiceLine(), null);
    } else if (context.get("invoiceId") != null) {
      Long invoiceId = Long.valueOf((Integer) context.get("invoiceId"));
      return AnalyticLineModelInitAccountService.castAsAnalyticLineModel(
          Beans.get(InvoiceLineRepository.class).find(invoiceId), null);
    }

    return null;
  }
}
