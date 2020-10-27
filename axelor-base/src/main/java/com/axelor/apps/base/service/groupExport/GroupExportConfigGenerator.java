/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.groupExport;

import com.axelor.apps.base.db.AdvancedExport;
import com.axelor.apps.base.db.AdvancedExportLine;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaModel;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GroupExportConfigGenerator {

  private final Logger log = LoggerFactory.getLogger(GroupExportConfigGenerator.class);

  private File config;
  private StringBuilder configBuilder;
  private static final String configFileName = "input-config.xml";
  private AdvancedExport advancedExport;

  protected void initialize() throws AxelorException {
    try {

      // config = File.createTempFile(configFileName, ".xml");
      config = new File(configFileName);
      configBuilder = new StringBuilder();
      configBuilder.append(ConfigGeneratorText.CONFIG_START);

    } catch (Exception e) {
      TraceBackService.trace(e);
      throw new AxelorException(e, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR);
    }
  }

  protected boolean addFileConfig(File file, AdvancedExport advancedExport) throws AxelorException {

    MetaModel metaModel = advancedExport.getMetaModel();
    this.advancedExport = advancedExport;

    try {

      this.addHeaderInput(file.getName(), metaModel);
      this.addInnerBinding(metaModel);
      configBuilder.append("\n</input>\n\n");

    } catch (Exception e) {
      log.debug("Error while adding config of {}", advancedExport.getMetaModel().getName());
      throw new AxelorException(TraceBackRepository.CATEGORY_INCONSISTENCY, e.getMessage());
    }

    log.debug("Adding config of {}", advancedExport.getMetaModel().getName());
    return true;
  }

  private void addHeaderInput(String fileName, MetaModel metaModel) throws AxelorException {
    List<AdvancedExportLine> exportLines = advancedExport.getAdvancedExportLineList();

    configBuilder.append("<input ");
    configBuilder.append("file=\"" + fileName + "\" ");
    configBuilder.append("separator=\";\" ");
    configBuilder.append("type=\"" + metaModel.getFullName() + "\" ");

    if (exportLines != null && !exportLines.isEmpty()) {
      List<String> targetFieldNamesList =
          exportLines.stream().map(AdvancedExportLine::getTargetField).collect(Collectors.toList());

      this.addSearch(null, targetFieldNamesList);
    }
    configBuilder.append(">");
  }

  private void addInnerBinding(MetaModel metaModel) throws AxelorException {

    List<String> targetFieldsList =
        CollectionUtils.emptyIfNull(advancedExport.getAdvancedExportLineList()).stream()
            .map(it -> it.getTargetField())
            .filter(it -> it.contains("."))
            .collect(Collectors.toList());

    if (targetFieldsList.isEmpty()) return;

    try {
      Mapper mapper = Mapper.of(Class.forName(metaModel.getFullName()));

      // Sort all the mapped targetFields
      Collections.sort(targetFieldsList);
      String lastBind = "";
      for (String field : targetFieldsList) {

        String[] splitField = field.split("\\.");

        if (!lastBind.equals(splitField[0])) {

          Property property = mapper.getProperty(splitField[0]);
          if (property.getType().toString().equals("ONE_TO_MANY")
              || property.getType().toString().equals("ONE_TO_ONE")) {

            this.setBind(
                splitField[0],
                targetFieldsList.stream()
                    .filter(it -> it.contains(splitField[0] + "."))
                    .collect(Collectors.toList()));
            lastBind = splitField[0];
          }
        }
      }

    } catch (Exception e) {
      throw new AxelorException(TraceBackRepository.CATEGORY_INCONSISTENCY, e.getMessage());
    }
  }

  protected void setBind(String relatedField, List<String> targetFieldsList)
      throws AxelorException {

    configBuilder.append("\n\t<bind to=\"" + relatedField + "\">");

    for (String field : targetFieldsList) {
      String[] splitField = field.split("\\.");

      if (splitField.length == 2) {
        configBuilder.append("\n\t<bind to=");
        configBuilder.append("\"" + splitField[1] + "\"");
        configBuilder.append(" column=");
        configBuilder.append("\"" + field + "\"");
        configBuilder.append("/>");
      }
    }
    configBuilder.append("\n\t</bind>");
  }

  protected void addSearch(String[] splitField, List<String> targetFieldsList)
      throws AxelorException {

    // For distinguishing between parentInput search and innerBinding search
    if (splitField != null) {
      targetFieldsList =
          targetFieldsList.stream()
              .filter(
                  it -> it.contains(splitField[0] + ".") && StringUtils.countMatches(it, ".") == 1)
              .map(it -> it.substring(it.indexOf(".") + 1))
              .filter(it -> !it.contains("."))
              .collect(Collectors.toList());
    } else {
      List<String> tempTargetFieldsList =
          targetFieldsList.stream().filter(it -> !it.contains(".")).collect(Collectors.toList());
      if (tempTargetFieldsList.isEmpty() && !targetFieldsList.isEmpty()) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get("Please export some non-related fields to generate config"));
      }
      targetFieldsList = tempTargetFieldsList;
    }

    // return if no field to include in search
    if (targetFieldsList.isEmpty()) return;
    StringBuilder searchBuilder = new StringBuilder();

    // Search Begins
    searchBuilder.append("search=\"");
    for (String field : targetFieldsList) {
      if (splitField != null && splitField.length != 0)
        searchBuilder.append("self." + field + " = :" + splitField[0] + "." + field);
      else searchBuilder.append("self." + field + " = :" + field);

      searchBuilder.append(" AND ");
    }
    if (searchBuilder.lastIndexOf("AND ") > -1) {
      searchBuilder.delete(searchBuilder.lastIndexOf("AND "), searchBuilder.length());
    }

    // Search ends
    searchBuilder.append("\"");
    configBuilder.append(searchBuilder);
  }

  protected boolean checkSearch(String[] splitField, Mapper mapper) {
    Property property = mapper.getProperty(splitField[0]);
    if (property.getType() == null) return false;
    // System.err.println(property.getType());
    /*
     * if (property.getType().toString().equals("MANY_TO_ONE") ||
     * property.getType().toString().equals("MANY_TO_MANY")) { return true; }
     */
    return false;
  }

  protected void setBind() {

    configBuilder.append("\n\t<bind to=\"");
    configBuilder.append("");
  }

  protected void endConfig() throws AxelorException {
    configBuilder.append(ConfigGeneratorText.CONFIG_END);

    String content = configBuilder.toString();

    if (!content.isEmpty()) {
      try (FileWriter fileWriter = new FileWriter(config)) {

        fileWriter.write(content);
        fileWriter.close();
      } catch (IOException e) {
        TraceBackService.trace(e);
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get("Config file is not found."));
      }
    }
    advancedExport = null;
  }

  public File getConfigFile() {
    return config;
  }
}
