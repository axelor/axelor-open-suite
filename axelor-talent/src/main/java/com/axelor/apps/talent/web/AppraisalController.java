/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.talent.web;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.apps.talent.db.Appraisal;
import com.axelor.apps.talent.db.repo.AppraisalRepository;
import com.axelor.apps.talent.exception.TalentExceptionMessage;
import com.axelor.apps.talent.service.AppraisalService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Singleton;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

@Singleton
public class AppraisalController {

  public void send(ActionRequest request, ActionResponse response) {

    Appraisal appraisal = request.getContext().asType(Appraisal.class);

    try {
      appraisal = Beans.get(AppraisalRepository.class).find(appraisal.getId());

      Beans.get(AppraisalService.class).send(appraisal);

      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void realize(ActionRequest request, ActionResponse response) {

    Appraisal appraisal = request.getContext().asType(Appraisal.class);

    try {
      appraisal = Beans.get(AppraisalRepository.class).find(appraisal.getId());

      Beans.get(AppraisalService.class).realize(appraisal);

      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void cancel(ActionRequest request, ActionResponse response) {

    Appraisal appraisal = request.getContext().asType(Appraisal.class);

    try {
      appraisal = Beans.get(AppraisalRepository.class).find(appraisal.getId());

      Beans.get(AppraisalService.class).cancel(appraisal);

      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void draft(ActionRequest request, ActionResponse response) {

    Appraisal appraisal = request.getContext().asType(Appraisal.class);

    try {
      appraisal = Beans.get(AppraisalRepository.class).find(appraisal.getId());

      Beans.get(AppraisalService.class).draft(appraisal);

      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void createAppraisals(ActionRequest request, ActionResponse response) {

    try {
      Context context = request.getContext();

      Set<Map<String, Object>> employeeSet = new HashSet<Map<String, Object>>();

      employeeSet.addAll((Collection<? extends Map<String, Object>>) context.get("employeeSet"));

      Set<Employee> employees = new HashSet<Employee>();

      EmployeeRepository employeeRepo = Beans.get(EmployeeRepository.class);

      for (Map<String, Object> emp : employeeSet) {
        Long empId = Long.parseLong(emp.get("id").toString());
        employees.add(employeeRepo.find(empId));
      }

      Long templateId = Long.parseLong(context.get("templateId").toString());

      Appraisal appraisalTemplate = Beans.get(AppraisalRepository.class).find(templateId);

      Boolean send = (Boolean) context.get("sendAppraisals");

      Set<Long> createdIds =
          Beans.get(AppraisalService.class).createAppraisals(appraisalTemplate, employees, send);

      if (Boolean.TRUE.equals(send) && !CollectionUtils.isEmpty(createdIds)) {
        List<Appraisal> appraisalList =
            Beans.get(AppraisalRepository.class)
                .all()
                .filter("self.id IN :createdIds AND self.statusSelect != :statusSent")
                .bind("createdIds", createdIds)
                .bind("statusSent", AppraisalRepository.STATUS_SENT)
                .fetch();
        if (CollectionUtils.isNotEmpty(appraisalList)) {
          String employeeNames =
              appraisalList.stream()
                  .map(appraisal -> appraisal.getEmployee().getName())
                  .collect(Collectors.joining(", "));
          response.setInfo(
              String.format(I18n.get(TalentExceptionMessage.APPRAISALS_NOT_SENT), employeeNames));
        }
      }

      response.setView(
          ActionView.define(I18n.get("Appraisal"))
              .model(Appraisal.class.getName())
              .add("grid", "appraisal-grid")
              .add("form", "appraisal-form")
              .param("search-filters", "appraisal-fitlers")
              .domain("self.id in :createdIds")
              .context("createdIds", createdIds)
              .map());

      response.setCanClose(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
