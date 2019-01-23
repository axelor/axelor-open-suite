/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.studio.service.excel.exporter;

import com.axelor.meta.db.MetaJsonModel;
import com.axelor.meta.db.MetaView;
import com.axelor.meta.db.repo.MetaJsonModelRepository;
import com.axelor.studio.service.builder.ViewBuilderService;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomModelExporter {

  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject private FormExporter formExporter;

  @Inject private MetaJsonModelRepository metaJsonModelRepo;

  @Inject private ViewBuilderService viewBuilderService;

  private ExcelExporterService excelExporterService;

  public void export(ExcelExporterService excelExporterService, MetaJsonModel model) {

    this.excelExporterService = excelExporterService;

    processModel(model);
  }

  private void processModel(MetaJsonModel model) {
    try {
      if (model != null) {
        MetaView formView = model.getFormView();
        if (formView == null) {
          return;
        }

        String formViewName =
            viewBuilderService.getDefaultViewName(formView.getType(), model.getName());

        if (excelExporterService.isViewProcessed(formViewName)) {
          log.debug("Form view is not considered: {}", formView);
          return;
        }
        formExporter.export(excelExporterService, formView, model.getName());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public List<MetaJsonModel> getCustomModels() {
    return metaJsonModelRepo.all().order("id").fetch();
  }
}
