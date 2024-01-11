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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.MoveLineQuery;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.repo.MoveLineQueryRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.ReconcileRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.google.inject.Inject;
import java.util.List;

public class MoveLineQueryServiceImpl implements MoveLineQueryService {

  protected MoveLineQueryRepository moveLineQueryRepository;
  protected AppBaseService appBaseService;
  protected ReconcileRepository reconcileRepository;
  protected ReconcileService reconcileService;

  @Inject
  public MoveLineQueryServiceImpl(
      MoveLineQueryRepository moveLineQueryRepository,
      AppBaseService appBaseService,
      ReconcileRepository reconcileRepository,
      ReconcileService reconcileService) {
    this.moveLineQueryRepository = moveLineQueryRepository;
    this.appBaseService = appBaseService;
    this.reconcileRepository = reconcileRepository;
    this.reconcileService = reconcileService;
  }

  @Override
  public String getMoveLineQuery(MoveLineQuery moveLineQuery) {
    moveLineQuery = moveLineQueryRepository.find(moveLineQuery.getId());

    String query = "";

    query += "self.move.company.id = " + moveLineQuery.getCompany().getId();

    if (appBaseService.getAppBase().getEnableTradingNamesManagement()
        && ObjectUtils.notEmpty(moveLineQuery.getTradingName())) {
      query += " AND self.move.tradingName.id = " + moveLineQuery.getTradingName().getId();
    }

    query += String.format(" AND self.date >= '%s'", moveLineQuery.getFromDate().toString());
    query += String.format(" AND self.date <= '%s'", moveLineQuery.getToDate().toString());

    query += " AND self.account.id = " + moveLineQuery.getAccount().getId();

    if (ObjectUtils.notEmpty(moveLineQuery.getPartner())) {
      query += " AND self.partner.id = " + moveLineQuery.getPartner().getId();
    }

    if (moveLineQuery.getProcessSelect() == MoveLineQueryRepository.PROCESS_RECONCILE) {
      query += "AND self.amountRemaining != 0 ";
    } else if (moveLineQuery.getProcessSelect() == MoveLineQueryRepository.PROCESS_UNRECONCILE) {
      query += "AND self.amountRemaining != debit + credit ";
    }

    query +=
        " AND self.move.statusSelect in ("
            + MoveRepository.STATUS_ACCOUNTED
            + ","
            + MoveRepository.STATUS_DAYBOOK
            + ")";

    return query;
  }

  public void ureconcileMoveLinesWithCacheManagement(List<Reconcile> reconcileList)
      throws AxelorException {
    for (Reconcile reconcile : reconcileList) {
      reconcile = reconcileRepository.find(reconcile.getId());
      reconcileService.unreconcile(reconcile);
      JPA.clear();
    }
  }
}
