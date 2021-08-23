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
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.account.db.repo.TaxLineRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.fixedasset.FixedAssetLineMoveService;
import com.axelor.apps.account.service.fixedasset.FixedAssetService;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.HashMap;

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

  @SuppressWarnings("unchecked")
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
    BigDecimal disposalQty = new BigDecimal(context.get("qty").toString());
    Integer disposalTypeSelect = (Integer) context.get("disposalTypeSelect");
    Integer disposalQtySelect = (Integer) context.get("disposalQtySelect");
    String comments = null;
    if (context.get("comments") != null) {
      comments = context.get("comments").toString();
    }
    Long fixedAssetId = Long.valueOf(context.get("_id").toString());
    Boolean generateSaleMove = false;
    TaxLine saleTaxLine = null;
    if (context.get("generateSaleMove") != null) {
      generateSaleMove = Boolean.parseBoolean(context.get("generateSaleMove").toString());
    }
    if (context.get("saleTaxLine") != null) {
      saleTaxLine =
          Beans.get(TaxLineRepository.class)
              .find(
                  ((Integer) ((HashMap<String, Object>) context.get("saleTaxLine")).get("id"))
                      .longValue());
    }

    FixedAsset fixedAsset = Beans.get(FixedAssetRepository.class).find(fixedAssetId);

    if (disposalQtySelect == FixedAssetRepository.DISPOSABLE_QTY_SELECT_PARTIAL
        && disposalQty.compareTo(fixedAsset.getQty()) > 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.IMMO_FIXED_ASSET_DISPOSAL_QTY_GREATER_ORIGINAL),
          fixedAsset.getQty().toString());
    }
    try {
      int transferredReason =
          Beans.get(FixedAssetService.class)
              .computeTransferredReason(disposalTypeSelect, disposalQtySelect);

      FixedAsset createdFixedAsset =
          Beans.get(FixedAssetService.class)
              .computeDisposal(
                  fixedAsset,
                  disposalDate,
                  disposalQty,
                  disposalAmount,
                  transferredReason,
                  comments);
      if (createdFixedAsset != null) {
        response.setView(
            ActionView.define("Fixed asset")
                .model(FixedAsset.class.getName())
                .add("form", "fixed-asset-form")
                .context("_showRecord", createdFixedAsset.getId())
                .map());
        response.setCanClose(true);
        response.setReload(true);
        if (generateSaleMove && saleTaxLine != null) {
          Beans.get(FixedAssetLineMoveService.class)
              .generateSaleMove(createdFixedAsset, saleTaxLine, disposalAmount, disposalDate);
        }
      } else {
        if (generateSaleMove && saleTaxLine != null) {
          Beans.get(FixedAssetLineMoveService.class)
              .generateSaleMove(fixedAsset, saleTaxLine, disposalAmount, disposalDate);
        }
        response.setCanClose(true);
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void validate(ActionRequest request, ActionResponse response) {
    FixedAsset fixedAsset =
        Beans.get(FixedAssetRepository.class)
            .find(request.getContext().asType(FixedAsset.class).getId());
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
  
  @SuppressWarnings("unchecked")
  public void massValidation(ActionRequest request, ActionResponse response) {
    try {
      if (!ObjectUtils.isEmpty(request.getContext().get("_ids"))) {
        List<Long> ids =
            (List)
                (((List) request.getContext().get("_ids"))
                    .stream()
                        .filter(ObjectUtils::notEmpty)
                        .map(input -> Long.parseLong(input.toString()))
                        .collect(Collectors.toList()));
        int validatedFixedAssets = Beans.get(FixedAssetService.class).massValidation(ids);
        response.setFlash(
            validatedFixedAssets
                + " "
                + I18n.get(
                    "fixed asset validated", "fixed assets validated", validatedFixedAssets));
        response.setReload(true);
      } else {
        response.setFlash(I18n.get("Please select something to validate"));
      }
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

  public void splitFixedAsset(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();
    Long fixedAssetId = Long.valueOf(context.get("_id").toString());
    FixedAsset fixedAsset = Beans.get(FixedAssetRepository.class).find(fixedAssetId);
    BigDecimal disposalQty = new BigDecimal(context.get("qty").toString());

    try {
      if (disposalQty.compareTo(fixedAsset.getQty()) > 0) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(IExceptionMessage.IMMO_FIXED_ASSET_DISPOSAL_QTY_GREATER_ORIGINAL),
            fixedAsset.getQty().toString());
      }
      FixedAsset splittedFixedAsset =
          Beans.get(FixedAssetService.class)
              .splitFixedAsset(fixedAsset, disposalQty, LocalDate.now(), fixedAsset.getComments());
      Beans.get(FixedAssetRepository.class).save(fixedAsset);
      FixedAsset createdFixedAsset = Beans.get(FixedAssetRepository.class).save(splittedFixedAsset);
      if (createdFixedAsset != null) {
        response.setView(
            ActionView.define("Fixed asset")
                .model(FixedAsset.class.getName())
                .add("form", "fixed-asset-form")
                .context("_showRecord", createdFixedAsset.getId())
                .map());
        response.setCanClose(true);
        response.setReload(true);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
