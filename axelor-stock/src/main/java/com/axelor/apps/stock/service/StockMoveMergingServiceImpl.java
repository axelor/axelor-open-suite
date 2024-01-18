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
package com.axelor.apps.stock.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.exception.StockExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StockMoveMergingServiceImpl implements StockMoveMergingService {

  protected StockMoveRepository stockMoveRepository;
  protected AppBaseService appBaseService;
  protected StockMoveService stockMoveService;
  protected StockMoveToolService stockMoveToolService;
  protected PartnerService partnerService;
  protected StockMoveLineRepository stockMoveLineRepository;

  @Inject
  public StockMoveMergingServiceImpl(
      StockMoveRepository stockMoveRepository,
      AppBaseService appBaseService,
      StockMoveService stockMoveService,
      StockMoveToolService stockMoveToolService,
      PartnerService partnerService,
      StockMoveLineRepository stockMoveLineRepository) {
    this.stockMoveRepository = stockMoveRepository;
    this.appBaseService = appBaseService;
    this.stockMoveService = stockMoveService;
    this.stockMoveToolService = stockMoveToolService;
    this.partnerService = partnerService;
    this.stockMoveLineRepository = stockMoveLineRepository;
  }

  public String canMerge(List<StockMove> stockMoveList) {
    StringJoiner errors = new StringJoiner("</li><li>", "<ul><li>", "</li></ul>").setEmptyValue("");
    checkErrors(stockMoveList, errors);
    return errors.toString();
  }

  protected void checkErrors(List<StockMove> stockMoveList, StringJoiner errors) {
    if (!checkAllSame(stockMoveList, StockMove::getCompany)) {
      errors.add(I18n.get(StockExceptionMessage.STOCK_MOVE_MERGE_ERROR_COMPANY));
    }
    boolean enableTradingNames = appBaseService.getAppBase().getEnableTradingNamesManagement();
    if (enableTradingNames && !checkAllSame(stockMoveList, StockMove::getTradingName)) {
      errors.add(I18n.get(StockExceptionMessage.STOCK_MOVE_MERGE_ERROR_TRADING_NAME));
    }
    if (!checkAllSame(stockMoveList, StockMove::getPartner)) {
      errors.add(I18n.get(StockExceptionMessage.STOCK_MOVE_MERGE_ERROR_PARTNER));
    }
    if (!checkAllSame(stockMoveList, StockMove::getFromStockLocation)
        || !checkAllSame(stockMoveList, StockMove::getToStockLocation)) {
      errors.add(I18n.get(StockExceptionMessage.STOCK_MOVE_MERGE_ERROR_FROM_AND_TO_STOCK_LOCATION));
    }
    if (!stockMoveList.stream()
        .map(StockMove::getStatusSelect)
        .allMatch(
            value ->
                value.equals(StockMoveRepository.STATUS_DRAFT)
                    || value.equals(StockMoveRepository.STATUS_PLANNED))) {
      errors.add(I18n.get(StockExceptionMessage.STOCK_MOVE_MERGE_ERROR_STATUS));
    }
  }

  protected <T> boolean checkAllSame(
      List<StockMove> stockMoveList, Function<StockMove, T> function) {
    return stockMoveList.stream().map(function).distinct().count() == 1;
  }

  @Override
  public Boolean checkShipmentValues(List<StockMove> stockMoveList) {
    List<Function<StockMove, Object>> fields = getShipmentFieldsToCheck();
    return fields.stream()
        .map(function -> this.checkAllSame(stockMoveList, function))
        .anyMatch(Boolean.FALSE::equals);
  }

  protected List<Function<StockMove, Object>> getShipmentFieldsToCheck() {
    return new ArrayList<>(
        List.of(
            StockMove::getShipmentMode,
            StockMove::getFreightCarrierMode,
            StockMove::getCarrierPartner,
            StockMove::getForwarderPartner,
            StockMove::getIncoterm,
            StockMove::getModeOfTransport,
            StockMove::getTrackingNumber));
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public StockMove mergeStockMoves(List<StockMove> stockMoveList) throws AxelorException {
    StockMove stockMove =
        stockMoveList.stream().min(Comparator.comparingLong(StockMove::getId)).orElse(null);

    Address address = partnerService.getDeliveryAddress(stockMove.getPartner());
    Address fromAddress = stockMove.getFromStockLocation().getAddress();
    Address toAddress = stockMove.getToStockLocation().getAddress();

    if (stockMove.getTypeSelect() == StockMoveRepository.TYPE_INCOMING) {
      fromAddress = address;
    } else if (stockMove.getTypeSelect() == StockMoveRepository.TYPE_OUTGOING) {
      toAddress = address;
    }

    LocalDate estimatedDate =
        stockMoveList.stream()
            .map(StockMove::getEstimatedDate)
            .filter(Objects::nonNull)
            .min(LocalDate::compareTo)
            .orElse(null);

    StockMove mergedStockMove =
        stockMoveService.createStockMove(
            fromAddress,
            toAddress,
            stockMove.getCompany(),
            stockMove.getPartner(),
            stockMove.getFromStockLocation(),
            stockMove.getToStockLocation(),
            null,
            estimatedDate,
            null,
            stockMove.getShipmentMode(),
            stockMove.getFreightCarrierMode(),
            stockMove.getCarrierPartner(),
            stockMove.getForwarderPartner(),
            stockMove.getIncoterm(),
            stockMove.getTypeSelect());

    fillStockMoveFields(stockMoveList, stockMove, mergedStockMove);

    mergeStockMoveLines(mergedStockMove, stockMoveList);
    mergedStockMove.setExTaxTotal(stockMoveToolService.compute(mergedStockMove));
    stockMoveList.forEach(stockMoveService::setMergedStatus);

    return stockMoveRepository.save(mergedStockMove);
  }

  protected void fillStockMoveFields(
      List<StockMove> stockMoveList, StockMove stockMove, StockMove mergedStockMove) {
    mergedStockMove.setTrackingNumber(stockMove.getTrackingNumber());
    mergedStockMove.setModeOfTransport(stockMove.getModeOfTransport());
    mergedStockMove.setIsReversion(stockMove.getIsReversion());
    mergedStockMove.setOrigin(stockMove.getOrigin());
    mergedStockMove.setNote(
        stockMoveList.stream()
            .map(StockMove::getNote)
            .filter(Objects::nonNull)
            .collect(Collectors.joining(" ")));
    mergedStockMove.setPickingOrderComments(
        stockMoveList.stream()
            .map(StockMove::getPickingOrderComments)
            .filter(Objects::nonNull)
            .collect(Collectors.joining(" ")));

    Integer numOfPackages =
        stockMoveList.stream().map(StockMove::getNumOfPackages).reduce(0, Integer::sum);
    mergedStockMove.setNumOfPackages(numOfPackages);
    Integer numOfPalettes =
        stockMoveList.stream().map(StockMove::getNumOfPalettes).reduce(0, Integer::sum);
    mergedStockMove.setNumOfPalettes(numOfPalettes);

    if (appBaseService.getAppBase().getEnableTradingNamesManagement()) {
      mergedStockMove.setTradingName(stockMove.getTradingName());
    }
    if (checkAllSame(stockMoveList, StockMove::getConformitySelect)) {
      mergedStockMove.setConformitySelect(stockMove.getConformitySelect());
    }
    if (appBaseService.getAppBase().getIsRegroupProductsOnPrintings()) {
      mergedStockMove.setGroupProductsOnPrintings(
          mergedStockMove.getPartner().getGroupProductsOnPrintings());
    }
  }

  protected void mergeStockMoveLines(StockMove mergedStockMove, List<StockMove> stockMoveList) {
    for (StockMove stockMove : stockMoveList) {
      for (StockMoveLine stockMoveLine : stockMove.getStockMoveLineList()) {
        mergedStockMove.addStockMoveLineListItem(
            stockMoveLineRepository.copy(stockMoveLine, false));
      }
    }
  }
}
