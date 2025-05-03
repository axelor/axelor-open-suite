package com.axelor.apps.production.test.prodprocesslinecomputation;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.ProdProcessLine;
import java.math.BigDecimal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestEntireCycleDuration extends TestProdProcessLineComputationService {

  @Test
  void testComputeEntireCycleDurationSimple1Cycle() throws AxelorException {
    ProdProcessLine ppl = new ProdProcessLine();
    BigDecimal qty = BigDecimal.valueOf(1);
    OperationOrder order = new OperationOrder();
    ppl.setWorkCenter(bothWorkCenter);
    ppl.setMaxCapacityPerCycle(BigDecimal.valueOf(2));
    ppl.setStartingDuration(30L);
    ppl.setEndingDuration(30L);
    ppl.setSetupDuration(60L);
    ppl.setDurationPerCycle(90L);
    ppl.setHumanDuration(180L);

    Assertions.assertEquals(
        180, prodProcessLineComputationService.computeEntireCycleDuration(order, ppl, qty));
  }

  @Test
  void testComputeEntireCycleDuration2CycleWithGreaterHumanDuration() throws AxelorException {
    ProdProcessLine ppl = new ProdProcessLine();
    BigDecimal qty = BigDecimal.valueOf(4);
    OperationOrder order = new OperationOrder();
    ppl.setWorkCenter(bothWorkCenter);
    ppl.setMaxCapacityPerCycle(BigDecimal.valueOf(2));
    ppl.setStartingDuration(30L);
    ppl.setEndingDuration(30L);
    ppl.setSetupDuration(60L);
    ppl.setDurationPerCycle(90L);
    ppl.setHumanDuration(180L);

    Assertions.assertEquals(
        360, prodProcessLineComputationService.computeEntireCycleDuration(order, ppl, qty));
  }

  @Test
  void testComputeEntireCycleDuration2CycleWithGreaterMachineDuration() throws AxelorException {
    ProdProcessLine ppl = new ProdProcessLine();
    BigDecimal qty = BigDecimal.valueOf(4);
    OperationOrder order = new OperationOrder();
    ppl.setWorkCenter(bothWorkCenter);
    ppl.setMaxCapacityPerCycle(BigDecimal.valueOf(2));
    ppl.setStartingDuration(30L);
    ppl.setEndingDuration(30L);
    ppl.setSetupDuration(60L);
    ppl.setDurationPerCycle(90L);
    ppl.setHumanDuration(120L);

    Assertions.assertEquals(
        300, prodProcessLineComputationService.computeEntireCycleDuration(order, ppl, qty));
  }

  @Test
  void testComputeEntireCycleDurationNoQty() throws AxelorException {
    ProdProcessLine ppl = new ProdProcessLine();
    BigDecimal qty = BigDecimal.valueOf(0);
    OperationOrder order = new OperationOrder();
    ppl.setWorkCenter(bothWorkCenter);
    ppl.setMaxCapacityPerCycle(BigDecimal.valueOf(2));
    ppl.setStartingDuration(30L);
    ppl.setEndingDuration(30L);
    ppl.setSetupDuration(60L);
    ppl.setDurationPerCycle(90L);
    ppl.setHumanDuration(120L);

    Assertions.assertEquals(
        0, prodProcessLineComputationService.computeEntireCycleDuration(order, ppl, qty));
  }

  @Test
  void testComputeEntireCycleDurationNegativeQty() throws AxelorException {
    ProdProcessLine ppl = new ProdProcessLine();
    BigDecimal qty = BigDecimal.valueOf(-1);
    OperationOrder order = new OperationOrder();
    ppl.setWorkCenter(bothWorkCenter);
    ppl.setMaxCapacityPerCycle(BigDecimal.valueOf(2));
    ppl.setStartingDuration(30L);
    ppl.setEndingDuration(30L);
    ppl.setSetupDuration(60L);
    ppl.setDurationPerCycle(90L);
    ppl.setHumanDuration(120L);

    Assertions.assertEquals(
        0, prodProcessLineComputationService.computeEntireCycleDuration(order, ppl, qty));
  }

  @Test
  void testComputeEntireCycleDurationNullQty() throws AxelorException {
    ProdProcessLine ppl = new ProdProcessLine();
    BigDecimal qty = null;
    OperationOrder order = new OperationOrder();
    ppl.setWorkCenter(bothWorkCenter);
    ppl.setMaxCapacityPerCycle(BigDecimal.valueOf(2));
    ppl.setStartingDuration(30L);
    ppl.setEndingDuration(30L);
    ppl.setSetupDuration(60L);
    ppl.setDurationPerCycle(90L);
    ppl.setHumanDuration(120L);

    Assertions.assertEquals(
        0, prodProcessLineComputationService.computeEntireCycleDuration(order, ppl, qty));
  }

  @Test
  void testComputeEntireCycleDurationInHumanCenterWithNormalPPL() throws AxelorException {
    ProdProcessLine ppl = new ProdProcessLine();
    BigDecimal qty = BigDecimal.valueOf(4);
    OperationOrder order = new OperationOrder();
    ppl.setWorkCenter(humanWorkCenter);
    ppl.setMaxCapacityPerCycle(BigDecimal.valueOf(2));
    ;
    ppl.setHumanDuration(120L);

    Assertions.assertEquals(
        240, prodProcessLineComputationService.computeEntireCycleDuration(order, ppl, qty));
  }

  @Test
  void testComputeEntireCycleDurationInHumanCenterWithInconsistentPPL() throws AxelorException {
    ProdProcessLine ppl = new ProdProcessLine();
    BigDecimal qty = BigDecimal.valueOf(4);
    OperationOrder order = new OperationOrder();
    ppl.setWorkCenter(humanWorkCenter);
    ppl.setMaxCapacityPerCycle(BigDecimal.valueOf(2));
    ppl.setStartingDuration(30L);
    ppl.setEndingDuration(30L);
    ppl.setSetupDuration(60L);
    ppl.setDurationPerCycle(90L);
    ppl.setHumanDuration(120L);

    Assertions.assertEquals(
        240, prodProcessLineComputationService.computeEntireCycleDuration(order, ppl, qty));
  }

  @Test
  void testComputeEntireCycleDurationInHumanCenterWithInconsistentPPL2() throws AxelorException {
    ProdProcessLine ppl = new ProdProcessLine();
    BigDecimal qty = BigDecimal.valueOf(4);
    OperationOrder order = new OperationOrder();
    ppl.setWorkCenter(humanWorkCenter);
    ppl.setMaxCapacityPerCycle(BigDecimal.valueOf(2));
    ppl.setStartingDuration(30L);
    ppl.setEndingDuration(30L);
    ppl.setSetupDuration(60L);
    ppl.setDurationPerCycle(90L);
    ppl.setHumanDuration(null);

    Assertions.assertEquals(
        0, prodProcessLineComputationService.computeEntireCycleDuration(order, ppl, qty));
  }

  @Test
  void testComputeEntireCycleDurationInMachineCenterWithNormalPPL() throws AxelorException {
    ProdProcessLine ppl = new ProdProcessLine();
    BigDecimal qty = BigDecimal.valueOf(4);
    OperationOrder order = new OperationOrder();
    ppl.setWorkCenter(machineWorkCenter);
    ppl.setMaxCapacityPerCycle(BigDecimal.valueOf(2));
    ;
    ppl.setStartingDuration(30L);
    ppl.setEndingDuration(30L);
    ppl.setSetupDuration(60L);
    ppl.setDurationPerCycle(90L);
    ppl.setHumanDuration(null);

    Assertions.assertEquals(
        300, prodProcessLineComputationService.computeEntireCycleDuration(order, ppl, qty));
  }

  @Test
  void testComputeEntireCycleDurationInMachineCenterWithInconsistentPPL() throws AxelorException {
    ProdProcessLine ppl = new ProdProcessLine();
    BigDecimal qty = BigDecimal.valueOf(4);
    OperationOrder order = new OperationOrder();
    ppl.setWorkCenter(machineWorkCenter);
    ppl.setMaxCapacityPerCycle(BigDecimal.valueOf(2));
    ppl.setStartingDuration(30L);
    ppl.setEndingDuration(30L);
    ppl.setSetupDuration(60L);
    ppl.setDurationPerCycle(90L);
    ppl.setHumanDuration(120L);

    Assertions.assertEquals(
        300, prodProcessLineComputationService.computeEntireCycleDuration(order, ppl, qty));
  }

  @Test
  void testComputeEntireCycleDurationInMachineCenterWithInconsistentPPL2() throws AxelorException {
    ProdProcessLine ppl = new ProdProcessLine();
    BigDecimal qty = BigDecimal.valueOf(4);
    OperationOrder order = new OperationOrder();
    ppl.setWorkCenter(machineWorkCenter);
    ppl.setMaxCapacityPerCycle(BigDecimal.valueOf(2));
    ppl.setStartingDuration(null);
    ppl.setEndingDuration(null);
    ppl.setSetupDuration(null);
    ppl.setDurationPerCycle(null);
    ppl.setHumanDuration(180L);

    Assertions.assertEquals(
        0, prodProcessLineComputationService.computeEntireCycleDuration(order, ppl, qty));
  }

  @Test
  void testComputeEntireCycleDurationNullCapacity() throws AxelorException {
    ProdProcessLine ppl = new ProdProcessLine();
    BigDecimal qty = BigDecimal.valueOf(4);
    OperationOrder order = new OperationOrder();
    ppl.setWorkCenter(bothWorkCenter);
    ppl.setMaxCapacityPerCycle(null);
    ppl.setStartingDuration(30L);
    ppl.setEndingDuration(30L);
    ppl.setSetupDuration(60L);
    ppl.setDurationPerCycle(90L);
    ppl.setHumanDuration(120L);

    Assertions.assertEquals(
        600, prodProcessLineComputationService.computeEntireCycleDuration(order, ppl, qty));
  }
}
