package com.axelor.apps.production.test.prodprocesslinecomputation;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.ProdProcessLine;
import java.math.BigDecimal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestMachineInstallationDurationComputation
    extends TestProdProcessLineComputationService {

  @Test
  void testComputeSetupDurationSimple() throws AxelorException {
    BigDecimal nbCycle = BigDecimal.valueOf(1);
    ProdProcessLine ppl = new ProdProcessLine();
    ppl.setSetupDuration(40L);
    ppl.setStartingDuration(50L);
    ppl.setEndingDuration(60L);

    Assertions.assertEquals(
        BigDecimal.valueOf(110),
        prodProcessLineComputationService.getMachineInstallingDuration(ppl, nbCycle));
  }

  @Test
  void testComputeSetupDurationSimple2() throws AxelorException {
    BigDecimal nbCycle = BigDecimal.valueOf(2);
    ProdProcessLine ppl = new ProdProcessLine();
    ppl.setSetupDuration(40L);
    ppl.setStartingDuration(50L);
    ppl.setEndingDuration(60L);

    Assertions.assertEquals(
        BigDecimal.valueOf(150),
        prodProcessLineComputationService.getMachineInstallingDuration(ppl, nbCycle));
  }

  @Test
  void testComputeSetupDurationNoCycle() throws AxelorException {
    BigDecimal nbCycle = BigDecimal.valueOf(0);
    ProdProcessLine ppl = new ProdProcessLine();
    ppl.setSetupDuration(40L);
    ppl.setStartingDuration(50L);
    ppl.setEndingDuration(60L);

    Assertions.assertEquals(
        BigDecimal.valueOf(0),
        prodProcessLineComputationService.getMachineInstallingDuration(ppl, nbCycle));
  }

  @Test
  void testComputeSetupDurationNegativeCycle() throws AxelorException {
    BigDecimal nbCycle = BigDecimal.valueOf(-1);
    ProdProcessLine ppl = new ProdProcessLine();
    ppl.setSetupDuration(40L);
    ppl.setStartingDuration(50L);
    ppl.setEndingDuration(60L);

    Assertions.assertEquals(
        BigDecimal.valueOf(0),
        prodProcessLineComputationService.getMachineInstallingDuration(ppl, nbCycle));
  }

  @Test
  void testComputeSetupDurationNullCycleExpectError() throws AxelorException {
    BigDecimal nbCycle = null;
    ProdProcessLine ppl = new ProdProcessLine();
    ppl.setSetupDuration(40L);
    ppl.setStartingDuration(50L);
    ppl.setEndingDuration(60L);

    Assertions.assertThrows(
        NullPointerException.class,
        () -> {
          prodProcessLineComputationService.getMachineInstallingDuration(ppl, nbCycle);
        });
  }

  @Test
  void testComputeSetupDurationNullSetupDuration() throws AxelorException {
    BigDecimal nbCycle = BigDecimal.valueOf(2);
    ProdProcessLine ppl = new ProdProcessLine();
    ppl.setSetupDuration(null);
    ppl.setStartingDuration(50L);
    ppl.setEndingDuration(60L);

    Assertions.assertEquals(
        BigDecimal.valueOf(110),
        prodProcessLineComputationService.getMachineInstallingDuration(ppl, nbCycle));
  }

  @Test
  void testComputeSetupDurationNullStartingDuration() throws AxelorException {
    BigDecimal nbCycle = BigDecimal.valueOf(2);
    ProdProcessLine ppl = new ProdProcessLine();
    ppl.setSetupDuration(40L);
    ppl.setStartingDuration(null);
    ppl.setEndingDuration(60L);

    Assertions.assertEquals(
        BigDecimal.valueOf(100),
        prodProcessLineComputationService.getMachineInstallingDuration(ppl, nbCycle));
  }

  @Test
  void testComputeSetupDurationNullEndingDuration() throws AxelorException {
    BigDecimal nbCycle = BigDecimal.valueOf(2);
    ProdProcessLine ppl = new ProdProcessLine();
    ppl.setSetupDuration(40L);
    ppl.setStartingDuration(50L);

    Assertions.assertEquals(
        BigDecimal.valueOf(90),
        prodProcessLineComputationService.getMachineInstallingDuration(ppl, nbCycle));
  }

  @Test
  void testComputeSetupDurationNegativeBigSetupDuration() throws AxelorException {
    BigDecimal nbCycle = BigDecimal.valueOf(2);
    ProdProcessLine ppl = new ProdProcessLine();
    ppl.setSetupDuration(-120L);
    ppl.setStartingDuration(50L);
    ppl.setEndingDuration(60L);

    Assertions.assertEquals(
        BigDecimal.valueOf(110),
        prodProcessLineComputationService.getMachineInstallingDuration(ppl, nbCycle));
  }

  @Test
  void testComputeSetupDurationNegativeStartingDuration() throws AxelorException {
    BigDecimal nbCycle = BigDecimal.valueOf(2);
    ProdProcessLine ppl = new ProdProcessLine();
    ppl.setSetupDuration(40L);
    ppl.setStartingDuration(-50L);
    ppl.setEndingDuration(60L);

    Assertions.assertEquals(
        BigDecimal.valueOf(100),
        prodProcessLineComputationService.getMachineInstallingDuration(ppl, nbCycle));
  }

  @Test
  void testComputeSetupDurationNegativeEndingDuration() throws AxelorException {
    BigDecimal nbCycle = BigDecimal.valueOf(2);
    ProdProcessLine ppl = new ProdProcessLine();
    ppl.setSetupDuration(40L);
    ppl.setStartingDuration(50L);
    ppl.setEndingDuration(-60L);

    Assertions.assertEquals(
        BigDecimal.valueOf(90),
        prodProcessLineComputationService.getMachineInstallingDuration(ppl, nbCycle));
  }
}
