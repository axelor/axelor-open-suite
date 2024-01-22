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
package com.axelor.apps.stock.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.stock.db.Inventory;
import com.axelor.apps.stock.exception.StockExceptionMessage;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import javax.persistence.NoResultException;
import javax.persistence.Query;

public class InventoryProductServiceImpl implements InventoryProductService {

  @Override
  public void checkDuplicate(Inventory inventory) throws AxelorException {
    Query query =
        JPA.em()
            .createQuery(
                "select COUNT(*) FROM InventoryLine self WHERE self.inventory.id = :invent GROUP BY self.product, self.stockLocation, self.trackingNumber HAVING COUNT(self) > 1");

    Long duplicateCounter = Long.valueOf(0);
    try {
      duplicateCounter = (Long) query.setParameter("invent", inventory.getId()).getSingleResult();
    } catch (NoResultException e) {
      // if control came here means no duplicate product.
    }
    if (duplicateCounter > 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(StockExceptionMessage.INVENTORY_PRODUCT_TRACKING_NUMBER_ERROR));
    }
  }
}
