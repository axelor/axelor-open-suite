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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GroupExportConfigGenerator {

  private final Logger log = LoggerFactory.getLogger(GroupExportConfigGenerator.class);
  private static final String CONFIG_FILE_NAME = "input-config.xml";
  private static final String ONE_TO_ONE = "ONE_TO_ONE";
  private static final String ONE_TO_MANY = "ONE_TO_MANY";
  private static final String MANY_TO_MANY = "MANY_TO_MANY";
  private static final String MANY_TO_ONE = "MANY_TO_ONE";

  private File config;
  private StringBuilder configBuilder;

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

    List<String> targetFieldsList =
        advancedExport.getAdvancedExportLineList().stream()
            .map(AdvancedExportLine::getTargetField)
            .collect(Collectors.toList());

    try {

      this.addHeaderInput(file.getName(), metaModel, targetFieldsList);
      this.addInnerBinding(
          1,
          metaModel,
          targetFieldsList.stream().filter(it -> it.contains(".")).collect(Collectors.toList()));
      configBuilder.append("\n</input>\n\n");

    } catch (Exception e) {
      log.debug("Error while adding config of {}", metaModel.getName());
      throw new AxelorException(TraceBackRepository.CATEGORY_INCONSISTENCY, e.getMessage());
    }

    log.debug("Adding config of {}", metaModel.getName());
    return true;
  }

  protected void addHeaderInput(String fileName, MetaModel metaModel, List<String> targetFieldsList)
      throws AxelorException {

    configBuilder.append("<input ");
    configBuilder.append("file=\"" + fileName + ConfigGeneratorText.QUOTE);
    configBuilder.append(" separator=\";\"");
    configBuilder.append(" type=\"" + metaModel.getFullName() + "\" ");

    targetFieldsList =
        targetFieldsList.stream().filter(it -> !it.contains(".")).collect(Collectors.toList());

    if (targetFieldsList != null && targetFieldsList.isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get("Please export some non-related fields to generate config"));
    }

    this.addSearch(true, targetFieldsList);
    configBuilder.append(">");
  }

  protected void addInnerBinding(
      Integer levelIndex, MetaModel metaModel, List<String> targetFieldsList)
      throws AxelorException {

    addInnerBinding(levelIndex, metaModel.getFullName(), targetFieldsList);
  }

  protected void addInnerBinding(int levelIndex, String targetName, List<String> targetFieldsList)
      throws AxelorException {

    if (targetFieldsList.isEmpty()) return;

    try {
      Mapper mapper = Mapper.of(Class.forName(targetName));

      // Sort all the mapped targetFields.
      Collections.sort(targetFieldsList);

      /*
       * This variable is used to check the last added bind's object and avoid repeated binding of
       * that object.
       */
      String lastBind = "";

      for (String field : targetFieldsList) {
        String[] splitFields = field.split("\\.");

        String mainRelatedField = splitFields[levelIndex - 1];

        // Only allow new object's bind.
        if (!lastBind.equals(mainRelatedField)) {
          lastBind = mainRelatedField;
          Property property = mapper.getProperty(mainRelatedField);

          switch (property.getType().toString()) {
            case ONE_TO_ONE:
              this.addDummyBindsAndSearch(mainRelatedField, levelIndex, targetFieldsList);
              addInnerBinding(
                  levelIndex + 1,
                  property.getTarget().getName(),
                  targetFieldsList.stream()
                      .filter(it -> it.contains(mainRelatedField + "."))
                      .collect(Collectors.toList()));
              configBuilder.append(ConfigGeneratorText.BIND_END);
              break;
            case ONE_TO_MANY:
              this.addDummyBindsAndSearch(mainRelatedField, levelIndex, targetFieldsList);
              addInnerBinding(
                  levelIndex + 1,
                  property.getTarget().getName(),
                  targetFieldsList.stream()
                      .filter(it -> it.contains(mainRelatedField + "."))
                      .collect(Collectors.toList()));
              configBuilder.append(ConfigGeneratorText.BIND_END);
              break;
            case MANY_TO_ONE:
              this.addDummyBindsAndSearch(mainRelatedField, levelIndex, targetFieldsList, true);
              configBuilder.append(ConfigGeneratorText.BIND_END);
              break;
            case MANY_TO_MANY:
              this.addDummyBindsAndSearch(mainRelatedField, levelIndex, targetFieldsList, true);
              configBuilder.append(ConfigGeneratorText.BIND_END);
              break;
            default:
              configBuilder.append(
                  ConfigGeneratorText.BIND_START
                      + ConfigGeneratorText.QUOTE
                      + lastWord(field)
                      + ConfigGeneratorText.QUOTE
                      + " eval="
                      + ConfigGeneratorText.QUOTE
                      + getDummyName(field)
                      + ConfigGeneratorText.QUOTE
                      + "/>");
              break;
          }
        }
      }

    } catch (Exception e) {
      throw new AxelorException(TraceBackRepository.CATEGORY_INCONSISTENCY, e.getMessage());
    }
  }

  protected void addDummyBindsAndSearch(
      String relatedField, Integer levelIndex, List<String> targetFieldsList)
      throws AxelorException {
    addDummyBindsAndSearch(relatedField, levelIndex, targetFieldsList, false);
  }

  protected void addDummyBindsAndSearch(
      String relatedField, Integer levelIndex, List<String> targetFieldsList, boolean isUpdate)
      throws AxelorException {

    targetFieldsList =
        targetFieldsList.stream()
            .filter(
                it ->
                    it.contains(relatedField + ".")
                        && StringUtils.countMatches(it, ".") == levelIndex)
            .collect(Collectors.toList());

    addDummyBindings(targetFieldsList);

    configBuilder.append(
        ConfigGeneratorText.BIND_START
            + ConfigGeneratorText.QUOTE
            + relatedField
            + ConfigGeneratorText.QUOTE);

    addSearch(false, targetFieldsList);

    if (isUpdate) {
      configBuilder.append(" update=\"" + isUpdate + ConfigGeneratorText.QUOTE);
    }

    configBuilder.append(">");
  }

  protected void addDummyBindings(List<String> targetFieldsList) {

    StringBuilder dummyBindingBuilder = new StringBuilder();

    for (String field : targetFieldsList) {

      dummyBindingBuilder.append(ConfigGeneratorText.BIND_START);
      dummyBindingBuilder.append(
          ConfigGeneratorText.QUOTE + getDummyName(field) + ConfigGeneratorText.QUOTE);
      dummyBindingBuilder.append(" column=\"" + field + ConfigGeneratorText.QUOTE);
      dummyBindingBuilder.append("/>");
    }
    configBuilder.append(dummyBindingBuilder);
  }

  /**
   * This method add search to binds.
   *
   * @return
   */
  protected void addSearch(boolean isHeaderInput, List<String> targetFieldsList)
      throws AxelorException {

    // return if no field to include in search
    if (targetFieldsList.isEmpty()) return;
    StringBuilder searchBuilder = new StringBuilder();

    // Search Begins
    searchBuilder.append(" search=\"");

    if (isHeaderInput) {
      for (String field : targetFieldsList) {
        searchBuilder.append("self." + field + " = :" + field + " AND ");
      }

    } else {
      for (String field : targetFieldsList) {
        searchBuilder.append("self." + lastWord(field) + " = :" + getDummyName(field) + " AND ");
      }
    }

    // Remove last 'AND'
    if (searchBuilder.lastIndexOf("AND ") > -1) {
      searchBuilder.delete(searchBuilder.lastIndexOf("AND "), searchBuilder.length());
    }

    // Search ends
    searchBuilder.append(ConfigGeneratorText.QUOTE);
    configBuilder.append(searchBuilder);
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
  }

  public File getConfigFile() {
    return config;
  }

  private String lastWord(String field) {
    return field.substring(field.lastIndexOf(".") + 1);
  }

  private String getDummyName(String field) {
    return field.replace(".", "_");
  }
}
