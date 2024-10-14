package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Language;
import java.util.List;

public class LanguageTestTools {

  public static void createLanguages(List<Language> languages) {
    languages.add(new Language("FR", "fr"));
    languages.add(new Language("EN", "en"));
  }
}
