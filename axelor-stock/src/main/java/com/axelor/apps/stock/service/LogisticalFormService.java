package com.axelor.apps.stock.service;

import com.axelor.apps.stock.db.LogisticalForm;
import com.axelor.apps.stock.db.StockMove;

public interface LogisticalFormService {

	void addLines(LogisticalForm logisticalForm, StockMove stockMove);

	void compute(LogisticalForm logisticalForm);

}
