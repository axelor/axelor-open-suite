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

import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.MetaJsonModel;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.studio.exception.IExceptionMessage;
import com.axelor.studio.service.CommonService;
import com.axelor.studio.service.excel.importer.DataReaderService;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ExcelExporterService {

  private Map<String, String> processedModels = new HashMap<String, String>();

  private Set<String> viewProcessed = new HashSet<String>();

  private DataWriter writer;

  private String[] headers;

  private String writerKey;

  @Inject private CustomModelExporter customModelExporter;

  @Inject private ModelExporter modelExporter;

  @Inject private MenuExporter menuExporter;

  @Inject private MetaModelRepository metaModelRepo;

  public MetaFile export(String moduleName, DataWriter writer, DataReaderService reader)
      throws AxelorException {

    this.writer = writer;
    writer.initialize();
    headers = CommonService.HEADERS;

    menuExporter.export(moduleName, writer);

    processCustom();

    process();

    return this.writer.export(null);
  }

  private void processCustom() throws AxelorException {
    List<MetaJsonModel> models = customModelExporter.getCustomModels();

    for (MetaJsonModel model : models) {

      MetaModel metaModel = metaModelRepo.findByName(model.getName());

      if (metaModel != null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(IExceptionMessage.NOT_CUSTOM_MODEL),
            model.getName());
      }

      String objectName = model.getName();
      if (model.getIsReal()) {
        writerKey = objectName + "(Real)";
      } else {
        writerKey = objectName + "(Custom)";
      }
      writer.write(writerKey, null, CommonService.HEADERS);

      customModelExporter.export(this, model);

      processedModels.put(objectName, model.getTitle());
    }
  }

  private void process() {
    Set<MetaModel> models = modelExporter.getModels();

    for (MetaModel model : models) {
      String objectName = model.getName();
      if (model.getIsReal()) {
        writerKey = objectName + "(Real)";
      } else {
        writerKey = objectName + "(Custom)";
      }
      writer.write(writerKey, null, CommonService.HEADERS);

      modelExporter.export(this, model);

      processedModels.put(objectName, model.getFullName());
    }
  }

  public boolean isViewProcessed(String name) {
    return viewProcessed.contains(name);
  }

  public void addViewProcessed(String name) {
    viewProcessed.add(name);
  }

  public void writeRow(Map<String, String> valMap) {

    writer.write(writerKey, null, valMap, headers);
  }
}
