package com.axelor.apps.account.service.fixedasset;

import static com.axelor.apps.account.service.fixedasset.FixedAssetServiceImpl.RETURNED_SCALE;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetDerogatoryLine;
import com.axelor.apps.account.db.FixedAssetLine;
import com.axelor.apps.account.db.repo.FixedAssetDerogatoryLineRepository;
import com.axelor.apps.account.db.repo.FixedAssetLineRepository;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;

public class FixedAssetDerogatoryLineServiceImpl implements FixedAssetDerogatoryLineService {

  protected FixedAssetDerogatoryLineMoveService fixedAssetDerogatoryLineMoveService;
  
  protected FixedAssetDerogatoryLineRepository fixedAssetDerogatoryLineRepository;

  @Inject
  public FixedAssetDerogatoryLineServiceImpl(
      FixedAssetDerogatoryLineMoveService fixedAssetDerogatoryLineMoveService,
      FixedAssetDerogatoryLineRepository fixedAssetDerogatoryLineRepository) {
    this.fixedAssetDerogatoryLineMoveService = fixedAssetDerogatoryLineMoveService;
    this.fixedAssetDerogatoryLineRepository = fixedAssetDerogatoryLineRepository;
  }

  @Override
  public FixedAssetDerogatoryLine createFixedAssetDerogatoryLine(
      LocalDate depreciationDate,
      BigDecimal depreciationAmount,
      BigDecimal fiscalDepreciationAmount,
      BigDecimal derogatoryAmount,
      BigDecimal incomeDepreciationAmount,
      BigDecimal derogatoryBalanceAmount,
      FixedAssetLine fixedAssetLine,
      FixedAssetLine fiscalFixedAssetLine,
      int statusSelect) {

    FixedAssetDerogatoryLine fixedAssetDerogatoryLine = new FixedAssetDerogatoryLine();
    fixedAssetDerogatoryLine.setStatusSelect(statusSelect);
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
  public void computeDerogatoryBalanceAmount(List<FixedAssetDerogatoryLine> fixedAssetDerogatoryLineList) {
	  if (fixedAssetDerogatoryLineList != null) {
		  fixedAssetDerogatoryLineList.sort((line1, line2) -> line1.getDepreciationDate().compareTo(line2.getDepreciationDate()));
		  FixedAssetDerogatoryLine previousFixedAssetDerogatoryLine = null;
		  for (FixedAssetDerogatoryLine line: fixedAssetDerogatoryLineList) {
			  line.setDerogatoryBalanceAmount(computeDerogatoryBalanceAmount(previousFixedAssetDerogatoryLine, line.getDerogatoryAmount(), line.getIncomeDepreciationAmount()));
			  previousFixedAssetDerogatoryLine = line;
		  }
	  }

  }

  @Override
  public List<FixedAssetDerogatoryLine> computePlannedFixedAssetDerogatoryLineList(FixedAsset fixedAsset) {
    // Preparation of data needed for computation
    List<FixedAssetDerogatoryLine> fixedAssetDerogatoryLineList = new ArrayList<>();
    List<FixedAssetLine> tmpList = new ArrayList<>();
    //This method will only compute line that are not realized.
    tmpList.addAll(fixedAsset.getFiscalFixedAssetLineList().stream().filter(line -> line.getStatusSelect() == FixedAssetLineRepository.STATUS_PLANNED).collect(Collectors.toList()));
    tmpList.addAll(fixedAsset.getFixedAssetLineList().stream().filter(line -> line.getStatusSelect() == FixedAssetLineRepository.STATUS_PLANNED).collect(Collectors.toList()));

    // Sorting by depreciation date
    tmpList.sort((f1, f2) -> f1.getDepreciationDate().compareTo(f2.getDepreciationDate()));

    // Grouping lines from both list by date and keeping the order (because we want to have the
    // previous line)
    LinkedHashMap<LocalDate, List<FixedAssetLine>> dateFixedAssetLineGrouped =
        tmpList.stream()
            .collect(
                Collectors.groupingBy(
                    FixedAssetLine::getDepreciationDate, LinkedHashMap::new, Collectors.toList()));
    // Since we are working on lambda, we need an AtomicReference to store the
    // previousFixedAssetDerogatoryLine
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

          // Initialisation of fiscal and economic depreciation
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

          // If fiscal depreciation is greater than economic depreciation then we fill
          // derogatoryAmount, else incomeDepreciation.
          if (fiscalDepreciationAmount.compareTo(depreciationAmount) > 0) {
            derogatoryAmount = (fiscalDepreciationAmount.subtract(depreciationAmount)).abs();
          } else {
            incomeDepreciationAmount =
                (fiscalDepreciationAmount.subtract(depreciationAmount)).abs();
          }

          BigDecimal derogatoryBalanceAmount = computeDerogatoryBalanceAmount(previousFixedAssetDerogatoryLine.get(),
				derogatoryAmount, incomeDepreciationAmount);
          FixedAssetDerogatoryLine fixedAssetDerogatoryLine =
              createFixedAssetDerogatoryLine(
                  date,
                  depreciationAmount,
                  fiscalDepreciationAmount,
                  derogatoryAmount,
                  incomeDepreciationAmount,
                  derogatoryBalanceAmount,
                  null,
                  null,
                  FixedAssetLineRepository.STATUS_PLANNED);
          // Adding to the result list and setting previousLine to the current line (for the next
          // line)
          fixedAssetDerogatoryLine.setFixedAsset(fixedAsset);
          fixedAssetDerogatoryLineList.add(fixedAssetDerogatoryLine);
          previousFixedAssetDerogatoryLine.set(fixedAssetDerogatoryLine);
        });

    return fixedAssetDerogatoryLineList;
  }

private BigDecimal computeDerogatoryBalanceAmount(
		FixedAssetDerogatoryLine previousFixedAssetDerogatoryLine, BigDecimal derogatoryAmount,
		BigDecimal incomeDepreciationAmount) {
	BigDecimal derogatoryBalanceAmount;
	  BigDecimal previousDerogatoryBalanceAmount =
			  previousFixedAssetDerogatoryLine == null
	          ? BigDecimal.ZERO
	          : previousFixedAssetDerogatoryLine.getDerogatoryBalanceAmount();
	  if (derogatoryAmount == null || derogatoryAmount.signum() == 0) {
	    derogatoryBalanceAmount =
	        BigDecimal.ZERO
	            .subtract(incomeDepreciationAmount)
	            .add(previousDerogatoryBalanceAmount);
	  } else {
	    derogatoryBalanceAmount =
	        derogatoryAmount.subtract(BigDecimal.ZERO).add(previousDerogatoryBalanceAmount);
	  }
	return derogatoryBalanceAmount;
}

  @Override
  public void multiplyLinesBy(
      List<FixedAssetDerogatoryLine> fixedAssetDerogatoryLine, BigDecimal prorata) {

    if (fixedAssetDerogatoryLine != null) {
      fixedAssetDerogatoryLine.forEach(line -> multiplyLineBy(line, prorata));
    }
  }

  private void multiplyLineBy(FixedAssetDerogatoryLine line, BigDecimal prorata) {

    line.setDepreciationAmount(
        prorata
            .multiply(line.getDepreciationAmount())
            .setScale(RETURNED_SCALE, RoundingMode.HALF_UP));
    line.setFiscalDepreciationAmount(
        prorata
            .multiply(line.getFiscalDepreciationAmount())
            .setScale(RETURNED_SCALE, RoundingMode.HALF_UP));
    line.setDerogatoryAmount(
        prorata
            .multiply(line.getDerogatoryAmount())
            .setScale(RETURNED_SCALE, RoundingMode.HALF_UP));
    line.setIncomeDepreciationAmount(
        prorata
            .multiply(line.getIncomeDepreciationAmount())
            .setScale(RETURNED_SCALE, RoundingMode.HALF_UP));
    line.setDerogatoryBalanceAmount(
        prorata
            .multiply(line.getDerogatoryBalanceAmount())
            .setScale(RETURNED_SCALE, RoundingMode.HALF_UP));
  }

  @Override
  public void generateDerogatoryCessionMove(
      FixedAssetDerogatoryLine firstPlannedDerogatoryLine,
      FixedAssetDerogatoryLine lastRealizedDerogatoryLine)
      throws AxelorException {
    Objects.requireNonNull(firstPlannedDerogatoryLine);
    Account creditAccount = computeCessionCreditAccount(firstPlannedDerogatoryLine);
    Account debitAccount = computeCessionDebitAccount(firstPlannedDerogatoryLine);
    BigDecimal lastDerogatoryBalanceAmount =
        lastRealizedDerogatoryLine == null
            ? BigDecimal.ZERO
            : lastRealizedDerogatoryLine.getDerogatoryBalanceAmount();
    BigDecimal amount =
        firstPlannedDerogatoryLine
            .getDerogatoryBalanceAmount()
            .subtract(lastDerogatoryBalanceAmount)
            .abs();
    fixedAssetDerogatoryLineMoveService.generateMove(
        firstPlannedDerogatoryLine, creditAccount, debitAccount, amount);
  }

  private Account computeCessionDebitAccount(FixedAssetDerogatoryLine fixedAssetDerogatoryLine) {
    FixedAsset fixedAsset = fixedAssetDerogatoryLine.getFixedAsset();
    if (fixedAssetDerogatoryLine.getDerogatoryBalanceAmount().compareTo(BigDecimal.ZERO) > 0) {
      return fixedAsset.getFixedAssetCategory().getExpenseDepreciationDerogatoryAccount();
    } else if (fixedAssetDerogatoryLine.getDerogatoryBalanceAmount().compareTo(BigDecimal.ZERO)
        < 0) {
      return fixedAsset.getFixedAssetCategory().getCapitalDepreciationDerogatoryAccount();
    }
    return fixedAsset.getFixedAssetCategory().getExpenseDepreciationDerogatoryAccount();
  }

  private Account computeCessionCreditAccount(FixedAssetDerogatoryLine fixedAssetDerogatoryLine) {
    FixedAsset fixedAsset = fixedAssetDerogatoryLine.getFixedAsset();
    if (fixedAssetDerogatoryLine.getDerogatoryBalanceAmount().compareTo(BigDecimal.ZERO) > 0) {
      return fixedAsset.getFixedAssetCategory().getCapitalDepreciationDerogatoryAccount();
    } else if (fixedAssetDerogatoryLine.getDerogatoryBalanceAmount().compareTo(BigDecimal.ZERO)
        < 0) {
      return fixedAsset.getFixedAssetCategory().getIncomeDepreciationDerogatoryAccount();
    }
    return fixedAsset.getFixedAssetCategory().getCapitalDepreciationDerogatoryAccount();
  }
  
  @Override
  public void copyFixedAssetDerogatoryLineList(FixedAsset fixedAsset, FixedAsset newFixedAsset) {
	if (newFixedAsset.getFixedAssetDerogatoryLineList() == null) {
		if (fixedAsset.getFixedAssetDerogatoryLineList() != null) {
			fixedAsset.getFixedAssetDerogatoryLineList()
			.forEach(line -> {
				FixedAssetDerogatoryLine copy = fixedAssetDerogatoryLineRepository.copy(line, false);
                copy.setFixedAsset(newFixedAsset);
                newFixedAsset.addFixedAssetDerogatoryLineListItem(fixedAssetDerogatoryLineRepository.save(copy));
			});
		}
	}
	
}
}
