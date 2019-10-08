/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.sale.service.configurator;

import com.axelor.apps.sale.db.ConfiguratorCreator;
import com.axelor.apps.sale.db.ConfiguratorFormula;
import com.axelor.data.Listener;
import com.axelor.data.xml.XMLImporter;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaJsonField;
import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.xmlbeans.impl.common.IOUtil;

public class ConfiguratorCreatorImportServiceImpl implements ConfiguratorCreatorImportService {

  protected ConfiguratorCreatorService configuratorCreatorService;

  @Inject
  public ConfiguratorCreatorImportServiceImpl(
      ConfiguratorCreatorService configuratorCreatorService) {
    this.configuratorCreatorService = configuratorCreatorService;
  }

  private static final String CONFIG_FILE_PATH =
      "/data-import/import-configurator-creator-config.xml";

  @Transactional(rollbackOn = {IOException.class, RuntimeException.class})
  @Override
  public String importConfiguratorCreators(String filePath) throws IOException {
    return importConfiguratorCreators(filePath, CONFIG_FILE_PATH);
  }

  @Transactional(rollbackOn = {IOException.class, RuntimeException.class})
  @Override
  public String importConfiguratorCreators(String filePath, String configFilePath)
      throws IOException {
    InputStream inputStream = this.getClass().getResourceAsStream(configFilePath);
    File configFile = File.createTempFile("config", ".xml");
    FileOutputStream fout = new FileOutputStream(configFile);
    IOUtil.copyCompletely(inputStream, fout);

    Path path = MetaFiles.getPath(filePath);
    File tempDir = Files.createTempDir();
    File importFile = new File(tempDir, "configurator-creator.xml");
    Files.copy(path.toFile(), importFile);

    XMLImporter importer = new XMLImporter(configFile.getAbsolutePath(), tempDir.getAbsolutePath());
    final StringBuilder importLog = new StringBuilder();
    Listener listener =
        new Listener() {

          @Override
          public void imported(Integer imported, Integer total) {
            importLog.append("Total records: " + total + ", Total imported: " + total);
          }

          @Override
          public void imported(Model arg0) {
            try {
              completeAfterImport(arg0);
            } catch (AxelorException e) {
              importLog.append("Error in import: " + Arrays.toString(e.getStackTrace()));
            }
          }

          @Override
          public void handle(Model arg0, Exception err) {
            importLog.append("Error in import: " + Arrays.toString(err.getStackTrace()));
          }
        };

    importer.addListener(listener);

    importer.run();

    FileUtils.forceDelete(configFile);

    FileUtils.forceDelete(tempDir);

    return importLog.toString();
  }

  protected void completeAfterImport(Object arg0) throws AxelorException {
    if (arg0.getClass().equals(ConfiguratorCreator.class)) {
      completeAfterImport((ConfiguratorCreator) arg0);
    }
  }

  protected void completeAfterImport(ConfiguratorCreator creator) throws AxelorException {
    fixAttributesName(creator);
    configuratorCreatorService.updateAttributes(creator);
    configuratorCreatorService.updateIndicators(creator);
  }

  /**
   * When exported, attribute name finish with '_XX' where XX is the id of the creator. After
   * importing, we need to fix these values.
   *
   * @param creator
   * @throws AxelorException
   */
  protected void fixAttributesName(ConfiguratorCreator creator) throws AxelorException {
    List<MetaJsonField> attributes = creator.getAttributes();
    if (attributes == null) {
      return;
    }
    for (MetaJsonField attribute : attributes) {
      String name = attribute.getName();
      if (name != null && name.contains("_")) {
        attribute.setName(name.substring(0, name.lastIndexOf('_')) + '_' + creator.getId());
      }
      updateAttributeNameInFormulas(creator, name, attribute.getName());
    }
  }

  /**
   * Update the changed attribute in all formula O2M.
   *
   * @param creator
   * @param oldName
   * @param newName
   */
  protected void updateAttributeNameInFormulas(
      ConfiguratorCreator creator, String oldName, String newName) throws AxelorException {
    if (creator.getConfiguratorProductFormulaList() != null) {
      updateAttributeNameInFormulas(creator.getConfiguratorProductFormulaList(), oldName, newName);
    }
    if (creator.getConfiguratorSOLineFormulaList() != null) {
      updateAttributeNameInFormulas(creator.getConfiguratorSOLineFormulaList(), oldName, newName);
    }
  }

  /**
   * Update the changed attribute in formulas.
   *
   * @param formulas
   * @param oldAttributeName
   * @param newAttributeName
   */
  protected void updateAttributeNameInFormulas(
      List<? extends ConfiguratorFormula> formulas,
      String oldAttributeName,
      String newAttributeName) {

    formulas
        .stream()
        .forEach(
            configuratorFormula ->
                configuratorFormula.setFormula(
                    configuratorFormula.getFormula().replace(oldAttributeName, newAttributeName)));
  }
}
