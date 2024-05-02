/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bankpayment.db.repo;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveManagementRepository;
import com.axelor.apps.account.service.invoice.InvoiceTermToolService;
import com.axelor.apps.account.service.move.MoveSequenceService;
import com.axelor.apps.account.util.InvoiceTermUtilsService;
import com.axelor.apps.account.util.MoveLineUtilsService;
import com.axelor.apps.account.util.MoveUtilsService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.utils.PeriodUtilsService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.List;

public class MoveBankPaymentRepository extends MoveManagementRepository {

  @Inject
  public MoveBankPaymentRepository(
      PeriodUtilsService periodUtilsService,
      AppBaseService appBaseService,
      MoveUtilsService moveUtilsService,
      MoveSequenceService moveSequenceService,
      InvoiceTermUtilsService invoiceTermUtilsService,
      InvoiceTermToolService invoiceTermToolService,
      MoveLineUtilsService moveLineUtilsService) {
    super(
        periodUtilsService,
        appBaseService,
        moveUtilsService,
        moveSequenceService,
        invoiceTermUtilsService,
        invoiceTermToolService,
        moveLineUtilsService);
  }

  @Override
  public Move copy(Move entity, boolean deep) {
    Move copy = super.copy(entity, deep);

    List<MoveLine> moveLineList = copy.getMoveLineList();

    if (moveLineList != null) {
      moveLineList.forEach(moveLine -> moveLine.setBankReconciledAmount(BigDecimal.ZERO));
    }

    return copy;
  }
}
