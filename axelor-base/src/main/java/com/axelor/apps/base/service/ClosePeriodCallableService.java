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
package com.axelor.apps.base.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.repo.PeriodRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.auth.AuthUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.message.service.MailMessageService;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoper;
import com.google.inject.servlet.ServletScopes;
import java.util.Collections;
import java.util.concurrent.Callable;

public class ClosePeriodCallableService implements Callable<Period> {

  private Period period;

  public void setPeriod(Period period) {
    this.period = period;
  }

  @Override
  public Period call() throws AxelorException {
    final RequestScoper scope = ServletScopes.scopeRequest(Collections.emptyMap());
    try (RequestScoper.CloseableScope ignored = scope.open()) {
      period = Beans.get(PeriodRepository.class).find(period.getId());
      closePeriodAndSendMessage();
      return period;
    } catch (Exception e) {
      onRunnerException(e);
      throw e;
    }
  }

  protected void closePeriodAndSendMessage() throws AxelorException {
    Beans.get(PeriodService.class).closePeriod(period);
    Beans.get(MailMessageService.class)
        .sendNotification(
            AuthUtils.getUser(),
            String.format(I18n.get(BaseExceptionMessage.PERIOD_CLOSING_MESSAGE), period.getName()),
            String.format(I18n.get(BaseExceptionMessage.PERIOD_CLOSING_MESSAGE), period.getName()),
            period.getId(),
            period.getClass());
  }

  @Transactional
  protected void onRunnerException(Exception e) {
    TraceBackService.trace(e);
    Beans.get(MailMessageService.class)
        .sendNotification(
            AuthUtils.getUser(),
            String.format(
                I18n.get(BaseExceptionMessage.PERIOD_CLOSING_EXCEPTION_MESSAGE), period.getName()),
            e.getMessage(),
            period.getId(),
            period.getClass());
  }
}
