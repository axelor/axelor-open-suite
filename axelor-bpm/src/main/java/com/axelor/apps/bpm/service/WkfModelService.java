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
import com.axelor.exception.AxelorException;
import com.axelor.meta.db.MetaFile;
import com.google.inject.persist.Transactional;
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

  public String importWkfModels(
      MetaFile metaFile, boolean isTranslate, String sourceLanguage, String targetLanguage)
      throws AxelorException;

  public List<Map<String, Object>> getProcessPerStatus(WkfModel wkfModel);

  public List<Map<String, Object>> getProcessPerUser(WkfModel wkfModel);
}
