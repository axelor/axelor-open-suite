package com.axelor.apps.production.test.prodprocesslinecomputation;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.ProdProcessLine;
import java.math.BigDecimal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestHumanDurationComputation extends TestProdProcessLineComputationService {

  @Test
  void testComputeHumanDurationSimple() throws AxelorException {
    BigDecimal nbCycle = BigDecimal.valueOf(1);

    ProdProcessLine ppl = new ProdProcessLine();
    ppl.setHumanDuration(60L);
    ppl.setWorkCenter(humanWorkCenter);

    Assertions.assertEquals(
        BigDecimal.valueOf(60), prodProcessLineComputationService.getHumanDuration(ppl, nbCycle));
  }

  @Test
  void testComputeHumanDurationSimple2() throws AxelorException {
    BigDecimal nbCycle = BigDecimal.valueOf(2);

    ProdProcessLine ppl = new ProdProcessLine();
    ppl.setHumanDuration(60L);
    ppl.setWorkCenter(humanWorkCenter);

    Assertions.assertEquals(
        BigDecimal.valueOf(120), prodProcessLineComputationService.getHumanDuration(ppl, nbCycle));
  }

  @Test
  void testComputeHumanDurationBothWorkCenterType() throws AxelorException {
    BigDecimal nbCycle = BigDecimal.valueOf(2);

    ProdProcessLine ppl = new ProdProcessLine();
    ppl.setHumanDuration(60L);
    ppl.setWorkCenter(bothWorkCenter);

    Assertions.assertEquals(
        BigDecimal.valueOf(120), prodProcessLineComputationService.getHumanDuration(ppl, nbCycle));
  }

  @Test
  void testComputeHumanDurationMachineWorkCenterType() throws AxelorException {
    BigDecimal nbCycle = BigDecimal.valueOf(2);

    ProdProcessLine ppl = new ProdProcessLine();
    ppl.setHumanDuration(60L);
    ppl.setWorkCenter(machineWorkCenter);

    Assertions.assertEquals(
        BigDecimal.valueOf(0), prodProcessLineComputationService.getHumanDuration(ppl, nbCycle));
  }

  @Test
  void testComputeHumanDurationNegativeCycle() throws AxelorException {
    BigDecimal nbCycle = BigDecimal.valueOf(-1);

    ProdProcessLine ppl = new ProdProcessLine();
    ppl.setHumanDuration(60L);
    ppl.setWorkCenter(humanWorkCenter);

    Assertions.assertEquals(
        BigDecimal.valueOf(0), prodProcessLineComputationService.getHumanDuration(ppl, nbCycle));
  }

  @Test
  void testComputeHumanDurationNegativeHumanDuration() throws AxelorException {
    BigDecimal nbCycle = BigDecimal.valueOf(2);

    ProdProcessLine ppl = new ProdProcessLine();
    ppl.setHumanDuration(-60L);
    ppl.setWorkCenter(humanWorkCenter);

    Assertions.assertEquals(
        BigDecimal.valueOf(0), prodProcessLineComputationService.getHumanDuration(ppl, nbCycle));
  }

  @Test
  void testComputeHumanDurationNullCycleExpectException() throws AxelorException {
    BigDecimal nbCycle = null;

    ProdProcessLine ppl = new ProdProcessLine();
    ppl.setHumanDuration(-60L);
    ppl.setWorkCenter(humanWorkCenter);

    Assertions.assertThrowsExactly(
        NullPointerException.class,
        () -> prodProcessLineComputationService.getHumanDuration(ppl, nbCycle));
  }

  @Test
  void testComputeHumanDurationNullPplExpectException() throws AxelorException {
    BigDecimal nbCycle = BigDecimal.ONE;

    ProdProcessLine ppl = null;

    Assertions.assertThrowsExactly(
        NullPointerException.class,
        () -> prodProcessLineComputationService.getHumanDuration(ppl, nbCycle));
  }

  @Test
  void testComputeHumanDurationNullHumanDuration() throws AxelorException {
    BigDecimal nbCycle = BigDecimal.ONE;

    ProdProcessLine ppl = new ProdProcessLine();
    ppl.setWorkCenter(humanWorkCenter);

    Assertions.assertEquals(
        BigDecimal.valueOf(0), prodProcessLineComputationService.getHumanDuration(ppl, nbCycle));
  }
}
