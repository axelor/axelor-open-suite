package com.axelor.apps.stock.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.axelor.apps.stock.db.LogisticalForm;
import com.axelor.apps.stock.db.LogisticalFormLine;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.LogisticalFormLineRepository;
import com.axelor.db.mapper.Mapper;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.Context;
import com.axelor.script.GroovyScriptHelper;
import com.axelor.script.ScriptHelper;
import com.google.common.base.Strings;
import com.google.inject.persist.Transactional;

public class LogisticalFormServiceImpl implements LogisticalFormService {

	@Override
	public void addLines(LogisticalForm logisticalForm, StockMove stockMove) {
		if (stockMove.getStockMoveLineList() != null) {
			for (StockMoveLine stockMoveLine : stockMove.getStockMoveLineList()) {
				LogisticalFormLine logisticalFormLine = createLine(stockMoveLine);
				logisticalForm.addLogisticalFormLineListItem(logisticalFormLine);
			}
		}
	}

	@Transactional(rollbackOn = { AxelorException.class, Exception.class })
	protected LogisticalFormLine createLine(StockMoveLine stockMoveLine) {
		LogisticalFormLine logisticalFormLine = new LogisticalFormLine();
		logisticalFormLine.setTypeSelect(LogisticalFormLineRepository.TYPE_DETAIL);
		logisticalFormLine.setStockMoveLine(stockMoveLine);
		logisticalFormLine.setQty(stockMoveLine.getRealQty());
		return logisticalFormLine;
	}

	@Override
	public void compute(LogisticalForm logisticalForm) {
		BigDecimal totalNetWeight = BigDecimal.ZERO;
		BigDecimal totalGrossWeight = BigDecimal.ZERO;
		BigDecimal totalVolume = BigDecimal.ZERO;

		if (logisticalForm.getLogisticalFormLineList() != null) {
			ScriptHelper scriptHelper = getScriptHelper(logisticalForm);

			for (LogisticalFormLine logisticalFormLine : logisticalForm.getLogisticalFormLineList()) {
				StockMoveLine stockMoveLine = logisticalFormLine.getStockMoveLine();

				if (logisticalFormLine.getTypeSelect() != LogisticalFormLineRepository.TYPE_DETAIL
						&& logisticalFormLine.getGrossWeight() != null) {
					totalGrossWeight = totalGrossWeight.add(logisticalFormLine.getGrossWeight());
					totalVolume = totalVolume.add(evalBigDecimal(scriptHelper, logisticalFormLine.getDimensions()));
				} else if (stockMoveLine != null) {
					totalNetWeight = totalNetWeight
							.add(logisticalFormLine.getQty().multiply(stockMoveLine.getNetWeight()));
				}

			}
		}

		totalVolume = totalVolume.divide(new BigDecimal(1_000_000), 2, RoundingMode.HALF_UP);
		logisticalForm.setTotalNetWeight(totalNetWeight);
		logisticalForm.setTotalGrossWeight(totalGrossWeight);
		logisticalForm.setTotalVolume(totalVolume);
	}

	protected BigDecimal evalBigDecimal(ScriptHelper scriptHelper, String script) {
		if (Strings.isNullOrEmpty(script)) {
			return BigDecimal.ZERO;
		}

		return (BigDecimal) scriptHelper.eval(String.format("new BigDecimal(%s)", script.replaceAll("x", "*")));
	}

	protected ScriptHelper getScriptHelper(LogisticalForm logisticalForm) {
		Context scriptContext = new Context(Mapper.toMap(logisticalForm), logisticalForm.getClass());
		return new GroovyScriptHelper(scriptContext);
	}

}
