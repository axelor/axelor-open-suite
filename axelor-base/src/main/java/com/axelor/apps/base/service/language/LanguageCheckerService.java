package com.axelor.apps.base.service.language;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Localization;
import javax.ws.rs.NotFoundException;

public interface LanguageCheckerService {

  void check(String languageCode) throws NotFoundException;

  void checkLanguage(Localization localization) throws AxelorException;
}
