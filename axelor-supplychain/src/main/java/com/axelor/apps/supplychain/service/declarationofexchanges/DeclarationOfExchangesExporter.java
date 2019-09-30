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
package com.axelor.apps.supplychain.service.declarationofexchanges;

import com.axelor.app.AppSettings;
import com.axelor.apps.supplychain.db.DeclarationOfExchanges;
import com.axelor.apps.tool.StringTool;
import com.axelor.dms.db.DMSFile;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.common.collect.ImmutableMap;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import org.apache.commons.lang3.tuple.Pair;

public abstract class DeclarationOfExchangesExporter {

  @FunctionalInterface
  private interface ThrowableSupplier<T, E extends Throwable> {

    /**
     * Get a result.
     *
     * @return a result
     * @throws Exception
     */
    T get() throws E;
  }

  protected final Map<String, ThrowableSupplier<String, AxelorException>> exportFuncMap;
  protected final DeclarationOfExchanges declarationOfExchanges;
  protected final ResourceBundle bundle;
  protected final String name;

  protected List<String> columnHeadersList;

  public DeclarationOfExchangesExporter(
      DeclarationOfExchanges declarationOfExchanges,
      ResourceBundle bundle,
      String name,
      List<String> columnHeadersList) {
    exportFuncMap = ImmutableMap.of("csv", this::exportToCSV, "pdf", this::exportToPDF);
    this.declarationOfExchanges = declarationOfExchanges;
    this.bundle = bundle;
    this.name = name;
    this.columnHeadersList = columnHeadersList;
  }

  public Pair<Path, String> export() throws AxelorException {
    Path path =
        Paths.get(
            exportFuncMap
                .getOrDefault(declarationOfExchanges.getFormatSelect(), this::exportToUnsupported)
                .get());
    return Pair.of(path, getTitle());
  }

  protected abstract String exportToCSV() throws AxelorException;

  protected abstract String exportToPDF() throws AxelorException;

  protected String exportToUnsupported() {
    throw new UnsupportedOperationException(
        String.format("Unsupported format: %s", declarationOfExchanges.getFormatSelect()));
  }

  protected String getTranslatedName() {
    return getTranslation(name);
  }

  protected String[] getTranslatedHeaders() {
    String[] headers = new String[columnHeadersList.size()];

    for (int i = 0; i < columnHeadersList.size(); ++i) {
      headers[i] = getTranslation(columnHeadersList.get(i));
    }

    return headers;
  }

  protected String getExportDir() {
    AppSettings appSettings = AppSettings.get();
    String exportDir = appSettings.get("data.export.dir");

    if (exportDir == null) {
      throw new IllegalArgumentException(I18n.get("Export directory is not configured."));
    }

    return exportDir;
  }

  protected String getTitle() {
    String translatedName = getTranslatedName();
    return String.format("%s %s", translatedName, declarationOfExchanges.getPeriod().getName());
  }

  protected String getFileName() {
    String title = getTitle();
    String filename = String.format("%s.%s", title, declarationOfExchanges.getFormatSelect());
    return StringTool.getFilename(filename);
  }

  protected Path getFilePath() {
    String fileName = getFileName();
    return Paths.get(getExportDir(), fileName);
  }

  protected String getTranslation(String text) {
    return bundle.getString(text);
  }

  protected String attach(String path) {
    DMSFile dmsFile;

    try (InputStream is = new FileInputStream(path)) {
      dmsFile = Beans.get(MetaFiles.class).attach(is, getFileName(), declarationOfExchanges);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }

    MetaFile metaFile = dmsFile.getMetaFile();
    return String.format(
        "ws/rest/com.axelor.meta.db.MetaFile/%d/content/download?v=%d/%s",
        metaFile.getId(), metaFile.getVersion(), path);
  }
}
