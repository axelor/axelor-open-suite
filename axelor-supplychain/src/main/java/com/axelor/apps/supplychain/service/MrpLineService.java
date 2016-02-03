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
package com.axelor.apps.supplychain.service;

import java.math.BigDecimal;

import org.joda.time.LocalDate;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.stock.db.Location;
import com.axelor.apps.supplychain.db.MrpLine;
import com.axelor.apps.supplychain.db.MrpLineOrigin;
import com.axelor.apps.supplychain.db.MrpLineType;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;


public interface MrpLineService {
	
	public void generateProposal(MrpLine mrpLine) throws AxelorException;

	public MrpLine createMrpLine(Product product, int maxLevel, MrpLineType mrpLineType, BigDecimal qty, LocalDate maturityDate, BigDecimal cumulativeQty, Location location, Model... models);
	
	public MrpLineOrigin createMrpLineOrigin(Model model);

	public MrpLineOrigin copyMrpLineOrigin(MrpLineOrigin mrpLineOrigin);
}
