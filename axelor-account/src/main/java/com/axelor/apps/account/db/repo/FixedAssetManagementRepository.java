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
package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BarcodeTypeConfig;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.BarcodeGeneratorService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaFile;
import com.axelor.studio.db.AppAccount;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import javax.persistence.PersistenceException;

public class FixedAssetManagementRepository extends FixedAssetRepository {

  protected AppAccountService appAcccountService;
  protected BarcodeGeneratorService barcodeGeneratorService;

  @Inject
  public FixedAssetManagementRepository(
      AppAccountService appAcccountService, BarcodeGeneratorService barcodeGeneratorService) {
    this.appAcccountService = appAcccountService;
    this.barcodeGeneratorService = barcodeGeneratorService;
  }

  @Override
  public FixedAsset save(FixedAsset fixedAsset) {
    try {
      computeReference(fixedAsset);
      // Default value if null or empty
      if (fixedAsset.getDepreciationPlanSelect() == null
          || fixedAsset.getDepreciationPlanSelect().isEmpty()) {
        fixedAsset.setDepreciationPlanSelect(DEPRECIATION_PLAN_NONE);
      }
      if (!ObjectUtils.isEmpty(fixedAsset.getSerialNumber())
          && fixedAsset.getBarcode() == null
          && appAcccountService.getAppAccount().getActivateFixedAssetBarCodeGeneration()) {
        if (!isSerialNumberUniqueForCompany(fixedAsset)) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_NO_UNIQUE_KEY,
              "This serial number is already used for this company.");
        }
        // barcode generation
        generateBarcode(fixedAsset);
      }
      return super.save(fixedAsset);
    } catch (Exception e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
    }
  }

  protected boolean isSerialNumberUniqueForCompany(FixedAsset fixedAsset) {
    Boolean isUnique =
        all()
                .filter("self.company = :company AND self.serialNumber = :serialNumber")
                .bind("company", fixedAsset.getCompany())
                .bind("serialNumber", fixedAsset.getSerialNumber())
                .fetchStream()
                .count()
            <= 1;
    return isUnique;
  }

  protected void generateBarcode(FixedAsset fixedAsset) {
    BarcodeTypeConfig barcodeTypeConfig;

    AppAccount appAccount = appAcccountService.getAppAccount();
    if (!appAccount.getEditFixedAssetBarcodeType() || fixedAsset.getBarcodeTypeConfig() == null) {
      barcodeTypeConfig = appAccount.getFixedAssetBarcodeTypeConfig();

    } else {
      barcodeTypeConfig = fixedAsset.getBarcodeTypeConfig();
    }
    if (barcodeTypeConfig == null) {
      return;
    }
    MetaFile barcodeFile =
        barcodeGeneratorService.createBarCode(
            fixedAsset.getId(),
            "FixedAssetBarCode%d.png",
            fixedAsset.getSerialNumber(),
            barcodeTypeConfig,
            false);
    if (barcodeFile != null) {
      fixedAsset.setBarcode(barcodeFile);
    }
  }

  protected void computeReference(FixedAsset fixedAsset) {
    try {

      if (fixedAsset.getId() != null && Strings.isNullOrEmpty(fixedAsset.getReference())) {
        fixedAsset.setReference(
            Beans.get(SequenceService.class).getDraftSequenceNumber(fixedAsset));
      }
    } catch (Exception e) {
      throw new PersistenceException(e.getMessage(), e);
    }
  }

  @Override
  public FixedAsset copy(FixedAsset entity, boolean deep) {

    FixedAsset copy = super.copy(entity, deep);
    copy.setStatusSelect(STATUS_DRAFT);
    copy.setFixedAssetSeq(null);
    copy.setReference(null);
    copy.setFixedAssetLineList(null);
    copy.setFiscalFixedAssetLineList(null);
    copy.setFixedAssetDerogatoryLineList(null);
    copy.setIfrsFixedAssetLineList(null);
    copy.setAssociatedFixedAssetsSet(null);
    copy.setCorrectedAccountingValue(null);
    copy.setSaleAccountMove(null);
    copy.setDisposalMove(null);
    return copy;
  }

  @Override
  public void remove(FixedAsset entity) {
    if (entity.getStatusSelect() != FixedAssetRepository.STATUS_DRAFT) {
      throw new PersistenceException(
          I18n.get(AccountExceptionMessage.FIXED_ASSET_CAN_NOT_BE_REMOVE));
    }
    super.remove(entity);
  }
}
