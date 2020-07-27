package com.axelor.apps.base.service;

import com.axelor.app.internal.AppFilter;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Language;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import java.util.Locale;
import java.util.Optional;

public class ReportingTool {

  /** Finds locale from user company. Defaults to user locale. */
  public static Locale getCompanyLocale() {
    // manage NPE using optional
    return Optional.of(AuthUtils.getUser())
        .map(User::getActiveCompany)
        .map(Company::getLanguage)
        .map(Language::getCode)
        .map(Locale::new)
        .orElseGet(AppFilter::getLocale);
  }
}
