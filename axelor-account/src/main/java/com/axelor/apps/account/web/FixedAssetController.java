/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
import com.axelor.apps.account.service.FixedAssetService;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.common.base.Function;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

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

  public void validate(ActionRequest request, ActionResponse response) {
    FixedAsset fixedAsset = request.getContext().asType(FixedAsset.class);
    if (fixedAsset.getStatusSelect() == FixedAssetRepository.STATUS_DRAFT) {
      try {
        Beans.get(FixedAssetService.class).validate(fixedAsset);
      } catch (Exception e) {
        TraceBackService.trace(response, e);
      }
    }
    response.setReload(true);
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

  public void massValidation(ActionRequest request, ActionResponse response) {
    try {
      Function<Collection<? extends Number>, Pair<Integer, Integer>> function =
          Beans.get(FixedAssetService.class)::massValidation;
      massProcess(request, response, function);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  private void massProcess(
      ActionRequest request,
      ActionResponse response,
      Function<Collection<? extends Number>, Pair<Integer, Integer>> function) {

    try {
      @SuppressWarnings("unchecked")
      List<Number> ids = (List<Number>) request.getContext().get("_ids");

      if (ObjectUtils.isEmpty(ids)) {
        response.setError(com.axelor.apps.base.exceptions.IExceptionMessage.RECORD_NONE_SELECTED);
        return;
      }

      Pair<Integer, Integer> massCount = function.apply(ids);

      String message = buildMassMessage(massCount.getLeft(), massCount.getRight());
      response.setFlash(message);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    } finally {
      response.setReload(true);
    }
  }

  private String buildMassMessage(int doneCount, int errorCount) {
    StringBuilder sb = new StringBuilder();
    sb.append(
        String.format(
            I18n.get(
                com.axelor.apps.base.exceptions.IExceptionMessage.ABSTRACT_BATCH_DONE_SINGULAR,
                com.axelor.apps.base.exceptions.IExceptionMessage.ABSTRACT_BATCH_DONE_PLURAL,
                doneCount),
            doneCount));
    sb.append(" ");
    sb.append(
        String.format(
            I18n.get(
                com.axelor.apps.base.exceptions.IExceptionMessage.ABSTRACT_BATCH_ANOMALY_SINGULAR,
                com.axelor.apps.base.exceptions.IExceptionMessage.ABSTRACT_BATCH_ANOMALY_PLURAL,
                errorCount),
            errorCount));
    return sb.toString();
  }
}
