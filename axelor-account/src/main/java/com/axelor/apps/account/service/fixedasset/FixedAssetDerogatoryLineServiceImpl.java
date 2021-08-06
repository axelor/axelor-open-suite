package com.axelor.apps.account.service.fixedasset;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetDerogatoryLine;
import com.axelor.apps.account.db.FixedAssetLine;
import com.axelor.apps.account.db.repo.FixedAssetLineRepository;

public class FixedAssetDerogatoryLineServiceImpl implements FixedAssetDerogatoryLineService {

  @Override
  public FixedAssetDerogatoryLine createFixedAssetDerogatoryLine(
      LocalDate depreciationDate,
      BigDecimal depreciationAmount,
      BigDecimal fiscalDepreciationAmount,
      BigDecimal derogatoryAmount,
      BigDecimal incomeDepreciationAmount,
      BigDecimal derogatoryBalanceAmount,
      FixedAssetLine fixedAssetLine,
      FixedAssetLine fiscalFixedAssetLine) {

    FixedAssetDerogatoryLine fixedAssetDerogatoryLine = new FixedAssetDerogatoryLine();
    fixedAssetDerogatoryLine.setStatusSelect(FixedAssetLineRepository.STATUS_PLANNED);
    fixedAssetDerogatoryLine.setDepreciationDate(depreciationDate);
    fixedAssetDerogatoryLine.setDepreciationAmount(depreciationAmount);
    fixedAssetDerogatoryLine.setFiscalDepreciationAmount(fiscalDepreciationAmount);
    fixedAssetDerogatoryLine.setDerogatoryAmount(derogatoryAmount);
    fixedAssetDerogatoryLine.setIncomeDepreciationAmount(incomeDepreciationAmount);
    fixedAssetDerogatoryLine.setDerogatoryBalanceAmount(derogatoryBalanceAmount);
    fixedAssetDerogatoryLine.setFixedAssetLine(fixedAssetLine);
    fixedAssetDerogatoryLine.setFiscalFixedAssetLine(fiscalFixedAssetLine);

    return fixedAssetDerogatoryLine;
  }

  @Override
  public List<FixedAssetDerogatoryLine> computeFixedAssetDerogatoryLineList(FixedAsset fixedAsset) {
    // Preparation of data needed for computation
    List<FixedAssetDerogatoryLine> fixedAssetDerogatoryLineList = new ArrayList<>();
    List<FixedAssetLine> tmpList = new ArrayList<>();
    tmpList.addAll(fixedAsset.getFiscalFixedAssetLineList());
    tmpList.addAll(fixedAsset.getFixedAssetLineList());

    // Sorting by depreciation date
    tmpList.sort((f1, f2) -> f1.getDepreciationDate().compareTo(f2.getDepreciationDate()));

    // Grouping lines from both list by date and keeping the order (because we want to have the
    // previous line)
    LinkedHashMap<LocalDate, List<FixedAssetLine>> dateFixedAssetLineGrouped =
        tmpList.stream()
            .collect(
                Collectors.groupingBy(
                    FixedAssetLine::getDepreciationDate, LinkedHashMap::new, Collectors.toList()));
    //Since we are working on lambda, we need an AtomicReference to store the previousFixedAssetDerogatoryLine
    AtomicReference<FixedAssetDerogatoryLine> previousFixedAssetDerogatoryLine =
        new AtomicReference<>(null);
    // Starting the computation
    dateFixedAssetLineGrouped.forEach(
        (date, fixedAssetLineList) -> {
          // FixedAssetLineList should have at least 1 element and maximum of 2 elements so it is
          // not null.
          FixedAssetLine economicFixedAssetLine =
              fixedAssetLineList.stream()
                  .filter(
                      fixedAssetLine ->
                          fixedAssetLine.getTypeSelect()
                              == FixedAssetLineRepository.TYPE_SELECT_ECONOMIC)
                  .findAny()
                  .orElse(null);
          FixedAssetLine fiscalFixedAssetLine =
              fixedAssetLineList.stream()
                  .filter(
                      fixedAssetLine ->
                          fixedAssetLine.getTypeSelect()
                              == FixedAssetLineRepository.TYPE_SELECT_FISCAL)
                  .findAny()
                  .orElse(null);
          
          //Initialisation of fiscal and economic depreciation
          BigDecimal depreciationAmount = BigDecimal.ZERO;
          if (economicFixedAssetLine != null) {
            depreciationAmount =
                economicFixedAssetLine.getDepreciation() == null
                    ? BigDecimal.ZERO
                    : economicFixedAssetLine.getDepreciation();
          }

          BigDecimal fiscalDepreciationAmount = BigDecimal.ZERO;
          if (fiscalFixedAssetLine != null) {
            fiscalDepreciationAmount =
                fiscalFixedAssetLine.getDepreciation() == null
                    ? BigDecimal.ZERO
                    : fiscalFixedAssetLine.getDepreciation();
          }

          BigDecimal derogatoryAmount = null;
          BigDecimal incomeDepreciationAmount = null;
          
          //If fiscal depreciation is greater than economic depreciation then we fill derogatoryAmount, else incomeDepreciation.
          if (fiscalDepreciationAmount.compareTo(depreciationAmount) > 0) {
            derogatoryAmount = fiscalDepreciationAmount.subtract(depreciationAmount);
          } else {
            incomeDepreciationAmount = fiscalDepreciationAmount.subtract(depreciationAmount);
          }
          
          BigDecimal derogatoryBalanceAmount;
          BigDecimal previousDerogatoryBalanceAmount =
              previousFixedAssetDerogatoryLine.get() == null
                  ? BigDecimal.ZERO
                  : previousFixedAssetDerogatoryLine.get().getDerogatoryBalanceAmount();
          if (derogatoryAmount == null) {
            derogatoryBalanceAmount =
                BigDecimal.ZERO
                    .subtract(incomeDepreciationAmount)
                    .add(previousDerogatoryBalanceAmount);
          } else {
            derogatoryBalanceAmount =
                derogatoryAmount.subtract(BigDecimal.ZERO).add(previousDerogatoryBalanceAmount);
          }
          FixedAssetDerogatoryLine fixedAssetDerogatoryLine =
              createFixedAssetDerogatoryLine(
                  date,
                  depreciationAmount,
                  fiscalDepreciationAmount,
                  derogatoryAmount,
                  incomeDepreciationAmount,
                  derogatoryBalanceAmount,
                  null,
                  null);
          //Adding to the result list and setting previousLine to the current line (for the next line)
          fixedAssetDerogatoryLine.setFixedAsset(fixedAsset);
          fixedAssetDerogatoryLineList.add(fixedAssetDerogatoryLine);
          previousFixedAssetDerogatoryLine.set(fixedAssetDerogatoryLine);
        });

    return fixedAssetDerogatoryLineList;
  }
}
