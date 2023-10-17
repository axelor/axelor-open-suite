package com.axelor.apps.sale.test;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineTreeComputationService;
import com.axelor.meta.loader.LoaderHelper;
import com.axelor.utils.junit.BaseTest;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestSaleOrderLineTreeComputationService extends BaseTest {

  protected final SaleOrderLineTreeComputationService saleOrderLineTreeComputationService;
  protected final SaleOrderLineRepository saleOrderLineRepository;
  protected final LoaderHelper loaderHelper;

  @Inject
  public TestSaleOrderLineTreeComputationService(
      SaleOrderLineTreeComputationService saleOrderLineTreeComputationService,
      SaleOrderLineRepository saleOrderLineRepository,
      LoaderHelper loaderHelper) {
    this.saleOrderLineTreeComputationService = saleOrderLineTreeComputationService;
    this.saleOrderLineRepository = saleOrderLineRepository;
    this.loaderHelper = loaderHelper;
  }

  @BeforeEach
  void setUp() {
    loaderHelper.importCsv("data/sale-order-lines-input.xml");
    loaderHelper.importCsv("data/sale-order-line-trees-input.xml");
  }

  @Test
  void testComputePrices() throws AxelorException {
    SaleOrderLine saleOrderLine =
        saleOrderLineRepository
            .all()
            .filter("self.importId = :importId")
            .bind("importId", 1L)
            .fetchOne();
    saleOrderLineTreeComputationService.computePrices(saleOrderLine);
    Assertions.assertEquals(
        new BigDecimal(720).setScale(2, RoundingMode.HALF_UP), saleOrderLine.getPrice());
  }

  @Test
  void testComputeSubTotalCostPrice() throws AxelorException {
    SaleOrderLine saleOrderLine =
        saleOrderLineRepository
            .all()
            .filter("self.importId = :importId")
            .bind("importId", 1L)
            .fetchOne();
    saleOrderLineTreeComputationService.computePrices(saleOrderLine);
    saleOrderLineTreeComputationService.computeSubTotalCostPrice(saleOrderLine);
    Assertions.assertEquals(
        new BigDecimal(648).setScale(2, RoundingMode.HALF_UP),
        saleOrderLine.getSubTotalCostPrice());
    Assertions.assertEquals(
        new BigDecimal(864).setScale(2, RoundingMode.HALF_UP), saleOrderLine.getInTaxPrice());
  }
}
