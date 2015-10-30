/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2014 Axelor (<http://axelor.com>).
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
package com.axelor.apps.production.service;

import java.math.BigDecimal;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.production.db.CostSheetGroup;
import com.axelor.apps.production.db.CostSheetLine;
import com.axelor.apps.production.db.WorkCenter;

public interface CostSheetLineService {

	public CostSheetLine createCostSheetLine(String name, String code, int bomLevel, BigDecimal consumptionQty, BigDecimal costPrice, 
			CostSheetGroup costSheetGroup, Product product, int typeSelect, Unit unit, WorkCenter workCenter, CostSheetLine parentCostSheetLine);

	public CostSheetLine createProducedProductCostSheetLine(Product product, BigDecimal consumptionQty);
 	
	public CostSheetLine createConsumedProductCostSheetLine(Product product, int bomLevel, CostSheetLine parentCostSheetLine, BigDecimal consumptionQty);
	
	public CostSheetLine createWorkCenterCostSheetLine(WorkCenter workCenter, int priority, int bomLevel, CostSheetLine parentCostSheetLine, BigDecimal consumptionQty, BigDecimal costPrice, Unit unit);

}
