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
package com.axelor.apps.account.service.reconcile.reconcilegroup;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.ReconcileGroup;
import com.axelor.apps.account.db.repo.ReconcileGroupRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.reconcilegroup.ReconcileGroupSequenceService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class ReconcileGroupToolServiceImpl implements ReconcileGroupToolService {

  protected AppBaseService appBaseService;
  protected ReconcileGroupSequenceService reconcileGroupSequenceService;

  @Inject
  public ReconcileGroupToolServiceImpl(
      AppBaseService appBaseService, ReconcileGroupSequenceService reconcileGroupSequenceService) {
    this.appBaseService = appBaseService;
    this.reconcileGroupSequenceService = reconcileGroupSequenceService;
  }

  /**
   * Check if the given reconcile lines are balanced.
   *
   * @param reconcileList a list of reconcile.
   */
  @Override
  public boolean isBalanced(List<Reconcile> reconcileList) {
    List<MoveLine> debitMoveLineList =
        reconcileList.stream()
            .map(Reconcile::getDebitMoveLine)
            .distinct()
            .collect(Collectors.toList());
    List<MoveLine> creditMoveLineList =
        reconcileList.stream()
            .map(Reconcile::getCreditMoveLine)
            .distinct()
            .collect(Collectors.toList());
    List<Account> accountList =
        debitMoveLineList.stream()
            .map(MoveLine::getAccount)
            .distinct()
            .collect(Collectors.toList());
    accountList.addAll(
        creditMoveLineList.stream()
            .map(MoveLine::getAccount)
            .distinct()
            .collect(Collectors.toList()));

    for (Account account : accountList) {
      BigDecimal totalDebit =
          debitMoveLineList.stream()
              .filter(moveLine -> moveLine.getAccount().equals(account))
              .map(MoveLine::getDebit)
              .reduce(BigDecimal::add)
              .orElse(BigDecimal.ZERO);
      BigDecimal totalCredit =
          creditMoveLineList.stream()
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

  /**
   * Validate the given reconcile group. A reconcile Group can be validated if it is not empty and
   * its lines are balanced.
   *
   * @param reconcileGroup a reconcile group.
   * @param reconcileList a list of reconcile.
   * @throws AxelorException if the reconcile list is empty.
   */
  @Override
  public void validate(ReconcileGroup reconcileGroup, List<Reconcile> reconcileList)
      throws AxelorException {
    if (CollectionUtils.isEmpty(reconcileList)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.RECONCILE_GROUP_VALIDATION_NO_LINES),
          reconcileGroup);
    }

    reconcileGroup.setStatusSelect(ReconcileGroupRepository.STATUS_BALANCED);
    reconcileGroup.setLetteringDateTime(
        appBaseService.getTodayDateTime(reconcileGroup.getCompany()).toLocalDateTime());

    reconcileGroupSequenceService.fillCodeFromSequence(reconcileGroup);
  }
}
