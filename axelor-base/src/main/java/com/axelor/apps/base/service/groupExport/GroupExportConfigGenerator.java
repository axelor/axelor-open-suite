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
  private static final String CONFIG_FILE_NAME = "input-config.xml";
  private static final String ONE_TO_ONE = "ONE_TO_ONE";
  private static final String ONE_TO_MANY = "ONE_TO_MANY";

  private File config;
  private StringBuilder configBuilder;
  private AdvancedExport advancedExport;

  protected void initialize() throws AxelorException {
    try {

      // config = File.createTempFile(configFileName, ".xml");
      config = new File(CONFIG_FILE_NAME);
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
      log.debug("Error while adding config of {}", metaModel.getName());
      throw new AxelorException(TraceBackRepository.CATEGORY_INCONSISTENCY, e.getMessage());
    }

    log.debug("Adding config of {}", metaModel.getName());
    return true;
  }

  private void addHeaderInput(String fileName, MetaModel metaModel) throws AxelorException {
    List<AdvancedExportLine> exportLines = advancedExport.getAdvancedExportLineList();

    configBuilder.append("<input ");
    configBuilder.append("file=\"" + fileName + ConfigGeneratorText.QUOTE);
    configBuilder.append(" separator=\";\"");
    configBuilder.append(" type=\"" + metaModel.getFullName() + "\" ");

    if (exportLines != null && !exportLines.isEmpty()) {

      // Allowing all the non related fields for search.
      List<String> targetFieldNamesList =
          exportLines.stream()
              .map(AdvancedExportLine::getTargetField)
              .filter(it -> !it.contains("."))
              .collect(Collectors.toList());

      if (targetFieldNamesList != null && targetFieldNamesList.isEmpty()) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get("Please export some non-related fields to generate config"));
      }

      this.addSearch(true, targetFieldNamesList);
    }
    configBuilder.append(">");
  }

  private void addInnerBinding(MetaModel metaModel) throws AxelorException {

    // Removing all the non related fields
    List<String> targetFieldsList =
        CollectionUtils.emptyIfNull(advancedExport.getAdvancedExportLineList()).stream()
            .map(it -> it.getTargetField())
            .filter(it -> it.contains("."))
            .collect(Collectors.toList());

    if (targetFieldsList.isEmpty()) return;

    try {
      Mapper mapper = Mapper.of(Class.forName(metaModel.getFullName()));

      // Sort all the mapped targetFields.
      Collections.sort(targetFieldsList);

      /*
       * This variable is used to check the last bind added object and avoid repeated binding of
       * same object.
       */
      String lastBind = "";

      for (String field : targetFieldsList) {

        String mainRelatedField = field.substring(0, field.indexOf("."));

        // Only allow new object's bind.
        if (!lastBind.equals(mainRelatedField)) {

          Property property = mapper.getProperty(mainRelatedField);
          if (property.getType().toString().equals(ONE_TO_MANY)
              || property.getType().toString().equals(ONE_TO_ONE)) {

            this.setBind(
                mainRelatedField,
                targetFieldsList.stream()
                    .filter(it -> it.contains(mainRelatedField + "."))
                    .collect(Collectors.toList()),
                property.getType().toString());
            lastBind = mainRelatedField;
          }
        }
      }

    } catch (Exception e) {
      throw new AxelorException(TraceBackRepository.CATEGORY_INCONSISTENCY, e.getMessage());
    }
  }

  protected void setBind(
      String relatedField, List<String> targetFieldsList, String relationshipType)
      throws AxelorException {

    targetFieldsList =
        targetFieldsList.stream()
            .filter(it -> StringUtils.countMatches(it, ".") == 1)
            .collect(Collectors.toList());

    if (relationshipType.equals(ONE_TO_ONE)) {

      this.addDummyBindings(targetFieldsList);

      configBuilder.append(
          ConfigGeneratorText.BIND_START + ConfigGeneratorText.QUOTE + relatedField + "\" ");

      targetFieldsList = this.addSearch(false, targetFieldsList);
      configBuilder.append(">");

      for (String field : targetFieldsList) {

        configBuilder.append(ConfigGeneratorText.BIND_START);

        configBuilder.append(ConfigGeneratorText.QUOTE + field + ConfigGeneratorText.QUOTE);
        configBuilder.append(" eval=");
        configBuilder.append(ConfigGeneratorText.QUOTE + "_" + field + ConfigGeneratorText.QUOTE);
        configBuilder.append("/>");
      }

    } else {
      configBuilder.append(
          ConfigGeneratorText.BIND_START + ConfigGeneratorText.QUOTE + relatedField + "\">");

      for (String field : targetFieldsList) {
        String[] splitField = field.split("\\.");

        configBuilder.append(ConfigGeneratorText.BIND_START);
        configBuilder.append(ConfigGeneratorText.QUOTE + splitField[1] + ConfigGeneratorText.QUOTE);
        configBuilder.append(" column=");
        configBuilder.append(ConfigGeneratorText.QUOTE + field + ConfigGeneratorText.QUOTE);
        configBuilder.append("/>");
      }
    }

    configBuilder.append("\n\t</bind>");
  }

  protected void addDummyBindings(List<String> targetFieldsList) {
    for (String field : targetFieldsList) {

      configBuilder.append(ConfigGeneratorText.BIND_START);
      configBuilder.append(
          ConfigGeneratorText.QUOTE
              + "_"
              + field.substring(field.indexOf(".") + 1)
              + ConfigGeneratorText.QUOTE);
      configBuilder.append(" column=\"" + field + ConfigGeneratorText.QUOTE);
      configBuilder.append("/>");
    }
  }

  protected List<String> addSearch(boolean isHeaderInput, List<String> targetFieldsList)
      throws AxelorException {

    // For distinguishing between parentInput search and innerBinding search
    if (!isHeaderInput) {
      targetFieldsList =
          targetFieldsList.stream()
              .map(it -> it.substring(it.indexOf(".") + 1))
              .collect(Collectors.toList());
    }

    // return if no field to include in search
    if (targetFieldsList.isEmpty()) return targetFieldsList;
    StringBuilder searchBuilder = new StringBuilder();

    // Search Begins
    searchBuilder.append(" search=\"");
    for (String field : targetFieldsList) {

      if (isHeaderInput) {
        searchBuilder.append("self." + field + " = :" + field);
      } else {
        searchBuilder.append("self." + field + " = :_" + field);
      }
      searchBuilder.append(" AND ");
    }

    // Remove last 'AND'
    if (searchBuilder.lastIndexOf("AND ") > -1) {
      searchBuilder.delete(searchBuilder.lastIndexOf("AND "), searchBuilder.length());
    }

    // Search ends
    searchBuilder.append(ConfigGeneratorText.QUOTE);
    configBuilder.append(searchBuilder);

    return targetFieldsList;
  }

  /**
   * This method Close the root config tag. And write the config into the configFile.
   *
   * @return
   */
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
