/**
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
package com.axelor.apps.hr.web;

import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

import com.axelor.apps.hr.db.EmploymentContract;
import com.axelor.apps.hr.db.ExtraHoursLine;
import com.axelor.apps.hr.db.PayrollLeave;
import com.axelor.apps.hr.db.PayrollPreparation;
import com.axelor.apps.hr.db.repo.EmploymentContractRepository;
import com.axelor.apps.hr.db.repo.HrBatchRepository;
import com.axelor.apps.hr.db.repo.PayrollLeaveRepository;
import com.axelor.apps.hr.db.repo.PayrollPreparationRepository;
import com.axelor.apps.hr.service.PayrollPreparationService;
import com.axelor.db.mapper.Mapper;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class PayrollPreparationController {

	@Inject
	protected PayrollPreparationService payrollPreparationService;
	
	@Inject
	protected PayrollPreparationRepository payrollPreparationRepo;
	
	@Inject
	protected PayrollLeaveRepository payrollLeaveRepo;
	
	public void generateFromEmploymentContract(ActionRequest request, ActionResponse response){

		PayrollPreparation payrollPreparation = request.getContext().asType(PayrollPreparation.class);
		EmploymentContract employmentContract = Beans.get(EmploymentContractRepository.class).find(new Long(request.getContext().get("_idEmploymentContract").toString()));

		response.setValues(payrollPreparationService.generateFromEmploymentContract(payrollPreparation, employmentContract));
	}
	
    public void fillInPayrollPreparation(ActionRequest request, ActionResponse response) throws AxelorException {
        try {
            PayrollPreparation payrollPreparation = request.getContext().asType(PayrollPreparation.class);

            List<PayrollLeave> payrollLeaveList = payrollPreparationService
                    .fillInPayrollPreparation(payrollPreparation);
            List<ExtraHoursLine> extraHoursLineList = payrollPreparationService
                    .getExtraHoursLineList(payrollPreparation);

            for (Entry<String, Object> entry : Mapper.toMap(payrollPreparation).entrySet()) {
                response.setValue(entry.getKey(), entry.getValue());
            }

            response.setValue("$payrollLeaveList", payrollLeaveList);
            response.setValue("$extraHoursLineList", extraHoursLineList);
        } catch (Exception e) {
            TraceBackService.trace(response, e);
        }
    }

    public void fillInPayrollPreparationLists(ActionRequest request, ActionResponse response) throws AxelorException {
        try {
            PayrollPreparation payrollPreparation = request.getContext().asType(PayrollPreparation.class);

            List<PayrollLeave> payrollLeaveList = payrollPreparationService.fillInLeaves(payrollPreparation);
            List<ExtraHoursLine> extraHoursLineList = payrollPreparationService
                    .getExtraHoursLineList(payrollPreparation);

            response.setValue("$payrollLeaveList", payrollLeaveList);
            response.setValue("$extraHoursLineList", extraHoursLineList);
        } catch (Exception e) {
            TraceBackService.trace(response, e);
        }
    }

	public void exportPayrollPreparation(ActionRequest request, ActionResponse response) throws IOException, AxelorException{
		
		PayrollPreparation payrollPreparation = payrollPreparationRepo.find( request.getContext().asType(PayrollPreparation.class).getId() );
		
		if (payrollPreparation.getExportTypeSelect() == HrBatchRepository.EXPORT_TYPE_STANDARD){
			response.setExportFile( payrollPreparationService.exportSinglePayrollPreparation(payrollPreparation) );
		}else if (payrollPreparation.getExportTypeSelect() == HrBatchRepository.EXPORT_TYPE_MEILLEURE_GESTION){
			response.setExportFile( payrollPreparationService.exportMeilleureGestionPayrollPreparation(payrollPreparation) );
		}
		
		response.setReload(true);
		
	}
	
}
