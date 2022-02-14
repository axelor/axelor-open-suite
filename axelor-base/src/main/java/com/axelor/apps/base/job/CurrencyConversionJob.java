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
package com.axelor.apps.base.job;

import com.axelor.apps.base.service.currency.CurrencyConversionFactory;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class CurrencyConversionJob implements Job {

  @Inject protected CurrencyConversionFactory currencyConversionFactory;

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    try {
      currencyConversionFactory.getCurrencyConversionService().updateCurrencyConverion();
    } catch (AxelorException e) {
      throw new JobExecutionException(e);
    }
  }
}
