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
import com.axelor.apps.base.db.IndicatorGeneratorGrouping;
import com.axelor.apps.base.db.repo.IndicatorGeneratorGroupingRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.i18n.I18n;
import com.axelor.studio.app.service.AppService;
import com.axelor.utils.file.CsvTool;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class IndicatorGeneratorGroupingService {

  @Inject private IndicatorGeneratorService indicatorGeneratorService;

  @Inject private IndicatorGeneratorGroupingRepository iggRepo;

  @Inject private AppService appService;

  @Transactional(rollbackOn = {Exception.class})
  public void run(IndicatorGeneratorGrouping indicatorGeneratorGrouping) throws AxelorException {

    String log = "";

    String result = "";

    for (IndicatorGenerator indicatorGenerator :
        indicatorGeneratorGrouping.getIndicatorGeneratorSet()) {

      indicatorGeneratorService.run(indicatorGenerator);

      result =
          result
              + "\n"
              + indicatorGenerator.getCode()
              + " "
              + indicatorGenerator.getName()
              + " : "
              + indicatorGenerator.getResult();

      if (indicatorGenerator.getLog() != null && !indicatorGenerator.getLog().isEmpty()) {
        log = log + "\n" + indicatorGenerator.getLog();
      }
    }

    indicatorGeneratorGrouping.setResult(result);

    indicatorGeneratorGrouping.setLog(log);

    iggRepo.save(indicatorGeneratorGrouping);
  }

  @Transactional(rollbackOn = {Exception.class})
  public void export(IndicatorGeneratorGrouping indicatorGeneratorGrouping) throws AxelorException {

    String log = "";
    String dataExportDir = appService.getDataExportDir();

    if (indicatorGeneratorGrouping.getPath() == null
        || indicatorGeneratorGrouping.getPath().isEmpty()) {

      log += "\n" + I18n.get(BaseExceptionMessage.INDICATOR_GENERATOR_GROUPING_1);
    }

    if (indicatorGeneratorGrouping.getCode() == null
        || indicatorGeneratorGrouping.getCode().isEmpty()) {

      log += "\n" + I18n.get(BaseExceptionMessage.INDICATOR_GENERATOR_GROUPING_2);
    }

    List<String[]> resultList = new ArrayList<String[]>();

    for (IndicatorGenerator indicatorGenerator :
        indicatorGeneratorGrouping.getIndicatorGeneratorSet()) {

      String[] result = {
        indicatorGenerator.getCode(), indicatorGenerator.getName(), indicatorGenerator.getResult()
      };

      resultList.add(result);

      log = log + "\n" + indicatorGenerator.getLog();
    }

    try {
      CsvTool.csvWriter(
          indicatorGeneratorGrouping.getPath() == null
              ? null
              : dataExportDir + indicatorGeneratorGrouping.getPath(),
          indicatorGeneratorGrouping.getCode() + ".csv",
          ';',
          null,
          resultList);
    } catch (IOException e) {
      log += I18n.get(BaseExceptionMessage.INDICATOR_GENERATOR_GROUPING_3);
    }

    if (!log.isEmpty() && log.length() != 0) {
      String log2 = indicatorGeneratorGrouping.getLog();

      log2 += "\n ---------------------------------------------------";

      log2 += log;

      indicatorGeneratorGrouping.setLog(log2);
    }

    iggRepo.save(indicatorGeneratorGrouping);
  }
}
