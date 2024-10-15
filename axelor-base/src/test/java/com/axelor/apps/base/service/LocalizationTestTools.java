package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Country;
import com.axelor.apps.base.db.Language;
import com.axelor.apps.base.db.Localization;
import java.util.List;

public class LocalizationTestTools {

  public static void createLocalizations(List<Localization> localizations) {
    Language languageFR = new Language("FR", "fr");
    Language languageEN = new Language("EN", "en");

    Country countryFR = new Country("France");
    countryFR.setAlpha2Code("FR");
    Country countryCA = new Country("Canada");
    countryCA.setAlpha2Code("CA");
    Country countryUS = new Country("USA");
    countryUS.setAlpha2Code("US");

    // FR localizations
    localizations.add(createLocalization(countryFR, languageFR));
    localizations.add(createLocalization(countryCA, languageFR));
    // EN localizations
    localizations.add(createLocalization(countryUS, languageEN));
    localizations.add(createLocalization(countryCA, languageEN));
  }

  static Localization createLocalization(Country country, Language language) {
    Localization localization = new Localization();
    localization.setCountry(country);
    localization.setLanguage(language);

    return localization;
  }
}
