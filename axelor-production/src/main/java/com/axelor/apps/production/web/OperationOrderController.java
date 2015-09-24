/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2014 Axelor (<http://axelor.com>).
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
package com.axelor.apps.production.web;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.Minutes;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.ReportSettings;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.repo.MachineRepository;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.apps.production.exceptions.IExceptionMessage;
import com.axelor.apps.production.report.IReport;
import com.axelor.apps.production.service.ManufOrderWorkflowService;
import com.axelor.apps.production.service.OperationOrderWorkflowService;
import com.axelor.apps.tool.net.URLService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Strings;
import com.google.inject.Inject;

public class OperationOrderController {
	
	@Inject
	protected MachineRepository machineRepo;
	
	@Inject
	protected OperationOrderRepository operationOrderRepo;
	
	@Inject
	protected GeneralService generalService;
	
	@Inject
	protected OperationOrderWorkflowService operationOrderWorkflowService;

	private static final Logger LOG = LoggerFactory.getLogger(ManufOrderController.class);
	
//	public void copyToConsume (ActionRequest request, ActionResponse response) {
//
//		OperationOrder operationOrder = request.getContext().asType( OperationOrder.class );
//
//		operationOrderService.copyToConsume(OperationOrder.find(operationOrder.getId()));
//		
//		response.setReload(true);
//		
//	}
	
	
//	public void copyToProduce (ActionRequest request, ActionResponse response) {
//
//		OperationOrder operationOrder = request.getContext().asType( OperationOrder.class );
//
//		operationOrderService.copyToProduce(OperationOrder.find(operationOrder.getId()));
//		
//		response.setReload(true);
//		
//	}
	
	
	public void computeDuration(ActionRequest request, ActionResponse response) {
		
		OperationOrder operationOrder = request.getContext().asType( OperationOrder.class );
		
		OperationOrderWorkflowService operationOrderWorkflowService = Beans.get(OperationOrderWorkflowService.class);
		
		if(operationOrder.getPlannedStartDateT() != null && operationOrder.getPlannedEndDateT() != null) {
			response.setValue("plannedDuration", 
					operationOrderWorkflowService.getDuration(
							operationOrderWorkflowService.computeDuration(operationOrder.getPlannedStartDateT(), operationOrder.getPlannedEndDateT())));
		}
		
		if(operationOrder.getRealStartDateT() != null && operationOrder.getRealEndDateT() != null) {
			response.setValue("realDuration", 
					operationOrderWorkflowService.getDuration(
							operationOrderWorkflowService.computeDuration(operationOrder.getRealStartDateT(), operationOrder.getRealEndDateT())));
		}
	}
	
	
	public void plan (ActionRequest request, ActionResponse response) throws AxelorException {
		OperationOrder operationOrder = request.getContext().asType( OperationOrder.class );
		
		operationOrder = operationOrderWorkflowService.plan(operationOrderRepo.find(operationOrder.getId()));
		
		response.setReload(true);
		
	}
	
	
	public void finish (ActionRequest request, ActionResponse response) throws AxelorException {

		OperationOrder operationOrder = request.getContext().asType( OperationOrder.class );
		OperationOrderWorkflowService operationOrderWorkflowService = Beans.get(OperationOrderWorkflowService.class);

		operationOrder = operationOrderWorkflowService.finish(operationOrderRepo.find(operationOrder.getId()));
		
		Beans.get(ManufOrderWorkflowService.class).allOpFinished(operationOrder.getManufOrder());
		
		response.setReload(true);
		
	}
	
	
	
	/**
	 * Fonction appeler par le bouton imprimer
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	public void print(ActionRequest request, ActionResponse response) {


		OperationOrder operationOrder = request.getContext().asType( OperationOrder.class );
		String operationOrderIds = "";

		@SuppressWarnings("unchecked")
		List<Integer> lstSelectedOperationOrder = (List<Integer>) request.getContext().get("_ids");
		if(lstSelectedOperationOrder != null){
			for(Integer it : lstSelectedOperationOrder) {
				operationOrderIds+= it.toString()+",";
			}
		}	
			
		if(!operationOrderIds.equals("")){
			operationOrderIds = operationOrderIds.substring(0, operationOrderIds.length()-1);	
			operationOrder =  operationOrderRepo.find(new Long(lstSelectedOperationOrder.get(0)));
		}else if(operationOrder.getId() != null){
			operationOrderIds = operationOrder.getId().toString();			
		}
		
		if(!operationOrderIds.equals("")){
			StringBuilder url = new StringBuilder();			
			User user = AuthUtils.getUser();
			
			Company company = null;
			if(operationOrder.getManufOrder() != null)  {
				company = operationOrder.getManufOrder().getCompany();
			}
			
			String language = "en";
			if(user != null && !Strings.isNullOrEmpty(user.getLanguage()))  {
				language = user.getLanguage();
			}
			else if(company != null && company.getPrintingSettings() != null && !Strings.isNullOrEmpty(company.getPrintingSettings().getLanguageSelect())) {
				language = company.getPrintingSettings().getLanguageSelect();
			}

			url.append(new ReportSettings(IReport.OPERATION_ORDER)
						.addParam("Locale", language)
						.addParam("__locale", "fr_FR")
						.addParam("OperationOrderId", operationOrderIds)
						.getUrl());
			
			LOG.debug("URL : {}", url);
			
			String urlNotExist = URLService.notExist(url.toString());
			if (urlNotExist == null){
				LOG.debug("Impression de l'Opération de production "+operationOrder.getName()+" : "+url.toString());
				
				String title = " ";
				if(operationOrder.getName() != null)  {
					title += lstSelectedOperationOrder == null ? "Op "+operationOrder.getName():"Ops";
				}
				
				Map<String,Object> mapView = new HashMap<String,Object>();
				mapView.put("title", title);
				mapView.put("resource", url);
				mapView.put("viewType", "html");
				response.setView(mapView);	
					
			}
			else {
				response.setFlash(urlNotExist);
			}
		}else{
			response.setFlash(I18n.get(IExceptionMessage.OPERATION_ORDER_1));
		}	
	}
	
	public void chargeByMachineHours(ActionRequest request, ActionResponse response) throws AxelorException {
		List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
		DateTimeFormatter parser = ISODateTimeFormat.dateTime();
		LocalDateTime fromDateTime = LocalDateTime.parse(request.getContext().get("fromDateTime").toString(),parser);
		LocalDateTime toDateTime = LocalDateTime.parse(request.getContext().get("toDateTime").toString(),parser);
		LocalDateTime itDateTime = new LocalDateTime(fromDateTime);
		
		if(Days.daysBetween(new LocalDate(fromDateTime.getYear(), fromDateTime.getMonthOfYear(), fromDateTime.getDayOfMonth()),
				new LocalDate(toDateTime.getYear(), toDateTime.getMonthOfYear(), toDateTime.getDayOfMonth())).getDays() > 20){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.CHARGE_MACHINE_DAYS)), IException.CONFIGURATION_ERROR);
		}
		
		List<OperationOrder> operationOrderListTemp = operationOrderRepo.all().filter("self.plannedStartDateT <= ?2 AND self.plannedEndDateT >= ?1", fromDateTime, toDateTime).fetch();
		Set<String> machineNameList = new HashSet<String>();
		for (OperationOrder operationOrder : operationOrderListTemp) {
			if(operationOrder.getWorkCenter() != null && operationOrder.getWorkCenter().getMachine() != null){
				if(!machineNameList.contains(operationOrder.getWorkCenter().getMachine().getName())){
					machineNameList.add(operationOrder.getWorkCenter().getMachine().getName());
				}
			}
		}
		while(!itDateTime.isAfter(toDateTime)){
			List<OperationOrder> operationOrderList = operationOrderRepo.all().filter("self.plannedStartDateT <= ?2 AND self.plannedEndDateT >= ?1", itDateTime, itDateTime.plusHours(1)).fetch();
			Map<String, BigDecimal> map = new HashMap<String, BigDecimal>();
			for (OperationOrder operationOrder : operationOrderList) {
				if(operationOrder.getWorkCenter() != null && operationOrder.getWorkCenter().getMachine() != null){
					String machine = operationOrder.getWorkCenter().getMachine().getName();
					int numberOfMinutes = 0;
					if(operationOrder.getPlannedStartDateT().isBefore(itDateTime)){
						numberOfMinutes = Minutes.minutesBetween(itDateTime, operationOrder.getPlannedEndDateT()).getMinutes();
					}
					else if(operationOrder.getPlannedEndDateT().isAfter(itDateTime.plusHours(1))){
						numberOfMinutes = Minutes.minutesBetween(operationOrder.getPlannedStartDateT(), itDateTime.plusHours(1)).getMinutes();
					}
					else{
						numberOfMinutes = Minutes.minutesBetween(operationOrder.getPlannedStartDateT(), operationOrder.getPlannedEndDateT()).getMinutes();
					}
					if(numberOfMinutes > 60){
						numberOfMinutes = 60;
					}
					BigDecimal percentage = new BigDecimal(numberOfMinutes).multiply(new BigDecimal(100)).divide(new BigDecimal(60), 2, RoundingMode.HALF_UP);
					if(map.containsKey(machine)){
						map.put(machine, map.get(machine).add(percentage));
					}
					else{
						map.put(machine, percentage);
					}
				}
			}
			Set<String> keyList = map.keySet();
			for (String key : machineNameList) {
				if(keyList.contains(key)){
					Map<String, Object> dataMap = new HashMap<String, Object>();
					if(Hours.hoursBetween(fromDateTime, toDateTime).getHours()>24){
						dataMap.put("dateTime",(Object)itDateTime.toString("dd/MM/yyyy HH:mm"));
					}
					else{
						dataMap.put("dateTime",(Object)itDateTime.toString("HH:mm"));
					}
					dataMap.put("charge", (Object)map.get(key));
					dataMap.put("machine", (Object) key);
					dataList.add(dataMap);
				}
				else{
					Map<String, Object> dataMap = new HashMap<String, Object>();
					if(Hours.hoursBetween(fromDateTime, toDateTime).getHours()>24){
						dataMap.put("dateTime",(Object)itDateTime.toString("dd/MM/yyyy HH:mm"));
					}
					else{
						dataMap.put("dateTime",(Object)itDateTime.toString("HH:mm"));
					}
					dataMap.put("charge", (Object)BigDecimal.ZERO);
					dataMap.put("machine", (Object) key);
					dataList.add(dataMap);
				}
			}
			
			
			itDateTime = itDateTime.plusHours(1);
		}
		
		
		response.setData(dataList);
	}
	
	public void chargeByMachineMinutes(ActionRequest request, ActionResponse response) throws AxelorException {
		List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
		DateTimeFormatter parser = ISODateTimeFormat.dateTime();
		LocalDateTime fromDateTime = LocalDateTime.parse(request.getContext().get("fromDateTime").toString(),parser);
		LocalDateTime toDateTime = LocalDateTime.parse(request.getContext().get("toDateTime").toString(),parser);
		LocalDateTime itDateTime = new LocalDateTime(fromDateTime);
		if(Days.daysBetween(new LocalDate(fromDateTime.getYear(), fromDateTime.getMonthOfYear(), fromDateTime.getDayOfMonth()),
				new LocalDate(toDateTime.getYear(), toDateTime.getMonthOfYear(), toDateTime.getDayOfMonth())).getDays() > 5){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.CHARGE_MACHINE_DAYS)), IException.CONFIGURATION_ERROR);
		}
		
		
		List<OperationOrder> operationOrderListTemp = operationOrderRepo.all().filter("self.plannedStartDateT <= ?2 AND self.plannedEndDateT >= ?1", fromDateTime, toDateTime).fetch();
		Set<String> machineNameList = new HashSet<String>();
		for (OperationOrder operationOrder : operationOrderListTemp) {
			if(operationOrder.getWorkCenter() != null && operationOrder.getWorkCenter().getMachine() != null){
				if(!machineNameList.contains(operationOrder.getWorkCenter().getMachine().getName())){
					machineNameList.add(operationOrder.getWorkCenter().getMachine().getName());
				}
			}
		}
		while(!itDateTime.isAfter(toDateTime)){
			List<OperationOrder> operationOrderList = operationOrderRepo.all().filter("self.plannedStartDateT <= ?1 AND self.plannedEndDateT >= ?1", itDateTime).fetch();
			Map<String, BigDecimal> map = new HashMap<String, BigDecimal>();
			for (OperationOrder operationOrder : operationOrderList) {
				if(operationOrder.getWorkCenter() != null && operationOrder.getWorkCenter().getMachine() != null){
					String machine = operationOrder.getWorkCenter().getMachine().getName();
					if(map.containsKey(machine)){
						map.put(machine, map.get(machine).add(BigDecimal.ONE));
					}
					else{
						map.put(machine, BigDecimal.ONE);
					}
				}
			}
			Set<String> keyList = map.keySet();
			
			for (String key : machineNameList) {
				if(keyList.contains(key)){
					Map<String, Object> dataMap = new HashMap<String, Object>();
					if(Hours.hoursBetween(fromDateTime, toDateTime).getHours()>24){
						dataMap.put("dateTime",(Object)itDateTime.toString("dd/MM/yyyy HH:mm"));
					}
					else{
						dataMap.put("dateTime",(Object)itDateTime.toString("HH:mm"));
					}
					dataMap.put("charge", (Object)map.get(key).multiply(new BigDecimal(100)));
					dataMap.put("machine", (Object) key);
					dataList.add(dataMap);
				}
				else{
					Map<String, Object> dataMap = new HashMap<String, Object>();
					if(Hours.hoursBetween(fromDateTime, toDateTime).getHours()>24){
						dataMap.put("dateTime",(Object)itDateTime.toString("dd/MM/yyyy HH:mm"));
					}
					else{
						dataMap.put("dateTime",(Object)itDateTime.toString("HH:mm"));
					}
					dataMap.put("charge", (Object)BigDecimal.ZERO);
					dataMap.put("machine", (Object) key);
					dataList.add(dataMap);
				}
			}
			
			
			itDateTime = itDateTime.plusMinutes(1);
		}
		
		response.setData(dataList);
	}
	
	public void start (ActionRequest request, ActionResponse response) throws AxelorException {
		OperationOrder operationOrder = request.getContext().asType( OperationOrder.class );
		operationOrder =operationOrderRepo.find(operationOrder.getId());
		Beans.get(ManufOrderWorkflowService.class).start(operationOrder.getManufOrder());
		response.setReload(true);
		
	}
	
}

