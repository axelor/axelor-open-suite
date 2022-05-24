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
package com.axelor.apps.bpm.service;

import com.axelor.apps.bpm.db.WkfModel;
import com.axelor.apps.bpm.db.WkfProcess;
import com.axelor.apps.bpm.db.WkfProcessConfig;
import com.axelor.apps.bpm.db.WkfTaskConfig;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.meta.db.MetaFile;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface WkfModelService {

  @Transactional
  public WkfModel createNewVersion(WkfModel wkfModel);

  @Transactional
  public WkfModel start(WkfModel wkfModel);

  @Transactional
  public WkfModel terminate(WkfModel wkfModel);

  @Transactional
  public WkfModel backToDraft(WkfModel wkfModel);

  public List<Long> findVersions(WkfModel wkfModel);

  public void importStandardBPM();

  public List<Long> getStatusPerMonthRecord(
      String tableName, String status, String month, String jsonModel);

  public List<Long> getStatusPerDayRecord(
      String tableName, String status, String day, String jsonModel);

  public List<Long> getTimespentPerStatusRecord(
      String tableName, String status, LocalDate fromDate, LocalDate toDate, String jsonModel);

  public String importWkfModels(
      MetaFile metaFile, boolean isTranslate, String sourceLanguage, String targetLanguage)
      throws AxelorException;

  public List<Map<String, Object>> getProcessPerStatus(WkfModel wkfModel);

  public List<Map<String, Object>> getProcessPerUser(WkfModel wkfModel);

  public List<WkfProcess> getProcesses(WkfModel wkfModel);

  public void sortProcessConfig(List<WkfProcessConfig> processConfigs);

  public List<WkfTaskConfig> getTaskConfigs(
      WkfProcess process, String modelName, boolean isMetaModel, User user, boolean withTask);

  public Object[] computeTaskConfig(
      List<WkfTaskConfig> taskConfigs,
      String modelName,
      boolean isMetaModel,
      User user,
      boolean withTask);
}
