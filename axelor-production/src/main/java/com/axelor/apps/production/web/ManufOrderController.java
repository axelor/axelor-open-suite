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
package com.axelor.apps.production.web;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.CostSheet;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.repo.CostSheetRepository;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.exceptions.IExceptionMessage;
import com.axelor.apps.production.report.IReport;
import com.axelor.apps.production.service.costsheet.CostSheetService;
import com.axelor.apps.production.service.manuforder.ManufOrderPrintService;
import com.axelor.apps.production.service.manuforder.ManufOrderService;
import com.axelor.apps.production.service.manuforder.ManufOrderStockMoveService;
import com.axelor.apps.production.service.manuforder.ManufOrderWorkflowService;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Singleton;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.birt.core.exception.BirtException;
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
        response.setNotify(I18n.get(IExceptionMessage.MANUF_ORDER_EMAIL_NOT_SENT));
      } else if (traceBackService.countMessageTraceBack(manufOrder) > tracebackCount) {
        traceBackService
            .findLastMessageTraceBack(manufOrder)
            .ifPresent(
                traceback ->
                    response.setNotify(
                        String.format(
                            I18n.get(
                                com.axelor.apps.message.exception.IExceptionMessage
                                    .SEND_EMAIL_EXCEPTION),
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
        response.setNotify(I18n.get(IExceptionMessage.MANUF_ORDER_EMAIL_NOT_SENT));
      } else if (traceBackService.countMessageTraceBack(manufOrder) > tracebackCount) {
        traceBackService
            .findLastMessageTraceBack(manufOrder)
            .ifPresent(
                traceback ->
                    response.setNotify(
                        String.format(
                            I18n.get(
                                com.axelor.apps.message.exception.IExceptionMessage
                                    .SEND_EMAIL_EXCEPTION),
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
      response.setFlash(I18n.get(IExceptionMessage.MANUF_ORDER_CANCEL));
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

      for (ManufOrder manufOrder : manufOrders) {

        Beans.get(ManufOrderWorkflowService.class).plan(manufOrder);

        if (manufOrder.getProdProcess().getGeneratePurchaseOrderOnMoPlanning()) {
          Beans.get(ManufOrderWorkflowService.class).createPurchaseOrder(manufOrder);
        }
      }

      response.setReload(true);
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

  /**
   * Method that generate a Pdf file for an manufacturing order
   *
   * @param request
   * @param response
   * @return
   * @throws BirtException
   * @throws IOException
   */
  public void print(ActionRequest request, ActionResponse response) {

    try {
      ManufOrder manufOrder = request.getContext().asType(ManufOrder.class);
      ManufOrderPrintService manufOrderPrintService = Beans.get(ManufOrderPrintService.class);
      @SuppressWarnings("unchecked")
      List<Integer> selectedManufOrderList = (List<Integer>) request.getContext().get("_ids");

      if (selectedManufOrderList != null) {
        String name = manufOrderPrintService.getManufOrdersFilename();
        String fileLink =
            manufOrderPrintService.printManufOrders(
                selectedManufOrderList.stream()
                    .map(Integer::longValue)
                    .collect(Collectors.toList()));
        LOG.debug("Printing {}", name);
        response.setView(ActionView.define(name).add("html", fileLink).map());
      } else if (manufOrder != null) {
        String name = manufOrderPrintService.getFileName(manufOrder);
        String fileLink = manufOrderPrintService.printManufOrder(manufOrder);
        LOG.debug("Printing {}", name);
        response.setView(ActionView.define(name).add("html", fileLink).map());
      } else {
        response.setFlash(I18n.get(IExceptionMessage.MANUF_ORDER_1));
      }
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
      String prodProcessId = manufOrder.getProdProcess().getId().toString();
      String prodProcessLable = manufOrder.getProdProcess().getName();

      String fileLink =
          ReportFactory.createReport(IReport.PROD_PROCESS, prodProcessLable + "-${date}")
              .addParam("Locale", ReportSettings.getPrintingLocale(null))
              .addParam(
                  "Timezone",
                  manufOrder.getCompany() != null ? manufOrder.getCompany().getTimezone() : null)
              .addParam("ProdProcessId", prodProcessId)
              .generate()
              .getFileLink();

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
}
