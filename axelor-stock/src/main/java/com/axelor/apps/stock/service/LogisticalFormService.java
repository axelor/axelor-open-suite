package com.axelor.apps.stock.service;

import com.axelor.apps.stock.db.LogisticalForm;
import com.axelor.apps.stock.db.StockMove;

public interface LogisticalFormService {

	/**
	 * Add detail lines from the stock move.
	 * 
	 * @param logisticalForm
	 * @param stockMove
	 */
	void addLines(LogisticalForm logisticalForm, StockMove stockMove);

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
	 */
	void compute(LogisticalForm logisticalForm);

}
