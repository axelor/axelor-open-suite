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
package com.axelor.apps.base.service.currency;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.base.Strings;
import com.google.inject.Inject;

public class CurrencyConversionFactory {

  protected AppBaseService appBaseService;

  @Inject
  public CurrencyConversionFactory(AppBaseService appBaseService) {
    this.appBaseService = appBaseService;
  }

  public CurrencyConversionService getCurrencyConversionService() throws AxelorException {

    String currencyWsurl = appBaseService.getAppBase().getCurrencyWsURL();

    if (Strings.isNullOrEmpty(currencyWsurl)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.CURRENCY_8));
    }
    try {
      return (CurrencyConversionService) Beans.get(Class.forName(currencyWsurl));
    } catch (ClassNotFoundException | ClassCastException e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY, I18n.get(BaseExceptionMessage.CURRENCY_9));
    }
  }
}
