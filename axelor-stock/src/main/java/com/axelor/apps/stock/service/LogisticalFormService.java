package com.axelor.apps.stock.service;

import com.axelor.apps.stock.db.LogisticalForm;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.exception.InconsistentLogisticalFormLines;
import com.axelor.exception.AxelorException;

public interface LogisticalFormService {

	/**
	 * Add detail lines from the stock move.
	 * 
	 * @param logisticalForm
	 * @param stockMove
	 */
	void addDetailLines(LogisticalForm logisticalForm, StockMove stockMove);

	/**
	 * Add parcel or pallet line.
	 * 
	 * @param logisticalForm
	 * @param typeSelect
	 */
	void addParcelPalletLine(LogisticalForm logisticalForm, int typeSelect);

	/**
	 * Compute totals.
	 * 
	 * @param logisticalForm
	 * @throws AxelorException
	 */
	void compute(LogisticalForm logisticalForm) throws AxelorException;

	/**
	 * Check lines.
	 * 
	 * @param logisticalForm
	 * @throws InconsistentLogisticalFormLines
	 */
	void checkLines(LogisticalForm logisticalForm) throws InconsistentLogisticalFormLines;

}
