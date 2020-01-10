/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.web;

import com.axelor.apps.base.db.AdvancedImport;
import com.axelor.apps.base.db.repo.AdvancedImportRepository;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.service.advanced.imports.AdvancedImportService;
import com.axelor.apps.base.service.advanced.imports.DataImportService;
import com.axelor.apps.base.service.advanced.imports.ValidatorService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaFile;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class AdvancedImportController {

  public void apply(ActionRequest request, ActionResponse response) throws AxelorException {
    try {
      AdvancedImport advancedImport = request.getContext().asType(AdvancedImport.class);
      if (advancedImport.getId() != null) {
        advancedImport = Beans.get(AdvancedImportRepository.class).find(advancedImport.getId());
      }

      boolean isValid = Beans.get(AdvancedImportService.class).apply(advancedImport);
      if (isValid) {
        response.setReload(true);
      } else {
        response.setFlash(I18n.get(IExceptionMessage.ADVANCED_IMPORT_FILE_FORMAT_INVALID));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void validate(ActionRequest request, ActionResponse response) {
    try {
      AdvancedImport advancedImport = request.getContext().asType(AdvancedImport.class);
      if (advancedImport.getId() != null) {
        advancedImport = Beans.get(AdvancedImportRepository.class).find(advancedImport.getId());
      }

      boolean isLog = Beans.get(ValidatorService.class).validate(advancedImport);
      if (isLog) {
        response.setFlash(I18n.get(IExceptionMessage.ADVANCED_IMPORT_CHECK_LOG));
        response.setReload(true);
      } else {
        response.setValue("statusSelect", 1);
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void importData(ActionRequest request, ActionResponse response) {
    try {
      AdvancedImport advancedImport = request.getContext().asType(AdvancedImport.class);
      if (advancedImport.getId() != null) {
        advancedImport = Beans.get(AdvancedImportRepository.class).find(advancedImport.getId());
      }

      MetaFile logFile = Beans.get(DataImportService.class).importData(advancedImport);
      if (logFile != null) {
        response.setValue("errorLog", logFile);
      } else {
        response.setValue("errorLog", null);
        response.setFlash(I18n.get(IExceptionMessage.ADVANCED_IMPORT_IMPORT_DATA));
        response.setSignal("refresh-app", true);
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void resetImport(ActionRequest request, ActionResponse response) {
    try {
      AdvancedImport advancedImport = request.getContext().asType(AdvancedImport.class);
      if (advancedImport.getId() != null) {
        advancedImport = Beans.get(AdvancedImportRepository.class).find(advancedImport.getId());
      }

      boolean isReset = Beans.get(AdvancedImportService.class).resetImport(advancedImport);
      if (isReset) {
        response.setFlash(I18n.get(IExceptionMessage.ADVANCED_IMPORT_RESET));
        response.setSignal("refresh-app", true);
      } else {
        response.setFlash(I18n.get(IExceptionMessage.ADVANCED_IMPORT_NO_RESET));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
