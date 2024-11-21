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
package com.axelor.apps.base.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.DMSImportWizardService;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import java.util.Map;

public class DmsImportWizardController {

  @Inject private MetaFileRepository metaFileRepo;

  @Inject DMSImportWizardService dmsImportWizardService;

  public void importDMS(ActionRequest request, ActionResponse response) throws AxelorException {
    try {
      @SuppressWarnings("unchecked")
      Map<String, Object> metaFileMap = (Map<String, Object>) request.getContext().get("metaFile");
      MetaFile metaFile = metaFileRepo.find(Long.parseLong(metaFileMap.get("id").toString()));
      dmsImportWizardService.importDMS(metaFile);
      response.setReload(true);
    } catch (AxelorException e) {
      response.setInfo(e.getMessage());
    }
  }
}
