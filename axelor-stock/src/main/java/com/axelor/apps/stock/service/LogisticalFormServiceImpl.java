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
import java.util.stream.Collectors;

import com.axelor.apps.stock.db.LogisticalForm;
import com.axelor.apps.stock.db.LogisticalFormLine;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.LogisticalFormLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.exception.IExceptionMessage;
import com.axelor.apps.stock.exception.InconsistentLogisticalFormLines;
import com.axelor.apps.stock.exception.InvalidLogisticalFormLineDimensions;
import com.axelor.apps.tool.StringTool;
import com.axelor.db.mapper.Mapper;
import com.axelor.inject.Beans;
import com.axelor.rpc.Context;
import com.axelor.script.GroovyScriptHelper;
import com.axelor.script.ScriptHelper;

public class LogisticalFormServiceImpl implements LogisticalFormService {

	@Override
	public void addDetailLines(LogisticalForm logisticalForm, StockMove stockMove) {
		if (stockMove.getStockMoveLineList() != null) {
			Map<StockMoveLine, BigDecimal> stockMoveLineMap = getStockMoveLineQtyMap(logisticalForm);

			for (StockMoveLine stockMoveLine : stockMove.getStockMoveLineList()) {
				BigDecimal qty = stockMoveLineMap.getOrDefault(stockMoveLine, BigDecimal.ZERO);
				if (qty.compareTo(stockMoveLine.getRealQty()) >= 0) {
					continue;
				}

				if (testForDetailLine(stockMoveLine)) {
					LogisticalFormLine logisticalFormLine = createDetailLine(logisticalForm, stockMoveLine,
							stockMoveLine.getRealQty().subtract(qty));
					logisticalForm.addLogisticalFormLineListItem(logisticalFormLine);
				}
			}
		}
	}

	/**
	 * Test for detail line (to be overridden).
	 * 
	 * @param stockMoveLine
	 * @return
	 */
	@SuppressWarnings("all")
	protected boolean testForDetailLine(StockMoveLine stockMoveLine) {
		return true;
	}

	@Override
	public void addParcelPalletLine(LogisticalForm logisticalForm, int typeSelect) {
		LogisticalFormLine logisticalFormLine = createParcelPalletLine(logisticalForm, typeSelect);
		logisticalForm.addLogisticalFormLineListItem(logisticalFormLine);
	}

	@Override
	public void checkInconsistentLines(LogisticalForm logisticalForm) throws InconsistentLogisticalFormLines {
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
	public void checkInvalidLineDimensions(LogisticalForm logisticalForm) throws InvalidLogisticalFormLineDimensions {
		if (logisticalForm.getLogisticalFormLineList() != null) {
			LogisticalFormLineService logisticalFormLineService = Beans.get(LogisticalFormLineService.class);
			for (LogisticalFormLine logisticalFormLine : logisticalForm.getLogisticalFormLineList()) {
				logisticalFormLineService.validateDimensions(logisticalFormLine);
			}
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
		List<StockMoveLine> fullySpreadStockMoveLineList = getFullySpreadStockMoveLineList(logisticalForm);

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
		logisticalFormLine.setSequence(getNextLineSequence(logisticalForm));
		return logisticalFormLine;
	}

	protected LogisticalFormLine createParcelPalletLine(LogisticalForm logisticalForm, int typeSelect) {
		LogisticalFormLine logisticalFormLine = new LogisticalFormLine();
		logisticalFormLine.setTypeSelect(typeSelect);
		logisticalFormLine.setParcelPalletNumber(getNextParcelPalletNumber(logisticalForm, typeSelect));
		logisticalFormLine.setSequence(getNextLineSequence(logisticalForm));
		return logisticalFormLine;
	}

	@Override
	public int getNextParcelPalletNumber(LogisticalForm logisticalForm, int typeSelect) {
		int highest = 0;
		Set<Integer> parcelPalletNumberSet = new HashSet<>();

		if (logisticalForm.getLogisticalFormLineList() != null) {
			for (LogisticalFormLine logisticalFormLine : logisticalForm.getLogisticalFormLineList()) {
				if (logisticalFormLine.getTypeSelect() == typeSelect
						&& logisticalFormLine.getParcelPalletNumber() != null) {
					parcelPalletNumberSet.add(logisticalFormLine.getParcelPalletNumber());

					if (logisticalFormLine.getParcelPalletNumber() > highest) {
						highest = logisticalFormLine.getParcelPalletNumber();
					}
				}
			}
		}

		for (int i = 1; i < highest; ++i) {
			if (!parcelPalletNumberSet.contains(i)) {
				return i;
			}
		}

		return highest + 1;
	}

	@Override
	public int getNextLineSequence(LogisticalForm logisticalForm) {
		return logisticalForm.getLogisticalFormLineList() != null
				? logisticalForm.getLogisticalFormLineList().stream().mapToInt(LogisticalFormLine::getSequence).max()
						.orElse(1)
				: 1;
	}

	@Override
	public void computeTotals(LogisticalForm logisticalForm) throws InvalidLogisticalFormLineDimensions {
		BigDecimal totalNetWeight = BigDecimal.ZERO;
		BigDecimal totalGrossWeight = BigDecimal.ZERO;
		BigDecimal totalVolume = BigDecimal.ZERO;

		if (logisticalForm.getLogisticalFormLineList() != null) {
			ScriptHelper scriptHelper = getScriptHelper(logisticalForm);
			LogisticalFormLineService logisticalFormLineService = Beans.get(LogisticalFormLineService.class);

			for (LogisticalFormLine logisticalFormLine : logisticalForm.getLogisticalFormLineList()) {
				StockMoveLine stockMoveLine = logisticalFormLine.getStockMoveLine();

				if (logisticalFormLine.getTypeSelect() != LogisticalFormLineRepository.TYPE_DETAIL) {
					if (logisticalFormLine.getGrossWeight() != null) {
						totalGrossWeight = totalGrossWeight.add(logisticalFormLine.getGrossWeight());
					}

					totalVolume = totalVolume
							.add(logisticalFormLineService.evalVolume(logisticalFormLine, scriptHelper));
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
