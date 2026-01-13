/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.db.DataSharingProductWizard;
import com.axelor.apps.base.db.DataSharingReferentialLine;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.DataSharingProductWizardService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.utils.helpers.StringHelper;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class DataSharingProductWizardController {

  public void generateDataSharingReferentialLines(ActionRequest request, ActionResponse response) {
    try {
      DataSharingProductWizard dataSharingProductWizard =
          request.getContext().asType(DataSharingProductWizard.class);

      List<DataSharingReferentialLine> dataSharingReferentialLineList =
          Beans.get(DataSharingProductWizardService.class)
              .generateDataSharingReferentialLines(dataSharingProductWizard);
      if (CollectionUtils.isEmpty(dataSharingReferentialLineList)) {
        response.setError(I18n.get(BaseExceptionMessage.DATA_SHARING_MISSING_ELEMENTS));
      }
      ActionViewBuilder actionViewBuilder =
          ActionView.define(I18n.get("Data sharing referential lines"))
              .model(DataSharingReferentialLine.class.getName())
              .add("grid", "data-sharing-referential-line-grid")
              .add("form", "data-sharing-referential-line-form")
              .domain(
                  String.format(
                      "self.id in (%s)",
                      StringHelper.getIdListString(dataSharingReferentialLineList)));
      response.setView(actionViewBuilder.map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
