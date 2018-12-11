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
package com.axelor.studio.service;

import com.axelor.app.AppSettings;
import com.axelor.common.FileUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaModule;
import com.axelor.meta.db.repo.MetaModuleRepository;
import com.google.inject.Inject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service provide configuration support for creator. It will check build directory path and create
 * custom module's directory structure.
 *
 * @author axelor
 */
public class ConfigurationService {

  protected Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject private MetaModuleRepository moduleRepo;

  private List<String> installedModules = null;

  public File getDomainDir(String module, boolean create) throws AxelorException {

    File moduleDir = getModuleDir(module, create);

    return getDir(getResourceDir(moduleDir, create), "domains");
  }

  public File getViewDir(String module, boolean create) throws AxelorException {

    File moduleDir = getModuleDir(module, create);

    return getDir(getResourceDir(moduleDir, create), "views");
  }

  public File getTranslationDir(String module, boolean create) throws AxelorException {

    File moduleDir = getModuleDir(module, create);

    return getDir(getResourceDir(moduleDir, create), "i18n");
  }

  /**
   * Method to get build directory from property setting.
   *
   * @return
   */
  private File getBuildDirectory() {

    String buildPath = AppSettings.get().get("creator.source.dir");

    if (buildPath != null) {
      File buildDir = new File(buildPath);
      if (buildDir.exists() && buildDir.isDirectory()) {
        return buildDir;
      }
    }

    return null;
  }

  /**
   * Create resource directory structure(with src,main,resource) inside given custom module
   * directory.
   *
   * @param moduleDir Custom module directory.
   * @return Resource directory file.
   */
  private File getResourceDir(File moduleDir, boolean create) {

    if (!moduleDir.exists()) {
      return null;
    }

    File resourceDir = FileUtils.getFile(moduleDir, "src", "main", "resources");

    if (!create) {
      return resourceDir;
    }

    if (!resourceDir.exists()) {
      resourceDir.mkdirs();
    }

    return resourceDir;
  }

  /**
   * Create directory inside given directory. Used for 'views' and 'domains' directory creation.
   *
   * @param resourceDir Resource directory.
   * @param rootName Name of directory to create
   * @return New Directory file created.
   * @throws IOException Exception thrown in directory creation.
   */
  private File getDir(File resourceDir, String rootName) {

    if (resourceDir == null || !resourceDir.exists()) {
      return null;
    }

    File rootDir = FileUtils.getFile(resourceDir, rootName);

    if (!rootDir.exists()) {
      rootDir.mkdir();
    }

    return rootDir;
  }

  /**
   * Create custom module directory inside root build directory given in configuration. It also
   * create build file(build.gradle) for custom module.
   *
   * @param buildDir Build directory file.
   * @return Custom module directory created.
   * @throws AxelorException
   */
  public File getModuleDir(String moduleName, boolean create) throws AxelorException {

    File buildDir = getBuildDirectory();
    if (buildDir == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, I18n.get("Build directory not exist"));
    }

    File moduleDir = FileUtils.getFile(buildDir, "modules", "custom");

    if (!moduleDir.exists()) {
      moduleDir.mkdir();
    }

    moduleDir = FileUtils.getFile(buildDir, "modules", "custom", moduleName);

    if (!create) {
      return moduleDir;
    }

    if (!moduleDir.exists()) {
      validateModuleName(moduleName);
      moduleDir.mkdir();
    }

    File buildFile = FileUtils.getFile(moduleDir, "build.gradle");

    createBuildFile(buildFile, moduleName);

    return moduleDir;
  }

  /**
   * Method to write build file(build.gradle) content for custom module.
   *
   * @param buildFile Blank buildFile.
   * @throws AxelorException
   */
  private void createBuildFile(File buildFile, String moduleName) throws AxelorException {

    try {

      MetaModule module = getModule(moduleName);

      if (module == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_NO_VALUE,
            I18n.get("No module found with name %s"),
            moduleName);
      }

      FileWriter fw = new FileWriter(buildFile);
      StringBuffer buildText =
          new StringBuffer("apply plugin: \"com.axelor.app-module\"\n")
              .append("apply {\n\t version = \"")
              .append(module.getVersion())
              .append("\"\n}\n")
              .append("axelor { ")
              .append("\n\t title \"")
              .append(moduleName.toUpperCase())
              .append("\"\n\t description \"Module generated by axelor creator\"")
              .append("\n\t removable false\n}");

      Set<MetaModule> depends = module.getDepends();

      if (depends != null) {
        buildText.append("\ndependencies {");
        for (MetaModule depend : depends) {
          if (depend.getName().equals("axelor-core")) {
            continue;
          }
          buildText
              .append("\n\t compile project(\":modules:")
              .append(depend.getName())
              .append("\")");
        }
        buildText.append("\n}");
      }

      fw.write(buildText.toString());
      fw.close();
    } catch (IOException e) {
      throw new AxelorException(e, 4);
    }
  }

  public void removeDomainFile(String fileName, String module) throws AxelorException {

    if (module != null && fileName != null) {
      File domainDir = getDomainDir(module, false);
      if (domainDir != null) {
        File domainFile = FileUtils.getFile(domainDir, fileName);
        if (domainFile.exists()) {
          domainFile.delete();
        }
      }
    }
  }

  public void removeDomainFile(String fileName) throws AxelorException {

    if (fileName == null) {
      return;
    }

    for (MetaModule module : moduleRepo.all().fetch()) {
      File domainDir = getDomainDir(module.getName(), false);
      if (domainDir != null) {
        File domainFile = FileUtils.getFile(domainDir, fileName);
        if (domainFile.exists()) {
          domainFile.delete();
        }
      }
    }
  }

  public void removeViewFile(String fileName, String module) throws AxelorException {

    if (module != null && fileName != null) {
      File viewDir = getViewDir(module, false);
      if (viewDir != null) {
        File viewFile = FileUtils.getFile(viewDir, fileName);
        if (viewFile.exists()) {
          viewFile.delete();
        }
      }
    }
  }

  public void removeViewFile(String fileName) throws AxelorException {

    if (fileName == null) {
      return;
    }

    for (MetaModule module : moduleRepo.all().fetch()) {
      File viewDir = getViewDir(module.getName(), false);
      if (viewDir != null) {
        File viewFile = FileUtils.getFile(viewDir, fileName);
        if (viewFile.exists()) {
          viewFile.delete();
        }
      }
    }
  }

  public List<MetaModule> getCustomizedModules() {

    return moduleRepo.all().filter("self.customised = true").fetch();
  }

  public List<String> getCustomizedModuleNames() {

    List<String> modules = new ArrayList<String>();

    for (MetaModule module : moduleRepo.all().filter("self.customised = true").fetch()) {
      modules.add(module.getName());
    }

    return modules;
  }

  public MetaModule getModule(String name) {

    MetaModule module = null;
    if (name != null) {
      module = moduleRepo.all().filter("self.name = ?1", name).fetchOne();
    }

    return module;
  }

  public List<String> getInstalledModules() {

    if (installedModules == null || installedModules.isEmpty()) {
      installedModules = new ArrayList<String>();
      List<MetaModule> modules = moduleRepo.all().filter("self.installed = true").fetch();
      for (MetaModule module : modules) {
        installedModules.add(module.getName());
      }
    }

    return installedModules;
  }

  public void validateModuleName(String name) throws AxelorException {

    if (name == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get("Blank module name not allowed."));
    }

    if (!name.startsWith("axelor-")) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get("Module name must starts with axelor-"));
    }

    if (!name.matches("([a-zA-Z0-9]+-[a-zA-Z0-9]+)+")) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get("Please follow standard module naming convension"));
    }
  }

  public void validateFieldName(String name) throws AxelorException {

    if (name == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get("Blank field name not allowed."));
    }

    if (!name.matches("([a-z][a-zA-Z0-9_]+)|([A-Z][A-Z0-9_]+)")) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get("Please follow standard field naming convension"));
    }
  }

  public void validateModelName(String name) throws AxelorException {

    if (name == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get("Blank model name not allowed."));
    }

    if (!name.matches("[A-Z][a-zA-Z0-9_]+")) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get("Please follow standard model naming convention"));
    }
  }
}
