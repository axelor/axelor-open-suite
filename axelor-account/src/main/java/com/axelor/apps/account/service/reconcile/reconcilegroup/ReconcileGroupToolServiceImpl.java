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
