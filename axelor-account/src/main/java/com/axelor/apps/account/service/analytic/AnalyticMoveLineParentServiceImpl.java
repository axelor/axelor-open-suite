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
import com.axelor.apps.account.db.repo.InvoiceLineRepository;
import com.axelor.apps.account.db.repo.MoveLineMassEntryRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.base.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
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
}
