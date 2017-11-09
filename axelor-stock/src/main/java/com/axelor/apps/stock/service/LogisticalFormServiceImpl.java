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
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.axelor.apps.stock.db.LogisticalForm;
import com.axelor.apps.stock.db.LogisticalFormLine;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.LogisticalFormLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.exception.IExceptionMessage;
import com.axelor.apps.stock.exception.InconsistentLogisticalFormLines;
import com.axelor.apps.tool.StringTool;
import com.axelor.db.mapper.Mapper;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.inject.Beans;
import com.axelor.rpc.Context;
import com.axelor.script.GroovyScriptHelper;
import com.axelor.script.ScriptHelper;
import com.google.common.base.Strings;

public class LogisticalFormServiceImpl implements LogisticalFormService {

	private static final Pattern DIMENSIONS_PATTERN = Pattern
			.compile("\\s*\\d+(\\.\\d*)?\\s*[x\\*]\\s*\\d+(\\.\\d*)?\\s*[x\\*]\\s*\\d+(\\.\\d*)?\\s*");

	@Override
	public void addDetailLines(LogisticalForm logisticalForm, StockMove stockMove) {
		if (stockMove.getStockMoveLineList() != null) {
			Map<StockMoveLine, BigDecimal> stockMoveLineMap = getStockMoveLineQtyMap(logisticalForm);

			for (StockMoveLine stockMoveLine : stockMove.getStockMoveLineList()) {
				BigDecimal qty = stockMoveLineMap.getOrDefault(stockMoveLine, BigDecimal.ZERO);
				if (qty.compareTo(stockMoveLine.getRealQty()) >= 0) {
					continue;
				}

				LogisticalFormLine logisticalFormLine = createDetailLine(logisticalForm, stockMoveLine,
						stockMoveLine.getRealQty().subtract(qty));
				logisticalForm.addLogisticalFormLineListItem(logisticalFormLine);
			}
		}

	}

	@Override
	public void addParcelPalletLine(LogisticalForm logisticalForm, int typeSelect) {
		LogisticalFormLine logisticalFormLine = createParcelPalletLine(logisticalForm, typeSelect);
		logisticalForm.addLogisticalFormLineListItem(logisticalFormLine);
	}

	@Override
	public void checkLines(LogisticalForm logisticalForm) throws InconsistentLogisticalFormLines {
		Map<StockMoveLine, BigDecimal> stockMoveLineMap = getStockMoveLineQtyMap(logisticalForm);
		List<String> errorMessageList = new ArrayList<>();

		for (Entry<StockMoveLine, BigDecimal> entry : stockMoveLineMap.entrySet()) {
			StockMoveLine stockMoveLine = entry.getKey();
			BigDecimal qty = entry.getValue();

			if (qty.compareTo(stockMoveLine.getRealQty()) != 0) {
				String errorMessage = String.format(
						IExceptionMessage.LOGISTICAL_FORM_LINES_INCONSISTENT_QUANTITY, String.format("%s (%s)",
								stockMoveLine.getProductName(), stockMoveLine.getStockMove().getStockMoveSeq()),
						qty, stockMoveLine.getRealQty());
				errorMessageList.add(errorMessage);
			}
		}

		if (!errorMessageList.isEmpty()) {
			String errorMessage = errorMessageList.stream().collect(Collectors.joining("<br />"));
			throw new InconsistentLogisticalFormLines(logisticalForm, errorMessage);
		}
	}

	@Override
	public List<StockMoveLine> getFullySpreadStockMoveLineList(LogisticalForm logisticalForm) {
		List<StockMoveLine> stockMoveLineList = new ArrayList<>();
		Map<StockMoveLine, BigDecimal> stockMoveLineMap = getStockMoveLineQtyMap(logisticalForm);

		for (Entry<StockMoveLine, BigDecimal> entry : stockMoveLineMap.entrySet()) {
			StockMoveLine stockMoveLine = entry.getKey();
			BigDecimal qty = entry.getValue();

			if (qty.compareTo(stockMoveLine.getRealQty()) >= 0) {
				stockMoveLineList.add(stockMoveLine);
			}
		}

		return stockMoveLineList;
	}

	protected List<StockMove> getFullSpreadStockMoveList(LogisticalForm logisticalForm) {
		List<StockMove> fullySpreadStockMoveList = new ArrayList<>();
		List<StockMoveLine> fullySpreadStockMoveLineList = Beans.get(LogisticalFormService.class)
				.getFullySpreadStockMoveLineList(logisticalForm);

		Set<StockMove> stockMoveSet = new HashSet<>();

		for (StockMoveLine stockMoveLine : fullySpreadStockMoveLineList) {
			stockMoveSet.add(stockMoveLine.getStockMove());
		}

		for (StockMove stockMove : stockMoveSet) {
			if (fullySpreadStockMoveLineList.containsAll(stockMove.getStockMoveLineList())) {
				fullySpreadStockMoveList.add(stockMove);
			}
		}

		return fullySpreadStockMoveList;
	}

	@Override
	public Map<StockMoveLine, BigDecimal> getStockMoveLineQtyMap(LogisticalForm logisticalForm) {
		Map<StockMoveLine, BigDecimal> stockMoveLineMap = new LinkedHashMap<>();

		if (logisticalForm.getLogisticalFormLineList() != null) {
			logisticalForm.getLogisticalFormLineList().stream()
					.filter(logisticalFormLine -> logisticalFormLine
							.getTypeSelect() == LogisticalFormLineRepository.TYPE_DETAIL)
					.forEach(logisticalFormLine -> {
						StockMoveLine stockMoveLine = logisticalFormLine.getStockMoveLine();
						if (stockMoveLine != null && logisticalFormLine.getQty() != null) {
							stockMoveLineMap.merge(stockMoveLine, logisticalFormLine.getQty(), BigDecimal::add);
						}
					});
		}

		return stockMoveLineMap;
	}

	protected LogisticalFormLine createDetailLine(LogisticalForm logisticalForm, StockMoveLine stockMoveLine,
			BigDecimal qty) {
		LogisticalFormLine logisticalFormLine = new LogisticalFormLine();
		logisticalFormLine.setTypeSelect(LogisticalFormLineRepository.TYPE_DETAIL);
		logisticalFormLine.setStockMoveLine(stockMoveLine);
		logisticalFormLine.setQty(qty);
		logisticalFormLine.setSequence(findHighestLineSequence(logisticalForm) + 1);
		return logisticalFormLine;
	}

	protected LogisticalFormLine createParcelPalletLine(LogisticalForm logisticalForm, int typeSelect) {
		LogisticalFormLine logisticalFormLine = new LogisticalFormLine();
		logisticalFormLine.setTypeSelect(typeSelect);
		logisticalFormLine.setParcelPalletNumber(findHighestParcelPalletNumber(logisticalForm, typeSelect) + 1);
		logisticalFormLine.setSequence(findHighestLineSequence(logisticalForm) + 1);
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

	protected int findHighestLineSequence(LogisticalForm logisticalForm) {
		return logisticalForm.getLogisticalFormLineList() != null
				? logisticalForm.getLogisticalFormLineList().stream().mapToInt(LogisticalFormLine::getSequence).max()
						.orElse(0)
				: 0;
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

				if (logisticalFormLine.getTypeSelect() != LogisticalFormLineRepository.TYPE_DETAIL) {
					if (logisticalFormLine.getGrossWeight() != null) {
						totalGrossWeight = totalGrossWeight.add(logisticalFormLine.getGrossWeight());
					}

					totalVolume = totalVolume.add(evalVolume(logisticalFormLine, scriptHelper));
				} else if (stockMoveLine != null) {
					totalNetWeight = totalNetWeight
							.add(logisticalFormLine.getQty().multiply(stockMoveLine.getNetWeight()));
				}

			}
		}

		totalVolume = totalVolume.divide(new BigDecimal(1_000_000), 10, RoundingMode.HALF_UP);
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

		if (!DIMENSIONS_PATTERN.matcher(script).matches()) {
			throw new AxelorException(logisticalFormLine, IException.CONFIGURATION_ERROR,
					IExceptionMessage.LOGISTICAL_FORM_LINE_INVALID_DIMENSIONS, logisticalFormLine.getSequence() + 1);
		}

		return (BigDecimal) scriptHelper.eval(String.format("new BigDecimal(%s)", script.replaceAll("x", "*")));
	}

	protected ScriptHelper getScriptHelper(LogisticalForm logisticalForm) {
		Context scriptContext = new Context(Mapper.toMap(logisticalForm), logisticalForm.getClass());
		return new GroovyScriptHelper(scriptContext);
	}

	@Override
	public String getStockMoveDomain(LogisticalForm logisticalForm) {
		List<String> domainList = new ArrayList<>();

		domainList.add("self.partner = :deliverToCustomer");
		domainList.add(String.format("self.typeSelect = %d", StockMoveRepository.TYPE_OUTGOING));

		List<StockMove> fullySpreadStockMoveList = getFullSpreadStockMoveList(logisticalForm);

		if (!fullySpreadStockMoveList.isEmpty()) {
			String idListString = StringTool.getIdListString(fullySpreadStockMoveList);
			domainList.add(String.format("self.id NOT IN (%s)", idListString));
		}

		return domainList.stream().map(domain -> String.format("(%s)", domain)).collect(Collectors.joining(" AND "));
	}

}
