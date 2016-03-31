/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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
package com.axelor.apps.production.db.repo;

import com.axelor.app.production.db.IManufOrder;
import com.axelor.apps.production.db.ManufOrder;

public class ManufOrderManagementRepository extends ManufOrderRepository {
	@Override
	public ManufOrder copy(ManufOrder entity, boolean deep) {
		entity.setStatusSelect(IManufOrder.STATUS_DRAFT);
		entity.setManufOrderSeq(null);
		entity.setRealEndDateT(null);
		entity.setRealEndDateT(null);
		entity.setInStockMove(null);
		entity.setOutStockMove(null);
		entity.setWasteStockMove(null);
		entity.setToConsumeProdProductList(null);
		entity.setConsumedStockMoveLineList(null);
		entity.setDiffConsumeProdProductList(null);
		entity.setToProduceProdProductList(null);
		entity.setProducedStockMoveLineList(null);
		entity.setWasteProdProductList(null);
		return super.copy(entity, deep);
	}
}
