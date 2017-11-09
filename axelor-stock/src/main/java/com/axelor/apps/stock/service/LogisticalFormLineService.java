/*
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
package com.axelor.apps.stock.service;

import java.math.BigDecimal;

import com.axelor.apps.stock.db.LogisticalFormLine;
import com.axelor.apps.stock.exception.InvalidLogisticalFormLineDimensions;
import com.axelor.script.ScriptHelper;

public interface LogisticalFormLineService {

	/**
	 * Get domain for stockMoveLine.
	 * 
	 * @param logisticalFormLine
	 * @return
	 */
	String getStockMoveLineDomain(LogisticalFormLine logisticalFormLine);

	/**
	 * Get unspread quantity.
	 * 
	 * @param logisticalFormLine
	 * @return
	 */
	BigDecimal getUnspreadQty(LogisticalFormLine logisticalFormLine);

	/**
	 * Validate dimensions
	 * 
	 * @param logisticalFormLine
	 * @throws InvalidLogisticalFormLineDimensions
	 */
	void validateDimensions(LogisticalFormLine logisticalFormLine) throws InvalidLogisticalFormLineDimensions;

	/**
	 * Evaluate volume.
	 * 
	 * @param logisticalFormLine
	 * @param scriptHelper
	 * @return
	 * @throws InvalidLogisticalFormLineDimensions
	 */
	BigDecimal evalVolume(LogisticalFormLine logisticalFormLine, ScriptHelper scriptHelper)
			throws InvalidLogisticalFormLineDimensions;

}
