/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
package com.axelor.apps.portal.db.repo;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductPicture;
import com.axelor.apps.production.db.repo.ProductProductionRepository;
import com.axelor.common.ObjectUtils;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.google.inject.Inject;

public class ProductPortalRepository extends ProductProductionRepository {

  @Inject MetaFileRepository metaFileRepo;

  @Override
  public Product save(Product product) {

    product = super.save(product);
    if (product.getPicture() != null && !product.getPicture().getIsShared()) {
      MetaFile picture = product.getPicture();
      picture.setIsShared(true);
      metaFileRepo.save(picture);
    }

    if (ObjectUtils.notEmpty(product.getOtherPictures())) {
      for (ProductPicture productPicture : product.getOtherPictures()) {
        MetaFile picture = productPicture.getPicture();
        if (picture == null || picture.getIsShared()) {
          continue;
        }

        picture.setIsShared(true);
        metaFileRepo.save(picture);
      }
    }

    return product;
  }
}
