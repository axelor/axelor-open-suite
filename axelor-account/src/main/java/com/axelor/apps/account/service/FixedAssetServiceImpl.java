/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetLine;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.FixedAssetLineRepository;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.config.AccountConfigService;
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

public class FixedAssetServiceImpl implements FixedAssetService {

  @Inject FixedAssetRepository fixedAssetRepo;

  @Inject FixedAssetLineService fixedAssetLineService;

  @Override
  public FixedAsset generateAndcomputeLines(FixedAsset fixedAsset) {

    BigDecimal depreciationValue = this.computeDepreciationValue(fixedAsset);
    BigDecimal cumulativeValue = depreciationValue;
    LocalDate depreciationDate = fixedAsset.getFirstDepreciationDate().plusYears(1);
    LocalDate endDate = depreciationDate.plusMonths(fixedAsset.getDurationInMonth());
    int counter = 1;

    while (depreciationDate.isBefore(endDate)) {
      FixedAssetLine fixedAssetLine = new FixedAssetLine();
      fixedAssetLine.setStatusSelect(FixedAssetLineRepository.STATUS_PLANNED);
      fixedAssetLine.setDepreciationDate(depreciationDate);
      fixedAssetLine.setDepreciation(depreciationValue);
      fixedAssetLine.setCumulativeDepreciation(cumulativeValue);
      fixedAssetLine.setResidualValue(
          fixedAsset.getGrossValue().subtract(fixedAssetLine.getCumulativeDepreciation()));

      fixedAsset.addFixedAssetLineListItem(fixedAssetLine);

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
              this.computeDegressiveDepreciation(
                  fixedAsset, fixedAssetLine.getResidualValue(), false);
        }
      }
      cumulativeValue = cumulativeValue.add(depreciationValue);
      depreciationDate = depreciationDate.plusMonths(fixedAsset.getPeriodicityInMonth());
      counter++;
    }
    return fixedAsset;
  }

  private BigDecimal computeDepreciationValue(FixedAsset fixedAsset) {
    int scale = Beans.get(AppBaseService.class).getNbDecimalDigitForUnitPrice();
    BigDecimal depreciationValue = BigDecimal.ZERO;
    if (fixedAsset.getComputationMethodSelect().equals("degressive")) {
      depreciationValue =
          this.computeDegressiveDepreciation(fixedAsset, fixedAsset.getGrossValue(), true);
    } else {
      depreciationValue =
          fixedAsset
              .getGrossValue()
              .divide(
                  new BigDecimal(fixedAsset.getNumberOfDepreciation()),
                  scale,
                  RoundingMode.HALF_EVEN);
    }
    return depreciationValue;
  }

  private BigDecimal computeProrataTemporis(FixedAsset fixedAsset, boolean isFirstYear) {
    int scale = Beans.get(AppBaseService.class).getNbDecimalDigitForUnitPrice();
    float prorataTemporis = 1;
    if (isFirstYear && fixedAsset.getFixedAssetCategory().getIsProrataTemporis()) {
      LocalDate date = fixedAsset.getAcquisitionDate();
      if (date.getMonthValue() > 1) {
        prorataTemporis =
            (fixedAsset.getPeriodicityInMonth().floatValue() - (date.getMonthValue() - 1))
                / fixedAsset.getPeriodicityInMonth().floatValue();
      }
    }
    return new BigDecimal(prorataTemporis).setScale(scale, RoundingMode.HALF_EVEN);
  }

  private BigDecimal computeDegressiveDepreciation(
      FixedAsset fixedAsset, BigDecimal residualValue, boolean isFirstYear) {

    int scale = Beans.get(AppBaseService.class).getNbDecimalDigitForUnitPrice();
    BigDecimal sdRate =
        BigDecimal.ONE
            .divide(
                new BigDecimal(fixedAsset.getNumberOfDepreciation()), scale, RoundingMode.HALF_EVEN)
            .multiply(new BigDecimal(100));
    BigDecimal ddRate =
        sdRate.multiply(fixedAsset.getDegressiveCoef()).divide(new BigDecimal(100), scale);
    BigDecimal prorataTemporis = this.computeProrataTemporis(fixedAsset, isFirstYear);

    return residualValue.multiply(ddRate).multiply(prorataTemporis);
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

        if (accountConfig.getFixedAssetCatReqOnInvoice()
            && invoiceLine.getFixedAssets()
            && invoiceLine.getFixedAssetCategory() != null) {

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
        BigDecimal cumulativeDepreciation =
            fixedAsset.getGrossValue().subtract(fixedAsset.getResidualValue()).add(disposalAmount);

        FixedAssetLine fixedAssetLine = new FixedAssetLine();
        fixedAssetLine.setDepreciationDate(disposalDate);
        fixedAssetLine.setDepreciation(disposalAmount);
        fixedAssetLine.setCumulativeDepreciation(cumulativeDepreciation);
        fixedAssetLine.setResidualValue(
            fixedAsset.getGrossValue().subtract(cumulativeDepreciation));

        if (fixedAssetLine.getResidualValue().compareTo(BigDecimal.ZERO) == 0) {
          fixedAsset.setStatusSelect(FixedAssetRepository.STATUS_TRANSFERRED);
        }
        fixedAsset.setDisposalDate(disposalDate);
        fixedAsset.setDisposalValue(fixedAsset.getDisposalValue().add(disposalAmount));
        fixedAsset.addFixedAssetLineListItem(fixedAssetLine);

        fixedAssetLineService.realize(fixedAssetLine);
      }
    } else {
      if (disposalAmount.compareTo(fixedAsset.getResidualValue()) != 0) {
        return;
      }
      fixedAsset.setStatusSelect(FixedAssetRepository.STATUS_TRANSFERRED);
      fixedAsset.setDisposalDate(disposalDate);
      fixedAsset.setDisposalValue(disposalAmount);
    }
    fixedAssetRepo.save(fixedAsset);
  }
}
