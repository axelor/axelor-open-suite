package com.axelor.apps.supplychain.service;

import com.axelor.apps.supplychain.db.MrpForecast;
import com.axelor.exception.AxelorException;

public interface MrpForecastService {

  /**
   * Set the forecast status to confirmed.
   *
   * @param mrpForecast
   * @throws AxelorException if the forecast wasn't drafted.
   */
  void confirm(MrpForecast mrpForecast) throws AxelorException;

  /**
   * Set the forecast status to canceled.
   *
   * @param mrpForecast
   * @throws AxelorException if the forecast wasn't drafted nor canceled.
   */
  void cancel(MrpForecast mrpForecast) throws AxelorException;
}
