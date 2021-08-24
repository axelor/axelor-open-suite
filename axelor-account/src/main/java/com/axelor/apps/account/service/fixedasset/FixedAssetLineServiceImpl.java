package com.axelor.apps.account.service.fixedasset;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetLine;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.FixedAssetLineRepository;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class FixedAssetLineServiceImpl implements FixedAssetLineService {

  protected FixedAssetRepository fixedAssetRepository;
  protected FixedAssetLineRepository fixedAssetLineRepository;

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject
  public FixedAssetLineServiceImpl(
      FixedAssetRepository fixedAssetRepository,
      FixedAssetLineRepository fixedAssetLineRepository) {
    this.fixedAssetRepository = fixedAssetRepository;
    this.fixedAssetLineRepository = fixedAssetLineRepository;
  }
  /**
   * {@inheritDoc}
   *
   * @throws NullPointerException if moveLine is null
   */
  @Override
  public FixedAsset generateFixedAsset(MoveLine moveLine) {
    log.debug("Starting generation of fixed asset for move line :" + moveLine);
    Objects.requireNonNull(moveLine);

    FixedAsset fixedAsset = new FixedAsset();
    fixedAsset.setStatusSelect(FixedAssetRepository.STATUS_DRAFT);
    fixedAsset.setName(moveLine.getDescription());
    if (moveLine.getMove() != null) {
      fixedAsset.setCompany(moveLine.getMove().getCompany());
      fixedAsset.setJournal(moveLine.getMove().getJournal());
    }
    fixedAsset.setFixedAssetCategory(moveLine.getFixedAssetCategory());
    fixedAsset.setPartner(moveLine.getPartner());
    fixedAsset.setPurchaseAccount(moveLine.getAccount());
    if (moveLine.getFixedAssetCategory() != null)
      fixedAsset.setAnalyticDistributionTemplate(
          moveLine.getFixedAssetCategory().getAnalyticDistributionTemplate());
    LocalDate acquisitionDate =
        moveLine.getOriginDate() != null ? moveLine.getOriginDate() : moveLine.getDate();
    fixedAsset.setAcquisitionDate(acquisitionDate);
    fixedAsset.setFirstServiceDate(acquisitionDate);
    fixedAsset.setFirstDepreciationDate(acquisitionDate);
    log.debug("Generated fixed asset : " + fixedAsset);
    return fixedAsset;
  }

  @Transactional
  @Override
  public FixedAsset generateAndSaveFixedAsset(MoveLine moveLine) {

    return fixedAssetRepository.save(generateFixedAsset(moveLine));
  }

  @Override
  public FixedAssetLine generateProrataDepreciationLine(
      FixedAsset fixedAsset, LocalDate disposalDate, FixedAssetLine previousRealizedLine) {
    FixedAssetLine fixedAssetLine = new FixedAssetLine();
    fixedAssetLine.setDepreciationDate(disposalDate);
    computeDepreciationWithProrata(fixedAsset, fixedAssetLine, previousRealizedLine, disposalDate);
    fixedAssetLine.setFixedAsset(fixedAsset);
    fixedAsset.addFixedAssetLineListItem(fixedAssetLine);
    return fixedAssetLine;
  }

  @Override
  public void computeDepreciationWithProrata(
      FixedAsset fixedAsset,
      FixedAssetLine fixedAssetLine,
      FixedAssetLine previousRealizedLine,
      LocalDate disposalDate) {
    LocalDate previousRealizedDate =
        previousRealizedLine != null
            ? previousRealizedLine.getDepreciationDate()
            : fixedAsset.getFirstServiceDate();
    long monthsBetweenDates =
        ChronoUnit.MONTHS.between(
            previousRealizedDate.withDayOfMonth(1), disposalDate.withDayOfMonth(1));

    BigDecimal prorataTemporis =
        BigDecimal.valueOf(monthsBetweenDates)
            .divide(
                BigDecimal.valueOf(fixedAsset.getPeriodicityInMonth()),
                FixedAssetServiceImpl.CALCULATION_SCALE,
                RoundingMode.HALF_UP);

    int numberOfDepreciation =
        fixedAsset.getFixedAssetCategory().getIsProrataTemporis()
            ? fixedAsset.getNumberOfDepreciation() - 1
            : fixedAsset.getNumberOfDepreciation();
    BigDecimal depreciationRate =
        BigDecimal.valueOf(100)
            .divide(
                BigDecimal.valueOf(numberOfDepreciation),
                FixedAssetServiceImpl.CALCULATION_SCALE,
                RoundingMode.HALF_UP);
    BigDecimal ddRate = BigDecimal.ONE;
    if (fixedAsset
        .getComputationMethodSelect()
        .equals(FixedAssetRepository.COMPUTATION_METHOD_DEGRESSIVE)) {
      ddRate = fixedAsset.getDegressiveCoef();
    }
    BigDecimal deprecationValue =
        fixedAsset
            .getGrossValue()
            .multiply(depreciationRate)
            .multiply(ddRate)
            .multiply(prorataTemporis)
            .divide(
                new BigDecimal(100), FixedAssetServiceImpl.RETURNED_SCALE, RoundingMode.HALF_UP);

    fixedAssetLine.setDepreciation(deprecationValue);
    BigDecimal cumulativeValue =
        previousRealizedLine != null
            ? previousRealizedLine.getCumulativeDepreciation().add(deprecationValue)
            : deprecationValue;
    fixedAssetLine.setCumulativeDepreciation(cumulativeValue);
    fixedAssetLine.setAccountingValue(
        fixedAsset.getGrossValue().subtract(fixedAssetLine.getCumulativeDepreciation()));
  }

  @Override
  public void copyFixedAssetLineList(FixedAsset fixedAsset, FixedAsset newFixedAsset) {
    if (newFixedAsset.getFixedAssetLineList() == null) {
      if (fixedAsset.getFixedAssetLineList() != null) {
        fixedAsset
            .getFixedAssetLineList()
            .forEach(
                line -> {
                  FixedAssetLine copy = fixedAssetLineRepository.copy(line, false);
                  copy.setFixedAsset(newFixedAsset);
                  newFixedAsset.addFixedAssetLineListItem(fixedAssetLineRepository.save(copy));
                });
      }
    }
    if (newFixedAsset.getFiscalFixedAssetLineList() == null) {
      if (fixedAsset.getFiscalFixedAssetLineList() != null) {
        fixedAsset
            .getFiscalFixedAssetLineList()
            .forEach(
                line -> {
                  FixedAssetLine copy = fixedAssetLineRepository.copy(line, false);
                  copy.setFixedAsset(newFixedAsset);
                  newFixedAsset.addFiscalFixedAssetLineListItem(
                      fixedAssetLineRepository.save(copy));
                });
      }
    }
  }
@Override
public Optional<FixedAssetLine> findOldestFixedAssetLine(List<FixedAssetLine> fixedAssetLineList, int status,
		int nbLineToSkip) {
    fixedAssetLineList.sort(
            (fa1, fa2) -> fa1.getDepreciationDate().compareTo(fa2.getDepreciationDate()));
        return fixedAssetLineList.stream()
            .filter(fixedAssetLine -> fixedAssetLine.getStatusSelect() == status)
            .findFirst();
}
@Override
public Optional<FixedAssetLine> findNewestFixedAssetLine(List<FixedAssetLine> fixedAssetLineList, int status,
		int nbLineToSkip) {
    fixedAssetLineList.sort(
            (fa1, fa2) -> fa2.getDepreciationDate().compareTo(fa1.getDepreciationDate()));
        Optional<FixedAssetLine> optFixedAssetLine =
            fixedAssetLineList.stream()
                .filter(fixedAssetLine -> fixedAssetLine.getStatusSelect() == status)
                .skip(nbLineToSkip)
                .findFirst();
        return optFixedAssetLine;
}
}
