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
package com.axelor.apps.sale.web;

import com.axelor.apps.sale.db.ConfiguratorCreator;
import com.axelor.apps.sale.db.repo.ConfiguratorCreatorRepository;
import com.axelor.apps.sale.service.configurator.ConfiguratorCreatorService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.data.Listener;
import com.axelor.data.xml.XMLImporter;
import com.axelor.db.Model;
import com.axelor.exception.service.TraceBackService;
import com.axelor.meta.MetaFiles;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.xmlbeans.impl.common.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ConfiguratorCreatorController {

  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private ConfiguratorCreatorRepository configuratorCreatorRepo;
  private ConfiguratorCreatorService configuratorCreatorService;

  @Inject
  public ConfiguratorCreatorController(
      ConfiguratorCreatorRepository configuratorCreatorRepo,
      ConfiguratorCreatorService configuratorCreatorService) {
    this.configuratorCreatorRepo = configuratorCreatorRepo;
    this.configuratorCreatorService = configuratorCreatorService;
  }

  /**
   * Called from the configurator creator form on formula changes
   *
   * @param request
   * @param response
   */
  public void updateAndActivate(ActionRequest request, ActionResponse response) {
    ConfiguratorCreator creator = request.getContext().asType(ConfiguratorCreator.class);
    creator = configuratorCreatorRepo.find(creator.getId());
    configuratorCreatorService.updateAttributes(creator);
    configuratorCreatorService.updateIndicators(creator);
    configuratorCreatorService.activate(creator);
    response.setSignal("refresh-app", true);
  }

  /**
   * Called from the configurator creator form on new
   *
   * @param request
   * @param response
   */
  public void configure(ActionRequest request, ActionResponse response) {
    ConfiguratorCreator creator = request.getContext().asType(ConfiguratorCreator.class);
    creator = configuratorCreatorRepo.find(creator.getId());
    User currentUser = AuthUtils.getUser();
    configuratorCreatorService.authorizeUser(creator, currentUser);
    try {
      configuratorCreatorService.addRequiredFormulas(creator);
    } catch (Exception e) {
      TraceBackService.trace(e);
      response.setError(e.getMessage());
    }
    response.setReload(true);
  }

  @Transactional
  public void importConfiguratorCreators(ActionRequest request, ActionResponse response) {

    String config = "/data-import/import-configurator-creator-config.xml";

    try {
      InputStream inputStream = this.getClass().getResourceAsStream(config);
      File configFile = File.createTempFile("config", ".xml");
      FileOutputStream fout = new FileOutputStream(configFile);
      IOUtil.copyCompletely(inputStream, fout);

      Path path =
          MetaFiles.getPath((String) ((Map) request.getContext().get("dataFile")).get("filePath"));
      File tempDir = Files.createTempDir();
      File importFile = new File(tempDir, "configurator-creator.xml");
      Files.copy(path.toFile(), importFile);

      XMLImporter importer =
          new XMLImporter(configFile.getAbsolutePath(), tempDir.getAbsolutePath());
      final StringBuilder importLog = new StringBuilder();
      Listener listener =
          new Listener() {

            @Override
            public void imported(Integer imported, Integer total) {
              importLog.append("Total records: " + total + ", Total imported: " + total);
            }

            @Override
            public void imported(Model arg0) {}

            @Override
            public void handle(Model arg0, Exception err) {
              importLog.append("Error in import: " + Arrays.toString(err.getStackTrace()));
            }
          };

      importer.addListener(listener);

      importer.run();

      FileUtils.forceDelete(configFile);

      FileUtils.forceDelete(tempDir);

      response.setValue("importLog", importLog.toString());

    } catch (IOException e) {
      log.error(e.getMessage());
    }
  }
}
