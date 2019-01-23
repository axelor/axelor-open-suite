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

import com.axelor.meta.db.MetaJsonField;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaJsonFieldRepository;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.google.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ModelExporter {

  @Inject private FormExporter formExporter;

  @Inject private MetaJsonFieldRepository metaJsonFieldRepo;

  @Inject private MetaModelRepository metaModelRepo;

  private ExcelExporterService excelExporterService;

  public void export(ExcelExporterService excelExporterService, MetaModel model) {

    this.excelExporterService = excelExporterService;

    processModel(model);
  }

  private void processModel(MetaModel model) {
    try {
      if (model != null) {
        formExporter.export(excelExporterService, null, model.getFullName());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public Set<MetaModel> getModels() {
    List<MetaJsonField> fields =
        metaJsonFieldRepo.all().filter("self.model NOT LIKE 'com.axelor.meta.db%'").fetch();

    return fields
        .stream()
        .map(field -> metaModelRepo.all().filter("self.fullName = ?", field.getModel()).fetchOne())
        .collect(Collectors.toSet());
  }
}
