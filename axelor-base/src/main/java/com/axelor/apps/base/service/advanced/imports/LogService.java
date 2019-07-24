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
package com.axelor.apps.base.service.advanced.imports;

import com.google.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LogService {

  public static final String COMMON_KEY = "Common log";
  public static final String HEADER_COL1 = "Field names";
  public static final String HEADER_COL2 = "Row numbers";

  private String fileName;
  private Map<String, Map<String, List<Integer>>> logMap;

  @Inject private ExcelLogWriter logWriter;

  public void initialize(String fileName) {
    this.fileName = fileName;
    logMap = new LinkedHashMap<String, Map<String, List<Integer>>>();
  }

  public void addLog(String key, String log, Integer rowNumber) {
    Map<String, List<Integer>> map;
    List<Integer> list;
    if (!logMap.containsKey(key)) {
      logMap.put(key, new LinkedHashMap<String, List<Integer>>());
    }

    map = logMap.get(key);
    if (!map.containsKey(log)) {
      map.put(log, new ArrayList<Integer>());
    }

    list = map.get(log);
    if (rowNumber != null) {
      list.add(rowNumber);
    }
  }

  public void write() throws IOException {
    logWriter.initialize(fileName);
    logWriter.writeHeader(new String[] {HEADER_COL1, HEADER_COL2});
    logWriter.writeBody(logMap);
  }

  public void close() throws IOException {
    logWriter.close();
  }

  public boolean isLogGenerated() {
    if (!logMap.isEmpty()) {
      return true;
    }
    return false;
  }

  public File getLogFile() {
    return logWriter.getExcelFile();
  }
}
