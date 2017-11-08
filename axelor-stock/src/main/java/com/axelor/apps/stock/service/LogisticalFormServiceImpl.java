package com.axelor.apps.stock.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.axelor.apps.stock.db.LogisticalForm;
import com.axelor.apps.stock.db.LogisticalFormLine;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.LogisticalFormLineRepository;
import com.axelor.apps.stock.exception.IExceptionMessage;
import com.axelor.db.mapper.Mapper;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.rpc.Context;
import com.axelor.script.GroovyScriptHelper;
import com.axelor.script.ScriptHelper;
import com.google.common.base.Strings;

public class LogisticalFormServiceImpl implements LogisticalFormService {

	private static final Pattern DIMENSIONS_PATTERN = Pattern
			.compile("\\d+(\\.\\d*)?\\s*[x\\*]\\s*\\d+(\\.\\d*)?\\s*[x\\*]\\s*\\d+(\\.\\d*)?");

	@Override
	public void addDetailLines(LogisticalForm logisticalForm, StockMove stockMove) {
		if (stockMove.getStockMoveLineList() != null) {
			for (StockMoveLine stockMoveLine : stockMove.getStockMoveLineList()) {
				LogisticalFormLine logisticalFormLine = createDetailLine(stockMoveLine);
				logisticalForm.addLogisticalFormLineListItem(logisticalFormLine);
			}
		}
	}

	@Override
	public void addParcelPalletLine(LogisticalForm logisticalForm, int typeSelect) {
		LogisticalFormLine logisticalFormLine = createParcelPalletLine(logisticalForm, typeSelect);
		logisticalForm.addLogisticalFormLineListItem(logisticalFormLine);
	}

	protected LogisticalFormLine createDetailLine(StockMoveLine stockMoveLine) {
		LogisticalFormLine logisticalFormLine = new LogisticalFormLine();
		logisticalFormLine.setTypeSelect(LogisticalFormLineRepository.TYPE_DETAIL);
		logisticalFormLine.setStockMoveLine(stockMoveLine);
		logisticalFormLine.setQty(stockMoveLine.getRealQty());
		return logisticalFormLine;
	}

	protected LogisticalFormLine createParcelPalletLine(LogisticalForm logisticalForm, int typeSelect) {
		LogisticalFormLine logisticalFormLine = new LogisticalFormLine();
		logisticalFormLine.setTypeSelect(typeSelect);
		logisticalFormLine.setParcelPalletNumber(findHighestParcelPalletNumber(logisticalForm, typeSelect) + 1);
		return logisticalFormLine;
	}

	protected int findHighestParcelPalletNumber(LogisticalForm logisticalForm, int typeSelect) {
		int highest = 0;

		if (logisticalForm.getLogisticalFormLineList() != null) {
			for (LogisticalFormLine logisticalFormLine : logisticalForm.getLogisticalFormLineList()) {
				if (logisticalFormLine.getTypeSelect() == typeSelect
						&& logisticalFormLine.getParcelPalletNumber() != null
						&& logisticalFormLine.getParcelPalletNumber() > highest) {
					highest = logisticalFormLine.getParcelPalletNumber();
				}
			}
		}

		return highest;
	}

	@Override
	public void compute(LogisticalForm logisticalForm) throws AxelorException {
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
					totalVolume = totalVolume.add(evalVolume(logisticalFormLine, scriptHelper));
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

	protected BigDecimal evalVolume(LogisticalFormLine logisticalFormLine, ScriptHelper scriptHelper)
			throws AxelorException {
		String script = logisticalFormLine.getDimensions();

		if (Strings.isNullOrEmpty(script)) {
			return BigDecimal.ZERO;
		}

		Matcher matcher = DIMENSIONS_PATTERN.matcher(script);

		if (!matcher.matches()) {
			throw new AxelorException(logisticalFormLine, IException.CONFIGURATION_ERROR,
					IExceptionMessage.INVALID_DIMENSIONS, logisticalFormLine.getSequence() + 1);
		}

		return (BigDecimal) scriptHelper.eval(String.format("new BigDecimal(%s)", script.replaceAll("x", "*")));
	}

	protected ScriptHelper getScriptHelper(LogisticalForm logisticalForm) {
		Context scriptContext = new Context(Mapper.toMap(logisticalForm), logisticalForm.getClass());
		return new GroovyScriptHelper(scriptContext);
	}

}
