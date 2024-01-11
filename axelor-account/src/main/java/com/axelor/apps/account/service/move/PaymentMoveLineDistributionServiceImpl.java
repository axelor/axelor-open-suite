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
package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentMoveLineDistribution;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.axelor.apps.account.db.repo.PaymentMoveLineDistributionRepository;
import com.axelor.apps.account.db.repo.ReconcileRepository;
import com.axelor.inject.Beans;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class PaymentMoveLineDistributionServiceImpl implements PaymentMoveLineDistributionService {

  protected PaymentMoveLineDistributionRepository paymentMvlDistributionRepository;

  @Inject
  public PaymentMoveLineDistributionServiceImpl(
      PaymentMoveLineDistributionRepository paymentMvlDistributionRepository) {

    this.paymentMvlDistributionRepository = paymentMvlDistributionRepository;
  }

  @Override
  @Transactional
  public void updateMoveInclusionInDas2Report(Move move, boolean state) {

    List<PaymentMoveLineDistribution> list =
        paymentMvlDistributionRepository.all().filter("self.move = ?1", move).fetch();
    for (PaymentMoveLineDistribution item : list) {
      item.setExcludeFromDas2Report(state);
      paymentMvlDistributionRepository.save(item);
    }
  }

  @Override
  @Transactional
  public void generatePaymentMoveLineDistributionList(Move move, Reconcile reconcile) {

    BigDecimal invoiceTotalAmount =
        move.getMoveLineList().stream()
            .map(MoveLine::getDebit)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal paymentAmount = reconcile.getAmount();

    for (MoveLine moveLine : move.getMoveLineList()) {
      // ignore move lines related to taxes
      if (moveLine
          .getAccount()
          .getAccountType()
          .getTechnicalTypeSelect()
          .equals(AccountTypeRepository.TYPE_TAX)) {
        continue;
      }
      PaymentMoveLineDistribution paymentMvlD =
          new PaymentMoveLineDistribution(
              move.getPartner(), reconcile, moveLine, move, moveLine.getTaxLine());

      if (moveLine.getAccount().getServiceType() == null
          || moveLine.getAccount().getServiceType().getN4dsCode() == null) {
        paymentMvlD.setExcludeFromDas2Report(true);
      }
      paymentMvlD.setOperationDate(reconcile.getReconciliationDateTime().toLocalDate());
      if (!moveLine.getAccount().getReconcileOk()) {
        this.computeProratedAmounts(
            paymentMvlD,
            invoiceTotalAmount,
            paymentAmount,
            moveLine.getCredit().add(moveLine.getDebit()),
            moveLine.getTaxLine());
      }
      reconcile.addPaymentMoveLineDistributionListItem(paymentMvlD);
    }

    Beans.get(ReconcileRepository.class).save(reconcile);
  }

  @Override
  @Transactional
  public void reversePaymentMoveLineDistributionList(Reconcile reconcile) {

    List<PaymentMoveLineDistribution> reverseLines = Lists.newArrayList();
    for (PaymentMoveLineDistribution paymentMvlD : reconcile.getPaymentMoveLineDistributionList()) {
      if (!paymentMvlD.getIsAlreadyReverse()) {
        paymentMvlD.setIsAlreadyReverse(true);
        PaymentMoveLineDistribution reversePaymentMvlD =
            new PaymentMoveLineDistribution(
                paymentMvlD.getPartner(),
                reconcile,
                paymentMvlD.getMoveLine(),
                paymentMvlD.getMove(),
                paymentMvlD.getTaxLine());

        reversePaymentMvlD.setIsAlreadyReverse(true);
        reversePaymentMvlD.setOperationDate(
            reconcile.getReconciliationCancelDateTime().toLocalDate());
        if (!paymentMvlD.getMoveLine().getAccount().getReconcileOk()) {
          reversePaymentMvlD.setExTaxProratedAmount(paymentMvlD.getExTaxProratedAmount().negate());
          reversePaymentMvlD.setTaxProratedAmount(paymentMvlD.getTaxProratedAmount().negate());
          reversePaymentMvlD.setInTaxProratedAmount(paymentMvlD.getInTaxProratedAmount().negate());
        }

        reverseLines.add(reversePaymentMvlD);
      }
    }
    reconcile.getPaymentMoveLineDistributionList().addAll(reverseLines);
    Beans.get(ReconcileRepository.class).save(reconcile);
  }

  protected void computeProratedAmounts(
      PaymentMoveLineDistribution paymentMvlD,
      BigDecimal invoiceTotalAmount,
      BigDecimal paymentAmount,
      BigDecimal moveLineAmount,
      TaxLine taxLine) {

    BigDecimal exTaxProratedAmount =
        moveLineAmount
            .multiply(paymentAmount)
            .divide(invoiceTotalAmount, 6, RoundingMode.HALF_UP)
            .setScale(2, RoundingMode.HALF_UP);

    BigDecimal taxProratedAmount = BigDecimal.ZERO;
    if (taxLine != null) {
      taxProratedAmount =
          taxLine == null
              ? BigDecimal.ZERO
              : exTaxProratedAmount
                  .multiply(taxLine.getValue().divide(new BigDecimal(100)))
                  .setScale(2, RoundingMode.HALF_UP);
    }

    paymentMvlD.setExTaxProratedAmount(exTaxProratedAmount);
    paymentMvlD.setTaxProratedAmount(taxProratedAmount);
    paymentMvlD.setInTaxProratedAmount(exTaxProratedAmount.add(taxProratedAmount));
  }
}
