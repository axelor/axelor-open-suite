/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.birt.core.exception.BirtException;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.Minutes;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.app.production.db.IOperationOrder;
import com.axelor.apps.ReportFactory;
import com.axelor.apps.base.db.DayPlanning;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.apps.production.exceptions.IExceptionMessage;
import com.axelor.apps.production.report.IReport;
import com.axelor.apps.production.service.ManufOrderService;
import com.axelor.apps.production.service.ManufOrderWorkflowService;
import com.axelor.apps.production.service.OperationOrderWorkflowService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class OperationOrderController {
	
	@Inject
	protected OperationOrderRepository operationOrderRepo;
	
	@Inject
	protected OperationOrderWorkflowService operationOrderWorkflowService;
	
	@Inject
	protected ManufOrderService manufOrderService;
	
	@Inject
	protected WeeklyPlanningService weeklyPlanningService;
	

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
	
	public void machineChange(ActionRequest request, ActionResponse response) throws AxelorException{
		OperationOrder operationOrder = request.getContext().asType( OperationOrder.class );
		
		operationOrder = operationOrderRepo.find(operationOrder.getId());
		if(operationOrder != null && operationOrder.getStatusSelect() == IOperationOrder.STATUS_PLANNED){
			operationOrder = operationOrderWorkflowService.replan(operationOrder);
			List<OperationOrder> operationOrderList = operationOrderRepo.all().filter("self.manufOrder = ?1 AND self.priority >= ?2 AND self.statusSelect = 3 AND self.id != ?3",
					operationOrder.getManufOrder(), operationOrder.getPriority(), operationOrder.getId()).order("self.priority").order("self.plannedEndDateT").fetch();
			for (OperationOrder operationOrderIt : operationOrderList) {
				operationOrderIt = operationOrderWorkflowService.replan(operationOrderIt);
			}
			response.setReload(true);
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
	 * Method that generate a Pdf file for an operation order
	 *
	 * @param request
	 * @param response
	 * @return
	 * @throws BirtException 
	 * @throws IOException 
	 */
	public void print(ActionRequest request, ActionResponse response) throws AxelorException  {
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
			
			String name = " ";
			if(operationOrder.getName() != null)  {
				name += lstSelectedOperationOrder == null ? "Op "+operationOrder.getName():"Ops";
			}
			
			String fileLink = ReportFactory.createReport(IReport.OPERATION_ORDER, name+"-${date}")
					.addParam("Locale", manufOrderService.getLanguageToPrinting(operationOrder.getManufOrder()))
					.addParam("OperationOrderId", operationOrderIds)
					.generate()
					.getFileLink();

			LOG.debug("Printing "+name);
		
			response.setView(ActionView
					.define(name)
					.add("html", fileLink).map());
		}
		else{
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
					dataMap.put("dateTime",(Object)itDateTime.toString("dd/MM/yyyy HH:mm"));
					dataMap.put("charge", (Object)map.get(key));
					dataMap.put("machine", (Object) key);
					dataList.add(dataMap);
				}
				else{
					Map<String, Object> dataMap = new HashMap<String, Object>();
					dataMap.put("dateTime",(Object)itDateTime.toString("dd/MM/yyyy HH:mm"));
					dataMap.put("charge", (Object)BigDecimal.ZERO);
					dataMap.put("machine", (Object) key);
					dataList.add(dataMap);
				}
			}
			
			
			itDateTime = itDateTime.plusHours(1);
		}
		
		
		response.setData(dataList);
	}
	
	public void chargeByMachineDays(ActionRequest request, ActionResponse response) throws AxelorException {
		List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
		DateTimeFormatter parser = ISODateTimeFormat.dateTime();
		LocalDateTime fromDateTime = LocalDateTime.parse(request.getContext().get("fromDateTime").toString(),parser);
		fromDateTime = fromDateTime.withHourOfDay(0).withMinuteOfHour(0);
		LocalDateTime toDateTime = LocalDateTime.parse(request.getContext().get("toDateTime").toString(),parser);
		toDateTime = toDateTime.withHourOfDay(23).withMinuteOfHour(59);
		LocalDateTime itDateTime = new LocalDateTime(fromDateTime);
		if(Days.daysBetween(new LocalDate(fromDateTime.getYear(), fromDateTime.getMonthOfYear(), fromDateTime.getDayOfMonth()),
				new LocalDate(toDateTime.getYear(), toDateTime.getMonthOfYear(), toDateTime.getDayOfMonth())).getDays() > 500){
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
					int numberOfMinutesPerDay = 0;
					if(operationOrder.getWorkCenter().getMachine().getWeeklyPlanning() != null){
						DayPlanning dayPlanning = weeklyPlanningService.findDayPlanning(operationOrder.getWorkCenter().getMachine().getWeeklyPlanning(), new LocalDate(itDateTime));
						if(dayPlanning != null){
							numberOfMinutesPerDay = Minutes.minutesBetween(dayPlanning.getMorningFrom(), dayPlanning.getMorningTo()).getMinutes();
							numberOfMinutesPerDay += Minutes.minutesBetween(dayPlanning.getAfternoonFrom(), dayPlanning.getAfternoonTo()).getMinutes();
						}
						else{
							numberOfMinutesPerDay = 0;
						}
					}
					else{
						numberOfMinutesPerDay = 60*8;
					}
					if(numberOfMinutesPerDay != 0){
						BigDecimal percentage = new BigDecimal(numberOfMinutes).multiply(new BigDecimal(100)).divide(new BigDecimal(numberOfMinutesPerDay), 2, RoundingMode.HALF_UP);
						if(map.containsKey(machine)){
							map.put(machine, map.get(machine).add(percentage));
						}
						else{
							map.put(machine, percentage);
						}
					}
				}
			}
			Set<String> keyList = map.keySet();
			for (String key : machineNameList) {
				if(keyList.contains(key)){
					int found = 0;
					for (Map<String, Object> mapIt : dataList) {
						if(mapIt.get("dateTime").equals((Object)itDateTime.toString("dd/MM/yyyy")) &&
							mapIt.get("machine").equals((Object) key)){
							mapIt.put("charge", new BigDecimal(mapIt.get("charge").toString()).add(map.get(key)));
							found = 1;
							break;
						}

					}
					if(found == 0){
						Map<String, Object> dataMap = new HashMap<String, Object>();
						
						dataMap.put("dateTime",(Object)itDateTime.toString("dd/MM/yyyy"));
						dataMap.put("charge", (Object)map.get(key));
						dataMap.put("machine", (Object) key);
						dataList.add(dataMap);
					}
				}
			}
			
			
			itDateTime = itDateTime.plusHours(1);
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

