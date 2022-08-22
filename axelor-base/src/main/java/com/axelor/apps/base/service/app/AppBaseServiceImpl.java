/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.service.app;

import com.axelor.app.AppSettings;
import com.axelor.apps.base.db.App;
import com.axelor.apps.base.db.AppBase;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.CurrencyConversionLine;
import com.axelor.apps.base.db.Language;
import com.axelor.apps.base.db.repo.AppBaseRepository;
import com.axelor.apps.base.db.repo.AppRepository;
import com.axelor.apps.tool.date.DateTool;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.common.StringUtils;
import com.axelor.db.Query;
import com.axelor.event.Observes;
import com.axelor.events.StartupEvent;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.meta.CallMethod;
import com.google.common.base.Strings;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoper;
import com.google.inject.servlet.ServletScopes;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.inject.Singleton;

@Singleton
public class AppBaseServiceImpl extends AppServiceImpl implements AppBaseService {

  protected static String DEFAULT_LOCALE = "en";

  @Override
  public AppBase getAppBase() {
    return Query.of(AppBase.class).cacheable().fetchOne();
  }

  @Override
  public ZonedDateTime getTodayDateTime() {
    return getTodayDateTime(null);
  }

  public ZonedDateTime getTodayDateTime(Company company) {

    ZonedDateTime todayDateTime = ZonedDateTime.now();
    if (company != null) {
      todayDateTime = DateTool.getTodayDateTime(company.getTimezone());
    }

    String applicationMode = AppSettings.get().get("application.mode", "prod");

    if ("dev".equals(applicationMode)) {
      User user = AuthUtils.getUser();
      if (user != null && user.getTodayDateT() != null) {
        todayDateTime = user.getTodayDateT();
      } else {
        AppBase appBase = getAppBase();
        if (appBase != null && appBase.getTodayDateT() != null) {
          return appBase.getTodayDateT();
        }
      }
    }

    return todayDateTime;
  }

  @Override
  public LocalDate getTodayDate(Company company) {

    return getTodayDateTime(company).toLocalDate();
  }

  @Override
  public int getNbDecimalDigitForUnitPrice() {

    AppBase appBase = getAppBase();

    if (appBase != null) {
      return appBase.getNbDecimalDigitForUnitPrice();
    }

    return DEFAULT_NB_DECIMAL_DIGITS;
  }

  @Override
  public int getGlobalTrackingLogPersistence() {
    AppBase appBase = getAppBase();
    if (appBase != null) {
      return appBase.getGlobalTrackingLogPersistence();
    }
    return DEFAULT_TRACKING_MONTHS_PERSISTENCE;
  }

  @Override
  public int getNbDecimalDigitForQty() {

    AppBase appBase = getAppBase();

    if (appBase != null) {
      return appBase.getNbDecimalDigitForQty();
    }

    return DEFAULT_NB_DECIMAL_DIGITS;
  }

  @Override
  public String getDefaultPartnerLanguageCode() {

    AppBase appBase = getAppBase();

    if (appBase != null) {
      Language language = appBase.getDefaultPartnerLanguage();
      if (language != null && !Strings.isNullOrEmpty(language.getCode())) {
        return language.getCode();
      }
    }
    return DEFAULT_LOCALE;
  }

  // Conversion de devise

  /**
   * Obtenir la tva à 0%
   *
   * @return
   */
  @Override
  public List<CurrencyConversionLine> getCurrencyConfigurationLineList() {
    AppBase appBase = getAppBase();
    if (appBase != null) {
      return appBase.getCurrencyConversionLineList();
    } else {
      return null;
    }
  }

  @Override
  public BigDecimal getDurationHours(BigDecimal duration) {

    if (duration == null) {
      return null;
    }

    AppBase appBase = this.getAppBase();

    if (appBase != null) {
      String timePref = appBase.getTimeLoggingPreferenceSelect();

      if (timePref.equals("days")) {
        duration = duration.multiply(appBase.getDailyWorkHours());
      } else if (timePref.equals("minutes")) {
        duration = duration.divide(new BigDecimal(60), 2, RoundingMode.HALF_UP);
      }
    }

    return duration;
  }

  @Override
  public BigDecimal getGeneralDuration(BigDecimal duration) {

    if (duration == null) {
      return null;
    }

    AppBase appBase = getAppBase();

    if (appBase != null) {
      String timePref = appBase.getTimeLoggingPreferenceSelect();

      BigDecimal dailyWorkHrs = appBase.getDailyWorkHours();

      if (timePref.equals("days")
          && dailyWorkHrs != null
          && dailyWorkHrs.compareTo(BigDecimal.ZERO) != 0) {
        duration = duration.divide(dailyWorkHrs, 2, RoundingMode.HALF_UP);
      } else if (timePref.equals("minutes")) {
        duration = duration.multiply(new BigDecimal(60));
      }
    }

    return duration;
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public void setManageMultiBanks(boolean manageMultiBanks) {
    getAppBase().setManageMultiBanks(manageMultiBanks);
  }

  @CallMethod
  @Override
  public String getCustomStyle() {

    AppBase appBase = Beans.get(AppBaseRepository.class).all().fetchOne();
    String style = appBase.getCustomAppStyle();
    if (StringUtils.isBlank(style)) {
      return null;
    }

    return style;
  }

  @Override
  public int getProcessTimeout() {
    int processTimeout = getAppBase().getProcessTimeout();
    if (processTimeout < 1) {
      return 10;
    } else {
      return processTimeout;
    }
  }

  public void installAppsOnStartup(@Observes StartupEvent event) {

    final RequestScoper scope = ServletScopes.scopeRequest(Collections.emptyMap());

    try (RequestScoper.CloseableScope ignored = scope.open()) {

      AppSettings appSettings = AppSettings.get();

      String apps = appSettings.get("aos.apps.install-apps");
      if (StringUtils.isBlank(apps)) {
        return;
      }

      boolean importDemoData = appSettings.getBoolean("data.import.demo-data", false);
      String lang = appSettings.get("application.locale", DEFAULT_LOCALE);

      List<App> appList = new ArrayList<>();
      AppRepository appRepo = Beans.get(AppRepository.class);

      if (apps.equalsIgnoreCase("all")) {
        appList = appRepo.all().filter("self.active IS NULL OR self.active = false").fetch();
      } else {
        String[] appCodes = apps.split(",");
        for (String code : appCodes) {
          App app = appRepo.findByCode(code.trim());
          if (app != null) {
            appList.add(app);
          }
        }
      }

      if (appList.size() == 0) {
        return;
      }

      try {
        Beans.get(AppService.class).bulkInstall(appList, importDemoData, lang);
      } catch (AxelorException | IOException e) {
        TraceBackService.trace(e);
        e.printStackTrace();
      }
    }
  }
}
