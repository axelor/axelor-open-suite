package com.axelor.apps.supplychain.service;

import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.stock.db.LogisticalForm;
import com.axelor.apps.stock.db.LogisticalFormLine;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.service.LogisticalFormServiceImpl;

public class LogisticalFormSupplychainServiceImpl extends LogisticalFormServiceImpl implements LogisticalFormSupplychainService {

	@Override
	public void addLines(LogisticalForm logisticalForm, StockMove stockMove) {
		if (stockMove.getStockMoveLineList() != null) {
			for (StockMoveLine stockMoveLine : stockMove.getStockMoveLineList()) {
				SaleOrderLine saleOrderLine = stockMoveLine.getSaleOrderLine();
				if (saleOrderLine != null && saleOrderLine.getTypeSelect() == SaleOrderLineRepository.TYPE_TITLE) {
					continue;
				}

				LogisticalFormLine logisticalFormLine = createDetailLine(stockMoveLine);
				logisticalForm.addLogisticalFormLineListItem(logisticalFormLine);
			}
		}
	}

}
