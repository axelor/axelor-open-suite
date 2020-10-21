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

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
