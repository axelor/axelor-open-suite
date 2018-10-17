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
package com.axelor.studio.service.module;

import com.axelor.data.csv.CSVBind;
import com.axelor.data.csv.CSVConfig;
import com.axelor.data.csv.CSVInput;
import com.axelor.meta.db.MetaJsonModel;
import com.axelor.meta.db.repo.MetaJsonModelRepository;
import com.axelor.studio.db.ViewBuilder;
import com.axelor.studio.db.ViewItem;
import com.axelor.studio.db.repo.ViewBuilderRepo;
import com.google.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipOutputStream;

public class ModuleExportViewBuilderService {

  @Inject private ViewBuilderRepo viewBuilderRepo;

  @Inject private ModuleExportDataInitService moduleExportDataInitService;

  @Inject private MetaJsonModelRepository metaJsonModelRepository;

  private static final String[] VIEW_BUILDER_HEADER =
      new String[] {
        "title",
        "name",
        "model",
        "isJson",
        "viewType",
        "onNew",
        "onLoad",
        "editable",
        "columnBy",
        "sequenceBy",
        "recordLimit",
        "customSearch",
        "freeSearchSelect",
        "draggable",
        "eventStart",
        "eventStop",
        "eventLength",
        "colorBy",
        "modeSelect"
      };

  private static final String[] VIEW_ITEM_HEADER =
      new String[] {"name", "sequence", "viewBuilder_name"};

  public void addViewBuilders(String modulePrefix, ZipOutputStream zipOut, CSVConfig csvConfig)
      throws IOException {

    List<ViewBuilder> viewBuilders = viewBuilderRepo.all().fetch();

    List<String[]> data = new ArrayList<>();
    List<ViewItem> viewItems = new ArrayList<>();
    for (ViewBuilder viewBuilder : viewBuilders) {
      data.add(
          new String[] {
            viewBuilder.getTitle(),
            viewBuilder.getName(),
            viewBuilder.getModel(),
            isExportedReal(viewBuilder),
            viewBuilder.getViewType(),
            viewBuilder.getOnNew(),
            viewBuilder.getOnLoad(),
            viewBuilder.getEditable().toString(),
            viewBuilder.getColumnBy(),
            viewBuilder.getSequenceBy(),
            viewBuilder.getRecordLimit().toString(),
            viewBuilder.getCustomSearch().toString(),
            viewBuilder.getFreeSearchSelect(),
            viewBuilder.getDraggable().toString(),
            viewBuilder.getEventStart(),
            viewBuilder.getEventStop(),
            viewBuilder.getEventLength().toString(),
            viewBuilder.getColorBy(),
            viewBuilder.getModeSelect()
          });
      viewItems.addAll(viewBuilder.getViewItemList());
    }

    String fileName = modulePrefix + ViewBuilder.class.getSimpleName() + ".csv";
    CSVInput input =
        moduleExportDataInitService.createCSVInput(
            fileName, ViewBuilder.class.getName(), null, "self.name = :name");
    csvConfig.getInputs().add(input);

    moduleExportDataInitService.addCsv(zipOut, fileName, VIEW_BUILDER_HEADER, data);

    addViewBuilderItems(modulePrefix, zipOut, csvConfig, viewItems);
  }

  private String isExportedReal(ViewBuilder viewBuilder) {

    if (viewBuilder.getIsJson()) {
      MetaJsonModel metaJsonModel = metaJsonModelRepository.findByName(viewBuilder.getModel());
      if (metaJsonModel != null && metaJsonModel.getIsReal()) {
        return "false";
      }
      return "true";
    }

    return "false";
  }

  private void addViewBuilderItems(
      String modulePrefix, ZipOutputStream zipOut, CSVConfig csvConfig, List<ViewItem> viewItems)
      throws IOException {

    List<String[]> data = new ArrayList<>();
    for (ViewItem viewItem : viewItems) {
      data.add(
          new String[] {
            viewItem.getName(),
            viewItem.getSequence().toString(),
            viewItem.getViewBuilder().getName()
          });
    }

    String fileName = modulePrefix + ViewItem.class.getSimpleName() + ".csv";

    CSVInput input =
        moduleExportDataInitService.createCSVInput(
            fileName,
            ViewItem.class.getName(),
            null,
            "self.name = :name and self.viewBuilder.name = :viewBuilder_name");
    csvConfig.getInputs().add(input);

    CSVBind bind =
        moduleExportDataInitService.createCSVBind(
            "viewBuilder_name", "viewBuilder", "self.name = :viewBuilder_name", null, true);
    input.getBindings().add(bind);

    moduleExportDataInitService.addCsv(zipOut, fileName, VIEW_ITEM_HEADER, data);
  }
}
