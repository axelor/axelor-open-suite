package com.axelor.apps.production.test.prodprocesslinecomputation;

import java.math.BigDecimal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestNbCycleComputation extends TestProdProcessLineComputationService {

  @Test
  void testComputeNbCycleSimple() {
    BigDecimal qty = BigDecimal.valueOf(1);
    BigDecimal maxCapacity = BigDecimal.valueOf(1);

    Assertions.assertEquals(
        BigDecimal.valueOf(1), prodProcessLineComputationService.computeNbCycle(qty, maxCapacity));
  }

  @Test
  void testComputeNbCycleSimple2() {
    BigDecimal qty = BigDecimal.valueOf(2);
    BigDecimal maxCapacity = BigDecimal.valueOf(1);

    Assertions.assertEquals(
        BigDecimal.valueOf(2), prodProcessLineComputationService.computeNbCycle(qty, maxCapacity));
  }

  @Test
  void testComputeNbCycleDecimal() {
    BigDecimal qty = BigDecimal.valueOf(3.8);
    BigDecimal maxCapacity = BigDecimal.valueOf(2);

    Assertions.assertEquals(
        BigDecimal.valueOf(2), prodProcessLineComputationService.computeNbCycle(qty, maxCapacity));
  }

  @Test
  void testComputeNbCycleNoQty() {
    BigDecimal qty = BigDecimal.valueOf(0);
    BigDecimal maxCapacity = BigDecimal.valueOf(1);

    Assertions.assertEquals(
        BigDecimal.valueOf(0), prodProcessLineComputationService.computeNbCycle(qty, maxCapacity));
  }

  @Test
  void testComputeNbCycleNoCapacity1() {
    BigDecimal qty = BigDecimal.valueOf(1);
    BigDecimal maxCapacity = BigDecimal.valueOf(0);

    Assertions.assertEquals(
        BigDecimal.valueOf(1), prodProcessLineComputationService.computeNbCycle(qty, maxCapacity));
  }

  @Test
  void testComputeNbCycleNoCapacity2() {
    BigDecimal qty = BigDecimal.valueOf(2);
    BigDecimal maxCapacity = BigDecimal.valueOf(0);

    Assertions.assertEquals(
        BigDecimal.valueOf(2), prodProcessLineComputationService.computeNbCycle(qty, maxCapacity));
  }

  @Test
  void testComputeNbCycleNoQtyAndCapacity() {
    BigDecimal qty = BigDecimal.valueOf(0);
    BigDecimal maxCapacity = BigDecimal.valueOf(0);

    Assertions.assertEquals(
        BigDecimal.valueOf(0), prodProcessLineComputationService.computeNbCycle(qty, maxCapacity));
  }

  @Test
  void testComputeNbCycleNegativeQty() {
    BigDecimal qty = BigDecimal.valueOf(-1);
    BigDecimal maxCapacity = BigDecimal.valueOf(1);

    Assertions.assertEquals(
        BigDecimal.valueOf(0), prodProcessLineComputationService.computeNbCycle(qty, maxCapacity));
  }

  @Test
  void testComputeNbCycleNegativeCapacity() {
    BigDecimal qty = BigDecimal.valueOf(1);
    BigDecimal maxCapacity = BigDecimal.valueOf(-1);

    Assertions.assertEquals(
        BigDecimal.valueOf(1), prodProcessLineComputationService.computeNbCycle(qty, maxCapacity));
  }

  @Test
  void testComputeNbCycleNegativeCapacityAndQty() {
    BigDecimal qty = BigDecimal.valueOf(-1);
    BigDecimal maxCapacity = BigDecimal.valueOf(-1);

    Assertions.assertEquals(
        BigDecimal.valueOf(0), prodProcessLineComputationService.computeNbCycle(qty, maxCapacity));
  }

  @Test
  void testComputeNbCycleNullQty() {
    BigDecimal qty = null;
    BigDecimal maxCapacity = BigDecimal.valueOf(1);

    Assertions.assertEquals(
        BigDecimal.valueOf(0), prodProcessLineComputationService.computeNbCycle(qty, maxCapacity));
  }

  @Test
  void testComputeNbCycleNullCapcacity() {
    BigDecimal qty = BigDecimal.valueOf(1);
    BigDecimal maxCapacity = null;

    Assertions.assertEquals(
        BigDecimal.valueOf(1), prodProcessLineComputationService.computeNbCycle(qty, maxCapacity));
  }

  @Test
  void testComputeNbCycleNullCapcacityAndQty() {
    BigDecimal qty = null;
    BigDecimal maxCapacity = null;

    Assertions.assertEquals(
        BigDecimal.valueOf(0), prodProcessLineComputationService.computeNbCycle(qty, maxCapacity));
  }
}
