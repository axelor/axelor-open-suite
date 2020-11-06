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

import com.axelor.apps.base.db.GroupExport;
import com.axelor.apps.base.db.repo.GroupExportRepository;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.service.groupExport.GroupExportService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaFile;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class GroupExportController {

  public void exportGroupExport(ActionRequest request, ActionResponse response) {
    try {

      GroupExport groupExport = request.getContext().asType(GroupExport.class);
      groupExport = Beans.get(GroupExportRepository.class).find(groupExport.getId());

      if (groupExport.getGroupAdvancedExportList() == null
          || groupExport.getGroupAdvancedExportList().isEmpty()) {
        response.setFlash(I18n.get(IExceptionMessage.GROUP_EXPORT_ADVANCE_EXPORT_LINE_LIST_EMPTY));
      }

      MetaFile metaFile = Beans.get(GroupExportService.class).exportAdvanceExports(groupExport);

      if (metaFile != null) {
        Beans.get(AdvancedExportController.class).downloadExportFile(response, metaFile);
      }
      response.setReload(true);

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
