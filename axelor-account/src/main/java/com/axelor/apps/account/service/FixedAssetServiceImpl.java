/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetLine;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.FixedAssetLineRepository;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FixedAssetServiceImpl implements FixedAssetService {

  @Inject FixedAssetRepository fixedAssetRepo;

  @Inject FixedAssetLineService fixedAssetLineService;

  @Inject private MoveCreateService moveCreateService;

  @Inject private MoveRepository moveRepo;

  @Override
  public FixedAsset generateAndcomputeLines(FixedAsset fixedAsset) {

    BigDecimal depreciationValue = this.computeDepreciationValue(fixedAsset);
    BigDecimal cumulativeValue = depreciationValue;
    LocalDate depreciationDate = fixedAsset.getFirstDepreciationDate();
    int numberOfDepreciation = fixedAsset.getNumberOfDepreciation();
    LocalDate endDate = depreciationDate.plusMonths(fixedAsset.getDurationInMonth());
    if (fixedAsset.getFixedAssetCategory().getIsProrataTemporis()
        && fixedAsset.getComputationMethodSelect().equals("linear")
        && depreciationDate.isAfter(depreciationDate.with(TemporalAdjusters.firstDayOfYear()))) {
      endDate = endDate.plusMonths(fixedAsset.getPeriodicityInMonth());
      numberOfDepreciation++;
    }
    int counter = 1;
    int scale = Beans.get(AppBaseService.class).getNbDecimalDigitForUnitPrice();
    numberOfDepreciation--;

    while (depreciationDate.isBefore(endDate)) {
      FixedAssetLine fixedAssetLine = new FixedAssetLine();
      fixedAssetLine.setStatusSelect(FixedAssetLineRepository.STATUS_PLANNED);
      fixedAssetLine.setDepreciationDate(depreciationDate);
      fixedAssetLine.setDepreciation(depreciationValue);
      fixedAssetLine.setCumulativeDepreciation(cumulativeValue);
      fixedAssetLine.setResidualValue(
          fixedAsset.getGrossValue().subtract(fixedAssetLine.getCumulativeDepreciation()));

      fixedAsset.addFixedAssetLineListItem(fixedAssetLine);
      if (counter == numberOfDepreciation) {
        depreciationValue = fixedAssetLine.getResidualValue();
        cumulativeValue = cumulativeValue.add(depreciationValue);
        depreciationDate = depreciationDate.plusMonths(fixedAsset.getPeriodicityInMonth());
        counter++;
        continue;
      }

      if (fixedAsset.getComputationMethodSelect().equals("degressive")) {
        if (counter > 2 && fixedAsset.getNumberOfDepreciation() > 3) {
          if (counter == 3) {
            int remainingYear = fixedAsset.getNumberOfDepreciation() - 3;
            depreciationValue =
                fixedAssetLine
                    .getResidualValue()
                    .divide(new BigDecimal(remainingYear), RoundingMode.HALF_EVEN);
          }
        } else {
          depreciationValue =
              this.computeDepreciation(fixedAsset, fixedAssetLine.getResidualValue(), false);
        }
        depreciationDate = depreciationDate.plusMonths(fixedAsset.getPeriodicityInMonth());
      } else {
        depreciationValue =
            this.computeDepreciation(fixedAsset, fixedAsset.getResidualValue(), false);
        depreciationDate = depreciationDate.plusMonths(fixedAsset.getPeriodicityInMonth());
      }
      depreciationValue = depreciationValue.setScale(scale, RoundingMode.HALF_EVEN);
      cumulativeValue =
          cumulativeValue.add(depreciationValue).setScale(scale, RoundingMode.HALF_EVEN);
      counter++;
    }
    return fixedAsset;
  }

  private BigDecimal computeDepreciationValue(FixedAsset fixedAsset) {
    BigDecimal depreciationValue = BigDecimal.ZERO;
    depreciationValue = this.computeDepreciation(fixedAsset, fixedAsset.getGrossValue(), true);
    return depreciationValue;
  }

  private BigDecimal computeProrataTemporis(FixedAsset fixedAsset, boolean isFirstYear) {
    int scale = Beans.get(AppBaseService.class).getNbDecimalDigitForUnitPrice();
    float prorataTemporis = 1;
    if (isFirstYear && fixedAsset.getFixedAssetCategory().getIsProrataTemporis()) {

      LocalDate acquisitionDate = fixedAsset.getAcquisitionDate();
      LocalDate depreciationDate = fixedAsset.getFirstDepreciationDate();

      long monthsBetweenDates =
          ChronoUnit.MONTHS.between(
              acquisitionDate.withDayOfMonth(1), depreciationDate.withDayOfMonth(1));

      monthsBetweenDates += (Math.abs(acquisitionDate.getYear() - depreciationDate.getYear()) * 12);
      prorataTemporis = monthsBetweenDates / fixedAsset.getPeriodicityInMonth().floatValue();
    }
    return new BigDecimal(prorataTemporis).setScale(scale, RoundingMode.HALF_EVEN);
  }

  private BigDecimal computeDepreciation(
      FixedAsset fixedAsset, BigDecimal residualValue, boolean isFirstYear) {

    int scale = Beans.get(AppBaseService.class).getNbDecimalDigitForUnitPrice();
    float depreciationRate = 1f / fixedAsset.getNumberOfDepreciation() * 100f;
    BigDecimal ddRate = BigDecimal.ONE;
    BigDecimal prorataTemporis = this.computeProrataTemporis(fixedAsset, isFirstYear);
    if (fixedAsset.getComputationMethodSelect().equals("degressive")) {
      ddRate = fixedAsset.getDegressiveCoef();
    }
    return residualValue
        .multiply(new BigDecimal(depreciationRate))
        .multiply(ddRate)
        .multiply(prorataTemporis)
        .divide(new BigDecimal(100), scale);
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void createFixedAsset(Invoice invoice) throws AxelorException {

    if (invoice != null && invoice.getInvoiceLineList() != null) {

      AccountConfig accountConfig =
          Beans.get(AccountConfigService.class).getAccountConfig(invoice.getCompany());

      for (InvoiceLine invoiceLine : invoice.getInvoiceLineList()) {

        if (accountConfig.getFixedAssetCatReqOnInvoice()
            && invoiceLine.getFixedAssets()
            && invoiceLine.getFixedAssetCategory() == null) {
          throw new AxelorException(
              invoiceLine,
              TraceBackRepository.CATEGORY_MISSING_FIELD,
              I18n.get(IExceptionMessage.INVOICE_LINE_ERROR_FIXED_ASSET_CATEGORY),
              invoiceLine.getProductName());
        }

        if (invoiceLine.getFixedAssets() && invoiceLine.getFixedAssetCategory() != null) {

          FixedAsset fixedAsset = new FixedAsset();
          fixedAsset.setFixedAssetCategory(invoiceLine.getFixedAssetCategory());
          if (fixedAsset.getFixedAssetCategory().getIsValidateFixedAsset()) {
            fixedAsset.setStatusSelect(FixedAssetRepository.STATUS_VALIDATED);
          } else {
            fixedAsset.setStatusSelect(FixedAssetRepository.STATUS_DRAFT);
          }
          fixedAsset.setAcquisitionDate(invoice.getInvoiceDate());
          fixedAsset.setFirstDepreciationDate(invoice.getInvoiceDate());
          fixedAsset.setReference(invoice.getInvoiceId());
          fixedAsset.setName(invoiceLine.getProductName() + " (" + invoiceLine.getQty() + ")");
          fixedAsset.setCompany(fixedAsset.getFixedAssetCategory().getCompany());
          fixedAsset.setJournal(fixedAsset.getFixedAssetCategory().getJournal());
          fixedAsset.setComputationMethodSelect(
              fixedAsset.getFixedAssetCategory().getComputationMethodSelect());
          fixedAsset.setDegressiveCoef(fixedAsset.getFixedAssetCategory().getDegressiveCoef());
          fixedAsset.setNumberOfDepreciation(
              fixedAsset.getFixedAssetCategory().getNumberOfDepreciation());
          fixedAsset.setPeriodicityInMonth(
              fixedAsset.getFixedAssetCategory().getPeriodicityInMonth());
          fixedAsset.setDurationInMonth(fixedAsset.getFixedAssetCategory().getDurationInMonth());
          fixedAsset.setGrossValue(invoiceLine.getCompanyExTaxTotal());
          fixedAsset.setPartner(invoice.getPartner());
          fixedAsset.setPurchaseAccount(invoiceLine.getAccount());
          fixedAsset.setInvoiceLine(invoiceLine);

          this.generateAndcomputeLines(fixedAsset);

          fixedAssetRepo.save(fixedAsset);
        }
      }
    }
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void disposal(LocalDate disposalDate, BigDecimal disposalAmount, FixedAsset fixedAsset)
      throws AxelorException {

    if (disposalAmount.compareTo(BigDecimal.ZERO) != 0) {

      if (disposalAmount.compareTo(fixedAsset.getResidualValue()) <= 0) {

        FixedAssetLine depreciationFixedAssetLine =
            generateProrataDepreciationLine(fixedAsset, disposalDate);

        FixedAssetLine fixedAssetLine = new FixedAssetLine();
        BigDecimal depreciationValue =
            depreciationFixedAssetLine.getResidualValue().subtract(disposalAmount).abs();
        BigDecimal cumulativeDepreciation =
            depreciationFixedAssetLine.getCumulativeDepreciation().add(depreciationValue);
        fixedAssetLine.setDepreciationDate(disposalDate);
        fixedAssetLine.setDepreciation(depreciationValue);
        fixedAssetLine.setCumulativeDepreciation(cumulativeDepreciation);
        fixedAssetLine.setResidualValue(
            fixedAsset.getGrossValue().subtract(cumulativeDepreciation));

        if (fixedAssetLine.getResidualValue().compareTo(BigDecimal.ZERO) == 0) {
          fixedAsset.setStatusSelect(FixedAssetRepository.STATUS_TRANSFERRED);
        }
        fixedAsset.setDisposalDate(disposalDate);
        fixedAsset.setDisposalValue(fixedAsset.getDisposalValue().add(disposalAmount));
        fixedAsset.addFixedAssetLineListItem(fixedAssetLine);

        fixedAsset.setStatusSelect(FixedAssetRepository.STATUS_TRANSFERRED);
        fixedAsset.setDisposalDate(disposalDate);
        fixedAsset.setDisposalValue(disposalAmount);

        fixedAssetLineService.realize(depreciationFixedAssetLine);
        fixedAssetLineService.realize(fixedAssetLine);
        generateDisposalMove(fixedAssetLine);
      }
    } else {
      if (disposalAmount.compareTo(fixedAsset.getResidualValue()) != 0) {
        return;
      }
    }
    List<FixedAssetLine> fixedAssetLineList =
        fixedAsset
            .getFixedAssetLineList()
            .stream()
            .filter(
                fixedAssetLine ->
                    fixedAssetLine.getStatusSelect() == FixedAssetLineRepository.STATUS_PLANNED)
            .collect(Collectors.toList());
    for (FixedAssetLine fixedAssetLine : fixedAssetLineList) {
      fixedAsset.removeFixedAssetLineListItem(fixedAssetLine);
    }
    fixedAssetRepo.save(fixedAsset);
  }

  private FixedAssetLine generateProrataDepreciationLine(
      FixedAsset fixedAsset, LocalDate disposalDate) {
    FixedAssetLine previousFixedAssetLine =
        fixedAsset.getFixedAssetLineList().get(fixedAsset.getFixedAssetLineList().size() - 1);
    long monthsBetweenDates =
        ChronoUnit.MONTHS.between(
            previousFixedAssetLine.getDepreciationDate().withDayOfMonth(1),
            disposalDate.withDayOfMonth(1));

    FixedAssetLine fixedAssetLine = new FixedAssetLine();
    fixedAssetLine.setDepreciationDate(disposalDate);
    BigDecimal prorataTemporis =
        new BigDecimal(monthsBetweenDates / fixedAsset.getPeriodicityInMonth().floatValue());

    int scale = Beans.get(AppBaseService.class).getNbDecimalDigitForUnitPrice();
    float depreciationRate = 1f / fixedAsset.getNumberOfDepreciation() * 100f;
    BigDecimal ddRate = BigDecimal.ONE;
    if (fixedAsset.getComputationMethodSelect().equals("degressive")) {
      ddRate = fixedAsset.getDegressiveCoef();
    }
    BigDecimal deprecationValue =
        fixedAsset
            .getResidualValue()
            .multiply(new BigDecimal(depreciationRate))
            .multiply(ddRate)
            .multiply(prorataTemporis)
            .divide(new BigDecimal(100), scale);

    fixedAssetLine.setDepreciation(deprecationValue);
    fixedAssetLine.setCumulativeDepreciation(deprecationValue);
    fixedAssetLine.setResidualValue(
        fixedAsset.getGrossValue().subtract(fixedAssetLine.getCumulativeDepreciation()));
    fixedAsset.addFixedAssetLineListItem(fixedAssetLine);
    return fixedAssetLine;
  }

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  private void generateDisposalMove(FixedAssetLine fixedAssetLine) throws AxelorException {
    FixedAsset fixedAsset = fixedAssetLine.getFixedAsset();

    Journal journal = fixedAsset.getJournal();
    Company company = fixedAsset.getCompany();
    Partner partner = fixedAsset.getPartner();
    LocalDate date = fixedAsset.getAcquisitionDate();

    // Creating move
    Move move =
        moveCreateService.createMove(
            journal,
            company,
            company.getCurrency(),
            partner,
            date,
            null,
            MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC);

    if (move != null) {
      List<MoveLine> moveLines = new ArrayList<MoveLine>();

      String origin = fixedAsset.getReference();
      Account chargeAccount = fixedAsset.getFixedAssetCategory().getChargeAccount();
      Account depreciationAccount = fixedAsset.getFixedAssetCategory().getDepreciationAccount();
      Account purchaseAccount = fixedAsset.getPurchaseAccount();
      BigDecimal chargeAmount = fixedAssetLine.getResidualValue();
      BigDecimal cumulativeDepreciationAmount = fixedAssetLine.getCumulativeDepreciation();

      // Creating accounting debit move line for charge account
      MoveLine chargeAccountDebitMoveLine =
          new MoveLine(
              move,
              partner,
              chargeAccount,
              date,
              null,
              1,
              chargeAmount,
              BigDecimal.ZERO,
              fixedAsset.getName(),
              origin,
              null,
              BigDecimal.ZERO,
              date);
      moveLines.add(chargeAccountDebitMoveLine);

      // Creating accounting debit move line for deprecation account
      MoveLine deprecationAccountDebitMoveLine =
          new MoveLine(
              move,
              partner,
              depreciationAccount,
              date,
              null,
              1,
              cumulativeDepreciationAmount,
              BigDecimal.ZERO,
              fixedAsset.getName(),
              origin,
              null,
              BigDecimal.ZERO,
              date);
      moveLines.add(deprecationAccountDebitMoveLine);

      // Creating accounting credit move line
      MoveLine creditMoveLine =
          new MoveLine(
              move,
              partner,
              purchaseAccount,
              date,
              null,
              2,
              BigDecimal.ZERO,
              fixedAsset.getGrossValue(),
              fixedAsset.getName(),
              origin,
              null,
              BigDecimal.ZERO,
              date);
      moveLines.add(creditMoveLine);

      move.getMoveLineList().addAll(moveLines);
    }

    moveRepo.save(move);

    fixedAsset.setDisposalMove(move);
  }
}
