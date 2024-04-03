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

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.meta.schema.views.Button;
import com.axelor.meta.schema.views.FormView;
import com.axelor.meta.schema.views.GridView;
import com.axelor.rpc.Response;
import com.axelor.web.ITranslation;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.reflect.FieldUtils;

public class PrintingTemplateMetaServiceImpl implements PrintingTemplateMetaService {

  protected static final String PRINT_BTN_NAME = "printBtn";

  protected PrintingTemplateService printingTemplateService;

  @Inject
  public PrintingTemplateMetaServiceImpl(PrintingTemplateService printingTemplateService) {
    this.printingTemplateService = printingTemplateService;
  }

  @Override
  public void addPrintButton(String model, Response response) {
    if (!printingTemplateService.hasActivePrintingTemplates(model)
        || !isValidViewForToolBar(response.getData())) {
      return;
    }
    try {
      List<Button> toolbar = getToolbar(response);
      if (toolbar.stream().map(Button::getName).noneMatch(PRINT_BTN_NAME::equals)) {
        toolbar.add(getPrintBtn());
      }
    } catch (IllegalAccessException e) {
      TraceBackService.trace(e);
    }
  }

  protected List<Button> getToolbar(Response response) {
    List<Button> toolbar = new ArrayList<>();
    Object data = response.getData();
    if (data instanceof FormView) {
      FormView form = (FormView) data;
      if (form.getToolbar() == null) {
        form.setToolbar(toolbar);
      }
      toolbar = form.getToolbar();
    } else if (data instanceof GridView) {
      GridView grid = (GridView) data;
      if (grid.getToolbar() == null) {
        grid.setToolbar(toolbar);
      }
      toolbar = grid.getToolbar();
    }
    return toolbar;
  }

  protected Button getPrintBtn() throws IllegalAccessException {
    Button printBtn = new Button();
    printBtn.setName(PRINT_BTN_NAME);
    printBtn.setOnClick("action-group-print-template");
    printBtn.setTitle(ITranslation.PRINTING_TEMPLATE_PRINT_BTN);
    FieldUtils.writeField(printBtn, "icon", "fa-print", true);
    return printBtn;
  }

  protected boolean isValidViewForToolBar(Object object) {
    var classes = List.of(FormView.class, GridView.class);
    return classes.stream().anyMatch(klass -> klass.isInstance(object));
  }
}
