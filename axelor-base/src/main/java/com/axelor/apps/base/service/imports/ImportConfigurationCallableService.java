/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.imports;

import com.axelor.apps.base.db.ImportConfiguration;
import com.axelor.apps.base.db.ImportHistory;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.auth.AuthUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.message.service.MailMessageService;
import com.google.inject.persist.Transactional;
import java.util.concurrent.Callable;

public class ImportConfigurationCallableService implements Callable<ImportHistory> {

  protected ImportConfiguration importConfiguration;

  @Override
  public ImportHistory call() throws Exception {
    try {
      ImportHistory importHistory = Beans.get(ImportService.class).run(importConfiguration);
      completeImportConfigurationAndSendMessage(importHistory);
      return importHistory;
    } catch (Exception e) {
      onRunnerException(e);
      throw e;
    }
  }

  public void setImportConfig(ImportConfiguration importConfiguration) {
    this.importConfiguration = importConfiguration;
  }

  protected void completeImportConfigurationAndSendMessage(ImportHistory importHistory) {
    Beans.get(ImportConfigurationService.class)
        .updateStatusCompleted(importConfiguration, importHistory);
    Beans.get(MailMessageService.class)
        .sendNotification(
            AuthUtils.getUser(),
            I18n.get(BaseExceptionMessage.IMPORT_CONFIGURATION_CLOSING_MESSAGE),
            I18n.get(BaseExceptionMessage.IMPORT_CONFIGURATION_CLOSING_MESSAGE),
            importHistory.getId(),
            importHistory.getClass());
  }

  @Transactional
  protected void onRunnerException(Exception e) {
    TraceBackService.trace(e);
    Beans.get(MailMessageService.class)
        .sendNotification(
            AuthUtils.getUser(),
            I18n.get(BaseExceptionMessage.IMPORT_CONFIGURATION_ERROR_MESSAGE),
            e.getMessage());
  }
}
