package com.axelor.apps.hr.service;

import com.axelor.apps.base.AxelorException;
import java.io.IOException;
import java.math.BigDecimal;

public interface KilometricOsmService {

  BigDecimal getDistanceUsingOSRMApi(String fromCity, String toCity)
      throws AxelorException, IOException;
}
