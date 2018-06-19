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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.ReconcileGroup;
import com.axelor.apps.account.db.repo.ReconcileGroupRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class ReconcileGroupServiceImpl implements ReconcileGroupService {

  @Override
  @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
  public void validate(ReconcileGroup reconcileGroup) throws AxelorException {
    List<Reconcile> reconcileList = reconcileGroup.getReconcileList();
    if (CollectionUtils.isEmpty(reconcileList)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.RECONCILE_GROUP_VALIDATION_NO_LINES),
          reconcileGroup);
    }

    if (!isBalanced(reconcileList)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.RECONCILE_GROUP_VALIDATION_NOT_BALANCED),
          reconcileGroup);
    }

    reconcileGroup.setStatusSelect(ReconcileGroupRepository.STATUS_FINAL);

    Beans.get(ReconcileGroupSequenceService.class).fillCodeFromSequence(reconcileGroup);
  }

  @Override
  public boolean isBalanced(List<Reconcile> reconcileList) {
    List<MoveLine> debitMoveLineList =
        reconcileList
            .stream()
            .map(Reconcile::getDebitMoveLine)
            .distinct()
            .collect(Collectors.toList());
    List<MoveLine> creditMoveLineList =
        reconcileList
            .stream()
            .map(Reconcile::getCreditMoveLine)
            .distinct()
            .collect(Collectors.toList());
    List<Account> accountList =
        debitMoveLineList
            .stream()
            .map(MoveLine::getAccount)
            .distinct()
            .collect(Collectors.toList());
    accountList.addAll(
        creditMoveLineList
            .stream()
            .map(MoveLine::getAccount)
            .distinct()
            .collect(Collectors.toList()));

    for (Account account : accountList) {
      BigDecimal totalDebit =
          debitMoveLineList
              .stream()
              .filter(moveLine -> moveLine.getAccount().equals(account))
              .map(MoveLine::getDebit)
              .reduce(BigDecimal::add)
              .orElse(BigDecimal.ZERO);
      BigDecimal totalCredit =
          creditMoveLineList
              .stream()
              .filter(moveLine -> moveLine.getAccount().equals(account))
              .map(MoveLine::getCredit)
              .reduce(BigDecimal::add)
              .orElse(BigDecimal.ZERO);
      if (totalDebit.compareTo(totalCredit) != 0) {
        return false;
      }
    }
    return true;
  }
}
