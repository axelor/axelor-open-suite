package com.axelor.apps.stock.web;

import java.util.Map;

import com.axelor.apps.stock.db.LogisticalForm;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.LogisticalFormService;
import com.axelor.db.mapper.Mapper;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class LogisticalFormController {

	public void addStockMove(ActionRequest request, ActionResponse response) {
		try {
			@SuppressWarnings("unchecked")
			Map<String, Object> stockMoveMap = (Map<String, Object>) request.getContext().get("stockMove");
			if (stockMoveMap != null) {
				StockMove stockMove = Mapper.toBean(StockMove.class, stockMoveMap);
				stockMove = Beans.get(StockMoveRepository.class).find(stockMove.getId());

				if (stockMove.getStockMoveLineList() != null) {
					LogisticalForm logisticalForm = request.getContext().asType(LogisticalForm.class);
					Beans.get(LogisticalFormService.class).addLines(logisticalForm, stockMove);
					response.setValue("logisticalFormLineList", logisticalForm.getLogisticalFormLineList());
					response.setValue("stockMove", null);
				}
			}
		} catch (Exception e) {
			TraceBackService.trace(response, e);
		}
	}

	public void compute(ActionRequest request, ActionResponse response) {
		try {
			LogisticalForm logisticalForm = request.getContext().asType(LogisticalForm.class);
			Beans.get(LogisticalFormService.class).compute(logisticalForm);
			response.setValue("totalNetWeight", logisticalForm.getTotalNetWeight());
			response.setValue("totalGrossWeight", logisticalForm.getTotalGrossWeight());
			response.setValue("totalVolume", logisticalForm.getTotalVolume());
		} catch (Exception e) {
			TraceBackService.trace(response, e);
		}
	}

}
