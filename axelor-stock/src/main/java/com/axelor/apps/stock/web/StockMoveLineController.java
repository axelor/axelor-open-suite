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
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.InternationalService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.exception.StockExceptionMessage;
import com.axelor.apps.stock.service.StockLocationLineService;
import com.axelor.apps.stock.service.StockMoveLineService;
import com.axelor.auth.AuthUtils;
import com.axelor.db.mapper.Mapper;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.axelor.utils.db.Wizard;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

@Singleton
public class StockMoveLineController {

  public void compute(ActionRequest request, ActionResponse response) throws AxelorException {
    StockMoveLine stockMoveLine = request.getContext().asType(StockMoveLine.class);
    StockMove stockMove = stockMoveLine.getStockMove();
    if (stockMove == null) {
      Context parentContext = request.getContext().getParent();
      Context superParentContext = parentContext.getParent();
      if (parentContext.getContextClass().equals(StockMove.class)) {
        stockMove = parentContext.asType(StockMove.class);
      } else if (superParentContext.getContextClass().equals(StockMove.class)) {
        stockMove = superParentContext.asType(StockMove.class);
      } else {
        return;
      }
    }
    stockMoveLine = Beans.get(StockMoveLineService.class).compute(stockMoveLine, stockMove);
    response.setValue("unitPriceUntaxed", stockMoveLine.getUnitPriceUntaxed());
    response.setValue("unitPriceTaxed", stockMoveLine.getUnitPriceTaxed());
    response.setValue("companyUnitPriceUntaxed", stockMoveLine.getCompanyUnitPriceUntaxed());
  }

  public void setProductInfo(ActionRequest request, ActionResponse response) {

    StockMoveLine stockMoveLine = request.getContext().asType(StockMoveLine.class);
    StockMoveLineService stockMoveLineService = Beans.get(StockMoveLineService.class);
    StockMove stockMove = stockMoveLine.getStockMove();
    try {

      if (stockMove == null) {
        stockMove = request.getContext().getParent().asType(StockMove.class);
      }

      if (stockMoveLine.getProduct() == null) {
        stockMoveLineService.resetStockMoveLine(stockMoveLine);
        stockMoveLine.setStockMove(stockMove);
        response.setValues(Mapper.toMap(stockMoveLine));
        return;
      }

      stockMoveLineService.setProductInfo(stockMove, stockMoveLine, stockMove.getCompany());
      response.setValues(stockMoveLine);
    } catch (Exception e) {
      stockMoveLineService.resetStockMoveLine(stockMoveLine);
      stockMoveLine.setStockMove(stockMove);
      response.setValues(Mapper.toMap(stockMoveLine));
      TraceBackService.trace(response, e, ResponseMessageType.INFORMATION);
    }
  }

  public void emptyLine(ActionRequest request, ActionResponse response) {
    StockMoveLine stockMoveLine = request.getContext().asType(StockMoveLine.class);
    if (stockMoveLine.getLineTypeSelect() != StockMoveLineRepository.TYPE_NORMAL) {
      Map<String, Object> newStockMoveLine = Mapper.toMap(new StockMoveLine());
      newStockMoveLine.put("productName", stockMoveLine.getProductName());
      newStockMoveLine.put("qty", BigDecimal.ZERO);
      newStockMoveLine.put("realQty", BigDecimal.ZERO);
      newStockMoveLine.put("id", stockMoveLine.getId());
      newStockMoveLine.put("version", stockMoveLine.getVersion());
      newStockMoveLine.put("lineTypeSelect", stockMoveLine.getLineTypeSelect());
      response.setValues(newStockMoveLine);
    }
  }

  public void splitStockMoveLineByTrackingNumber(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();

    if (context.get("trackingNumbers") == null) {
      response.setAlert(I18n.get(StockExceptionMessage.TRACK_NUMBER_WIZARD_NO_RECORD_ADDED_ERROR));
    } else {
      @SuppressWarnings("unchecked")
      LinkedHashMap<String, Object> stockMoveLineMap =
          (LinkedHashMap<String, Object>) context.get("_stockMoveLine");
      Integer stockMoveLineId = (Integer) stockMoveLineMap.get("id");
      StockMoveLine stockMoveLine =
          Beans.get(StockMoveLineRepository.class).find(new Long(stockMoveLineId));

      @SuppressWarnings("unchecked")
      ArrayList<LinkedHashMap<String, Object>> trackingNumbers =
          (ArrayList<LinkedHashMap<String, Object>>) context.get("trackingNumbers");

      Beans.get(StockMoveLineService.class)
          .splitStockMoveLineByTrackingNumber(stockMoveLine, trackingNumbers);
      response.setCanClose(true);
    }
  }

  public void openTrackNumberWizard(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();
    StockMoveLine stockMoveLine = context.asType(StockMoveLine.class);
    StockMove stockMove = null;
    if (context.getParent() != null
        && context.getParent().get("_model").equals("com.axelor.apps.stock.db.StockMove")) {
      stockMove = context.getParent().asType(StockMove.class);
    } else if (stockMoveLine.getStockMove() != null
        && stockMoveLine.getStockMove().getId() != null) {
      stockMove = Beans.get(StockMoveRepository.class).find(stockMoveLine.getStockMove().getId());
    }

    boolean _hasWarranty = false, _isPerishable = false, _isSeqUsedForSerialNumber = false;
    if (stockMoveLine.getProduct() != null) {
      Product product = stockMoveLine.getProduct();
      _hasWarranty = product.getHasWarranty();
      _isPerishable = product.getIsPerishable();
      if (product.getTrackingNumberConfiguration() != null) {
        _isSeqUsedForSerialNumber =
            product.getTrackingNumberConfiguration().getUseTrackingNumberSeqAsSerialNbr();
      }
    }
    response.setView(
        ActionView.define(I18n.get(StockExceptionMessage.TRACK_NUMBER_WIZARD_TITLE))
            .model(Wizard.class.getName())
            .add("form", "stock-move-line-track-number-wizard-form")
            .param("popup", "reload")
            .param("show-toolbar", "false")
            .param("show-confirm", "false")
            .param("width", "large")
            .param("popup-save", "false")
            .context("_stockMove", stockMove)
            .context("_stockMoveLine", stockMoveLine)
            .context("_hasWarranty", _hasWarranty)
            .context("_isPerishable", _isPerishable)
            .context("_isSeqUsedForSerialNumber", _isSeqUsedForSerialNumber)
            .map());
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public void computeAvailableQty(ActionRequest request, ActionResponse response)
      throws AxelorException {

    Context context = request.getContext();
    StockMoveLine stockMoveLineContext = context.asType(StockMoveLine.class);
    StockMoveLine stockMoveLine = null;
    if (stockMoveLineContext.getId() != null) {
      stockMoveLine = Beans.get(StockMoveLineRepository.class).find(stockMoveLineContext.getId());
      if (stockMoveLineContext.getProduct() != null
          && !stockMoveLineContext.getProduct().equals(stockMoveLine.getProduct())) {
        stockMoveLine = stockMoveLineContext;
      }
    } else {
      stockMoveLine = stockMoveLineContext;
    }

    StockLocation stockLocation = null;
    if (context.get("_parent") != null
        && ((Map) context.get("_parent")).get("fromStockLocation") != null) {

      Map<String, Object> _parent = (Map<String, Object>) context.get("_parent");

      stockLocation =
          Beans.get(StockLocationRepository.class)
              .find(Long.parseLong(((Map) _parent.get("fromStockLocation")).get("id").toString()));

    } else if (stockMoveLine.getStockMove() != null) {
      stockLocation = stockMoveLine.getStockMove().getFromStockLocation();
    }

    if (stockLocation != null) {
      Beans.get(StockMoveLineService.class).updateAvailableQty(stockMoveLine, stockLocation);
      response.setValue("$availableQty", stockMoveLine.getAvailableQty());
      response.setValue("$availableQtyForProduct", stockMoveLine.getAvailableQtyForProduct());
    }
  }

  public void setProductDomain(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();
    StockMoveLine stockMoveLine = context.asType(StockMoveLine.class);
    StockMove stockMove =
        context.getParent() != null
            ? context.getParent().asType(StockMove.class)
            : stockMoveLine.getStockMove();
    try {
      String domain =
          Beans.get(StockMoveLineService.class).createDomainForProduct(stockMoveLine, stockMove);
      response.setAttr("product", "domain", domain);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setAvailableStatus(ActionRequest request, ActionResponse response) {
    StockMoveLine stockMoveLine = request.getContext().asType(StockMoveLine.class);
    Beans.get(StockMoveLineService.class).setAvailableStatus(stockMoveLine);
    response.setValue("availableStatus", stockMoveLine.getAvailableStatus());
    response.setValue("availableStatusSelect", stockMoveLine.getAvailableStatusSelect());
  }

  public void displayAvailableTrackingNumber(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();

    @SuppressWarnings("unchecked")
    LinkedHashMap<String, Object> stockMoveLineMap =
        (LinkedHashMap<String, Object>) context.get("_stockMoveLine");
    @SuppressWarnings("unchecked")
    LinkedHashMap<String, Object> stockMoveMap =
        (LinkedHashMap<String, Object>) context.get("_stockMove");
    Integer stockMoveLineId = (Integer) stockMoveLineMap.get("id");
    Integer stockMoveId = (Integer) stockMoveMap.get("id");
    StockMoveLine stockMoveLine =
        Beans.get(StockMoveLineRepository.class).find(new Long(stockMoveLineId));
    StockMove stockMove = Beans.get(StockMoveRepository.class).find(new Long(stockMoveId));

    if (stockMoveLine == null
        || stockMoveLine.getProduct() == null
        || stockMove == null
        || stockMove.getFromStockLocation() == null) {
      return;
    }

    List<TrackingNumber> trackingNumberList =
        Beans.get(StockMoveLineService.class).getAvailableTrackingNumbers(stockMoveLine, stockMove);
    if (trackingNumberList == null || trackingNumberList.isEmpty()) {
      return;
    }

    SortedSet<Map<String, Object>> trackingNumbers =
        new TreeSet<Map<String, Object>>(
            Comparator.comparing(m -> (String) m.get("trackingNumberSeq")));
    StockLocationLineService stockLocationLineService = Beans.get(StockLocationLineService.class);
    for (TrackingNumber trackingNumber : trackingNumberList) {
      StockLocationLine detailStockLocationLine =
          stockLocationLineService.getDetailLocationLine(
              stockMove.getFromStockLocation(), stockMoveLine.getProduct(), trackingNumber);
      BigDecimal availableQty =
          detailStockLocationLine != null
              ? detailStockLocationLine.getCurrentQty()
              : BigDecimal.ZERO;
      Map<String, Object> map = new HashMap<String, Object>();
      map.put("trackingNumber", trackingNumber);
      map.put("trackingNumberSeq", trackingNumber.getTrackingNumberSeq());
      map.put("counter", BigDecimal.ZERO);
      map.put("warrantyExpirationDate", trackingNumber.getWarrantyExpirationDate());
      map.put("perishableExpirationDate", trackingNumber.getPerishableExpirationDate());
      map.put("$availableQty", availableQty);
      map.put("$moveTypeSelect", stockMove.getTypeSelect());
      map.put("origin", trackingNumber.getOrigin());
      map.put("note", trackingNumber.getNote());

      trackingNumbers.add(map);
    }
    response.setValue("$trackingNumbers", trackingNumbers);
  }

  public void translateProductDescriptionAndName(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();
      InternationalService internationalService = Beans.get(InternationalService.class);
      StockMoveLine stockMoveLine = context.asType(StockMoveLine.class);
      Context parentContext = context.getParent();
      if (parentContext != null && parentContext.getContextClass().equals(StockMove.class)) {
        StockMove stockMove = parentContext.asType(StockMove.class);
        Partner partner = stockMove.getPartner();
        String userLanguage = AuthUtils.getUser().getLanguage();
        Product product = stockMoveLine.getProduct();

        if (product != null) {
          Map<String, String> translation =
              internationalService.getProductDescriptionAndNameTranslation(
                  product, partner, userLanguage);

          String description = translation.get("description");
          String productName = translation.get("productName");

          if (description != null
              && !description.isEmpty()
              && productName != null
              && !productName.isEmpty()) {
            response.setValue("description", description);
            response.setValue("productName", productName);
          }
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
