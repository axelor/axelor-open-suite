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
package com.axelor.studio.service.importer;

import com.axelor.apps.message.db.Template;
import com.axelor.apps.message.db.repo.TemplateRepository;
import com.axelor.meta.MetaStore;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaModule;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.axelor.meta.schema.views.Selection.Option;
import com.axelor.studio.db.ActionBuilder;
import com.axelor.studio.db.ActionBuilderLine;
import com.axelor.studio.db.repo.ActionBuilderRepository;
import com.axelor.studio.service.ConfigurationService;
import com.axelor.studio.service.exporter.ActionExporter;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ActionImporterService {

  private Map<String, Integer> typeMap = new HashMap<String, Integer>();

  private Set<Long> actionCleared;

  @Inject private ConfigurationService configService;

  @Inject private ActionBuilderRepository actionBuilderRepo;

  @Inject private MetaFieldRepository metaFieldRepo;

  @Inject private TemplateRepository templateRepo;

  @Inject private MenuImporterService menuImporter;

  public void importActions(DataReaderService reader, String key) {

    setTypeMap();
    actionCleared = new HashSet<Long>();

    int totalLines = reader.getTotalLines(key);

    for (int ind = 0; ind < totalLines; ind++) {
      String[] row = reader.read(key, ind);
      if (row == null) {
        continue;
      }

      String module = row[ActionExporter.MODULE];
      if (module == null) {
        continue;
      }

      MetaModule metaModule = configService.getModule(module);
      if (metaModule != null) {
        importActionBuilder(row, metaModule);
      }
    }
  }

  private void setTypeMap() {

    typeMap = new HashMap<String, Integer>();

    for (Option option : MetaStore.getSelectionList("studio.action.builder.type.select")) {
      typeMap.put(option.getTitle(), Integer.parseInt(option.getValue()));
    }
  }

  @Transactional
  public void importActionBuilder(String[] values, MetaModule module) {

    String name = values[ActionExporter.NAME];
    String type = values[ActionExporter.TYPE];

    if (name == null || type == null) {
      return;
    }

    ActionBuilder builder = findCreateAction(name, type, module);

    builder.setModel(values[ActionExporter.OBJECT]);
    builder.setTargetModel(values[ActionExporter.TARGET_OBJECT]);
    if (values[ActionExporter.VIEW] != null) {
      builder = menuImporter.setActionViews(builder, values[ActionExporter.VIEW]);
    }
    builder.setFirstGroupBy(values[ActionExporter.FIRST_GROUPBY]);
    builder.setSecondGroupBy(values[ActionExporter.SECOND_GROUPBY]);
    builder.setEmailTemplate(getEmailTemplate(values[ActionExporter.EMAIL_TEMPLATE]));

    builder = actionBuilderRepo.save(builder);

    builder = importLines(values, builder);

    actionBuilderRepo.save(builder);
  }

  private ActionBuilder findCreateAction(String name, String type, MetaModule module) {

    Integer typeSelect = typeMap.get(type);
    ActionBuilder builder =
        actionBuilderRepo
            .all()
            .filter(
                "self.name = ?1 and self.metaModule = ?2 and self.typeSelect = ?3",
                name,
                module,
                typeSelect)
            .fetchOne();

    if (builder == null) {
      builder = new ActionBuilder(name);
      builder.setTypeSelect(typeSelect);
      builder.setMetaModule(module);
    }

    return builder;
  }

  private Template getEmailTemplate(String name) {

    if (name == null) {
      return null;
    }

    return templateRepo.findByName(name);
  }

  private ActionBuilder importLines(String[] values, ActionBuilder builder) {

    if (!actionCleared.contains(builder.getId())) {
      builder.clearLines();
      actionCleared.add(builder.getId());
    }

    ActionBuilderLine line = new ActionBuilderLine();

    String target = values[ActionExporter.LINE_TARGET];
    if (target != null) {
      line.setName(target);
      if (target.contains(".")) {
        target = target.split("\\.")[0];
      }
      if (builder.getModel() != null) {
        MetaField field =
            metaFieldRepo
                .all()
                .filter(
                    "self.name = ?1 and self.metaModel.fullName = ?2", target, builder.getModel())
                .fetchOne();
        line.setMetaField(field);
      }
    }

    line.setValue(values[ActionExporter.LINE_VALUE]);
    line.setConditionText(values[ActionExporter.LINE_CONDITIONS]);
    line.setFilter(values[ActionExporter.LINE_FILTERS]);
    line.setValidationMsg(values[ActionExporter.LINE_VALIDATION_MSG]);
    line.setValidationTypeSelect(values[ActionExporter.LINE_VALIDATION_TYPE]);

    builder.addLine(line);

    return builder;
  }
}
