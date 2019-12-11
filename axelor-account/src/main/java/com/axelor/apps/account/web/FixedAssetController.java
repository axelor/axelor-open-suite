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
package com.axelor.apps.account.web;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetLine;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.account.service.FixedAssetService;
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
    try {
      if (fixedAsset.getGrossValue().compareTo(BigDecimal.ZERO) > 0) {

        if (!fixedAsset.getFixedAssetLineList().isEmpty()) {
          fixedAsset.getFixedAssetLineList().clear();
        }
        fixedAsset = Beans.get(FixedAssetService.class).generateAndcomputeLines(fixedAsset);

      } else {
        fixedAsset.getFixedAssetLineList().clear();
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
    response.setValue("residualValue", fixedAsset.getGrossValue());
    response.setValue("fixedAssetLineList", fixedAsset.getFixedAssetLineList());
  }

  public void disposal(ActionRequest request, ActionResponse response) throws AxelorException {
    Context context = request.getContext();
    if (context.get("disposalDate") == null || context.get("disposalAmount") == null) {
      return;
    }
    LocalDate disposalDate = (LocalDate) context.get("disposalDate");
    BigDecimal disposalAmount = new BigDecimal(context.get("disposalAmount").toString());
    Long fixedAssetId = Long.valueOf(context.get("_id").toString());
    FixedAsset fixedAsset = Beans.get(FixedAssetRepository.class).find(fixedAssetId);

    Beans.get(FixedAssetService.class).disposal(disposalDate, disposalAmount, fixedAsset);

    response.setCanClose(true);
  }

  public void createAnalyticDistributionWithTemplate(
      ActionRequest request, ActionResponse response) {

    try {
      FixedAsset fixedAsset = request.getContext().asType(FixedAsset.class);
      if (fixedAsset.getAnalyticDistributionTemplate() != null) {
        if (fixedAsset.getDisposalMove() != null) {
          for (MoveLine moveLine : fixedAsset.getDisposalMove().getMoveLineList()) {
            fixedAssetService.createAnalyticOnMoveLine(
                fixedAsset.getAnalyticDistributionTemplate(), moveLine);
          }
        }
        if (fixedAsset.getFixedAssetLineList() != null) {
          for (FixedAssetLine fixedAssetLine : fixedAsset.getFixedAssetLineList()) {
            if (fixedAssetLine.getDepreciationAccountMove() != null) {
              for (MoveLine moveLine :
                  fixedAssetLine.getDepreciationAccountMove().getMoveLineList()) {
                fixedAssetService.createAnalyticOnMoveLine(
                    fixedAsset.getAnalyticDistributionTemplate(), moveLine);
              }
            }
          }
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
