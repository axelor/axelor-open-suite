package com.axelor.apps.supplychain.service.packaging;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.stock.db.LogisticalForm;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.supplychain.db.Packaging;
import com.axelor.apps.supplychain.db.PackagingLine;
import com.axelor.apps.supplychain.db.repo.PackagingLineRepository;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.service.LogisticalFormComputeService;
import com.axelor.i18n.I18n;
import com.axelor.utils.helpers.StringHelper;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class PackagingLineCreationServiceImpl implements PackagingLineCreationService {

  protected final PackagingLineRepository packagingLineRepository;
  protected final StockMoveLineRepository stockMoveLineRepository;
  protected final LogisticalFormComputeService logisticalFormComputeService;

  @Inject
  public PackagingLineCreationServiceImpl(
      PackagingLineRepository packagingLineRepository,
      StockMoveLineRepository stockMoveLineRepository,
      LogisticalFormComputeService logisticalFormComputeService) {
    this.packagingLineRepository = packagingLineRepository;
    this.stockMoveLineRepository = stockMoveLineRepository;
    this.logisticalFormComputeService = logisticalFormComputeService;
  }

  @Override
  public void addPackagingLines(Packaging packaging, List<StockMoveLine> stockMoveLineList)
      throws AxelorException {
    if (packaging == null || CollectionUtils.isEmpty(stockMoveLineList)) {
      return;
    }
    for (StockMoveLine stockMoveLine : stockMoveLineList) {
      createPackagingLine(packaging, stockMoveLine, stockMoveLine.getQtyRemainingToPackage());
    }
  }

  @Override
  public String getStockMoveLineDomain(LogisticalForm logisticalForm) {
    if (logisticalForm == null) {
      return "self.stockMove.typeSelect = 2 AND self.qtyRemainingToPackage > 0";
    }
    String stockMoveIds = StringHelper.getIdListString(logisticalForm.getStockMoveList());
    if (stockMoveIds.isEmpty()) {
      return "self.id = 0";
    }
    return String.format(
        "self.stockMove.typeSelect = 2 AND self.qtyRemainingToPackage > 0 AND self.stockMove.id IN (%s)",
        stockMoveIds);
  }

  @Transactional(rollbackOn = Exception.class)
  @Override
  public PackagingLine createPackagingLine(
      Packaging packaging, StockMoveLine stockMoveLine, BigDecimal quantity)
      throws AxelorException {
    PackagingLine packagingLine = new PackagingLine();
    packagingLine.setPackaging(packaging);
    if (stockMoveLine != null) {
      checkStockMoveLine(stockMoveLine, packagingLine);
      packagingLine.setStockMoveLine(stockMoveLine);
    }

    setQty(stockMoveLine, quantity, packagingLine);
    return packagingLineRepository.save(packagingLine);
  }

  protected void checkStockMoveLine(StockMoveLine stockMoveLine, PackagingLine packagingLine)
      throws AxelorException {
    LogisticalForm logisticalForm = getParentLogisticalForm(packagingLine);
    if (!stockMoveLineRepository
        .all()
        .filter(getStockMoveLineDomain(logisticalForm))
        .fetch()
        .contains(stockMoveLine)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SupplychainExceptionMessage.PACKAGING_LINE_STOCK_MOVE_LINE_NOT_VALID));
    }
  }

  protected void setQty(
      StockMoveLine stockMoveLine, BigDecimal quantity, PackagingLine packagingLine) {
    if (quantity != null) {
      packagingLine.setQty(quantity);
    } else {
      if (stockMoveLine != null) {
        packagingLine.setQty(stockMoveLine.getQtyRemainingToPackage());
      } else {
        packagingLine.setQty(BigDecimal.ZERO);
      }
    }
  }

  @Transactional(rollbackOn = Exception.class)
  @Override
  public void updateQuantity(PackagingLine packagingLine, BigDecimal quantity)
      throws AxelorException {
    LogisticalForm logisticalForm = getParentLogisticalForm(packagingLine);
    packagingLine.setQty(quantity);
    packagingLineRepository.save(packagingLine);
    logisticalFormComputeService.computeLogisticalForm(logisticalForm);
  }

  @Transactional(rollbackOn = Exception.class)
  @Override
  public void deletePackagingLine(PackagingLine packagingLine) throws AxelorException {
    LogisticalForm logisticalForm = getParentLogisticalForm(packagingLine);
    Packaging packaging = packagingLine.getPackaging();
    packaging.removePackagingLineListItem(packagingLine);
    logisticalFormComputeService.computeLogisticalForm(logisticalForm);
  }

  @Override
  public LogisticalForm getParentLogisticalForm(PackagingLine packagingLine) {
    Packaging packaging = packagingLine.getPackaging();
    Packaging parentPackaging = getParentPackaging(packaging);
    return parentPackaging.getLogisticalForm();
  }

  protected Packaging getParentPackaging(Packaging packaging) {
    Packaging parentPackaging = packaging.getParentPackaging();
    if (parentPackaging != null) {
      return getParentPackaging(parentPackaging);
    }
    return packaging;
  }
}
