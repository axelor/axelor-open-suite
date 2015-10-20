package com.axelor.apps.cash.management.web;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.LocalDate;

import com.axelor.apps.cash.management.db.ForecastRecap;
import com.axelor.apps.cash.management.db.ForecastRecapLine;
import com.axelor.apps.cash.management.db.repo.ForecastRecapRepository;
import com.axelor.apps.cash.management.exception.IExceptionMessage;
import com.axelor.apps.cash.management.service.ForecastRecapService;
import com.axelor.apps.tool.net.URLService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class ForecastRecapController {
	
	@Inject
	protected ForecastRecapService forecastRecapService;
	
	public void populate(ActionRequest request, ActionResponse response) throws AxelorException{
		ForecastRecap forecastRecap = request.getContext().asType(ForecastRecap.class);
		if(forecastRecap.getCompany() == null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.FORECAST_COMPANY)), IException.CONFIGURATION_ERROR);
		}
		forecastRecapService.populate(forecastRecap);
		response.setValues(forecastRecap);
		
	}
	
	public void showReport(ActionRequest request, ActionResponse response) {

		ForecastRecap forecastRecap = request.getContext().asType(ForecastRecap.class);

		StringBuilder url = new StringBuilder();

		url.append(forecastRecapService.getURLForecastRecapPDF(forecastRecap));

		
		String urlNotExist = URLService.notExist(url.toString());

		if(urlNotExist == null) {

			String title = I18n.get("ForecastRecap");
			title += forecastRecap.getId();

			Map<String,Object> mapView = new HashMap<String,Object>();
			mapView.put("title", title);
			mapView.put("resource", url);
			mapView.put("viewType", "html");
			response.setView(mapView);
		}
		else {
			response.setFlash(urlNotExist);
		}
	}
	
	public void sales(ActionRequest request, ActionResponse response) throws AxelorException {
		Long id = new Long(request.getContext().get("_id").toString());
		ForecastRecap forecastRecap = Beans.get(ForecastRecapRepository.class).find(id);
		forecastRecap.setForecastRecapLineList(new ArrayList<ForecastRecapLine>());
		Map<LocalDate, BigDecimal> mapExpected = new HashMap<LocalDate, BigDecimal>();
		Map<LocalDate, BigDecimal> mapConfirmed = new HashMap<LocalDate, BigDecimal>();
		if(forecastRecap.getOpportunitiesTypeSelect() != null && forecastRecap.getOpportunitiesTypeSelect() != ForecastRecapRepository.OPPORTUNITY_TYPE_NO){
			forecastRecapService.getOpportunities(forecastRecap, mapExpected, mapConfirmed);
		}
		forecastRecapService.getInvoices(forecastRecap, mapExpected, mapConfirmed);
		forecastRecapService.getTimetablesOrOrders(forecastRecap, mapExpected, mapConfirmed);
		forecastRecapService.getForecasts(forecastRecap, mapExpected, mapConfirmed);
		List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
		Set<LocalDate> keyList = mapExpected.keySet();
		for (LocalDate date : keyList) {
			Map<String, Object> dataMap = new HashMap<String, Object>();
			dataMap.put("date", (Object)date);
			dataMap.put("amount", (Object)mapExpected.get(date));
			dataMap.put("type", (Object)I18n.get("Expected"));
			dataList.add(dataMap);
		}
		keyList = mapConfirmed.keySet();
		for (LocalDate date : keyList) {
			Map<String, Object> dataMap = new HashMap<String, Object>();
			dataMap.put("date", (Object)date);
			dataMap.put("amount", (Object)mapExpected.get(date));
			dataMap.put("type", (Object)I18n.get("Confirmed"));
			dataList.add(dataMap);
		}
	}
		
}
