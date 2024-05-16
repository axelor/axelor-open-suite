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
import com.axelor.apps.base.db.PrintTemplateLine;
import com.axelor.apps.base.db.repo.PrintTemplateLineRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.base.service.print.PrintTemplateLineService;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import java.io.IOException;

public class PrintTemplateLineController {

  public void checkTemplateLineExpression(ActionRequest request, ActionResponse response) {

    Context context = request.getContext();
    PrintTemplateLine printTemplateLine =
        Beans.get(PrintTemplateLineRepository.class)
            .find(Long.valueOf(context.get("_printTemplateLine").toString()));
    MetaModel metaModel =
        Beans.get(MetaModelRepository.class)
            .all()
            .filter("self.fullName = ?", context.get("reference"))
            .fetchOne();
    try {
      String result =
          Beans.get(PrintTemplateLineService.class)
              .checkExpression(
                  Long.valueOf(context.get("referenceId").toString()),
                  metaModel,
                  printTemplateLine);
      response.setValue("$contentResult", result);
    } catch (NumberFormatException | ClassNotFoundException | AxelorException | IOException e) {
      TraceBackService.trace(response, e);
    }
  }
}
