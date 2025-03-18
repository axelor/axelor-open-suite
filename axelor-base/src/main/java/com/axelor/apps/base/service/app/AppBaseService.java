/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.AddressTemplate;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.CurrencyConversionLine;
import com.axelor.apps.base.db.Unit;
import com.axelor.studio.app.service.AppService;
import com.axelor.studio.db.AppBase;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

public interface AppBaseService extends AppService {

  public static final int DEFAULT_NB_DECIMAL_DIGITS = 2;

  // Used to scale exchange rates according to domain definition
  public static final int DEFAULT_EXCHANGE_RATE_SCALE = 6;

  // Used to scale inverse exchange rate (1 / exchangeRate)
  public static final int DEFAULT_EXCHANGE_RATE_REVERSION_SCALE = 8;

  public static final int DEFAULT_TRACKING_MONTHS_PERSISTENCE = 1;

  public static final int COMPUTATION_SCALING = 20;

  public AppBase getAppBase();

  // Date du jour

  /**
   * Retrieve the current date and time according to the server timezone. Returns the current date
   * set in the user if it exists, otherwise retrieves the one from the general administration,
   * otherwise the current date.
   *
   * @return
   */
  public ZonedDateTime getTodayDateTime();

  /**
   * Retrieve the current date and time according to the timezone entered in the given company.
   * Returns the current date set in the user if it exists, otherwise retrieve the general
   * administration's one, otherwise current date.
   *
   * @return
   */
  public ZonedDateTime getTodayDateTime(Company company);

  /**
   * Retrieve the current date according to the timezone entered in the given company. Returns the
   * current date set in the user if it exists, otherwise retrieves the one from the general
   * administration, otherwise the current date.
   *
   * @return
   */
  public LocalDate getTodayDate(Company company);

  public int getNbDecimalDigitForUnitPrice();

  public int getNbDecimalDigitForQty();

  public int getGlobalTrackingLogPersistence();

  public String getDefaultPartnerLocale();

  // Conversion de devise

  AddressTemplate getDefaultAddressTemplate() throws AxelorException;

  /**
   * Get 0% vat
   *
   * @return
   */
  public List<CurrencyConversionLine> getCurrencyConfigurationLineList();

  public BigDecimal getDurationHours(BigDecimal duration);

  public BigDecimal getGeneralDuration(BigDecimal duration);

  Unit getUnitDays() throws AxelorException;

  Unit getUnitHours() throws AxelorException;

  Unit getUnitMinutes() throws AxelorException;

  BigDecimal getDailyWorkHours() throws AxelorException;

  /**
   * Set the manageMultiBanks boolean in the general object.
   *
   * @param manageMultiBanks the new value for the manageMultiBanks boolean
   */
  void setManageMultiBanks(boolean manageMultiBanks);

  /**
   * Get process timeout value. If the value is inferior or equal to 0, we return the default value
   * (10 seconds).
   */
  int getProcessTimeout();

  String getSireneTokenGeneratorUrl() throws AxelorException;

  String getSireneUrl() throws AxelorException;

  String getSireneKey() throws AxelorException;

  String getSireneSecret() throws AxelorException;
}
