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
import com.axelor.meta.MetaFiles;
import com.axelor.studio.db.AppBuilder;
import com.axelor.studio.db.repo.AppBuilderRepository;
import com.google.common.base.Joiner;
import com.google.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.FilenameUtils;

public class ModuleExportAppBuilderService {

  private static final String[] APP_HEADER =
      new String[] {"code", "name", "description", "imagePath", "sequence", "depends"};

  @Inject private AppBuilderRepository appBuilderRepo;

  @Inject private ModuleExportDataInitService moduleExportDataInitService;

  @Inject private ModuleExportService moduleExportService;

  public void addAppBuilders(String modulePrefix, ZipOutputStream zipOut, CSVConfig csvConfig)
      throws IOException {

    // FIXME: There should be a filter on records but have to include all app builders due to
    // workflow.
    //		    List<AppBuilder> appBuilders = appBuilderRepo.all().filter("self.isReal =
    // false").fetch();

    List<AppBuilder> appBuilders = appBuilderRepo.all().fetch();

    if (appBuilders.isEmpty()) {
      return;
    }

    List<String[]> data = new ArrayList<>();

    String fileName = modulePrefix + AppBuilder.class.getSimpleName() + ".csv";

    for (AppBuilder appBuilder : appBuilders) {
      createAppRecord(appBuilder, data, zipOut);
    }

    CSVInput input =
        moduleExportDataInitService.createCSVInput(
            fileName,
            AppBuilder.class.getName(),
            "com.axelor.studio.csv.script.ImportAppBuilder:importAppBuilder",
            "self.code = :code",
            false);

    CSVBind bind =
        moduleExportDataInitService.createCSVBind(
            "depends", "dependsOnSet", "self.code in :depends", "depends.split('|') as List", true);
    input.getBindings().add(bind);
    csvConfig.getInputs().add(input);

    moduleExportDataInitService.addCsv(zipOut, fileName, APP_HEADER, data);
  }

  private void createAppRecord(AppBuilder appBuilder, List<String[]> data, ZipOutputStream zipOut)
      throws IOException {

    String imageName = null;
    if (appBuilder.getImage() != null) {
      imageName = addImage(appBuilder, zipOut);
      ;
    }

    List<String> dependsOn =
        appBuilder.getDependsOnSet().stream().map(it -> it.getCode()).collect(Collectors.toList());

    String[] record =
        new String[] {
          appBuilder.getCode(),
          appBuilder.getName(),
          appBuilder.getDescription(),
          imageName,
          appBuilder.getSequence().toString(),
          Joiner.on("|").join(dependsOn)
        };

    data.add(record);
  }

  private String addImage(AppBuilder appBuilder, ZipOutputStream zipOut) throws IOException {

    File imageFile = MetaFiles.getPath(appBuilder.getImage()).toFile();
    String extension = FilenameUtils.getExtension(imageFile.getName());
    String imageName = "app-" + appBuilder.getCode() + "." + extension;
    moduleExportService.addZipEntry(
        ModuleExportDataInitService.DATA_INIT_DIR + "img/" + imageName, imageFile, zipOut);

    return imageName;
  }
}
