package com.axelor.apps.production.test.prodprocessComputation;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.ProdProcess;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.ProductionConfig;
import com.axelor.apps.production.db.WorkCenter;
import com.axelor.apps.production.db.repo.ProductionConfigRepository;
import com.axelor.apps.production.service.ProdProcessComputationService;
import com.axelor.apps.production.service.ProdProcessComputationServiceImpl;
import com.axelor.apps.production.service.ProdProcessLineComputationService;
import com.axelor.apps.production.service.config.ProductionConfigService;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class TestProdProcessComputationService {

  protected ProdProcessComputationService prodProcessComputationService;
  protected ProdProcessLineComputationService prodProcessLineComputationService;
  protected ProductionConfigService prodConfigService;
  protected ProductionConfig productionConfig;

  protected static List<WorkCenter> workCenters;

  @BeforeAll
  static void prepareWorkCenters() {
    workCenters =
        IntStream.range(0, 10)
            .mapToObj(
                i -> {
                  WorkCenter wc = new WorkCenter();
                  wc.setId((long) i);
                  return wc;
                })
            .collect(Collectors.toList());
  }

  @BeforeEach
  void prepare() throws AxelorException {

    prodConfigService = Mockito.mock(ProductionConfigService.class);
    productionConfig = Mockito.mock(ProductionConfig.class);
    Mockito.when(prodConfigService.getProductionConfig(Mockito.any())).thenReturn(productionConfig);
    prodProcessLineComputationService = Mockito.mock(ProdProcessLineComputationService.class);

    prodProcessComputationService =
        new ProdProcessComputationServiceImpl(prodProcessLineComputationService, prodConfigService);
  }

  ProdProcessLine generatePPL(
      long id,
      int priority,
      WorkCenter workCenter,
      long totalDuration,
      long timeBeforeNextOperation,
      BigDecimal nbCycle)
      throws AxelorException {
    ProdProcessLine prodProcessLine = new ProdProcessLine();
    prodProcessLine.setPriority(priority);
    prodProcessLine.setWorkCenter(workCenter);
    prodProcessLine.setTimeBeforeNextOperation(timeBeforeNextOperation);
    prodProcessLine.setId(id);

    Mockito.when(
            prodProcessLineComputationService.getTotalDuration(
                Mockito.eq(prodProcessLine), Mockito.any()))
        .thenReturn(BigDecimal.valueOf(totalDuration));
    Mockito.when(
            prodProcessLineComputationService.computeEntireCycleDuration(
                Mockito.any(), Mockito.eq(prodProcessLine), Mockito.any()))
        .thenReturn(totalDuration);
    Mockito.when(
            prodProcessLineComputationService.getNbCycle(
                Mockito.eq(prodProcessLine), Mockito.any()))
        .thenReturn(nbCycle);

    return prodProcessLine;
  }

  @Test
  void testComputeLeadTimeSimpleSumInfiniteCapacity() throws AxelorException {
    Mockito.when(productionConfig.getCapacity())
        .thenReturn(ProductionConfigRepository.INFINITE_CAPACITY_SCHEDULING);
    ProdProcessLine ppl1 = generatePPL(1, 10, workCenters.get(0), 120L, 31, BigDecimal.ONE);
    ProdProcessLine ppl2 = generatePPL(2, 20, workCenters.get(1), 80L, 32, BigDecimal.ONE);
    ProdProcessLine ppl3 = generatePPL(3, 30, workCenters.get(2), 60L, 30L, BigDecimal.ONE);
    ProdProcess prodProcess = new ProdProcess();
    prodProcess.setLaunchQty(BigDecimal.ONE);

    prodProcess.addProdProcessLineListItem(ppl1);
    prodProcess.addProdProcessLineListItem(ppl2);
    prodProcess.addProdProcessLineListItem(ppl3);

    Assert.assertEquals(
        323, prodProcessComputationService.getLeadTime(prodProcess, BigDecimal.ONE));
  }

  @Test
  void testComputeLeadTimeSimpleSumFiniteCapacity() throws AxelorException {
    Mockito.when(productionConfig.getCapacity())
        .thenReturn(ProductionConfigRepository.FINITE_CAPACITY_SCHEDULING);
    ProdProcessLine ppl1 = generatePPL(1, 10, workCenters.get(0), 120L, 31, BigDecimal.ONE);
    ProdProcessLine ppl2 = generatePPL(2, 20, workCenters.get(1), 80L, 32, BigDecimal.ONE);
    ProdProcessLine ppl3 = generatePPL(3, 30, workCenters.get(2), 60L, 30L, BigDecimal.ONE);
    ProdProcess prodProcess = new ProdProcess();
    prodProcess.setLaunchQty(BigDecimal.ONE);

    prodProcess.addProdProcessLineListItem(ppl1);
    prodProcess.addProdProcessLineListItem(ppl2);
    prodProcess.addProdProcessLineListItem(ppl3);

    Assert.assertEquals(
        323, prodProcessComputationService.getLeadTime(prodProcess, BigDecimal.ONE));
  }

  @Test
  void testComputeLeadTimeSubLastTimeBeforeNextOperationInfiniteCapacity() throws AxelorException {
    Mockito.when(productionConfig.getCapacity())
        .thenReturn(ProductionConfigRepository.INFINITE_CAPACITY_SCHEDULING);
    ProdProcessLine ppl1 = generatePPL(1, 10, workCenters.get(0), 0, 10, BigDecimal.ONE);
    ProdProcessLine ppl2 = generatePPL(2, 20, workCenters.get(1), 0, 10, BigDecimal.ONE);
    ProdProcessLine ppl3 = generatePPL(3, 30, workCenters.get(2), 0, 2000, BigDecimal.ONE);
    ProdProcess prodProcess = new ProdProcess();
    prodProcess.setLaunchQty(BigDecimal.ONE);

    prodProcess.addProdProcessLineListItem(ppl1);
    prodProcess.addProdProcessLineListItem(ppl2);
    prodProcess.addProdProcessLineListItem(ppl3);

    Assert.assertEquals(20, prodProcessComputationService.getLeadTime(prodProcess, BigDecimal.ONE));
  }

  @Test
  void testComputeLeadTimeSubLastTimeBeforeNextOperationFiniteCapacity() throws AxelorException {
    Mockito.when(productionConfig.getCapacity())
        .thenReturn(ProductionConfigRepository.FINITE_CAPACITY_SCHEDULING);
    ProdProcessLine ppl1 = generatePPL(1, 10, workCenters.get(0), 0, 10, BigDecimal.ONE);
    ProdProcessLine ppl2 = generatePPL(2, 20, workCenters.get(1), 0, 10, BigDecimal.ONE);
    ProdProcessLine ppl3 = generatePPL(3, 30, workCenters.get(2), 0, 2000, BigDecimal.ONE);
    ProdProcess prodProcess = new ProdProcess();
    prodProcess.setLaunchQty(BigDecimal.ONE);

    prodProcess.addProdProcessLineListItem(ppl1);
    prodProcess.addProdProcessLineListItem(ppl2);
    prodProcess.addProdProcessLineListItem(ppl3);

    Assert.assertEquals(20, prodProcessComputationService.getLeadTime(prodProcess, BigDecimal.ONE));
  }

  @Test
  void testComputeLeadTimeSubLastTimeBeforeNextOperationWithSamePriorityInfiniteCapacity()
      throws AxelorException {
    Mockito.when(productionConfig.getCapacity())
        .thenReturn(ProductionConfigRepository.INFINITE_CAPACITY_SCHEDULING);
    ProdProcessLine ppl1 = generatePPL(1, 10, workCenters.get(0), 100, 100, BigDecimal.ONE);
    ProdProcessLine ppl2 = generatePPL(2, 20, workCenters.get(1), 100, 50, BigDecimal.ONE);
    ProdProcessLine ppl3 = generatePPL(3, 20, workCenters.get(2), 100, 70, BigDecimal.ONE);
    ProdProcess prodProcess = new ProdProcess();
    prodProcess.setLaunchQty(BigDecimal.ONE);

    prodProcess.addProdProcessLineListItem(ppl1);
    prodProcess.addProdProcessLineListItem(ppl2);
    prodProcess.addProdProcessLineListItem(ppl3);

    Assert.assertEquals(
        300, prodProcessComputationService.getLeadTime(prodProcess, BigDecimal.ONE));
  }

  @Test
  void testComputeLeadTimeSubLastTimeBeforeNextOperationWithSamePriorityFiniteCapacity()
      throws AxelorException {
    Mockito.when(productionConfig.getCapacity())
        .thenReturn(ProductionConfigRepository.FINITE_CAPACITY_SCHEDULING);
    ProdProcessLine ppl1 = generatePPL(1, 10, workCenters.get(0), 100, 100, BigDecimal.ONE);
    ProdProcessLine ppl2 = generatePPL(2, 20, workCenters.get(1), 100, 50, BigDecimal.ONE);
    ProdProcessLine ppl3 = generatePPL(3, 20, workCenters.get(2), 100, 70, BigDecimal.ONE);
    ProdProcess prodProcess = new ProdProcess();
    prodProcess.setLaunchQty(BigDecimal.ONE);

    prodProcess.addProdProcessLineListItem(ppl1);
    prodProcess.addProdProcessLineListItem(ppl2);
    prodProcess.addProdProcessLineListItem(ppl3);

    Assert.assertEquals(
        300, prodProcessComputationService.getLeadTime(prodProcess, BigDecimal.ONE));
  }

  @Test
  void testComputeLeadTimeSubLastTimeBeforeNextOperationNegativeResultExpect0InfiniteCapacity()
      throws AxelorException {
    Mockito.when(productionConfig.getCapacity())
        .thenReturn(ProductionConfigRepository.INFINITE_CAPACITY_SCHEDULING);
    ProdProcessLine ppl1 = generatePPL(1, 10, workCenters.get(0), 0, 0, BigDecimal.ONE);
    ProdProcessLine ppl2 = generatePPL(2, 20, workCenters.get(1), 0, 0, BigDecimal.ONE);
    ProdProcessLine ppl3 = generatePPL(3, 30, workCenters.get(2), 0, 2000, BigDecimal.ONE);
    ProdProcess prodProcess = new ProdProcess();
    prodProcess.setLaunchQty(BigDecimal.ONE);

    prodProcess.addProdProcessLineListItem(ppl1);
    prodProcess.addProdProcessLineListItem(ppl2);
    prodProcess.addProdProcessLineListItem(ppl3);

    Assert.assertEquals(0, prodProcessComputationService.getLeadTime(prodProcess, BigDecimal.ONE));
  }

  @Test
  void testComputeLeadTimeSubLastTimeBeforeNextOperationNegativeResultExpect0FiniteCapacity()
      throws AxelorException {
    Mockito.when(productionConfig.getCapacity())
        .thenReturn(ProductionConfigRepository.FINITE_CAPACITY_SCHEDULING);
    ProdProcessLine ppl1 = generatePPL(1, 10, workCenters.get(0), 0, 0, BigDecimal.ONE);
    ProdProcessLine ppl2 = generatePPL(2, 20, workCenters.get(1), 0, 0, BigDecimal.ONE);
    ProdProcessLine ppl3 = generatePPL(3, 30, workCenters.get(2), 0, 2000, BigDecimal.ONE);
    ProdProcess prodProcess = new ProdProcess();
    prodProcess.setLaunchQty(BigDecimal.ONE);

    prodProcess.addProdProcessLineListItem(ppl1);
    prodProcess.addProdProcessLineListItem(ppl2);
    prodProcess.addProdProcessLineListItem(ppl3);

    Assert.assertEquals(0, prodProcessComputationService.getLeadTime(prodProcess, BigDecimal.ONE));
  }

  @Test
  void testComputeLeadTimeTakeIntoAccountMaxDurationPerPriorityInfiniteCapacity()
      throws AxelorException {
    Mockito.when(productionConfig.getCapacity())
        .thenReturn(ProductionConfigRepository.INFINITE_CAPACITY_SCHEDULING);
    ProdProcessLine ppl1 = generatePPL(1, 10, workCenters.get(0), 1600, 31, BigDecimal.ONE);
    ProdProcessLine ppl2 = generatePPL(2, 10, workCenters.get(0), 1600, 32, BigDecimal.ONE);
    ProdProcessLine ppl3 = generatePPL(3, 10, workCenters.get(1), 3000, 34, BigDecimal.ONE);
    ProdProcessLine ppl4 = generatePPL(4, 50, workCenters.get(3), 1600, 0, BigDecimal.ONE);
    ProdProcess prodProcess = new ProdProcess();
    prodProcess.setLaunchQty(BigDecimal.ONE);

    prodProcess.addProdProcessLineListItem(ppl1);
    prodProcess.addProdProcessLineListItem(ppl2);
    prodProcess.addProdProcessLineListItem(ppl3);
    prodProcess.addProdProcessLineListItem(ppl4);

    Assert.assertEquals(
        4634, prodProcessComputationService.getLeadTime(prodProcess, BigDecimal.ONE));
  }

  @Test
  void testComputeLeadTimeTakeIntoAccountMaxDurationPerPriorityFiniteCapacity()
      throws AxelorException {
    Mockito.when(productionConfig.getCapacity())
        .thenReturn(ProductionConfigRepository.FINITE_CAPACITY_SCHEDULING);
    ProdProcessLine ppl1 = generatePPL(1, 10, workCenters.get(0), 1600, 31, BigDecimal.ONE);
    ProdProcessLine ppl2 = generatePPL(2, 10, workCenters.get(0), 1600, 32, BigDecimal.ONE);
    ProdProcessLine ppl3 = generatePPL(3, 10, workCenters.get(1), 3000, 34, BigDecimal.ONE);
    ProdProcessLine ppl4 = generatePPL(4, 50, workCenters.get(3), 1600, 0, BigDecimal.ONE);
    ProdProcess prodProcess = new ProdProcess();
    prodProcess.setLaunchQty(BigDecimal.ONE);

    prodProcess.addProdProcessLineListItem(ppl1);
    prodProcess.addProdProcessLineListItem(ppl2);
    prodProcess.addProdProcessLineListItem(ppl3);
    prodProcess.addProdProcessLineListItem(ppl4);

    Assert.assertEquals(
        4863, prodProcessComputationService.getLeadTime(prodProcess, BigDecimal.ONE));
  }

  @Test
  void testComputeLeadGlobalTestInfiniteCapacity() throws AxelorException {
    Mockito.when(productionConfig.getCapacity())
        .thenReturn(ProductionConfigRepository.INFINITE_CAPACITY_SCHEDULING);
    ProdProcessLine ppl1 = generatePPL(1, 10, workCenters.get(0), 120, 31, BigDecimal.ONE);
    ProdProcessLine ppl2 = generatePPL(2, 10, workCenters.get(0), 80, 32, BigDecimal.ONE);
    ProdProcessLine ppl3 = generatePPL(3, 10, workCenters.get(1), 60, 34, BigDecimal.ONE);
    ProdProcessLine ppl4 = generatePPL(4, 20, workCenters.get(3), 40, 10, BigDecimal.ONE);
    ProdProcessLine ppl5 = generatePPL(5, 30, workCenters.get(4), 200, 2000, BigDecimal.ONE);
    ProdProcessLine ppl6 = generatePPL(6, 30, workCenters.get(5), 50, 50, BigDecimal.ONE);

    ProdProcess prodProcess = new ProdProcess();
    prodProcess.setLaunchQty(BigDecimal.ONE);

    prodProcess.addProdProcessLineListItem(ppl1);
    prodProcess.addProdProcessLineListItem(ppl2);
    prodProcess.addProdProcessLineListItem(ppl3);
    prodProcess.addProdProcessLineListItem(ppl4);
    prodProcess.addProdProcessLineListItem(ppl5);
    prodProcess.addProdProcessLineListItem(ppl6);

    Assert.assertEquals(
        401, prodProcessComputationService.getLeadTime(prodProcess, BigDecimal.ONE));
  }

  @Test
  void testComputeLeadGlobalTestFiniteCapacity() throws AxelorException {
    Mockito.when(productionConfig.getCapacity())
        .thenReturn(ProductionConfigRepository.FINITE_CAPACITY_SCHEDULING);
    ProdProcessLine ppl1 = generatePPL(1, 10, workCenters.get(0), 120, 31, BigDecimal.ONE);
    ProdProcessLine ppl2 = generatePPL(2, 10, workCenters.get(0), 80, 32, BigDecimal.ONE);
    ProdProcessLine ppl3 = generatePPL(3, 10, workCenters.get(1), 60, 34, BigDecimal.ONE);
    ProdProcessLine ppl4 = generatePPL(4, 20, workCenters.get(3), 40, 10, BigDecimal.ONE);
    ProdProcessLine ppl5 = generatePPL(5, 30, workCenters.get(4), 200, 2000, BigDecimal.ONE);
    ProdProcessLine ppl6 = generatePPL(6, 30, workCenters.get(5), 50, 50, BigDecimal.ONE);

    ProdProcess prodProcess = new ProdProcess();
    prodProcess.setLaunchQty(BigDecimal.ONE);

    prodProcess.addProdProcessLineListItem(ppl1);
    prodProcess.addProdProcessLineListItem(ppl2);
    prodProcess.addProdProcessLineListItem(ppl3);
    prodProcess.addProdProcessLineListItem(ppl4);
    prodProcess.addProdProcessLineListItem(ppl5);
    prodProcess.addProdProcessLineListItem(ppl6);

    Assert.assertEquals(
        513, prodProcessComputationService.getLeadTime(prodProcess, BigDecimal.ONE));
  }

  @Test
  void testComputeLeadEmptyListInfiniteCapacity() throws AxelorException {
    Mockito.when(productionConfig.getCapacity())
        .thenReturn(ProductionConfigRepository.INFINITE_CAPACITY_SCHEDULING);

    ProdProcess prodProcess = new ProdProcess();
    prodProcess.setLaunchQty(BigDecimal.ONE);

    Assert.assertEquals(0, prodProcessComputationService.getLeadTime(prodProcess, BigDecimal.ONE));
  }

  @Test
  void testComputeLeadEmptyListFiniteCapacity() throws AxelorException {
    Mockito.when(productionConfig.getCapacity())
        .thenReturn(ProductionConfigRepository.FINITE_CAPACITY_SCHEDULING);

    ProdProcess prodProcess = new ProdProcess();
    prodProcess.setLaunchQty(BigDecimal.ONE);

    Assert.assertEquals(0, prodProcessComputationService.getLeadTime(prodProcess, BigDecimal.ONE));
  }
}
