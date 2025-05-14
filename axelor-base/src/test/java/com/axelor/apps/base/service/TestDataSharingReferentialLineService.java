/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.db.DataSharingReferentialLine;
import com.axelor.apps.base.db.Product;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.db.Query;
import com.axelor.meta.db.MetaModel;
import com.axelor.utils.junit.BaseTest;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TestDataSharingReferentialLineService extends BaseTest {

  protected final DataSharingReferentialLineService dataSharingReferentialLineService;

  @Inject
  public TestDataSharingReferentialLineService(
      DataSharingReferentialLineService dataSharingReferentialLineService) {
    this.dataSharingReferentialLineService = dataSharingReferentialLineService;
  }

  @Test
  @Transactional
  void testGetQuery() {
    MetaModel metaModel = createMetaModel();
    Assertions.assertNotNull(metaModel);

    createDataSharingReferentialLine(metaModel, "self.sellable = true");
    createDataSharingReferentialLine(metaModel, "self.purchasable = false");

    Query<? extends Model> query = dataSharingReferentialLineService.getQuery(Product.class);
    Assertions.assertNotNull(query);
    Assertions.assertTrue(
        query.toString().contains("self.sellable = true OR self.purchasable = false"));
  }

  protected MetaModel createMetaModel() {
    MetaModel metaModel = new MetaModel();
    metaModel.setName("Product");
    metaModel.setPackageName("com.axelor.apps.base.db");
    metaModel.setFullName("com.axelor.apps.base.db.Product");
    return JPA.save(metaModel);
  }

  protected void createDataSharingReferentialLine(MetaModel metaModel, String condition) {
    DataSharingReferentialLine dataSharingReferentialLine = new DataSharingReferentialLine();
    dataSharingReferentialLine.setMetaModel(metaModel);
    dataSharingReferentialLine.setQueryCondition(condition);
    JPA.save(dataSharingReferentialLine);
  }
}
