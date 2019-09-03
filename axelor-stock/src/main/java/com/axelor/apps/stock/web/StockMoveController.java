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
package com.axelor.apps.stock.web;

import com.axelor.apps.base.db.PrintingSettings;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.TradingNameService;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.exception.IExceptionMessage;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.apps.tool.StringTool;
import com.axelor.db.mapper.Mapper;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Singleton
public class StockMoveController {

  @Inject private StockMoveService stockMoveService;

  @Inject private StockMoveRepository stockMoveRepo;

  public void plan(ActionRequest request, ActionResponse response) {

    StockMove stockMove = request.getContext().asType(StockMove.class);
    try {
      stockMoveService.plan(stockMoveRepo.find(stockMove.getId()));
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void realize(ActionRequest request, ActionResponse response) {

    StockMove stockMoveFromRequest = request.getContext().asType(StockMove.class);

    try {
      StockMove stockMove = stockMoveRepo.find(stockMoveFromRequest.getId());
      String newSeq = stockMoveService.realize(stockMove);

      response.setReload(true);

      if (newSeq != null) {
        if (stockMove.getTypeSelect() == StockMoveRepository.TYPE_INCOMING) {
          response.setFlash(
              String.format(
                  I18n.get(IExceptionMessage.STOCK_MOVE_INCOMING_PARTIAL_GENERATED), newSeq));
        } else if (stockMove.getTypeSelect() == StockMoveRepository.TYPE_OUTGOING) {
          response.setFlash(
              String.format(
                  I18n.get(IExceptionMessage.STOCK_MOVE_OUTGOING_PARTIAL_GENERATED), newSeq));
        } else {
          response.setFlash(String.format(I18n.get(IExceptionMessage.STOCK_MOVE_9), newSeq));
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void cancel(ActionRequest request, ActionResponse response) {
    StockMove stockMove = request.getContext().asType(StockMove.class);

    try {
      stockMoveService.cancel(stockMoveRepo.find(stockMove.getId()), stockMove.getCancelReason());
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
  public void printStockMove(ActionRequest request, ActionResponse response) {
    StockMove stockMove = request.getContext().asType(StockMove.class);
    @SuppressWarnings("unchecked")
    List<Integer> lstSelectedMove = (List<Integer>) request.getContext().get("_ids");

    try {
      String fileLink = stockMoveService.printStockMove(stockMove, lstSelectedMove, false);
      response.setView(ActionView.define(I18n.get("Stock move")).add("html", fileLink).map());
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
  public void printPickingStockMove(ActionRequest request, ActionResponse response) {
    StockMove stockMove = request.getContext().asType(StockMove.class);
    @SuppressWarnings("unchecked")
    List<Integer> lstSelectedMove = (List<Integer>) request.getContext().get("_ids");

    try {
      String fileLink = stockMoveService.printStockMove(stockMove, lstSelectedMove, true);
      response.setView(ActionView.define(I18n.get("Stock move")).add("html", fileLink).map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void viewDirection(ActionRequest request, ActionResponse response) {

    StockMove stockMove = request.getContext().asType(StockMove.class);
    try {
      Map<String, Object> result = Beans.get(StockMoveService.class).viewDirection(stockMove);
      Map<String, Object> mapView = new HashMap<>();
      mapView.put("title", I18n.get("Map"));
      mapView.put("resource", result.get("url"));
      mapView.put("viewType", "html");
      response.setView(mapView);
    } catch (Exception e) {
      response.setFlash(e.getLocalizedMessage());
    }
  }

  @SuppressWarnings("unchecked")
  public void splitStockMoveLinesUnit(ActionRequest request, ActionResponse response) {
    List<StockMoveLine> stockMoveLines =
        (List<StockMoveLine>) request.getContext().get("stockMoveLineList");
    if (stockMoveLines == null) {
      response.setFlash(I18n.get(IExceptionMessage.STOCK_MOVE_14));
      return;
    }
    Boolean selected = stockMoveService.splitStockMoveLinesUnit(stockMoveLines, new BigDecimal(1));

    if (!selected) response.setFlash(I18n.get(IExceptionMessage.STOCK_MOVE_15));
    response.setReload(true);
    response.setCanClose(true);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public void splitStockMoveLinesSpecial(ActionRequest request, ActionResponse response) {
    try {
      List<HashMap> selectedStockMoveLineMapList =
          (List<HashMap>) request.getContext().get("stockMoveLineList");
      Map stockMoveMap = (Map<String, Object>) request.getContext().get("stockMove");
      if (selectedStockMoveLineMapList == null) {
        response.setFlash(I18n.get(IExceptionMessage.STOCK_MOVE_14));
        return;
      }

      List<StockMoveLine> stockMoveLineList = new ArrayList<>();
      StockMoveLineRepository stockMoveLineRepo = Beans.get(StockMoveLineRepository.class);
      for (HashMap map : selectedStockMoveLineMapList) {
        StockMoveLine stockMoveLine = (StockMoveLine) Mapper.toBean(StockMoveLine.class, map);
        stockMoveLineList.add(stockMoveLineRepo.find(stockMoveLine.getId()));
      }

      if (stockMoveLineList.isEmpty()) {
        response.setFlash(I18n.get(IExceptionMessage.STOCK_MOVE_15));
        return;
      }

      BigDecimal splitQty = new BigDecimal(request.getContext().get("splitQty").toString());
      if (splitQty == null || splitQty.compareTo(BigDecimal.ZERO) < 1) {
        response.setFlash(I18n.get(IExceptionMessage.STOCK_MOVE_16));
        return;
      }

      StockMove stockMove = Mapper.toBean(StockMove.class, stockMoveMap);
      stockMove = Beans.get(StockMoveRepository.class).find(stockMove.getId());
      stockMoveService.splitStockMoveLinesSpecial(stockMove, stockMoveLineList, splitQty);
      response.setCanClose(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void shipReciveAllProducts(ActionRequest request, ActionResponse response) {
    StockMove stockMove = request.getContext().asType(StockMove.class);
    stockMoveService.copyQtyToRealQty(stockMoveRepo.find(stockMove.getId()));
    response.setReload(true);
  }

  public void generateReversion(ActionRequest request, ActionResponse response) {

    StockMove stockMove = request.getContext().asType(StockMove.class);

    try {
      Optional<StockMove> reversion =
          stockMoveService.generateReversion(stockMoveRepo.find(stockMove.getId()));
      if (reversion.isPresent()) {
        response.setView(
            ActionView.define(I18n.get("Stock move"))
                .model(StockMove.class.getName())
                .add("grid", "stock-move-grid")
                .add("form", "stock-move-form")
                .param("forceEdit", "true")
                .context("_showRecord", String.valueOf(reversion.get().getId()))
                .map());
      } else {
        response.setFlash(I18n.get("No reversion generated"));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void splitInto2(ActionRequest request, ActionResponse response) {
    StockMove stockMove = request.getContext().asType(StockMove.class);
    List<StockMoveLine> modifiedStockMoveLineList = stockMove.getStockMoveLineList();
    stockMove = Beans.get(StockMoveRepository.class).find(stockMove.getId());
    try {
      StockMove newStockMove = stockMoveService.splitInto2(stockMove, modifiedStockMoveLineList);

      if (newStockMove == null) {
        response.setFlash(I18n.get(IExceptionMessage.STOCK_MOVE_SPLIT_NOT_GENERATED));
      } else {
        response.setCanClose(true);

        response.setView(
            ActionView.define("Stock move")
                .model(StockMove.class.getName())
                .add("grid", "stock-move-grid")
                .add("form", "stock-move-form")
                .param("forceEdit", "true")
                .context("_showRecord", String.valueOf(newStockMove.getId()))
                .map());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void changeConformityStockMove(ActionRequest request, ActionResponse response) {
    StockMove stockMove = request.getContext().asType(StockMove.class);

    response.setValue("stockMoveLineList", stockMoveService.changeConformityStockMove(stockMove));
  }

  public void changeConformityStockMoveLine(ActionRequest request, ActionResponse response) {
    StockMove stockMove = request.getContext().asType(StockMove.class);

    response.setValue(
        "conformitySelect", stockMoveService.changeConformityStockMoveLine(stockMove));
  }

  public void compute(ActionRequest request, ActionResponse response) {

    StockMove stockMove = request.getContext().asType(StockMove.class);
    response.setValue("exTaxTotal", stockMoveService.compute(stockMove));
  }

  public void openStockPerDay(ActionRequest request, ActionResponse response) {

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
  }

  public void fillAddressesStr(ActionRequest request, ActionResponse response) {
    StockMove stockMove = request.getContext().asType(StockMove.class);
    stockMoveService.computeAddressStr(stockMove);

    response.setValues(stockMove);
  }

  /**
   * Called on printing settings select. Set the the domain for {@link StockMove#printingSettings}
   *
   * @param request
   * @param response
   */
  public void filterPrintingSettings(ActionRequest request, ActionResponse response) {
    StockMove stockMove = request.getContext().asType(StockMove.class);

    List<PrintingSettings> printingSettingsList =
        Beans.get(TradingNameService.class)
            .getPrintingSettingsList(stockMove.getTradingName(), stockMove.getCompany());
    String domain =
        String.format(
            "self.id IN (%s)",
            !printingSettingsList.isEmpty()
                ? StringTool.getIdListString(printingSettingsList)
                : "0");

    response.setAttr("printingSettings", "domain", domain);
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
}
