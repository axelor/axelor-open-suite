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
package com.axelor.apps.stock.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.TraceBack;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.TradingNameService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.exception.StockExceptionMessage;
import com.axelor.apps.stock.service.StockMoveCheckWapService;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.apps.stock.service.StockMoveToolService;
import com.axelor.apps.stock.service.config.StockConfigService;
import com.axelor.apps.stock.service.stockmove.print.ConformityCertificatePrintService;
import com.axelor.apps.stock.service.stockmove.print.PickingStockMovePrintService;
import com.axelor.apps.stock.service.stockmove.print.StockMovePrintService;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.db.mapper.Mapper;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.message.exception.MessageExceptionMessage;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Singleton;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class StockMoveController {

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public void plan(ActionRequest request, ActionResponse response) {
    try {
      StockMove stockMove = request.getContext().asType(StockMove.class);
      // we have to inject TraceBackService to use non static methods
      TraceBackService traceBackService = Beans.get(TraceBackService.class);
      long tracebackCount = traceBackService.countMessageTraceBack(stockMove);
      Beans.get(StockMoveService.class)
          .plan(Beans.get(StockMoveRepository.class).find(stockMove.getId()));
      response.setReload(true);
      if (traceBackService.countMessageTraceBack(stockMove) > tracebackCount) {
        traceBackService
            .findLastMessageTraceBack(stockMove)
            .ifPresent(
                traceback ->
                    response.setNotify(
                        String.format(
                            I18n.get(MessageExceptionMessage.SEND_EMAIL_EXCEPTION),
                            traceback.getMessage())));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void manageBackorder(ActionRequest request, ActionResponse response) {
    try {

      StockMove stockMove = request.getContext().asType(StockMove.class);
      response.setView(
          ActionView.define(I18n.get("Manage backorder?"))
              .model(StockMove.class.getName())
              .add("form", "popup-stock-move-backorder-form")
              .param("popup", "reload")
              .param("show-toolbar", "false")
              .param("show-confirm", "false")
              .param("popup-save", "false")
              .context("_showRecord", stockMove.getId())
              .map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void realize(ActionRequest request, ActionResponse response) {

    try {
      StockMove stockMoveFromRequest = request.getContext().asType(StockMove.class);

      StockMove stockMove = Beans.get(StockMoveRepository.class).find(stockMoveFromRequest.getId());
      // we have to inject TraceBackService to use non static methods
      TraceBackService traceBackService = Beans.get(TraceBackService.class);
      long tracebackCount = traceBackService.countMessageTraceBack(stockMove);
      Optional<TraceBack> lastTracebackBeforeOptional =
          traceBackService.findLastAlertTraceBack(stockMove);
      String newSeq = Beans.get(StockMoveService.class).realize(stockMove);

      response.setReload(true);

      if (newSeq != null) {
        if (stockMove.getTypeSelect() == StockMoveRepository.TYPE_INCOMING) {
          response.setInfo(
              String.format(
                  I18n.get(StockExceptionMessage.STOCK_MOVE_INCOMING_PARTIAL_GENERATED), newSeq));
        } else if (stockMove.getTypeSelect() == StockMoveRepository.TYPE_OUTGOING) {
          response.setInfo(
              String.format(
                  I18n.get(StockExceptionMessage.STOCK_MOVE_OUTGOING_PARTIAL_GENERATED), newSeq));
        } else {
          response.setInfo(String.format(I18n.get(StockExceptionMessage.STOCK_MOVE_9), newSeq));
        }
      }
      if (traceBackService.countMessageTraceBack(stockMove) > tracebackCount) {
        traceBackService
            .findLastMessageTraceBack(stockMove)
            .ifPresent(
                traceback ->
                    response.setNotify(
                        String.format(
                            I18n.get(MessageExceptionMessage.SEND_EMAIL_EXCEPTION),
                            traceback.getMessage())));
      }
      Optional<TraceBack> lastTracebackAfterOptional =
          traceBackService.findLastAlertTraceBack(stockMove);
      if (lastTracebackAfterOptional.isPresent()) {
        TraceBack lastTracebackAfter = lastTracebackAfterOptional.get();
        if (!lastTracebackBeforeOptional.isPresent()
            || !lastTracebackAfter.equals(lastTracebackBeforeOptional.get())) {
          response.setInfo(lastTracebackAfter.getMessage());
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void draft(ActionRequest request, ActionResponse response) {
    try {
      StockMove stockMove = request.getContext().asType(StockMove.class);

      Beans.get(StockMoveService.class)
          .goBackToDraft(Beans.get(StockMoveRepository.class).find(stockMove.getId()));
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void cancel(ActionRequest request, ActionResponse response) {
    try {
      StockMove stockMove = request.getContext().asType(StockMove.class);

      Beans.get(StockMoveService.class)
          .cancel(
              Beans.get(StockMoveRepository.class).find(stockMove.getId()),
              stockMove.getCancelReason());
      response.setCanClose(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Method called from stock move form and grid view. Print one or more stock move as PDF
   *
   * @param request
   * @param response
   */
  @SuppressWarnings("unchecked")
  public void printStockMove(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();
      String fileLink;
      String title;

      StockMovePrintService stockMovePrintService = Beans.get(StockMovePrintService.class);

      if (!ObjectUtils.isEmpty(request.getContext().get("_ids"))) {
        List<Long> ids =
            (List)
                (((List) context.get("_ids"))
                    .stream()
                        .filter(ObjectUtils::notEmpty)
                        .map(input -> Long.parseLong(input.toString()))
                        .collect(Collectors.toList()));
        fileLink = stockMovePrintService.printStockMoves(ids);
        title = I18n.get("Stock Moves");
      } else if (context.get("id") != null) {
        StockMove stockMove =
            Beans.get(StockMoveRepository.class).find(Long.parseLong(context.get("id").toString()));
        title = stockMovePrintService.getFileName(stockMove);
        fileLink = stockMovePrintService.printStockMove(stockMove, ReportSettings.FORMAT_PDF);
        logger.debug("Printing " + title);
      } else {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            I18n.get(StockExceptionMessage.STOCK_MOVE_PRINT));
      }
      response.setView(ActionView.define(title).add("html", fileLink).map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Method called from stock move form and grid view. Print one or more stock move as PDF
   *
   * @param request
   * @param response
   */
  @SuppressWarnings("unchecked")
  public void printPickingStockMove(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();
      String fileLink;
      String title;
      String userType = (String) context.get("_userType");

      PickingStockMovePrintService pickingstockMovePrintService =
          Beans.get(PickingStockMovePrintService.class);

      if (!ObjectUtils.isEmpty(context.get("_ids"))) {
        List<Long> ids =
            (List)
                (((List) context.get("_ids"))
                    .stream()
                        .filter(ObjectUtils::notEmpty)
                        .map(input -> Long.parseLong(input.toString()))
                        .collect(Collectors.toList()));
        fileLink = pickingstockMovePrintService.printStockMoves(ids, userType);
        title = I18n.get("Stock Moves");
      } else if (context.get("id") != null) {
        StockMove stockMove = context.asType(StockMove.class);
        stockMove = Beans.get(StockMoveRepository.class).find(stockMove.getId());
        title = pickingstockMovePrintService.getFileName(stockMove);
        fileLink =
            pickingstockMovePrintService.printStockMove(
                stockMove, ReportSettings.FORMAT_PDF, userType);
        logger.debug("Printing " + title);
      } else {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            I18n.get(StockExceptionMessage.STOCK_MOVE_PRINT));
      }
      response.setReload(true);
      response.setView(ActionView.define(title).add("html", fileLink).map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called from stock move form view. Print conformity certificate for the given stock move.
   *
   * @param request
   * @param response
   */
  @SuppressWarnings("unchecked")
  public void printConformityCertificate(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();
      String fileLink;
      String title;

      ConformityCertificatePrintService conformityCertificatePrintService =
          Beans.get(ConformityCertificatePrintService.class);

      if (!ObjectUtils.isEmpty(context.get("_ids"))) {
        List<Long> ids =
            (List)
                (((List) context.get("_ids"))
                    .stream()
                        .filter(ObjectUtils::notEmpty)
                        .map(input -> Long.parseLong(input.toString()))
                        .collect(Collectors.toList()));
        fileLink = conformityCertificatePrintService.printConformityCertificates(ids);
        title = I18n.get("Conformity Certificates");
      } else if (context.get("id") != null) {

        StockMove stockMove = context.asType(StockMove.class);
        title = conformityCertificatePrintService.getFileName(stockMove);
        fileLink =
            conformityCertificatePrintService.printConformityCertificate(
                stockMove, ReportSettings.FORMAT_PDF);

        logger.debug("Printing {}", title);
      } else {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            I18n.get(StockExceptionMessage.STOCK_MOVE_PRINT));
      }
      response.setView(ActionView.define(title).add("html", fileLink).map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void viewDirection(ActionRequest request, ActionResponse response) {

    try {
      StockMove stockMove = request.getContext().asType(StockMove.class);
      Map<String, Object> result = Beans.get(StockMoveService.class).viewDirection(stockMove);
      Map<String, Object> mapView = new HashMap<>();
      mapView.put("title", I18n.get("Map"));
      mapView.put("resource", result.get("url"));
      mapView.put("viewType", "html");
      response.setView(mapView);
    } catch (Exception e) {
      response.setInfo(e.getLocalizedMessage());
    }
  }

  @SuppressWarnings("unchecked")
  public void splitStockMoveLinesUnit(ActionRequest request, ActionResponse response) {
    try {
      StockMove stockMove = request.getContext().asType(StockMove.class);
      List<StockMoveLine> stockMoveLineContextList =
          (List<StockMoveLine>) request.getContext().get("stockMoveLineList");
      stockMove = Beans.get(StockMoveRepository.class).find(stockMove.getId());
      if (stockMoveLineContextList == null) {
        response.setInfo(I18n.get(StockExceptionMessage.STOCK_MOVE_14));
        return;
      }
      List<StockMoveLine> stockMoveLineList = new ArrayList<>();
      StockMoveLineRepository stockMoveLineRepo = Beans.get(StockMoveLineRepository.class);
      for (StockMoveLine stockMoveLineContext :
          stockMoveLineContextList.stream()
              .filter(StockMoveLine::isSelected)
              .collect(Collectors.toList())) {
        StockMoveLine stockMoveLine = stockMoveLineRepo.find(stockMoveLineContext.getId());
        stockMoveLine.setSelected(true);
        stockMoveLineList.add(stockMoveLine);
      }
      boolean selected =
          Beans.get(StockMoveService.class)
              .splitStockMoveLines(stockMove, stockMoveLineList, BigDecimal.ONE);

      if (!selected) {
        response.setInfo(I18n.get(StockExceptionMessage.STOCK_MOVE_15));
      }
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @SuppressWarnings("unchecked")
  public void splitStockMoveLinesSpecial(ActionRequest request, ActionResponse response) {
    try {
      List<HashMap<String, Object>> selectedStockMoveLineMapList =
          (List<HashMap<String, Object>>) request.getContext().get("stockMoveLineList");
      Map<String, Object> stockMoveMap =
          (Map<String, Object>) request.getContext().get("stockMove");
      if (selectedStockMoveLineMapList == null) {
        response.setInfo(I18n.get(StockExceptionMessage.STOCK_MOVE_14));
        return;
      }

      List<StockMoveLine> stockMoveLineList = new ArrayList<>();
      StockMoveLineRepository stockMoveLineRepo = Beans.get(StockMoveLineRepository.class);
      for (HashMap<String, Object> map : selectedStockMoveLineMapList) {
        StockMoveLine stockMoveLine = Mapper.toBean(StockMoveLine.class, map);
        stockMoveLine = stockMoveLineRepo.find(stockMoveLine.getId());
        stockMoveLine.setSelected(true);
        stockMoveLineList.add(stockMoveLine);
      }

      if (stockMoveLineList.isEmpty()) {
        response.setInfo(I18n.get(StockExceptionMessage.STOCK_MOVE_15));
        return;
      }

      BigDecimal splitQty = null;
      if (request.getContext().containsKey("splitQty")) {
        splitQty = new BigDecimal(request.getContext().get("splitQty").toString());
      }
      if (splitQty == null || splitQty.compareTo(BigDecimal.ZERO) < 1) {
        response.setInfo(I18n.get(StockExceptionMessage.STOCK_MOVE_16));
        return;
      }

      StockMove stockMove = Mapper.toBean(StockMove.class, stockMoveMap);
      stockMove = Beans.get(StockMoveRepository.class).find(stockMove.getId());
      Beans.get(StockMoveService.class).splitStockMoveLines(stockMove, stockMoveLineList, splitQty);
      response.setCanClose(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void shipReciveAllProducts(ActionRequest request, ActionResponse response) {
    try {
      StockMove stockMove = request.getContext().asType(StockMove.class);
      Beans.get(StockMoveService.class)
          .copyQtyToRealQty(Beans.get(StockMoveRepository.class).find(stockMove.getId()));
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void generateReversion(ActionRequest request, ActionResponse response) {

    try {
      StockMove stockMove = request.getContext().asType(StockMove.class);

      Optional<StockMove> reversion =
          Beans.get(StockMoveService.class)
              .generateReversion(Beans.get(StockMoveRepository.class).find(stockMove.getId()));
      if (reversion.isPresent()) {
        response.setView(
            ActionView.define(I18n.get("Stock move"))
                .model(StockMove.class.getName())
                .add("grid", "stock-move-grid")
                .add("form", "stock-move-form")
                .param("search-filters", "internal-stock-move-filters")
                .param("forceEdit", "true")
                .context("_showRecord", String.valueOf(reversion.get().getId()))
                .map());
      } else {
        response.setInfo(I18n.get("No reversion generated"));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void splitInto2(ActionRequest request, ActionResponse response) {
    try {
      StockMove stockMove = request.getContext().asType(StockMove.class);
      List<StockMoveLine> modifiedStockMoveLineList = stockMove.getStockMoveLineList();
      stockMove = Beans.get(StockMoveRepository.class).find(stockMove.getId());
      StockMove newStockMove =
          Beans.get(StockMoveService.class).splitInto2(stockMove, modifiedStockMoveLineList);

      if (newStockMove == null) {
        response.setInfo(I18n.get(StockExceptionMessage.STOCK_MOVE_SPLIT_NOT_GENERATED));
      } else {
        response.setCanClose(true);

        response.setView(
            ActionView.define(I18n.get("Stock move"))
                .model(StockMove.class.getName())
                .add("grid", "stock-move-grid")
                .add("form", "stock-move-form")
                .param("search-filters", "internal-stock-move-filters")
                .param("forceEdit", "true")
                .context("_showRecord", String.valueOf(newStockMove.getId()))
                .map());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void changeConformityStockMove(ActionRequest request, ActionResponse response) {
    try {

      StockMove stockMove = request.getContext().asType(StockMove.class);

      response.setValue(
          "stockMoveLineList",
          Beans.get(StockMoveService.class).changeConformityStockMove(stockMove));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void changeConformityStockMoveLine(ActionRequest request, ActionResponse response) {
    try {
      StockMove stockMove = request.getContext().asType(StockMove.class);

      response.setValue(
          "conformitySelect",
          Beans.get(StockMoveService.class).changeConformityStockMoveLine(stockMove));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void compute(ActionRequest request, ActionResponse response) {
    try {
      StockMove stockMove = request.getContext().asType(StockMove.class);
      response.setValue("exTaxTotal", Beans.get(StockMoveToolService.class).compute(stockMove));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void openStockPerDay(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();

      Long locationId =
          Long.parseLong(((Map<String, Object>) context.get("stockLocation")).get("id").toString());
      LocalDate fromDate = LocalDate.parse(context.get("stockFromDate").toString());
      LocalDate toDate = LocalDate.parse(context.get("stockToDate").toString());

      Collection<Map<String, Object>> products =
          (Collection<Map<String, Object>>) context.get("productSet");

      String domain = null;
      List<Object> productIds = null;
      if (products != null && !products.isEmpty()) {
        productIds = Arrays.asList(products.stream().map(p -> p.get("id")).toArray());
        domain = "self.id in (:productIds)";
      }

      response.setView(
          ActionView.define(I18n.get("Stocks"))
              .model(Product.class.getName())
              .add("cards", "stock-product-cards")
              .add("grid", "stock-product-grid")
              .add("form", "stock-product-form")
              .domain(domain)
              .context("fromStockWizard", true)
              .context("productIds", productIds)
              .context("stockFromDate", fromDate)
              .context("stockToDate", toDate)
              .context("locationId", locationId)
              .map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void fillAddressesStr(ActionRequest request, ActionResponse response) {
    try {
      StockMove stockMove = request.getContext().asType(StockMove.class);
      Beans.get(StockMoveToolService.class).computeAddressStr(stockMove);

      response.setValues(stockMove);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called on trading name change. Set the default value for {@link StockMove#printingSettings}
   *
   * @param request
   * @param response
   */
  public void fillDefaultPrintingSettings(ActionRequest request, ActionResponse response) {
    try {
      StockMove stockMove = request.getContext().asType(StockMove.class);
      response.setValue(
          "printingSettings",
          Beans.get(TradingNameService.class)
              .getDefaultPrintingSettings(stockMove.getTradingName(), stockMove.getCompany()));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setAvailableStatus(ActionRequest request, ActionResponse response) {
    try {
      StockMove stockMove = request.getContext().asType(StockMove.class);
      Beans.get(StockMoveService.class).setAvailableStatus(stockMove);
      response.setValue("stockMoveLineList", stockMove.getStockMoveLineList());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void updateMoveLineFilterOnAvailableproduct(
      ActionRequest request, ActionResponse response) {
    try {
      StockMove stockMove = request.getContext().asType(StockMove.class);
      if (stockMove.getStockMoveLineList() != null) {
        for (StockMoveLine stockMoveLine : stockMove.getStockMoveLineList()) {
          stockMoveLine.setFilterOnAvailableProducts(stockMove.getFilterOnAvailableProducts());
        }
        response.setValue("stockMoveLineList", stockMove.getStockMoveLineList());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called from stock move form view on save. Call {@link
   * StockMoveService#updateStocks(StockMove)}.
   *
   * @param request
   * @param response
   */
  public void updateStocks(ActionRequest request, ActionResponse response) {
    try {
      StockMove stockMove = request.getContext().asType(StockMove.class);
      Beans.get(StockMoveService.class)
          .updateStocks(Beans.get(StockMoveRepository.class).find(stockMove.getId()));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void refreshProductNetMass(ActionRequest request, ActionResponse response) {
    try {
      StockMove stockMove = request.getContext().asType(StockMove.class);
      Beans.get(StockMoveService.class).updateProductNetMass(stockMove);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void getFromStockLocation(ActionRequest request, ActionResponse response) {
    StockMove stockMove = request.getContext().asType(StockMove.class);
    try {
      StockLocation fromStockLocation =
          Beans.get(StockMoveService.class).getFromStockLocation(stockMove);
      response.setValue("fromStockLocation", fromStockLocation);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void getToStockLocation(ActionRequest request, ActionResponse response) {
    StockMove stockMove = request.getContext().asType(StockMove.class);
    try {
      StockLocation toStockLocation =
          Beans.get(StockMoveService.class).getToStockLocation(stockMove);
      response.setValue("toStockLocation", toStockLocation);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void checkWap(ActionRequest request, ActionResponse response) {

    try {
      StockMove stockMove = request.getContext().asType(StockMove.class);
      stockMove = Beans.get(StockMoveRepository.class).find(stockMove.getId());
      String productsWithErrorStr = Beans.get(StockMoveCheckWapService.class).checkWap(stockMove);
      if (StringUtils.isEmpty(productsWithErrorStr)) {
        response.setValue("isValidWAP", true);
        return;
      }

      Integer percentToleranceForWapChange =
          Beans.get(StockConfigService.class)
              .getStockConfig(stockMove.getToStockLocation().getCompany())
              .getPercentToleranceForWapChange();
      response.setView(
          ActionView.define(I18n.get("Stock move"))
              .model(StockMove.class.getName())
              .add("form", "popup-stock-move-check-wap-form")
              .param("popup", "reload")
              .param("show-toolbar", "false")
              .param("show-confirm", "false")
              .param("popup-save", "false")
              .context("_showRecord", stockMove.getId())
              .context("_percentToleranceForWapChange", percentToleranceForWapChange)
              .context("_productsWithErrorStr", productsWithErrorStr)
              .map());

      response.setValue("isValidWAP", false);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }
}
