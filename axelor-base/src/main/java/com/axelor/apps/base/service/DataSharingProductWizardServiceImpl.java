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
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductCategory;
import com.axelor.apps.base.db.repo.DataSharingReferentialLineRepository;
import com.axelor.utils.helpers.StringHelper;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;

public class DataSharingProductWizardServiceImpl implements DataSharingProductWizardService {

  protected DataSharingReferentialLineService dataSharingReferentialLineService;
  protected DataSharingReferentialLineRepository dataSharingReferentialLineRepository;

  @Inject
  public DataSharingProductWizardServiceImpl(
      DataSharingReferentialLineService dataSharingReferentialLineService,
      DataSharingReferentialLineRepository dataSharingReferentialLineRepository) {
    this.dataSharingReferentialLineService = dataSharingReferentialLineService;
    this.dataSharingReferentialLineRepository = dataSharingReferentialLineRepository;
  }

  @Override
  public List<DataSharingReferentialLine> generateDataSharingReferentialLines(
      DataSharingProductWizard dataSharingProductWizard) {
    Set<Product> productSet = dataSharingProductWizard.getProductSet();
    Set<ProductCategory> productCategorySet = dataSharingProductWizard.getProductCategorySet();
    DataSharingReferential dataSharingReferential =
        dataSharingProductWizard.getDataSharingReferential();

    List<String> conditionList = new ArrayList<>();
    List<DataSharingReferentialLine> dataSharingReferentialLineList = new ArrayList<>();

    if (!CollectionUtils.isEmpty(productSet)) {
      conditionList.add("self.id IN (" + StringHelper.getIdListString(productSet) + ")");
    }
    if (!CollectionUtils.isEmpty(productCategorySet)) {
      conditionList.add(
          "self.productCategory IN (" + StringHelper.getIdListString(productCategorySet) + ")");
    }
    if (!CollectionUtils.isEmpty(conditionList)) {
      for (String condition : conditionList) {
        DataSharingReferentialLine dataSharingReferentialLine =
            dataSharingReferentialLineService.createDataSharingReferentialLine(
                dataSharingReferential,
                "Product",
                condition,
                "DataSharingProductWizard",
                dataSharingProductWizard.getId());
        dataSharingReferentialLineList.add(dataSharingReferentialLine);
      }
    }
    return dataSharingReferentialLineList;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void deleteDataSharingReferentialLines(List<Long> dataSharingProductWizardIds) {
    for (long dataSharingProductWizardId : dataSharingProductWizardIds) {
      List<DataSharingReferentialLine> dataSharingReferentialLineList =
          getDataSharingReferentialLines(dataSharingProductWizardId);
      if (!CollectionUtils.isEmpty(dataSharingReferentialLineList)) {
        dataSharingReferentialLineList.forEach(dataSharingReferentialLineRepository::remove);
      }
    }
  }

  protected List<DataSharingReferentialLine> getDataSharingReferentialLines(
      long dataSharingProductWizardId) {
    return dataSharingReferentialLineRepository
        .all()
        .filter("self.wizardRefId = :id")
        .bind("id", dataSharingProductWizardId)
        .fetch();
  }
}
