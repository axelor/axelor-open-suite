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
package com.axelor.apps.base.service.printing.template;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BirtTemplate;
import com.axelor.apps.base.db.PrintingTemplateLine;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.PrintFromBirtTemplateService;
import com.axelor.apps.base.service.printing.template.model.PrintingGenFactoryContext;
import com.axelor.apps.base.service.printing.template.model.TemplatePrint;
import com.axelor.db.Model;
import com.axelor.inject.Beans;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class PrintingGeneratorFactoryBirt implements PrintingGeneratorFactory {

  @Override
  public TemplatePrint generate(
      PrintingTemplateLine printTemplateLine, PrintingGenFactoryContext context)
      throws AxelorException {

    TemplatePrint print = new TemplatePrint();
    BirtTemplate birtTemplate = printTemplateLine.getBirtTemplate();
    try {
      Model model = null;
      Map<String, Object> extraContext = null;
      if (context != null) {
        model = context.getModel();
        extraContext = context.getContext();
      }
      File file =
          Beans.get(PrintFromBirtTemplateService.class)
              .generateBirtTemplate(birtTemplate, model, extraContext);
      print.setOutputFormat(birtTemplate.getFormat());
      print.setPrint(renameFile(file, printTemplateLine));
    } catch (IOException e) {
      throw new AxelorException(e, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR);
    }
    return print;
  }
}
