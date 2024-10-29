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
package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountType;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.util.TaxConfiguration;
import com.axelor.apps.base.AxelorException;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;

public class AdvancePaymentMoveLineCreateServiceImpl
    implements AdvancePaymentMoveLineCreateService {

  protected InvoicePaymentRepository invoicePaymentRepository;
  protected MoveLineCreateService moveLineCreateService;

  @Inject
  public AdvancePaymentMoveLineCreateServiceImpl(
      InvoicePaymentRepository invoicePaymentRepository,
      MoveLineCreateService moveLineCreateService) {
    this.invoicePaymentRepository = invoicePaymentRepository;
    this.moveLineCreateService = moveLineCreateService;
  }

  @Override
  public void manageAdvancePaymentInvoiceTaxMoveLines(
      Move move,
      MoveLine defaultMoveLine,
      BigDecimal prorata,
      LocalDate paymentDate,
      Map<TaxConfiguration, Pair<BigDecimal, BigDecimal>> taxConfigurationAmountMap) {
    if (defaultMoveLine.getMove() == null
        || invoicePaymentRepository
                .all()
                .filter("self.typeSelect = :typeSelect AND self.move = :move")
                .bind("typeSelect", InvoicePaymentRepository.TYPE_ADVANCEPAYMENT)
                .bind("move", defaultMoveLine.getMove())
                .count()
            == 0) {
      return;
    }

    List<MoveLine> taxMoveLineList =
        defaultMoveLine.getMove().getMoveLineList().stream()
            .filter(
                ml ->
                    AccountTypeRepository.TYPE_TAX.equals(
                            Optional.of(ml)
                                .map(MoveLine::getAccount)
                                .map(Account::getAccountType)
                                .map(AccountType::getTechnicalTypeSelect)
                                .orElse(""))
                        && !ObjectUtils.isEmpty(ml.getTaxLineSet())
                        && ml.getTaxLineSet().size() == 1)
            .collect(Collectors.toList());
    if (ObjectUtils.isEmpty(taxMoveLineList)) {
      return;
    }

    for (MoveLine moveLine : taxMoveLineList) {
      TaxLine taxLine = moveLine.getTaxLineSet().iterator().next();
      TaxConfiguration taxConfiguration =
          new TaxConfiguration(taxLine, moveLine.getAccount(), moveLine.getVatSystemSelect());

      BigDecimal moveLineAmount = moveLine.getCurrencyAmount().negate().multiply(prorata);
      BigDecimal moveLineCompanyAmount =
          moveLine.getDebit().max(moveLine.getCredit()).multiply(prorata);

      if (taxConfigurationAmountMap.containsKey(taxConfiguration)) {
        Pair<BigDecimal, BigDecimal> pairAmount = taxConfigurationAmountMap.get(taxConfiguration);
        BigDecimal amount = pairAmount.getLeft().add(moveLineAmount);
        BigDecimal currencyAmount = pairAmount.getRight().add(moveLineCompanyAmount);

        taxConfigurationAmountMap.replace(taxConfiguration, Pair.of(amount, currencyAmount));
      } else {
        taxConfigurationAmountMap.put(
            taxConfiguration, Pair.of(moveLineAmount, moveLineCompanyAmount));
      }

      /*
      counter++;
      MoveLine taxMoveLine =
          moveLineCreateService.createTaxMoveLine(
              move,
              moveLine.getPartner(),
              moveLine.getCredit().signum() > 0,
              paymentDate,
              counter,
              moveLine.getMove().getOrigin(),
              moveLine.getCurrencyAmount().negate().multiply(prorata),
              moveLine.getDebit().max(moveLine.getCredit()).multiply(prorata),
              taxConfiguration);
      move.addMoveLineListItem(taxMoveLine);*/
    }
  }

  @Override
  public void fillMoveWithTaxMoveLines(
      Move move, Map<TaxConfiguration, Pair<BigDecimal, BigDecimal>> taxConfigurationAmountMap)
      throws AxelorException {
    if (taxConfigurationAmountMap.isEmpty()) {
      return;
    }

    int counter = move.getMoveLineList().size();

    for (Map.Entry<TaxConfiguration, Pair<BigDecimal, BigDecimal>> taxConfigurationAmountPairEntry :
        taxConfigurationAmountMap.entrySet()) {
      TaxConfiguration taxConfiguration = taxConfigurationAmountPairEntry.getKey();
      Pair<BigDecimal, BigDecimal> pairAmount = taxConfigurationAmountPairEntry.getValue();

      counter++;
      MoveLine taxMoveLine =
          moveLineCreateService.createTaxMoveLine(
              move,
              move.getPartner(),
              pairAmount.getLeft().signum() > 0,
              move.getDate(),
              counter,
              move.getOrigin(),
              pairAmount.getLeft(),
              pairAmount.getRight(),
              taxConfiguration);
      move.addMoveLineListItem(taxMoveLine);
    }
  }
}
