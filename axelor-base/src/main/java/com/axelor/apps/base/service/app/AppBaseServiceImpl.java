/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.service.app;

import com.axelor.app.AppSettings;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.CurrencyConversionLine;
import com.axelor.apps.base.db.Language;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.common.StringUtils;
import com.axelor.db.Query;
import com.axelor.inject.Beans;
import com.axelor.meta.CallMethod;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.studio.app.service.AppServiceImpl;
import com.axelor.studio.app.service.AppVersionService;
import com.axelor.studio.db.AppBase;
import com.axelor.studio.db.repo.AppBaseRepository;
import com.axelor.studio.db.repo.AppRepository;
import com.axelor.studio.service.AppSettingsStudioService;
import com.axelor.utils.date.DateTool;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import javax.inject.Singleton;

@Singleton
public class AppBaseServiceImpl extends AppServiceImpl implements AppBaseService {

  protected static String DEFAULT_LOCALE = "en";

  @Inject
  public AppBaseServiceImpl(
      AppRepository appRepo,
      MetaFiles metaFiles,
      AppVersionService appVersionService,
      MetaModelRepository metaModelRepo,
      AppSettingsStudioService appSettingsStudioService) {
    super(appRepo, metaFiles, appVersionService, metaModelRepo, appSettingsStudioService);
  }

  @Override
  public AppBase getAppBase() {
    return Query.of(AppBase.class).fetchOne();
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
    if (appBase == null) {
      return null;
    }
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
}
