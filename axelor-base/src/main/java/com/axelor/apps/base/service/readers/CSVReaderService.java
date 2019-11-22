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
package com.axelor.apps.base.service.readers;

import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.opencsv.CSVReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CSVReaderService implements DataReaderService {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private CSVReader csvReader = null;
  private List<String[]> totalRows = new ArrayList<>();
  private String fileName;

  @Override
  public boolean initialize(MetaFile input, String separator) {

    if (input == null) {
      return false;
    }

    fileName = input.getFileName().replaceAll(".csv", "");
    File inFile = MetaFiles.getPath(input).toFile();
    if (!inFile.exists()) {
      return false;
    }

    try (FileInputStream inSteam = new FileInputStream(inFile)) {

      csvReader =
          new CSVReader(
              new InputStreamReader(inSteam, StandardCharsets.UTF_8), separator.charAt(0));
      totalRows = csvReader.readAll();
      if (csvReader.getLinesRead() == 0) {
        return false;
      }
    } catch (IOException e) {
      LOG.error(e.getMessage());
      return false;
    }
    return true;
  }

  @Override
  public String[] read(String sheetName, int index, int headerSize) {

    if (csvReader == null || CollectionUtils.isEmpty(totalRows)) {
      return new String[0];
    }

    return totalRows.get(index);
  }

  @Override
  public int getTotalLines(String sheetName) {
    if (csvReader == null) {
      return 0;
    }

    return (int) csvReader.getLinesRead();
  }

  @Override
  public String[] getSheetNames() {

    if (csvReader == null || CollectionUtils.isEmpty(totalRows)) {
      return new String[0];
    }

    String[] sheets = new String[1];
    sheets[0] = fileName;

    return sheets;
  }
}
