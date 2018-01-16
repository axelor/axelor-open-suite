/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.axelor.app.production.db.IManufOrder;
import com.axelor.apps.production.service.OperationOrderService;
import com.axelor.exception.service.TraceBackService;
import org.eclipse.birt.core.exception.BirtException;
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

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class OperationOrderController {
	protected OperationOrderRepository operationOrderRepo;
	protected OperationOrderWorkflowService operationOrderWorkflowService;
	protected ManufOrderService manufOrderService;
	protected ManufOrderWorkflowService manufOrderWorkflowService;
	protected WeeklyPlanningService weeklyPlanningService;
	
	private static final DateTimeFormatter DATE_TIME_FORMAT =  DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
	private static final DateTimeFormatter DATE_FORMAT =  DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private static final Logger LOG = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	@Inject
	public OperationOrderController(OperationOrderRepository operationOrderRepo, OperationOrderWorkflowService operationOrderWorkflowService,
									ManufOrderService manufOrderService, ManufOrderWorkflowService manufOrderWorkflowService,
									WeeklyPlanningService weeklyPlanningService) {
		this.operationOrderRepo = operationOrderRepo;
		this.operationOrderWorkflowService = operationOrderWorkflowService;
		this.manufOrderService = manufOrderService;
		this.manufOrderWorkflowService = manufOrderWorkflowService;
		this.weeklyPlanningService = weeklyPlanningService;
	}

	
	
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
		operationOrder = operationOrderRepo.find(operationOrder.getId());
		
		operationOrderWorkflowService.computeDuration(operationOrder);
		response.setReload(true);

	}

	public void setPlannedDates(ActionRequest request, ActionResponse response) {
		OperationOrder operationOrder = request.getContext().asType(OperationOrder.class);
		LocalDateTime plannedStartDateT = operationOrder.getPlannedStartDateT();
		LocalDateTime plannedEndDateT = operationOrder.getPlannedEndDateT();
		operationOrder = operationOrderRepo.find(operationOrder.getId());
		operationOrderWorkflowService.setPlannedDates(operationOrder, plannedStartDateT, plannedEndDateT);
	}

	public void setRealDates(ActionRequest request, ActionResponse response) {
		OperationOrder operationOrder = request.getContext().asType(OperationOrder.class);
		LocalDateTime realStartDateT = operationOrder.getRealStartDateT();
		LocalDateTime realEndDateT = operationOrder.getRealEndDateT();
		operationOrder = operationOrderRepo.find(operationOrder.getId());
		operationOrderWorkflowService.setRealDates(operationOrder, realStartDateT, realEndDateT);
	}

	public void machineChange(ActionRequest request, ActionResponse response) throws AxelorException{
		OperationOrder operationOrder = request.getContext().asType( OperationOrder.class );
		
		operationOrder = operationOrderRepo.find(operationOrder.getId());
		if(operationOrder != null && operationOrder.getStatusSelect() == IOperationOrder.STATUS_PLANNED){
			operationOrder = operationOrderWorkflowService.replan(operationOrder);
			List<OperationOrder> operationOrderList = operationOrderRepo.all().filter("self.manufOrder = ?1 AND self.priority >= ?2 AND self.statusSelect = 3 AND self.id != ?3",
					operationOrder.getManufOrder(), operationOrder.getPriority(), operationOrder.getId()).order("priority").order("plannedEndDateT").fetch();
			for (OperationOrder operationOrderIt : operationOrderList) {
				operationOrderIt = operationOrderWorkflowService.replan(operationOrderIt);
			}
			response.setReload(true);
		}
	}

	public void plan (ActionRequest request, ActionResponse response) throws AxelorException {
		OperationOrder operationOrder = request.getContext().asType( OperationOrder.class );
		if (operationOrder.getManufOrder() != null
				&& operationOrder.getManufOrder().getStatusSelect() <
				IManufOrder.STATUS_PLANNED) {
		    return;
		}
		operationOrder = operationOrderWorkflowService.plan(operationOrderRepo.find(operationOrder.getId()));
		response.setReload(true);
	}

	public void pause(ActionRequest request, ActionResponse response) throws AxelorException {
		OperationOrder operationOrder = request.getContext().asType(OperationOrder.class);
		operationOrder = operationOrderRepo.find(operationOrder.getId());
		operationOrderWorkflowService.pause(operationOrder);

		response.setReload(true);
	}

	public void resume(ActionRequest request, ActionResponse response) throws AxelorException {
		OperationOrder operationOrder = request.getContext().asType(OperationOrder.class);
		operationOrder = operationOrderRepo.find(operationOrder.getId());
		manufOrderWorkflowService.resume(operationOrder.getManufOrder());

		response.setReload(true);
	}

	public void finish(ActionRequest request, ActionResponse response) throws AxelorException {
		OperationOrder operationOrder = request.getContext().asType(OperationOrder.class);
		//this attribute is not in the database, only in the view
		LocalDateTime realStartDateT = operationOrder.getRealStartDateT();
		operationOrder = operationOrderRepo.find(operationOrder.getId());
        operationOrder.setRealStartDateT(realStartDateT);
		operationOrderWorkflowService.finish(operationOrder);
		manufOrderWorkflowService.allOpFinished(operationOrder.getManufOrder());

		response.setReload(true);
	}

	public void cancel(ActionRequest request, ActionResponse response) throws AxelorException {
		OperationOrder operationOrder = request.getContext().asType(OperationOrder.class);
		operationOrderWorkflowService.cancel(operationOrderRepo.find(operationOrder.getId()));

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
		LocalDateTime fromDateTime = LocalDateTime.parse(request.getContext().get("fromDateTime").toString(),DateTimeFormatter.ISO_DATE_TIME);
		LocalDateTime toDateTime = LocalDateTime.parse(request.getContext().get("toDateTime").toString(),DateTimeFormatter.ISO_DATE_TIME);
		LocalDateTime itDateTime = LocalDateTime.parse(fromDateTime.toString(), DateTimeFormatter.ISO_DATE_TIME);
		
		if (Duration.between(fromDateTime,toDateTime).toDays() > 20) {
			throw new AxelorException(IException.CONFIGURATION_ERROR, I18n.get(IExceptionMessage.CHARGE_MACHINE_DAYS));
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
					long numberOfMinutes = 0;
					if(operationOrder.getPlannedStartDateT().isBefore(itDateTime)){
						numberOfMinutes = Duration.between(itDateTime, operationOrder.getPlannedEndDateT()).toMinutes();
					}
					else if(operationOrder.getPlannedEndDateT().isAfter(itDateTime.plusHours(1))){
						numberOfMinutes = Duration.between(operationOrder.getPlannedStartDateT(), itDateTime.plusHours(1)).toMinutes();
					}
					else{
						numberOfMinutes = Duration.between(operationOrder.getPlannedStartDateT(), operationOrder.getPlannedEndDateT()).toMinutes();
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
					dataMap.put("dateTime",(Object)itDateTime.format(DATE_TIME_FORMAT));
					dataMap.put("charge", (Object)map.get(key));
					dataMap.put("machine", (Object) key);
					dataList.add(dataMap);
				}
				else{
					Map<String, Object> dataMap = new HashMap<String, Object>();
					dataMap.put("dateTime",(Object)itDateTime.format(DATE_TIME_FORMAT));
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
		LocalDateTime fromDateTime = LocalDateTime.parse(request.getContext().get("fromDateTime").toString(), DateTimeFormatter.ISO_DATE_TIME);
		fromDateTime = fromDateTime.withHour(0).withMinute(0);
		LocalDateTime toDateTime = LocalDateTime.parse(request.getContext().get("toDateTime").toString(), DateTimeFormatter.ISO_DATE_TIME);
		toDateTime = toDateTime.withHour(23).withMinute(59);
		LocalDateTime itDateTime = LocalDateTime.parse(fromDateTime.toString(), DateTimeFormatter.ISO_DATE_TIME);
		if (Duration.between(fromDateTime,toDateTime).toDays() > 500) {
			throw new AxelorException(IException.CONFIGURATION_ERROR, I18n.get(IExceptionMessage.CHARGE_MACHINE_DAYS));
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
					long numberOfMinutes = 0;
					if(operationOrder.getPlannedStartDateT().isBefore(itDateTime)){
						numberOfMinutes = Duration.between(itDateTime, operationOrder.getPlannedEndDateT()).toMinutes();
					}
					else if(operationOrder.getPlannedEndDateT().isAfter(itDateTime.plusHours(1))){
						numberOfMinutes = Duration.between(operationOrder.getPlannedStartDateT(), itDateTime.plusHours(1)).toMinutes();
					}
					else{
						numberOfMinutes = Duration.between(operationOrder.getPlannedStartDateT(), operationOrder.getPlannedEndDateT()).toMinutes();
					}
					if(numberOfMinutes > 60){
						numberOfMinutes = 60;
					}
					long numberOfMinutesPerDay = 0;
					if(operationOrder.getWorkCenter().getMachine().getWeeklyPlanning() != null){
						DayPlanning dayPlanning = weeklyPlanningService.findDayPlanning(operationOrder.getWorkCenter().getMachine().getWeeklyPlanning(), LocalDateTime.parse(itDateTime.toString(), DateTimeFormatter.ISO_DATE_TIME).toLocalDate());
						if(dayPlanning != null){
							numberOfMinutesPerDay = Duration.between(dayPlanning.getMorningFrom(), dayPlanning.getMorningTo()).toMinutes();
							numberOfMinutesPerDay += Duration.between(dayPlanning.getAfternoonFrom(), dayPlanning.getAfternoonTo()).toMinutes();
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
						if(mapIt.get("dateTime").equals((Object)itDateTime.format(DATE_FORMAT)) &&
							mapIt.get("machine").equals((Object) key)){
							mapIt.put("charge", new BigDecimal(mapIt.get("charge").toString()).add(map.get(key)));
							found = 1;
							break;
						}

					}
					if(found == 0){
						Map<String, Object> dataMap = new HashMap<String, Object>();
						
						dataMap.put("dateTime",(Object)itDateTime.format(DATE_FORMAT));
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

	public void startManufOrder(ActionRequest request, ActionResponse response) throws AxelorException {
		OperationOrder operationOrder = request.getContext().asType( OperationOrder.class );
		operationOrder = operationOrderRepo.find(operationOrder.getId());
		Beans.get(OperationOrderWorkflowService.class).start(operationOrder);
		response.setReload(true);
	}

	public void start(ActionRequest request, ActionResponse response) throws AxelorException {
		OperationOrder operationOrder = request.getContext().asType(OperationOrder.class);
		operationOrder = operationOrderRepo.find(operationOrder.getId());
		Beans.get(OperationOrderWorkflowService.class).start(operationOrder);
		response.setReload(true);
	}

	public void updateDiffProdProductList(ActionRequest request, ActionResponse response) throws AxelorException {
		OperationOrder operationOrder = request.getContext().asType(OperationOrder.class);
		try {
		    Beans.get(OperationOrderService.class).updateDiffProdProductList(operationOrder);
			response.setValue("diffConsumeProdProductList", operationOrder.getDiffConsumeProdProductList());
		} catch (AxelorException e) {
			TraceBackService.trace(response, e);
		}
	}

}

