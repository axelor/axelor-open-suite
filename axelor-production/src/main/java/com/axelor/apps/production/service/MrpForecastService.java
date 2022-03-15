package com.axelor.apps.production.service;

import com.axelor.apps.supplychain.db.MrpForecast;
import com.axelor.exception.AxelorException;

public interface MrpForecastService {

  void confirm(MrpForecast mrpForecast) throws AxelorException;

  void cancel(MrpForecast mrpForecast) throws AxelorException;
}
