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

import com.axelor.apps.base.db.DataSharingReferentialLine;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.DataSharingReferentialLineService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class DataSharingReferentialLineController {

  public void testQueryCondition(ActionRequest request, ActionResponse response) {
    try {
      DataSharingReferentialLine dataSharingReferentialLine =
          request.getContext().asType(DataSharingReferentialLine.class);
      Beans.get(DataSharingReferentialLineService.class)
          .testQueryCondition(dataSharingReferentialLine);
      response.setInfo(
          I18n.get(BaseExceptionMessage.DATA_SHARING_REFERENTIAL_LINE_JPQL_SYNTAX_IS_CORRECT));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
