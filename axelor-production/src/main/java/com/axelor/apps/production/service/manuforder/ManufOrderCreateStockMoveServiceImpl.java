package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.StockMoveService;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ManufOrderCreateStockMoveServiceImpl implements ManufOrderCreateStockMoveService {

  protected PartnerService partnerService;
  protected StockMoveService stockMoveService;
  protected ManufOrderOutsourceService manufOrderOutsourceService;

  @Inject
  public ManufOrderCreateStockMoveServiceImpl(
      PartnerService partnerService,
      StockMoveService stockMoveService,
      ManufOrderOutsourceService manufOrderOutsourceService) {
    this.partnerService = partnerService;
    this.stockMoveService = stockMoveService;
    this.manufOrderOutsourceService = manufOrderOutsourceService;
  }

  @Override
  public StockMove _createToProduceStockMove(
      ManufOrder manufOrder,
      Company company,
      StockLocation virtualStockLocation,
      StockLocation producedProductStockLocation)
      throws AxelorException {

    StockMove stockMove;
    if (manufOrder.getOutsourcing()) {
      stockMove =
          _createToProduceIncomingOutsourceStockMove(
              manufOrder, company, virtualStockLocation, producedProductStockLocation);
    } else {
      stockMove =
          _createToProduceProductionStockMove(
              manufOrder, company, virtualStockLocation, producedProductStockLocation);
    }

    stockMove.setManufOrder(manufOrder);
    stockMove.setOrigin(manufOrder.getManufOrderSeq());
    return stockMove;
  }

  @Override
  public StockMove _createToConsumeStockMove(
      ManufOrder manufOrder,
      Company company,
      StockLocation fromStockLocation,
      StockLocation virtualStockLocation)
      throws AxelorException {

    StockMove stockMove;
    if (manufOrder.getOutsourcing()) {
      stockMove =
          _createToConsumeOutgoingOutsourceStockMove(
              manufOrder, company, fromStockLocation, virtualStockLocation);
    } else {
      stockMove =
          _createToConsumeProductionStockMove(
              manufOrder, company, fromStockLocation, virtualStockLocation);
    }

    stockMove.setManufOrder(manufOrder);
    stockMove.setOrigin(manufOrder.getManufOrderSeq());

    return stockMove;
  }

  @Override
  public StockMove _createToProduceIncomingOutsourceStockMove(
      ManufOrder manufOrder,
      Company company,
      StockLocation fromStockLocation,
      StockLocation toStockLocation)
      throws AxelorException {
    LocalDateTime plannedEndDateT = manufOrder.getPlannedEndDateT();
    LocalDate plannedEndDate = plannedEndDateT != null ? plannedEndDateT.toLocalDate() : null;

    Address toAddress = toStockLocation.getAddress();
    Address fromAddress =
        partnerService.getDefaultAddress(
            manufOrderOutsourceService.getOutsourcePartner(manufOrder).orElse(null));
    return stockMoveService.createStockMove(
        fromAddress,
        toAddress,
        company,
        fromStockLocation,
        toStockLocation,
        null,
        plannedEndDate,
        null,
        StockMoveRepository.TYPE_INCOMING);
  }

  @Override
  public StockMove _createToProduceProductionStockMove(
      ManufOrder manufOrder,
      Company company,
      StockLocation virtualStockLocation,
      StockLocation producedProductStockLocation)
      throws AxelorException {
    LocalDateTime plannedEndDateT = manufOrder.getPlannedEndDateT();
    LocalDate plannedEndDate = plannedEndDateT != null ? plannedEndDateT.toLocalDate() : null;
    return stockMoveService.createStockMove(
        null,
        null,
        company,
        virtualStockLocation,
        producedProductStockLocation,
        null,
        plannedEndDate,
        null,
        StockMoveRepository.TYPE_INTERNAL);
  }

  @Override
  public StockMove _createToConsumeOutgoingOutsourceStockMove(
      ManufOrder manufOrder,
      Company company,
      StockLocation fromStockLocation,
      StockLocation virtualStockLocation)
      throws AxelorException {

    Address fromAddress = fromStockLocation.getAddress();
    Address toAddress =
        partnerService.getDefaultAddress(
            manufOrderOutsourceService.getOutsourcePartner(manufOrder).orElse(null));

    return stockMoveService.createStockMove(
        fromAddress,
        toAddress,
        company,
        fromStockLocation,
        virtualStockLocation,
        null,
        manufOrder.getPlannedStartDateT().toLocalDate(),
        null,
        StockMoveRepository.TYPE_OUTGOING);
  }

  protected StockMove _createToConsumeProductionStockMove(
      ManufOrder manufOrder,
      Company company,
      StockLocation fromStockLocation,
      StockLocation virtualStockLocation)
      throws AxelorException {

    return stockMoveService.createStockMove(
        null,
        null,
        company,
        fromStockLocation,
        virtualStockLocation,
        null,
        manufOrder.getPlannedStartDateT().toLocalDate(),
        null,
        StockMoveRepository.TYPE_INTERNAL);
  }
}
