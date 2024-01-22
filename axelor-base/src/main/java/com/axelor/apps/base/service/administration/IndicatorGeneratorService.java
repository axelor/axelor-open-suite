/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.service.administration;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.IndicatorGenerator;
import com.axelor.apps.base.db.repo.IndicatorGeneratorRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.utils.file.CsvTool;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.persistence.Query;
import org.hibernate.transform.BasicTransformerAdapter;

public class IndicatorGeneratorService {

  @Inject private IndicatorGeneratorRepository indicatorGeneratorRepo;

  protected static final String QUERY_RESULT_CSV_FILE_NAME = "query_result";

  @Transactional(rollbackOn = {Exception.class})
  public String run(IndicatorGenerator indicatorGenerator) throws AxelorException {

    String log = "";

    int requestType = indicatorGenerator.getRequestLanguage();

    String request = indicatorGenerator.getRequest();

    if (request == null || request.isEmpty()) {
      log =
          String.format(
              I18n.get(BaseExceptionMessage.INDICATOR_GENERATOR_1), indicatorGenerator.getCode());
    }

    String result = "";

    try {
      if (request != null && !request.isEmpty()) {
        List<Map<String, Object>> requestResultList =
            this.getRequestResultList(request, requestType);

        result = this.generateQueryResultTable(requestResultList);
      }
    } catch (Exception e) {

      log +=
          String.format(
              I18n.get(BaseExceptionMessage.INDICATOR_GENERATOR_2), indicatorGenerator.getCode());
    }

    indicatorGenerator.setLog(log);

    indicatorGenerator.setResult(result);

    indicatorGeneratorRepo.save(indicatorGenerator);

    return result;
  }

  @SuppressWarnings("unchecked")
  public List<Map<String, Object>> getRequestResultList(String request, int requestType) {
    Query query = null;

    if (requestType == 0) {
      query = JPA.em().createNativeQuery(request);
    } else {
      query = JPA.em().createQuery(request);
    }

    transformQueryResult(query);

    return query.getResultList();
  }

  @SuppressWarnings("deprecation")
  protected void transformQueryResult(Query query) {
    query.unwrap(org.hibernate.query.Query.class).setResultTransformer(new DataSetTransformer());
  }

  @SuppressWarnings("serial")
  private static final class DataSetTransformer extends BasicTransformerAdapter {

    @Override
    public Object transformTuple(Object[] tuple, String[] aliases) {
      Map<String, Object> result = new LinkedHashMap<>(tuple.length);
      for (int i = 0; i < tuple.length; ++i) {
        String alias = aliases[i];
        if (alias != null) {
          result.put(alias, tuple[i]);
        }
      }
      return result;
    }
  }

  protected String generateQueryResultTable(List<Map<String, Object>> requestResultList) {
    StringBuilder htmlBuilder = new StringBuilder();
    List<Object> rowDataList = new ArrayList<>();
    int columnCount = 0;

    if (ObjectUtils.notEmpty(requestResultList)) {
      htmlBuilder.append("<!DOCTYPE html>");
      htmlBuilder.append("<html>");
      htmlBuilder.append("<head>");
      htmlBuilder.append("<title></title>");
      htmlBuilder.append("<meta charset=\"utf-8\"/>");
      htmlBuilder.append("<style type=\"text/css\">");
      htmlBuilder.append("th, td {padding: 10px;}");
      htmlBuilder.append("</style>");
      htmlBuilder.append("</head>");
      htmlBuilder.append("<body>");
      htmlBuilder.append("<div style='overflow:auto; max-height:436px;'>");
      htmlBuilder.append("<table border='1'>");

      // Extracting columns for table header
      htmlBuilder.append("<tr>");
      for (String column : requestResultList.get(0).keySet()) {
        htmlBuilder.append("<th>");
        htmlBuilder.append(column);
        htmlBuilder.append("</th>");
        columnCount++;
      }
      htmlBuilder.append("</tr>");

      // Extracting values into a single list from list of map to avoid nested loops
      for (Map<String, Object> row : requestResultList) {
        rowDataList.addAll(row.values());
      }

      // Create a table row based on column count
      int dataCount = 0;
      if (columnCount > 0) {
        for (Object value : rowDataList) {
          if (dataCount % columnCount == 0) {
            htmlBuilder.append("<tr>");
          }
          htmlBuilder.append("<td>");
          htmlBuilder.append(value);
          htmlBuilder.append("</td>");
          dataCount++;
          if (dataCount % columnCount == 0) {
            htmlBuilder.append("</tr>");
          }
        }
      }

      htmlBuilder.append("</table>");
      htmlBuilder.append("</div>");
      htmlBuilder.append("</body>");
      htmlBuilder.append("</html>");
    }

    return htmlBuilder.toString();
  }

  public MetaFile getQueryResultCsvFile(IndicatorGenerator indicatorGenerator) throws IOException {
    File csvDir = java.nio.file.Files.createTempDirectory(null).toFile();
    String filePath = csvDir.getAbsolutePath();
    List<String> columnHeader = new ArrayList<>();
    List<String[]> queryResultData = new ArrayList<>();

    List<Map<String, Object>> requestResultList =
        this.getRequestResultList(
            indicatorGenerator.getRequest(), indicatorGenerator.getRequestLanguage());

    if (ObjectUtils.notEmpty(requestResultList)) {
      for (String column : requestResultList.get(0).keySet()) {
        columnHeader.add(column);
      }
      for (Map<String, Object> row : requestResultList) {
        List<String> tempList =
            row.values().stream()
                .map(x -> x != null ? x.toString().replaceAll("(\\t|\\r?\\n)+", " ") : "null")
                .collect(Collectors.toList());
        queryResultData.add(tempList.toArray(new String[0]));
      }
    }

    String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
    String fileName =
        indicatorGenerator.getName() != null
            ? indicatorGenerator.getName()
                + "_"
                + QUERY_RESULT_CSV_FILE_NAME
                + "_"
                + timeStamp
                + ".csv"
            : QUERY_RESULT_CSV_FILE_NAME + "_" + timeStamp + ".csv";

    CsvTool.csvWriter(
        filePath, fileName, ';', columnHeader.toArray(new String[0]), queryResultData);

    Path path = Paths.get(filePath, fileName);
    try (InputStream is = new FileInputStream(path.toFile())) {
      return Beans.get(MetaFiles.class).attach(is, fileName, indicatorGenerator).getMetaFile();
    }
  }
}
