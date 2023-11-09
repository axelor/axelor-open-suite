/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.production.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BirtTemplate;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.birt.template.BirtTemplateService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.production.db.CostSheet;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ProdProcess;
import com.axelor.apps.production.db.ProdProduct;
import com.axelor.apps.production.db.repo.CostSheetRepository;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.db.repo.ProdProcessRepository;
import com.axelor.apps.production.db.repo.ProdProductRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.service.ProdProductProductionRepository;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.config.ProductionConfigService;
import com.axelor.apps.production.service.costsheet.CostSheetService;
import com.axelor.apps.production.service.manuforder.ManufOrderReservedQtyService;
import com.axelor.apps.production.service.manuforder.ManufOrderService;
import com.axelor.apps.production.service.manuforder.ManufOrderStockMoveService;
import com.axelor.apps.production.service.manuforder.ManufOrderWorkflowService;
import com.axelor.apps.production.translation.ITranslation;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.message.exception.MessageExceptionMessage;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.axelor.utils.db.Wizard;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.inject.Singleton;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ManufOrderController {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public void start(ActionRequest request, ActionResponse response) {

    try {
      Long manufOrderId = (Long) request.getContext().get("id");
      ManufOrder manufOrder = Beans.get(ManufOrderRepository.class).find(manufOrderId);
      Beans.get(ManufOrderWorkflowService.class).start(manufOrder);
      response.setReload(true);
      String message = "";
      if (!Strings.isNullOrEmpty(manufOrder.getMoCommentFromSaleOrder())) {
        message = manufOrder.getMoCommentFromSaleOrder();
      }

      if (!Strings.isNullOrEmpty(manufOrder.getMoCommentFromSaleOrderLine())) {
        message =
            message
                .concat(System.lineSeparator())
                .concat(manufOrder.getMoCommentFromSaleOrderLine());
      }

      if (!message.isEmpty()) {
        message =
            I18n.get(ITranslation.PRODUCTION_COMMENT)
                .concat(System.lineSeparator())
                .concat(message);
        response.setInfo(message);
        response.setCanClose(true);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void pause(ActionRequest request, ActionResponse response) {

    try {
      Long manufOrderId = (Long) request.getContext().get("id");
      ManufOrder manufOrder = Beans.get(ManufOrderRepository.class).find(manufOrderId);

      Beans.get(ManufOrderWorkflowService.class).pause(manufOrder);

      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void resume(ActionRequest request, ActionResponse response) {

    try {
      Long manufOrderId = (Long) request.getContext().get("id");
      ManufOrder manufOrder = Beans.get(ManufOrderRepository.class).find(manufOrderId);

      Beans.get(ManufOrderWorkflowService.class).resume(manufOrder);

      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void finish(ActionRequest request, ActionResponse response) {

    try {
      Long manufOrderId = (Long) request.getContext().get("id");
      ManufOrder manufOrder = Beans.get(ManufOrderRepository.class).find(manufOrderId);

      // we have to inject TraceBackService to use non static methods
      TraceBackService traceBackService = Beans.get(TraceBackService.class);
      long tracebackCount = traceBackService.countMessageTraceBack(manufOrder);

      if (!Beans.get(ManufOrderWorkflowService.class).finish(manufOrder)) {
        response.setNotify(I18n.get(ProductionExceptionMessage.MANUF_ORDER_EMAIL_NOT_SENT));
      } else if (traceBackService.countMessageTraceBack(manufOrder) > tracebackCount) {
        traceBackService
            .findLastMessageTraceBack(manufOrder)
            .ifPresent(
                traceback ->
                    response.setNotify(
                        String.format(
                            I18n.get(MessageExceptionMessage.SEND_EMAIL_EXCEPTION),
                            traceback.getMessage())));
      }

      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void partialFinish(ActionRequest request, ActionResponse response) {
    try {
      ManufOrder manufOrder = request.getContext().asType(ManufOrder.class);
      manufOrder = Beans.get(ManufOrderRepository.class).find(manufOrder.getId());

      // we have to inject TraceBackService to use non static methods
      TraceBackService traceBackService = Beans.get(TraceBackService.class);
      long tracebackCount = traceBackService.countMessageTraceBack(manufOrder);

      if (!Beans.get(ManufOrderWorkflowService.class).partialFinish(manufOrder)) {
        response.setNotify(I18n.get(ProductionExceptionMessage.MANUF_ORDER_EMAIL_NOT_SENT));
      } else if (traceBackService.countMessageTraceBack(manufOrder) > tracebackCount) {
        traceBackService
            .findLastMessageTraceBack(manufOrder)
            .ifPresent(
                traceback ->
                    response.setNotify(
                        String.format(
                            I18n.get(MessageExceptionMessage.SEND_EMAIL_EXCEPTION),
                            traceback.getMessage())));
      }
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void cancel(ActionRequest request, ActionResponse response) {

    try {
      Context context = request.getContext();
      ManufOrder manufOrder = context.asType(ManufOrder.class);

      Beans.get(ManufOrderWorkflowService.class)
          .cancel(
              Beans.get(ManufOrderRepository.class).find(manufOrder.getId()),
              manufOrder.getCancelReason(),
              manufOrder.getCancelReasonStr());
      response.setInfo(I18n.get(ProductionExceptionMessage.MANUF_ORDER_CANCEL));
      response.setCanClose(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void plan(ActionRequest request, ActionResponse response) {

    try {
      Context context = request.getContext();
      List<ManufOrder> manufOrders = new ArrayList<>();

      if (context.get("id") != null) {
        Long manufOrderId = (Long) request.getContext().get("id");
        manufOrders.add(Beans.get(ManufOrderRepository.class).find(manufOrderId));

      } else if (context.get("_ids") != null) {
        manufOrders =
            Beans.get(ManufOrderRepository.class)
                .all()
                .filter(
                    "self.id in ?1 and self.statusSelect in (?2,?3)",
                    context.get("_ids"),
                    ManufOrderRepository.STATUS_DRAFT,
                    ManufOrderRepository.STATUS_CANCELED)
                .fetch();
      }

      String message = Beans.get(ManufOrderWorkflowService.class).planManufOrders(manufOrders);

      response.setReload(true);
      if (!message.isEmpty()) {
        message =
            I18n.get(ITranslation.PRODUCTION_COMMENT)
                .concat(System.lineSeparator())
                .concat(message);
        response.setInfo(message);
        response.setCanClose(true);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called from manuf order form on clicking realize button. Call {@link
   * ManufOrderStockMoveService#consumeInStockMoves(ManufOrder)} to consume material used in manuf
   * order.
   *
   * @param request
   * @param response
   */
  public void consumeStockMove(ActionRequest request, ActionResponse response) {
    try {

      ManufOrder manufOrder = request.getContext().asType(ManufOrder.class);
      manufOrder = Beans.get(ManufOrderRepository.class).find(manufOrder.getId());
      Beans.get(ManufOrderStockMoveService.class).consumeInStockMoves(manufOrder);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void preFillOperations(ActionRequest request, ActionResponse response)
      throws AxelorException {

    try {
      ManufOrder manufOrder = request.getContext().asType(ManufOrder.class);
      ManufOrderService moService = Beans.get(ManufOrderService.class);
      manufOrder = Beans.get(ManufOrderRepository.class).find(manufOrder.getId());
      moService.preFillOperations(manufOrder);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void generateWasteStockMove(ActionRequest request, ActionResponse response) {
    try {
      ManufOrder manufOrder = request.getContext().asType(ManufOrder.class);
      manufOrder = Beans.get(ManufOrderRepository.class).find(manufOrder.getId());
      Beans.get(ManufOrderService.class).generateWasteStockMove(manufOrder);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called from manuf order wizard view. Call {@link
   * ManufOrderService#updatePlannedQty(ManufOrder)}
   *
   * @param request
   * @param response
   */
  public void updatePlannedQty(ActionRequest request, ActionResponse response) {
    try {
      ManufOrder manufOrder = request.getContext().asType(ManufOrder.class);
      manufOrder = Beans.get(ManufOrderRepository.class).find(manufOrder.getId());
      Beans.get(ManufOrderService.class).updatePlannedQty(manufOrder);
      response.setReload(true);
      response.setCanClose(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called from manuf order wizard view. Call {@link ManufOrderService#updateRealQty(ManufOrder,
   * BigDecimal)}
   *
   * @param request
   * @param response
   */
  public void updateRealQty(ActionRequest request, ActionResponse response) {
    try {
      ManufOrder manufOrder = request.getContext().asType(ManufOrder.class);
      manufOrder = Beans.get(ManufOrderRepository.class).find(manufOrder.getId());
      BigDecimal qtyToUpdate = new BigDecimal(request.getContext().get("qtyToUpdate").toString());
      Beans.get(ManufOrderService.class).updateRealQty(manufOrder, qtyToUpdate);
      response.setReload(true);
      response.setCanClose(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void printProdProcess(ActionRequest request, ActionResponse response) {

    try {
      ManufOrder manufOrder = request.getContext().asType(ManufOrder.class);

      Company company = manufOrder.getCompany();
      BirtTemplate prodProcessBirtTemplate = null;
      if (ObjectUtils.notEmpty(company)) {
        prodProcessBirtTemplate =
            Beans.get(ProductionConfigService.class)
                .getProductionConfig(company)
                .getProdProcessBirtTemplate();
      }
      if (ObjectUtils.isEmpty(prodProcessBirtTemplate)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(BaseExceptionMessage.BIRT_TEMPLATE_CONFIG_NOT_FOUND));
      }

      ProdProcess prodProcess =
          Beans.get(ProdProcessRepository.class).find(manufOrder.getProdProcess().getId());
      String prodProcessLable = manufOrder.getProdProcess().getName();
      String fileLink =
          Beans.get(BirtTemplateService.class)
              .generateBirtTemplateLink(
                  prodProcessBirtTemplate, prodProcess, prodProcessLable + "-${date}");

      response.setView(ActionView.define(prodProcessLable).add("html", fileLink).map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void updatePlannedDates(ActionRequest request, ActionResponse response) {

    try {
      ManufOrder manufOrderView = request.getContext().asType(ManufOrder.class);

      if (manufOrderView.getStatusSelect() == ManufOrderRepository.STATUS_PLANNED) {
        ManufOrder manufOrder = Beans.get(ManufOrderRepository.class).find(manufOrderView.getId());

        if (manufOrderView.getPlannedStartDateT() != null) {
          if (!manufOrderView.getPlannedStartDateT().isEqual(manufOrder.getPlannedStartDateT())) {
            Beans.get(ManufOrderWorkflowService.class)
                .updatePlannedDates(manufOrder, manufOrderView.getPlannedStartDateT());
            response.setReload(true);
          }
        } else {
          response.setValue("plannedStartDateT", manufOrder.getPlannedStartDateT());
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called from manuf order form, on produced stock move line change. Call {@link
   * ManufOrderService#checkProducedStockMoveLineList(ManufOrder, ManufOrder)}.
   *
   * @param request
   * @param response
   */
  public void checkProducedStockMoveLineList(ActionRequest request, ActionResponse response) {
    try {
      ManufOrder manufOrder = request.getContext().asType(ManufOrder.class);
      ManufOrder oldManufOrder = Beans.get(ManufOrderRepository.class).find(manufOrder.getId());
      Beans.get(ManufOrderService.class).checkProducedStockMoveLineList(manufOrder, oldManufOrder);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
      response.setReload(true);
    }
  }

  /**
   * Called from manuf order form, on produced stock move line change. Call {@link
   * ManufOrderService#updateProducedStockMoveFromManufOrder(ManufOrder)}.
   *
   * @param request
   * @param response
   */
  public void updateProducedStockMoveFromManufOrder(
      ActionRequest request, ActionResponse response) {
    try {
      ManufOrder manufOrder = request.getContext().asType(ManufOrder.class);
      manufOrder = Beans.get(ManufOrderRepository.class).find(manufOrder.getId());
      Beans.get(ManufOrderService.class).updateProducedStockMoveFromManufOrder(manufOrder);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called from manuf order form, on consumed stock move line change. Call {@link
   * ManufOrderService#checkConsumedStockMoveLineList(ManufOrder, ManufOrder)}.
   *
   * @param request
   * @param response
   */
  public void checkConsumedStockMoveLineList(ActionRequest request, ActionResponse response) {
    try {
      ManufOrder manufOrder = request.getContext().asType(ManufOrder.class);
      ManufOrder oldManufOrder = Beans.get(ManufOrderRepository.class).find(manufOrder.getId());
      Beans.get(ManufOrderService.class).checkConsumedStockMoveLineList(manufOrder, oldManufOrder);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
      response.setReload(true);
    }
  }

  /**
   * Called from manuf order form, on consumed stock move line change. Call {@link
   * ManufOrderService#updateConsumedStockMoveFromManufOrder(ManufOrder)}.
   *
   * @param request
   * @param response
   */
  public void updateConsumedStockMoveFromManufOrder(
      ActionRequest request, ActionResponse response) {
    try {
      ManufOrder manufOrder = request.getContext().asType(ManufOrder.class);
      manufOrder = Beans.get(ManufOrderRepository.class).find(manufOrder.getId());
      Beans.get(ManufOrderService.class).updateConsumedStockMoveFromManufOrder(manufOrder);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called from manuf order form, on clicking "compute cost price" button. Call {@link
   * CostSheetService#computeCostPrice(ManufOrder, int, LocalDate)}.
   *
   * @param request
   * @param response
   */
  public void computeCostPrice(ActionRequest request, ActionResponse response) {
    try {
      ManufOrder manufOrder = request.getContext().asType(ManufOrder.class);
      manufOrder = Beans.get(ManufOrderRepository.class).find(manufOrder.getId());

      CostSheet costSheet =
          Beans.get(CostSheetService.class)
              .computeCostPrice(
                  manufOrder,
                  CostSheetRepository.CALCULATION_WORK_IN_PROGRESS,
                  Beans.get(AppBaseService.class).getTodayDate(manufOrder.getCompany()));

      response.setView(
          ActionView.define(I18n.get("Cost sheet"))
              .model(CostSheet.class.getName())
              .param("popup", "true")
              .param("show-toolbar", "false")
              .param("show-confirm", "false")
              .param("popup-save", "false")
              .add("grid", "cost-sheet-bill-of-material-grid")
              .add("form", "cost-sheet-bill-of-material-form")
              .context("_showRecord", String.valueOf(costSheet.getId()))
              .map());

      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void checkMergeValues(ActionRequest request, ActionResponse response) {
    try {
      if (request.getContext().get("id") != null) {
        response.setError(I18n.get(ProductionExceptionMessage.MANUF_ORDER_ONLY_ONE_SELECTED));
      } else {
        Object _ids = request.getContext().get("_ids");
        if (!ObjectUtils.isEmpty(_ids)) {
          List<Long> ids = (List<Long>) _ids;
          if (ids.size() < 2) {
            response.setError(I18n.get(ProductionExceptionMessage.MANUF_ORDER_ONLY_ONE_SELECTED));
          } else {
            boolean canMerge = Beans.get(ManufOrderService.class).canMerge(ids);
            if (canMerge) {
              response.setAlert(I18n.get(ProductionExceptionMessage.MANUF_ORDER_MERGE_VALIDATION));
            } else {
              if (Beans.get(AppProductionService.class).getAppProduction().getManageWorkshop()) {
                response.setError(I18n.get(ProductionExceptionMessage.MANUF_ORDER_MERGE_ERROR));
              } else {
                response.setError(
                    I18n.get(
                        ProductionExceptionMessage.MANUF_ORDER_MERGE_ERROR_MANAGE_WORKSHOP_FALSE));
              }
            }
          }
        } else {
          response.setError(I18n.get(ProductionExceptionMessage.MANUF_ORDER_NO_ONE_SELECTED));
        }
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void generateMergeManufOrder(ActionRequest request, ActionResponse response) {
    try {
      List<Long> ids = (List<Long>) request.getContext().get("_ids");
      Beans.get(ManufOrderService.class).merge(ids);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called from manuf order form, on clicking "Multi-level planning" button.
   *
   * @param request
   * @param response
   */
  public void multiLevelManufOrderOnLoad(ActionRequest request, ActionResponse response) {
    try {
      Long moId = Long.valueOf(request.getContext().get("id").toString());
      ManufOrder mo = Beans.get(ManufOrderRepository.class).find(moId);
      boolean showOnlyMissingQty =
          request.getContext().get("_showOnlyMissingQty") != null
              && Boolean.parseBoolean(request.getContext().get("_showOnlyMissingQty").toString());
      ProdProductProductionRepository prodProductProductionRepository =
          Beans.get(ProdProductProductionRepository.class);
      List<ProdProduct> prodProducts =
          mo.getToConsumeProdProductList().stream()
              .filter(
                  prodProduct ->
                      prodProduct.getProduct().getProductSubTypeSelect()
                              == ProductRepository.PRODUCT_SUB_TYPE_FINISHED_PRODUCT
                          || prodProduct.getProduct().getProductSubTypeSelect()
                              == ProductRepository.PRODUCT_SUB_TYPE_SEMI_FINISHED_PRODUCT)
              .filter(
                  prodProduct ->
                      !showOnlyMissingQty
                          || prodProductProductionRepository
                                  .computeMissingQty(
                                      prodProduct.getProduct().getId(), prodProduct.getQty(), moId)
                                  .compareTo(BigDecimal.ZERO)
                              > 0)
              .collect(Collectors.toList());
      response.setValue("$components", prodProducts);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called from multi-level-planing-wizard-form, on clicking "Generate MO" button.
   *
   * @param request
   * @param response
   */
  @SuppressWarnings("unchecked")
  public void generateMultiLevelManufOrder(ActionRequest request, ActionResponse response) {
    try {
      Long moId = Long.valueOf(request.getContext().get("id").toString());
      ManufOrder mo = Beans.get(ManufOrderRepository.class).find(moId);
      ProdProductRepository prodProductRepository = Beans.get(ProdProductRepository.class);
      List<ProdProduct> prodProductList =
          ((List<LinkedHashMap<String, Object>>) request.getContext().get("components"))
              .stream()
                  .filter(map -> (boolean) map.get("selected"))
                  .map(map -> prodProductRepository.find(Long.valueOf(map.get("id").toString())))
                  .collect(Collectors.toList());
      if (prodProductList.isEmpty()) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            I18n.get(ProductionExceptionMessage.NO_PRODUCT_SELECTED));
      }

      List<Product> selectedProductList = new ArrayList<>();

      for (ProdProduct prod : prodProductList) {
        if (selectedProductList.contains(prod.getProduct())) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_MISSING_FIELD,
              I18n.get(ProductionExceptionMessage.DUPLICATE_PRODUCT_SELECTED));
        }
        selectedProductList.add(prod.getProduct());
      }

      List<ManufOrder> moList =
          Beans.get(ManufOrderService.class).generateAllSubManufOrder(selectedProductList, mo);

      response.setCanClose(true);
      response.setView(
          ActionView.define(I18n.get("Manufacturing orders"))
              .model(Wizard.class.getName())
              .add("form", "multi-level-generated-draft-manuf-order-wizard-form")
              .param("popup", "true")
              .param("popup-save", "false")
              .param("show-toolbar", "false")
              .param("show-confirm", "false")
              .context("_moList", moList)
              .map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called from the toolbar in manuf order form view. Call {@link
   * com.axelor.apps.production.service.manuforder.ManufOrderReservedQtyService#allocateAll(ManufOrder)}.
   *
   * @param request
   * @param response
   */
  public void allocateAll(ActionRequest request, ActionResponse response) {
    try {
      ManufOrder manufOrder = request.getContext().asType(ManufOrder.class);
      manufOrder = Beans.get(ManufOrderRepository.class).find(manufOrder.getId());
      Beans.get(ManufOrderReservedQtyService.class).allocateAll(manufOrder);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called from the toolbar in manuf order form view. Call {@link
   * com.axelor.apps.production.service.manuforder.ManufOrderReservedQtyService##deallocateAll(ManufOrder)}.
   *
   * @param request
   * @param response
   */
  public void deallocateAll(ActionRequest request, ActionResponse response) {
    try {
      ManufOrder manufOrder = request.getContext().asType(ManufOrder.class);
      manufOrder = Beans.get(ManufOrderRepository.class).find(manufOrder.getId());
      Beans.get(ManufOrderReservedQtyService.class).deallocateAll(manufOrder);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called from the toolbar in manuf order form view. Call {@link
   * com.axelor.apps.production.service.manuforder.ManufOrderReservedQtyService#reserveAll(ManufOrder)}.
   *
   * @param request
   * @param response
   */
  public void reserveAll(ActionRequest request, ActionResponse response) {
    try {
      ManufOrder manufOrder = request.getContext().asType(ManufOrder.class);
      manufOrder = Beans.get(ManufOrderRepository.class).find(manufOrder.getId());
      Beans.get(ManufOrderReservedQtyService.class).reserveAll(manufOrder);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called from the toolbar in manuf order form view. Call {@link
   * com.axelor.apps.production.service.manuforder.ManufOrderReservedQtyService#cancelReservation(ManufOrder)}.
   *
   * @param request
   * @param response
   */
  public void cancelReservation(ActionRequest request, ActionResponse response) {
    try {
      ManufOrder manufOrder = request.getContext().asType(ManufOrder.class);
      manufOrder = Beans.get(ManufOrderRepository.class).find(manufOrder.getId());
      Beans.get(ManufOrderReservedQtyService.class).cancelReservation(manufOrder);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @SuppressWarnings("unchecked")
  public void removeUnselectedMOs(ActionRequest request, ActionResponse response) {
    try {
      Object object = request.getContext().get("draftManufOrderList");
      if (object == null) {
        return;
      }
      List<Map<String, Object>> manufOrders = (List<Map<String, Object>>) object;
      List<Long> ids =
          Beans.get(ManufOrderService.class).planSelectedOrdersAndDiscardOthers(manufOrders);
      if (ObjectUtils.isEmpty(ids)) {
        ids.add(0L);
      }
      response.setView(
          ActionView.define(I18n.get("Manufacturing orders"))
              .model(ManufOrder.class.getName())
              .add("grid", "generated-manuf-order-grid")
              .add("form", "manuf-order-form")
              .domain("self.id in (" + Joiner.on(",").join(ids) + ")")
              .map());

    } catch (AxelorException e) {
      TraceBackService.trace(response, e);
    }
  }

  public void computeProducibleQty(ActionRequest request, ActionResponse response) {
    try {
      ManufOrder manufOrder = request.getContext().asType(ManufOrder.class);
      BigDecimal producibleQty =
          Beans.get(ManufOrderService.class).computeProducibleQty(manufOrder);
      response.setValue("$producibleQty", producibleQty);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
