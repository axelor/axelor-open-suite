/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.web;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.account.service.fixedasset.FixedAssetService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.time.LocalDate;

@Singleton
public class FixedAssetController {

  public void computeDepreciation(ActionRequest request, ActionResponse response) {

    FixedAsset fixedAsset = request.getContext().asType(FixedAsset.class);
    // There is nothing to do if depreciation plan is None.
    if (fixedAsset.getDepreciationPlanSelect() == null
        || fixedAsset
            .getDepreciationPlanSelect()
            .equals(FixedAssetRepository.DEPRECIATION_PLAN_NONE)) {
      return;
    }
    try {
      if (fixedAsset.getGrossValue().compareTo(BigDecimal.ZERO) > 0) {

        if (!fixedAsset.getFixedAssetLineList().isEmpty()) {
          fixedAsset.getFixedAssetLineList().clear();
        }
        if (!fixedAsset.getFiscalFixedAssetLineList().isEmpty()) {
          fixedAsset.getFiscalFixedAssetLineList().clear();
        }
        fixedAsset = Beans.get(FixedAssetService.class).generateAndComputeLines(fixedAsset);

      } else {
        fixedAsset.getFixedAssetLineList().clear();
        fixedAsset.getFiscalFixedAssetLineList().clear();
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
    response.setValue("fixedAssetLineList", fixedAsset.getFixedAssetLineList());
    response.setValue("fiscalFixedAssetLineList", fixedAsset.getFiscalFixedAssetLineList());
    response.setValue("fixedAssetDerogatoryLineList", fixedAsset.getFixedAssetDerogatoryLineList());
  }

  public void disposal(ActionRequest request, ActionResponse response) throws AxelorException {
    Context context = request.getContext();
    if (context.get("disposalDate") == null
        || context.get("disposalAmount") == null
        || context.get("disposalTypeSelect") == null
        || context.get("disposalQtySelect") == null) {
      return;
    }
    LocalDate disposalDate = (LocalDate) context.get("disposalDate");
    BigDecimal disposalAmount = new BigDecimal(context.get("disposalAmount").toString());
    Integer disposalTypeSelect = (Integer) context.get("disposalTypeSelect");
    Integer disposalQtySelect = (Integer) context.get("disposalQtySelect");
    Long fixedAssetId = Long.valueOf(context.get("_id").toString());
    FixedAsset fixedAsset = Beans.get(FixedAssetRepository.class).find(fixedAssetId);

    Beans.get(FixedAssetService.class)
        .disposal(disposalDate, disposalAmount, fixedAsset, disposalTypeSelect, disposalQtySelect);

    response.setCanClose(true);
  }

  public void createAnalyticDistributionWithTemplate(
      ActionRequest request, ActionResponse response) {

    try {
      FixedAsset fixedAsset = request.getContext().asType(FixedAsset.class);

      Beans.get(FixedAssetService.class).updateAnalytic(fixedAsset);

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void computeFirstDepreciationDate(ActionRequest request, ActionResponse response) {

    try {
      FixedAsset fixedAsset = request.getContext().asType(FixedAsset.class);
      Beans.get(FixedAssetService.class).computeFirstDepreciationDate(fixedAsset);

      response.setValue("firstDepreciationDate", fixedAsset.getFirstDepreciationDate());

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void updateDepreciation(ActionRequest request, ActionResponse response) {
    try {
      FixedAsset fixedAsset = request.getContext().asType(FixedAsset.class);
      Beans.get(FixedAssetService.class).updateDepreciation(fixedAsset);
      response.setValue("fixedAssetLineList", fixedAsset.getFixedAssetLineList());
      response.setValue("fiscalFixedAssetLineList", fixedAsset.getFiscalFixedAssetLineList());
      response.setValue(
          "fixedAssetDerogatoryLineList", fixedAsset.getFixedAssetDerogatoryLineList());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
