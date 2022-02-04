/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.studio.service;

import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.MetaScanner;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.meta.db.MetaJsonModel;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.axelor.meta.db.repo.MetaJsonFieldRepository;
import com.axelor.meta.db.repo.MetaJsonModelRepository;
import com.axelor.studio.db.ActionBuilder;
import com.axelor.studio.db.AppBuilder;
import com.axelor.studio.db.AppLoader;
import com.axelor.studio.db.ChartBuilder;
import com.axelor.studio.db.DashboardBuilder;
import com.axelor.studio.db.MenuBuilder;
import com.axelor.studio.db.SelectionBuilder;
import com.axelor.studio.db.repo.ActionBuilderRepository;
import com.axelor.studio.db.repo.AppBuilderRepository;
import com.axelor.studio.db.repo.AppLoaderRepository;
import com.axelor.studio.db.repo.ChartBuilderRepository;
import com.axelor.studio.db.repo.DashboardBuilderRepository;
import com.axelor.studio.db.repo.MenuBuilderRepository;
import com.axelor.studio.db.repo.SelectionBuilderRepository;
import com.google.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.xml.bind.JAXBException;
import org.apache.commons.io.IOUtils;

public class ImportService {

  @Inject private ChartBuilderRepository chartBuilderRepo;

  @Inject private MetaJsonModelRepository metaJsonModelRepo;

  @Inject private MetaJsonFieldRepository metaJsonFieldRepo;

  @Inject private SelectionBuilderRepository selectionBuilderRepo;

  @Inject private DashboardBuilderRepository dashboardBuilderRepo;

  @Inject private MenuBuilderRepository menuBuilderRepo;

  @Inject private ActionBuilderRepository actionBuilderRepo;

  @Inject private AppBuilderRepository appBuilderRepo;

  @Inject private MetaFiles metaFiles;

  @Inject private MetaFileRepository metaFileRepo;

  @Inject private AppLoaderRepository appLoaderRepository;

  public Object importMetaJsonModel(Object bean, Map<String, Object> values) {

    assert bean instanceof MetaJsonModel;

    return metaJsonModelRepo.save((MetaJsonModel) bean);
  }

  public Object importMetaJsonField(Object bean, Map<String, Object> values) {

    assert bean instanceof MetaJsonField;

    return metaJsonFieldRepo.save((MetaJsonField) bean);
  }

  public Object importSelectionBuilder(Object bean, Map<String, Object> values)
      throws JAXBException, AxelorException {

    assert bean instanceof SelectionBuilder;

    return selectionBuilderRepo.save((SelectionBuilder) bean);
  }

  public Object importChartBuilder(Object bean, Map<String, Object> values)
      throws JAXBException, AxelorException {

    assert bean instanceof ChartBuilder;

    return chartBuilderRepo.save((ChartBuilder) bean);
  }

  public Object importDashboardBuilder(Object bean, Map<String, Object> values) {

    assert bean instanceof DashboardBuilder;

    return dashboardBuilderRepo.save((DashboardBuilder) bean);
  }

  public Object importMenuBuilder(Object bean, Map<String, Object> values) {

    assert bean instanceof MenuBuilder;

    return menuBuilderRepo.save((MenuBuilder) bean);
  }

  public Object importActionBuilder(Object bean, Map<String, Object> values) {

    assert bean instanceof ActionBuilder;

    return actionBuilderRepo.save((ActionBuilder) bean);
  }

  public Object importAppBuilderImg(Object bean, Map<String, Object> values) {

    assert bean instanceof AppBuilder;

    AppBuilder appBuilder = (AppBuilder) bean;
    String fileName = (String) values.get("fileName");
    String imageData = (String) values.get("imageData");

    if (fileName != null && imageData != null) {
      appBuilder.setImage(importImg(fileName, imageData));
    }

    appBuilder = appBuilderRepo.save(appBuilder);

    return appBuilder;
  }

  public Object importAppBuilder(Object bean, Map<String, Object> values) {

    assert bean instanceof AppBuilder;

    AppBuilder appBuilder = (AppBuilder) bean;

    appBuilder = appBuilderRepo.save(appBuilder);

    Long appLoaderId = (Long) values.get("appLoaderId");

    if (appLoaderId != null) {
      appLoaderRepository.find(appLoaderId).addImportedAppBuilderSetItem(appBuilder);
    }

    return appBuilder;
  }

  // Import methods specific for import from AppBuilder
  private MetaFile importImg(String name, String data) {

    if (data == null) {
      return null;
    }

    byte[] img = Base64.getDecoder().decode(data);

    ByteArrayInputStream inImg = new ByteArrayInputStream(img);

    MetaFile metaFile = metaFileRepo.all().filter("self.fileName = ?1", name).fetchOne();

    try {
      if (metaFile != null) {
        return metaFiles.upload(inImg, metaFile);
      } else {
        return metaFiles.upload(inImg, name);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return null;
  }

  public MetaJsonField importJsonModelField(Object bean, Map<String, Object> values) {

    assert bean instanceof MetaJsonField;

    MetaJsonField field = (MetaJsonField) bean;

    if (field.getJsonModel() == null) {
      return null;
    }

    return metaJsonFieldRepo.save(field);
  }

  public MetaJsonField importJsonField(Object bean, Map<String, Object> values) {

    assert bean instanceof MetaJsonField;

    MetaJsonField field = (MetaJsonField) bean;

    if (field.getJsonModel() != null) {
      return null;
    }

    return field;
  }

  public Object importAppMetaJsonModel(Object bean, Map<String, Object> values) {

    assert bean instanceof MetaJsonModel;

    MetaJsonModel model = (MetaJsonModel) bean;

    JPA.flush();

    JPA.refresh(model);

    return metaJsonModelRepo.save(model);
  }

  public Object importAppDashboardBuilder(Object bean, Map<String, Object> values) {

    assert bean instanceof DashboardBuilder;

    DashboardBuilder dashboard = (DashboardBuilder) bean;

    JPA.flush();

    JPA.refresh(dashboard);

    return dashboardBuilderRepo.save(dashboard);
  }

  public Object importAppLoader(Object bean, Map<String, Object> values) throws Exception {

    assert bean instanceof AppLoader;

    AppLoader appLoader = (AppLoader) bean;

    String importPath = (String) values.get("importFilePath");

    if (importPath != null) {
      File importZipFile = createAppLoaderImportZip(importPath);
      if (importZipFile != null) {
        appLoader.setImportMetaFile(metaFiles.upload(importZipFile));
      }
    }

    return appLoader;
  }

  public File createAppLoaderImportZip(String importPath) {

    importPath = importPath.replaceAll("/|\\\\", "(/|\\\\\\\\)");
    List<URL> fileUrls = MetaScanner.findAll(importPath);

    if (fileUrls.isEmpty()) {
      return null;
    }

    try {
      File zipFile = MetaFiles.createTempFile("app-", ".zip").toFile();
      ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile));
      for (URL url : fileUrls) {
        File file = new File(url.getFile());
        ZipEntry zipEntry = new ZipEntry(file.getName());
        zipOutputStream.putNextEntry(zipEntry);
        IOUtils.copy(url.openStream(), zipOutputStream);
      }
      zipOutputStream.close();

      return zipFile;
    } catch (IOException e) {
      e.printStackTrace();
    }

    return null;
  }
}
