package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ProdProduct;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import java.math.BigDecimal;
import java.util.List;

public interface ManufOrderCreateStockMoveLineService {

  StockMoveLine _createStockMoveLine(
      ProdProduct prodProduct,
      StockMove stockMove,
      int inOrOutType,
      StockLocation fromStockLocation,
      StockLocation toStockLocation)
      throws AxelorException;

  StockMoveLine _createStockMoveLine(
      ProdProduct prodProduct,
      StockMove stockMove,
      int inOrOutType,
      BigDecimal qty,
      StockLocation fromStockLocation,
      StockLocation toStockLocation)
      throws AxelorException;

  void createNewProducedStockMoveLineList(ManufOrder manufOrder, BigDecimal qtyToUpdate)
      throws AxelorException;

  void createNewStockMoveLines(
      ManufOrder manufOrder,
      StockMove stockMove,
      int inOrOut,
      StockLocation fromStockLocation,
      StockLocation toStockLocation)
      throws AxelorException;

  void createNewStockMoveLines(
      List<ProdProduct> diffProdProductList,
      StockMove stockMove,
      int stockMoveLineType,
      StockLocation fromStockLocation,
      StockLocation toStockLocation)
      throws AxelorException;

  void createNewConsumedStockMoveLineList(ManufOrder manufOrder, BigDecimal qtyToUpdate)
      throws AxelorException;

  void createResidualStockMoveLines(
      ManufOrder manufOrder,
      StockMove stockMove,
      StockLocation virtualStockLocation,
      StockLocation residualProductStockLocation)
      throws AxelorException;

  void createToConsumeStockMoveLines(
      List<ProdProduct> prodProductList,
      StockMove stockMove,
      StockLocation fromStockLocation,
      StockLocation virtualStockLocation)
      throws AxelorException;

  void createToProduceStockMoveLines(
      ManufOrder manufOrder,
      StockMove stockMove,
      StockLocation virtualStockLocation,
      StockLocation producedProductStockLocation)
      throws AxelorException;
}
