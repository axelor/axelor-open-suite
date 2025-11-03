package com.axelor.apps.hr.service;

import com.axelor.apps.base.AxelorException;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;

public interface KilometricGoogleService {
  BigDecimal getDistanceUsingGoogle(String fromCity, String toCity)
      throws AxelorException, URISyntaxException, IOException;
}
