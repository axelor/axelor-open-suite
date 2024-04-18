package com.axelor.apps.account.service.moveline;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountType;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.axelor.apps.account.util.TaxConfiguration;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;

public class MoveLineTaxToolServiceImpl implements MoveLineTaxToolService {

  @Inject
  public MoveLineTaxToolServiceImpl() {}

  @Override
  public Map<TaxConfiguration, Pair<BigDecimal, BigDecimal>> getTaxMoveLineMapToRevert(
      List<MoveLine> advancePaymentMoveLineList) {
    List<MoveLine> taxMoveLineList = getMoveLineListToRevert(advancePaymentMoveLineList);
    // This map is to link taxMoveLines with their amount and companyAmount.
    Map<TaxConfiguration, Pair<BigDecimal, BigDecimal>> amountsByTaxConfigMap = new HashMap<>();

    if (ObjectUtils.isEmpty(taxMoveLineList)) {
      return amountsByTaxConfigMap;
    }

    for (MoveLine moveLine : taxMoveLineList) {
      fillAmountByTaxConfigMap(amountsByTaxConfigMap, moveLine);
    }

    return amountsByTaxConfigMap;
  }

  protected List<MoveLine> getMoveLineListToRevert(List<MoveLine> advancePaymentMoveLineList) {
    List<MoveLine> taxMoveLineList = new ArrayList<>();
    if (ObjectUtils.isEmpty(advancePaymentMoveLineList)) {
      return taxMoveLineList;
    }

    for (MoveLine moveLine : advancePaymentMoveLineList) {
      if (moveLine.getMove() != null) {
        taxMoveLineList.addAll(
            moveLine.getMove().getMoveLineList().stream()
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
                .collect(Collectors.toList()));
      }
    }
    return taxMoveLineList;
  }

  protected void fillAmountByTaxConfigMap(
      Map<TaxConfiguration, Pair<BigDecimal, BigDecimal>> amountsByTaxConfigMap,
      MoveLine moveLine) {
    TaxLine taxLine = (TaxLine) moveLine.getTaxLineSet().toArray()[0];
    if (taxLine != null && moveLine.getAccount() != null && moveLine.getVatSystemSelect() != null) {
      TaxConfiguration taxConfiguration =
          new TaxConfiguration(taxLine, moveLine.getAccount(), moveLine.getVatSystemSelect());
      if (amountsByTaxConfigMap.containsKey(taxConfiguration)) {
        Pair<BigDecimal, BigDecimal> amountPair = amountsByTaxConfigMap.get(taxConfiguration);
        Pair<BigDecimal, BigDecimal> newAmountPair =
            Pair.of(
                amountPair.getLeft().add(moveLine.getCurrencyAmount()),
                amountPair.getRight().add(moveLine.getDebit().subtract(moveLine.getCredit())));

        amountsByTaxConfigMap.put(taxConfiguration, newAmountPair);
      } else {
        amountsByTaxConfigMap.put(
            taxConfiguration,
            Pair.of(
                moveLine.getCurrencyAmount(), moveLine.getDebit().subtract(moveLine.getCredit())));
      }
    }
  }
}
