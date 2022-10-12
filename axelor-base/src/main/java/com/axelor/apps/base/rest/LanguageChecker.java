package com.axelor.apps.base.rest;

import com.axelor.apps.base.db.Language;
import com.axelor.apps.base.db.repo.LanguageRepository;
import com.axelor.db.Query;
import com.axelor.inject.Beans;
import javax.ws.rs.NotFoundException;

public class LanguageChecker {

  public static void check(String languageCode) throws NotFoundException {
    Query<Language> query = Beans.get(LanguageRepository.class).all().filter("self.code = :code ");
    query.bind("code", languageCode);
    if (query.count() == 0) {
      throw new NotFoundException("Language with code " + languageCode + " was not found.");
    }
  }
}
