package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.google.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class ManufOrderGetStockMoveServiceImpl implements ManufOrderGetStockMoveService {

  protected ManufOrderPlanStockMoveService manufOrderPlanStockMoveService;
  protected ManufOrderStockMoveService manufOrderStockMoveService;

  @Inject
  public ManufOrderGetStockMoveServiceImpl(
      ManufOrderPlanStockMoveService manufOrderPlanStockMoveService,
      ManufOrderStockMoveService manufOrderStockMoveService) {
    this.manufOrderPlanStockMoveService = manufOrderPlanStockMoveService;
    this.manufOrderStockMoveService = manufOrderStockMoveService;
  }

  @Override
  public StockMove getProducedStockMoveFromManufOrder(ManufOrder manufOrder)
      throws AxelorException {

    Optional<StockMove> stockMoveOpt =
        getPlannedStockMove(getNonResidualOutStockMoveLineList(manufOrder));

    Company company = manufOrder.getCompany();

    StockMove stockMove;
    if (stockMoveOpt.isPresent()) {
      return stockMoveOpt.get();
    } else {
      return manufOrderPlanStockMoveService
          .createAndPlanToProduceStockMoveWithLines(manufOrder)
          .map(
              sm -> {
                manufOrder.addOutStockMoveListItem(sm);
                return sm;
              })
          .orElse(null);
    }
  }

  @Override
  public StockMove getResidualStockMoveFromManufOrder(ManufOrder manufOrder)
      throws AxelorException {

    Optional<StockMove> stockMoveOpt =
        getPlannedStockMove(getResidualOutStockMoveLineList(manufOrder));

    Company company = manufOrder.getCompany();

    StockMove stockMove;
    if (stockMoveOpt.isPresent()) {
      return stockMoveOpt.get();
    } else {
      return manufOrderPlanStockMoveService
          .createAndPlanResidualStockMoveWithLines(manufOrder)
          .map(
              sm -> {
                manufOrder.addOutStockMoveListItem(sm);
                return sm;
              })
          .orElse(null);
    }
  }

  @Override
  public StockMove getConsumedStockMoveFromManufOrder(ManufOrder manufOrder)
      throws AxelorException {
    Optional<StockMove> stockMoveOpt = getPlannedStockMove(manufOrder.getInStockMoveList());

    if (stockMoveOpt.isPresent()) {
      return stockMoveOpt.get();
    } else {
      return manufOrderPlanStockMoveService
          .createAndPlanToConsumeStockMove(manufOrder)
          .map(
              sm -> {
                manufOrder.addInStockMoveListItem(sm);
                return sm;
              })
          .orElse(null);
    }
  }

  @Override
  public List<StockMove> getResidualOutStockMoveLineList(ManufOrder manufOrder)
      throws AxelorException {
    Objects.requireNonNull(manufOrder);
    StockLocation residualProductStockLocation =
        manufOrderStockMoveService.getResidualProductStockLocation(manufOrder);

    if (manufOrder.getOutStockMoveList() == null || residualProductStockLocation == null) {
      return manufOrder.getOutStockMoveList();
    }

    return manufOrder.getOutStockMoveList().stream()
        .filter(sm -> residualProductStockLocation.equals(sm.getToStockLocation()))
        .collect(Collectors.toList());
  }

  @Override
  public List<StockMove> getNonResidualOutStockMoveLineList(ManufOrder manufOrder)
      throws AxelorException {
    Objects.requireNonNull(manufOrder);
    StockLocation residualProductStockLocation =
        manufOrderStockMoveService.getResidualProductStockLocation(manufOrder);

    if (manufOrder.getOutStockMoveList() == null || residualProductStockLocation == null) {
      return manufOrder.getOutStockMoveList();
    }

    return manufOrder.getOutStockMoveList().stream()
        .filter(sm -> !residualProductStockLocation.equals(sm.getToStockLocation()))
        .collect(Collectors.toList());
  }

  /**
   * Get the planned stock move in a stock move list
   *
   * @param stockMoveList
   * @return an optional stock move
   */
  @Override
  public Optional<StockMove> getPlannedStockMove(List<StockMove> stockMoveList) {
    return stockMoveList.stream()
        .filter(stockMove -> stockMove.getStatusSelect() == StockMoveRepository.STATUS_PLANNED)
        .findFirst();
  }
}
