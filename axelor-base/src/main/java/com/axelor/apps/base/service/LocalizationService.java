package com.axelor.apps.base.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Localization;

public interface LocalizationService {
  public void validateLocale(Localization localization) throws AxelorException;
}
