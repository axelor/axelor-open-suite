/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.AssetDisposalReason;
import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.account.db.repo.FixedAssetTypeRepository;
import com.axelor.apps.account.db.repo.TaxLineRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.analytic.AnalyticDistributionTemplateService;
import com.axelor.apps.account.service.analytic.AnalyticToolService;
import com.axelor.apps.account.service.fixedasset.FixedAssetCategoryService;
import com.axelor.apps.account.service.fixedasset.FixedAssetDateService;
import com.axelor.apps.account.service.fixedasset.FixedAssetFailOverControlService;
import com.axelor.apps.account.service.fixedasset.FixedAssetGenerationService;
import com.axelor.apps.account.service.fixedasset.FixedAssetLineMoveService;
import com.axelor.apps.account.service.fixedasset.FixedAssetService;
import com.axelor.apps.account.service.fixedasset.FixedAssetValidateService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.ResponseMessageType;
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
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Singleton
public class FixedAssetController {

  public void computeDepreciation(ActionRequest request, ActionResponse response) {

    FixedAsset fixedAsset = request.getContext().asType(FixedAsset.class);
    fixedAsset = Beans.get(FixedAssetRepository.class).find(fixedAsset.getId());
    // There is nothing to do if depreciation plan is None.
    if (fixedAsset.getDepreciationPlanSelect() == null
        || fixedAsset
            .getDepreciationPlanSelect()
            .equals(FixedAssetRepository.DEPRECIATION_PLAN_NONE)) {
      return;
    }

    try {
      fixedAsset = Beans.get(FixedAssetGenerationService.class).generateAndComputeLines(fixedAsset);

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }

    response.setReload(true);
  }

  @SuppressWarnings("unchecked")
  public void disposal(ActionRequest request, ActionResponse response) throws AxelorException {
    Context context = request.getContext();
    if (context.get("disposalDate") == null
        || context.get("disposalTypeSelect") == null
        || context.get("disposalQtySelect") == null) {
      return;
    }
    LocalDate disposalDate = (LocalDate) context.get("disposalDate");
    BigDecimal disposalAmount =
        new BigDecimal(
            Optional.ofNullable(context.get("disposalAmount"))
                .map(Object::toString)
                .orElse(BigDecimal.ZERO.toString()));

    BigDecimal disposalQty = new BigDecimal(context.get("qty").toString());
    Integer disposalTypeSelect = (Integer) context.get("disposalTypeSelect");
    Integer disposalQtySelect = (Integer) context.get("disposalQtySelect");
    AssetDisposalReason assetDisposalReason =
        (AssetDisposalReason) context.get("assetDisposalReason");
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
    if (disposalTypeSelect == FixedAssetRepository.DISPOSABLE_TYPE_SELECT_ONGOING_CESSION) {
      generateSaleMove = false;
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
          I18n.get(AccountExceptionMessage.IMMO_FIXED_ASSET_DISPOSAL_QTY_GREATER_ORIGINAL),
          fixedAsset.getQty().toString());
    }
    try {
      int transferredReason =
          Beans.get(FixedAssetService.class)
              .computeTransferredReason(
                  disposalTypeSelect, disposalQtySelect, disposalQty, fixedAsset);

      FixedAsset createdFixedAsset =
          Beans.get(FixedAssetService.class)
              .computeDisposal(
                  fixedAsset,
                  disposalDate,
                  disposalQty,
                  disposalAmount,
                  transferredReason,
                  assetDisposalReason,
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
        Beans.get(FixedAssetValidateService.class).validate(fixedAsset);
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
        int validatedFixedAssets = Beans.get(FixedAssetValidateService.class).massValidation(ids);
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

  public void personalizeAnalyticDistributionTemplate(
      ActionRequest request, ActionResponse response) {
    try {
      FixedAsset fixedAsset = request.getContext().asType(FixedAsset.class);
      AnalyticDistributionTemplate analyticDistributionTemplate =
          fixedAsset.getAnalyticDistributionTemplate();
      AnalyticDistributionTemplate specificAnalyticDistributionTemplate =
          Beans.get(AnalyticDistributionTemplateService.class)
              .personalizeAnalyticDistributionTemplate(
                  analyticDistributionTemplate, fixedAsset.getCompany());
      if (analyticDistributionTemplate == null || !analyticDistributionTemplate.getIsSpecific()) {
        response.setValue("analyticDistributionTemplate", specificAnalyticDistributionTemplate);
        response.setView(
            ActionView.define("Specific Analytic Distribution Template")
                .model(AnalyticDistributionTemplate.class.getName())
                .add("form", "analytic-distribution-template-fixed-asset-form")
                .param("popup", "true")
                .param("forceEdit", "true")
                .param("show-toolbar", "false")
                .param("show-confirm", "false")
                .param("popup-save", "true")
                .context("_showRecord", specificAnalyticDistributionTemplate.getId())
                .context("fixedAsset", fixedAsset.getId())
                .map());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void computeFirstDepreciationDate(ActionRequest request, ActionResponse response) {

    try {
      FixedAsset fixedAsset = request.getContext().asType(FixedAsset.class);
      Beans.get(FixedAssetDateService.class).computeFirstDepreciationDate(fixedAsset);

      response.setValue("firstDepreciationDate", fixedAsset.getFirstDepreciationDate());
      response.setValue("fiscalFirstDepreciationDate", fixedAsset.getFiscalFirstDepreciationDate());
      response.setValue("ifrsFirstDepreciationDate", fixedAsset.getIfrsFirstDepreciationDate());

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void updateDepreciation(ActionRequest request, ActionResponse response) {
    try {
      FixedAsset fixedAsset =
          Beans.get(FixedAssetRepository.class)
              .find(request.getContext().asType(FixedAsset.class).getId());
      Beans.get(FixedAssetService.class).updateDepreciation(fixedAsset);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void splitFixedAsset(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();
    Long fixedAssetId = Long.valueOf(context.get("_id").toString());
    FixedAsset fixedAsset = Beans.get(FixedAssetRepository.class).find(fixedAssetId);
    BigDecimal disposalQty = new BigDecimal(context.get("qty").toString());
    FixedAssetService fixedAssetService = Beans.get(FixedAssetService.class);
    try {
      fixedAssetService.checkFixedAssetScissionQty(disposalQty, fixedAsset);
      FixedAsset createdFixedAsset =
          fixedAssetService.splitAndSaveFixedAsset(
              fixedAsset,
              disposalQty,
              Beans.get(AppBaseService.class).getTodayDate(fixedAsset.getCompany()),
              fixedAsset.getComments());
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
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void setTitleForButton(ActionRequest request, ActionResponse response) {
    try {
      FixedAsset fixedAsset = request.getContext().asType(FixedAsset.class);
      if (fixedAsset.getAnalyticDistributionTemplate() != null) {
        response.setAttr(
            "personalizeBtn", "title", I18n.get("Personalize selected analytic template"));
      } else {
        response.setAttr(
            "personalizeBtn", "title", I18n.get("Create personalized analytic template"));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void failOverControl(ActionRequest request, ActionResponse response)
      throws AxelorException {
    try {
      FixedAsset fixedAsset = request.getContext().asType(FixedAsset.class);
      Beans.get(FixedAssetFailOverControlService.class).controlFailOver(fixedAsset);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void setDepreciationPlanSelectToNone(ActionRequest request, ActionResponse response) {
    try {
      FixedAsset fixedAsset = request.getContext().asType(FixedAsset.class);
      FixedAssetCategoryService fixedAssetCategoryService =
          Beans.get(FixedAssetCategoryService.class);

      fixedAssetCategoryService.setDepreciationPlanSelectToNone(
          fixedAsset,
          FixedAssetTypeRepository.FIXED_ASSET_CATEGORY_TECHNICAL_TYPE_SELECT_ONGOING_ASSET);
      if (StringUtils.notEmpty(fixedAsset.getDepreciationPlanSelect())) {
        response.setValue("depreciationPlanSelect", fixedAsset.getDepreciationPlanSelect());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setDepreciationPlanSelectReadonly(ActionRequest request, ActionResponse response) {
    try {
      FixedAsset fixedAsset = request.getContext().asType(FixedAsset.class);
      FixedAssetCategoryService fixedAssetCategoryService =
          Beans.get(FixedAssetCategoryService.class);
      boolean isReadonly =
          fixedAssetCategoryService.compareFixedAssetCategoryTypeSelect(
              fixedAsset,
              FixedAssetTypeRepository.FIXED_ASSET_CATEGORY_TECHNICAL_TYPE_SELECT_ONGOING_ASSET);

      response.setAttr("depreciationPlanSelect", "readonly", isReadonly);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void onChangeDepreciationPlan(ActionRequest request, ActionResponse response) {
    try {
      FixedAsset fixedAsset = request.getContext().asType(FixedAsset.class);
      FixedAssetService fixedAssetService = Beans.get(FixedAssetService.class);
      fixedAssetService.onChangeDepreciationPlan(fixedAsset);
      response.setValues(fixedAsset);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void hideAnalytic(ActionRequest request, ActionResponse response) {
    try {
      FixedAsset fixedAsset = request.getContext().asType(FixedAsset.class);
      response.setAttr(
          "analyticPanel",
          "hidden",
          !Beans.get(AnalyticToolService.class).isManageAnalytic(fixedAsset.getCompany()));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void checkPartialDisposal(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();
    if (context.get("disposalDate") == null
        || context.get("disposalAmount") == null
        || context.get("disposalTypeSelect") == null
        || context.get("disposalQtySelect") == null) {
      return;
    }
    BigDecimal disposalQty = new BigDecimal(context.get("qty").toString());
    Integer disposalQtySelect = (Integer) context.get("disposalQtySelect");
    FixedAsset fixedAsset =
        Beans.get(FixedAssetRepository.class).find(Long.valueOf(context.get("_id").toString()));
    try {
      if (disposalQtySelect == FixedAssetRepository.DISPOSABLE_QTY_SELECT_PARTIAL
          && disposalQty.compareTo(fixedAsset.getQty()) == 0) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(AccountExceptionMessage.FIXED_ASSET_PARTIAL_TO_TOTAL_DISPOSAL));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.WARNING);
    }
  }
}
