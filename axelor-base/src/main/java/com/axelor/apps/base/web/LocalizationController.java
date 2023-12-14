package com.axelor.apps.base.web;

import com.axelor.apps.base.db.Country;
import com.axelor.apps.base.db.Language;
import com.axelor.apps.base.db.Localization;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class LocalizationController {
  public void setNameAndCode(ActionRequest request, ActionResponse response) {
    //    Localization localization = request.getContext().asType(Localization.class);
    //    String countryName = localization.getCountry().getName();
    //    String languageName = localization.getLanguage().getName();
    //    localization = Beans.get(LocalizationRepository.class).find(localization.getId());
    //    LocalizationService localizationService = Beans.get(LocalizationService.class);
    //    localizationService.setName(languageName, countryName, localization);
    //    response.setReload(true);
    Localization localization = request.getContext().asType(Localization.class);
    Language language = localization.getLanguage();
    Country country = localization.getCountry();
    if (language == null || country == null) {
      return;
    }
    response.setValue("name", language.getName() + " (" + country.getName() + ")");
    response.setValue("code", language.getCode() + "_" + country.getAlpha2Code());
  }
}
