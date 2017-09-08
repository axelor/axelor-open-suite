/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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
package com.axelor.csv.script;

import com.axelor.apps.stock.db.Inventory;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.repo.InventoryRepository;
import com.axelor.apps.stock.service.InventoryService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ImportInventory {
	
	@Inject
	InventoryRepository inventoryRepo;
	
	@Inject
	InventoryService inventoryService;
	
	@Transactional
	void validateInventory(Long inventoryId) {
		try {
			Inventory inventory = inventoryRepo.find(inventoryId);
			StockMove stockMove = inventoryService.validateInventory(inventory);
			stockMove.setRealDate(inventory.getDateT().toLocalDate());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}