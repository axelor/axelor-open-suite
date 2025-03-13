package com.axelor.apps.production.test.prodprocesslinecomputation;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.service.ProdProcessLineComputationServiceImpl;
import java.math.BigDecimal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class TestMachineDurationComputation extends TestProdProcessLineComputationService {

  @BeforeEach
  void prepareMock() throws AxelorException {
    prodProcessLineComputationService = Mockito.mock(ProdProcessLineComputationServiceImpl.class);
    Mockito.when(
            prodProcessLineComputationService.getMachineInstallingDuration(
                Mockito.any(ProdProcessLine.class), Mockito.eq(BigDecimal.ONE)))
        .thenReturn(BigDecimal.valueOf(150L));
    Mockito.when(
            prodProcessLineComputationService.getMachineInstallingDuration(
                Mockito.any(ProdProcessLine.class), Mockito.eq(BigDecimal.valueOf(-1))))
        .thenReturn(BigDecimal.valueOf(0));
    Mockito.when(
            prodProcessLineComputationService.getMachineInstallingDuration(
                Mockito.any(ProdProcessLine.class), Mockito.eq(BigDecimal.valueOf(2))))
        .thenReturn(BigDecimal.valueOf(150L));
    Mockito.when(
            prodProcessLineComputationService.getMachineInstallingDuration(
                Mockito.any(ProdProcessLine.class), Mockito.eq(BigDecimal.valueOf(0))))
        .thenReturn(BigDecimal.valueOf(0));
    Mockito.when(prodProcessLineComputationService.getMachineDuration(Mockito.any(), Mockito.any()))
        .thenCallRealMethod();
  }

  @Test
  void testComputeMachineDurationSimpleTypeMachine() throws AxelorException {
    BigDecimal nbCycle = BigDecimal.valueOf(1);
    ProdProcessLine ppl = new ProdProcessLine();
    ppl.setDurationPerCycle(30L);
    ppl.setWorkCenter(machineWorkCenter);
    Assertions.assertEquals(
        BigDecimal.valueOf(180),
        prodProcessLineComputationService.getMachineDuration(ppl, nbCycle));
  }

  @Test
  void testComputeMachineDurationSimpleTypeBoth() throws AxelorException {
    BigDecimal nbCycle = BigDecimal.valueOf(1);
    ProdProcessLine ppl = new ProdProcessLine();
    ppl.setDurationPerCycle(30L);
    ppl.setWorkCenter(bothWorkCenter);
    Assertions.assertEquals(
        BigDecimal.valueOf(180),
        prodProcessLineComputationService.getMachineDuration(ppl, nbCycle));
  }

  @Test
  void testComputeMachineDurationSimpleTypeHuman() throws AxelorException {
    BigDecimal nbCycle = BigDecimal.valueOf(1);
    ProdProcessLine ppl = new ProdProcessLine();
    ppl.setDurationPerCycle(30L);
    ppl.setWorkCenter(humanWorkCenter);
    Assertions.assertEquals(
        BigDecimal.valueOf(0), prodProcessLineComputationService.getMachineDuration(ppl, nbCycle));
  }

  @Test
  void testComputeMachineDurationSimpleTypeMachine2() throws AxelorException {
    BigDecimal nbCycle = BigDecimal.valueOf(2);
    ProdProcessLine ppl = new ProdProcessLine();
    ppl.setDurationPerCycle(30L);
    ppl.setWorkCenter(machineWorkCenter);
    Assertions.assertEquals(
        BigDecimal.valueOf(210),
        prodProcessLineComputationService.getMachineDuration(ppl, nbCycle));
  }

  @Test
  void testComputeMachineDurationNegativeCycle() throws AxelorException {
    BigDecimal nbCycle = BigDecimal.valueOf(-1);
    ProdProcessLine ppl = new ProdProcessLine();
    ppl.setDurationPerCycle(30L);
    ppl.setWorkCenter(machineWorkCenter);
    Assertions.assertEquals(
        BigDecimal.valueOf(0), prodProcessLineComputationService.getMachineDuration(ppl, nbCycle));
  }

  @Test
  void testComputeMachineDurationNegativeDurationPerCycle() throws AxelorException {
    BigDecimal nbCycle = BigDecimal.valueOf(1);
    ProdProcessLine ppl = new ProdProcessLine();
    ppl.setDurationPerCycle(-180L);
    ppl.setWorkCenter(machineWorkCenter);
    Assertions.assertEquals(
        BigDecimal.valueOf(150),
        prodProcessLineComputationService.getMachineDuration(ppl, nbCycle));
  }

  @Test
  void testComputeMachineDurationNoCycle() throws AxelorException {
    BigDecimal nbCycle = BigDecimal.valueOf(0);
    ProdProcessLine ppl = new ProdProcessLine();
    ppl.setDurationPerCycle(30L);
    ppl.setWorkCenter(machineWorkCenter);
    Assertions.assertEquals(
        BigDecimal.valueOf(0), prodProcessLineComputationService.getMachineDuration(ppl, nbCycle));
  }

  @Test
  void testComputeMachineDurationNullCycleExpectError() throws AxelorException {
    BigDecimal nbCycle = null;
    ProdProcessLine ppl = new ProdProcessLine();
    ppl.setDurationPerCycle(30L);
    ppl.setWorkCenter(machineWorkCenter);
    Assertions.assertThrowsExactly(
        NullPointerException.class,
        () -> prodProcessLineComputationService.getMachineDuration(ppl, nbCycle));
  }

  @Test
  void testComputeMachineDurationNullDurationPerCycle() throws AxelorException {
    BigDecimal nbCycle = BigDecimal.valueOf(1);
    ProdProcessLine ppl = new ProdProcessLine();
    ppl.setWorkCenter(machineWorkCenter);
    Assertions.assertEquals(
        BigDecimal.valueOf(150),
        prodProcessLineComputationService.getMachineDuration(ppl, nbCycle));
  }
}
