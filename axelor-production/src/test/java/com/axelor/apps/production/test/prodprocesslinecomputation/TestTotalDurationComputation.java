package com.axelor.apps.production.test.prodprocesslinecomputation;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.service.ProdProcessLineComputationServiceImpl;
import java.math.BigDecimal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class TestTotalDurationComputation extends TestProdProcessLineComputationService {

  @BeforeEach
  void prepareMock() throws AxelorException {
    prodProcessLineComputationService = Mockito.mock(ProdProcessLineComputationServiceImpl.class);
    Mockito.when(prodProcessLineComputationService.getTotalDuration(Mockito.any(), Mockito.any()))
        .thenCallRealMethod();
  }

  @Test
  void testComputeTotalDurationMachineDurationGreater() throws AxelorException {
    Mockito.when(prodProcessLineComputationService.getHumanDuration(Mockito.any(), Mockito.any()))
        .thenReturn(BigDecimal.valueOf(180));
    Mockito.when(prodProcessLineComputationService.getMachineDuration(Mockito.any(), Mockito.any()))
        .thenReturn(BigDecimal.valueOf(360));
    BigDecimal nbCycle = BigDecimal.valueOf(1);
    ProdProcessLine ppl = new ProdProcessLine();
    Assertions.assertEquals(
        BigDecimal.valueOf(360), prodProcessLineComputationService.getTotalDuration(ppl, nbCycle));
  }

  @Test
  void testComputeTotalDurationHumanDurationGreater() throws AxelorException {
    Mockito.when(prodProcessLineComputationService.getHumanDuration(Mockito.any(), Mockito.any()))
        .thenReturn(BigDecimal.valueOf(320));
    Mockito.when(prodProcessLineComputationService.getMachineDuration(Mockito.any(), Mockito.any()))
        .thenReturn(BigDecimal.valueOf(180));
    BigDecimal nbCycle = BigDecimal.valueOf(1);
    ProdProcessLine ppl = new ProdProcessLine();
    Assertions.assertEquals(
        BigDecimal.valueOf(320), prodProcessLineComputationService.getTotalDuration(ppl, nbCycle));
  }

  @Test
  void testComputeTotalDurationNegativeValues() throws AxelorException {
    Mockito.when(prodProcessLineComputationService.getHumanDuration(Mockito.any(), Mockito.any()))
        .thenReturn(BigDecimal.valueOf(-320));
    Mockito.when(prodProcessLineComputationService.getMachineDuration(Mockito.any(), Mockito.any()))
        .thenReturn(BigDecimal.valueOf(-180));
    BigDecimal nbCycle = BigDecimal.valueOf(1);
    ProdProcessLine ppl = new ProdProcessLine();
    Assertions.assertEquals(
        BigDecimal.valueOf(0), prodProcessLineComputationService.getTotalDuration(ppl, nbCycle));
  }
}
