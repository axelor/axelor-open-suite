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
package com.axelor.apps.base.service.imports.listener;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.ExceptionOriginRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.data.Listener;
import com.axelor.db.Model;
import com.axelor.i18n.I18n;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImporterListener implements Listener {

  protected Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private String name, importLog = "";
  private int totalRecord, successRecord, notNull, anomaly;

  public ImporterListener(String name) {
    this.name = name;
  }

  public String getImportLog() {

    String log = importLog;
    log +=
        "\n"
            + I18n.get(BaseExceptionMessage.IMPORTER_LISTERNER_1)
            + " "
            + totalRecord
            + " "
            + I18n.get(BaseExceptionMessage.IMPORTER_LISTERNER_2)
            + " "
            + successRecord
            + " "
            + I18n.get(BaseExceptionMessage.IMPORTER_LISTERNER_5)
            + " "
            + notNull;
    log += "\n" + I18n.get(BaseExceptionMessage.IMPORTER_LISTERNER_3) + anomaly;

    return log;
  }

  @Override
  public void imported(Model bean) {
    if (bean != null) {
      ++notNull;
    }
  }

  @Override
  public void imported(Integer total, Integer success) {
    totalRecord += total;
    successRecord += success;
  }

  @Override
  public void handle(Model bean, Exception e) {
    anomaly++;
    importLog += "\n" + e;
    TraceBackService.trace(
        new AxelorException(
            e,
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(BaseExceptionMessage.IMPORTER_LISTERNER_4),
            name),
        ExceptionOriginRepository.IMPORT);
  }

  public boolean isImported() {
    if ((anomaly == 0 || notNull == 0) && (totalRecord == successRecord)) {
      return true;
    }
    return false;
  }
}
