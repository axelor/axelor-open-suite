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
package com.axelor.apps.base.service;

import com.axelor.apps.base.db.DataSharingProductWizard;
import com.axelor.apps.base.db.DataSharingReferential;
import com.axelor.apps.base.db.DataSharingReferentialLine;
import com.axelor.apps.base.db.repo.DataSharingReferentialLineRepository;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.db.Query;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class DataSharingReferentialLineServiceImpl implements DataSharingReferentialLineService {

  protected DataSharingReferentialLineRepository dataSharingReferentialLineRepository;
  protected MetaModelRepository metaModelRepository;

  @Inject
  public DataSharingReferentialLineServiceImpl(
      DataSharingReferentialLineRepository dataSharingReferentialLineRepository,
      MetaModelRepository metaModelRepository) {
    this.dataSharingReferentialLineRepository = dataSharingReferentialLineRepository;
    this.metaModelRepository = metaModelRepository;
  }

  @Override
  public <T extends Model> Query<T> getQuery(
      DataSharingReferential dataSharingReferential, Class<T> modelClass) {
    return this.getQuery(dataSharingReferential.getDataSharingReferentialLineList(), modelClass);
  }

  @Override
  public <T extends Model> Query<T> getQuery(Class<T> modelClass) {
    return this.getQuery(this.getDataSharingReferentialLineList(modelClass), modelClass);
  }

  protected <T extends Model> Query<T> getQuery(
      List<DataSharingReferentialLine> dataSharingReferentialLineList, Class<T> modelClass) {
    if (CollectionUtils.isEmpty(dataSharingReferentialLineList)) {
      return null;
    }

    List<String> conditionList = getConditionList(dataSharingReferentialLineList);

    if (CollectionUtils.isEmpty(conditionList)) {
      return null;
    }

    String condition = String.join(" OR ", conditionList);
    return JPA.all(modelClass).filter(condition);
  }

  protected List<DataSharingReferentialLine> getDataSharingReferentialLineList(
      Class<? extends Model> modelClass) {
    return dataSharingReferentialLineRepository
        .all()
        .filter("self.metaModel.name = :modelName")
        .bind("modelName", modelClass.getSimpleName())
        .fetch();
  }

  protected List<String> getConditionList(
      List<DataSharingReferentialLine> dataSharingReferentialLineList) {
    List<String> conditionList = new ArrayList<>();

    for (DataSharingReferentialLine dataSharingReferentialLine : dataSharingReferentialLineList) {
      String condition = dataSharingReferentialLine.getQueryCondition();
      if (condition != null && !condition.trim().isEmpty()) {
        conditionList.add(condition.trim());
      }
    }
    return conditionList;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public DataSharingReferentialLine createDataSharingReferentialLine(
      DataSharingReferential dataSharingReferential,
      String metaModelName,
      String condition,
      String wizardModelName,
      Long wizardRefId) {
    DataSharingReferentialLine dataSharingReferentialLine = new DataSharingReferentialLine();
    dataSharingReferentialLine.setDataSharingReferential(dataSharingReferential);
    dataSharingReferentialLine.setMetaModel(metaModelRepository.findByName(metaModelName));
    dataSharingReferentialLine.setQueryCondition(condition);
    dataSharingReferentialLine.setWizardMetaModel(metaModelRepository.findByName(wizardModelName));
    dataSharingReferentialLine.setWizardRefId(wizardRefId);
    return dataSharingReferentialLineRepository.save(dataSharingReferentialLine);
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void removeDataSharingReferentialLines(DataSharingProductWizard dataSharingProductWizard) {
    List<DataSharingReferentialLine> dataSharingReferentialLineList =
        dataSharingReferentialLineRepository
            .all()
            .filter("self.wizardRefId = :id")
            .bind("id", dataSharingProductWizard.getId())
            .fetch();

    if (!CollectionUtils.isEmpty(dataSharingReferentialLineList)) {
      dataSharingReferentialLineList.forEach(dataSharingReferentialLineRepository::remove);
    }
  }
}
