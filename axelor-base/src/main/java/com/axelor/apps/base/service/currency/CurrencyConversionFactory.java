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
package com.axelor.apps.base.service.currency;

import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
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
    try {
      String currencyWsurl = appBaseService.getAppBase().getCurrencyWsURL();

      if (Strings.isNullOrEmpty(currencyWsurl)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_MISSING_FIELD, I18n.get(IExceptionMessage.CURRENCY_6));
      }
      return (CurrencyConversionService) Beans.get(Class.forName(currencyWsurl));
    } catch (ClassNotFoundException | AxelorException e) {
      throw new AxelorException(
          e.getCause(), TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, e.getLocalizedMessage());
    }
  }
}
