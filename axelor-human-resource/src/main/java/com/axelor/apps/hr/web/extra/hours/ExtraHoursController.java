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
package com.axelor.apps.hr.web.extra.hours;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.base.service.message.MessageServiceBaseImpl;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.ExtraHours;
import com.axelor.apps.hr.db.repo.ExtraHoursRepository;
import com.axelor.apps.hr.service.HRMenuTagService;
import com.axelor.apps.hr.service.HRMenuValidateService;
import com.axelor.apps.hr.service.extra.hours.ExtraHoursDomainService;
import com.axelor.apps.hr.service.extra.hours.ExtraHoursService;
import com.axelor.apps.hr.service.extra.hours.ExtraHoursViewService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.message.db.Message;
import com.axelor.message.db.repo.MessageRepository;
import com.axelor.meta.CallMethod;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import jakarta.inject.Singleton;
import java.util.Map;
import java.util.Optional;

@Singleton
public class ExtraHoursController {

  public void editExtraHours(ActionRequest request, ActionResponse response) {
    User user = AuthUtils.getUser();
    Company company = Optional.ofNullable(user).map(User::getActiveCompany).orElse(null);

    response.setView(
        Beans.get(ExtraHoursViewService.class).buildEditExtraHoursView(user, company).map());
  }

  public void validateExtraHours(ActionRequest request, ActionResponse response)
      throws AxelorException {

    User user = AuthUtils.getUser();
    Employee employee = user.getEmployee();

    ActionViewBuilder actionView =
        ActionView.define(I18n.get("Extra hours to Validate"))
            .model(ExtraHours.class.getName())
            .add("grid", "extra-hours-validate-grid")
            .add("form", "extra-hours-form")
            .param("search-filters", "extra-hours-filters");

    Beans.get(HRMenuValidateService.class).createValidateDomain(user, employee, actionView);

    response.setView(actionView.map());
  }

  public void editExtraHoursSelected(ActionRequest request, ActionResponse response) {
    Map extraHoursMap = (Map) request.getContext().get("extraHoursSelect");
    Long extraHoursId = Long.valueOf((Integer) extraHoursMap.get("id"));

    response.setView(
        Beans.get(ExtraHoursViewService.class).buildEditSelectedExtraHoursView(extraHoursId).map());
  }

  public void historicExtraHours(ActionRequest request, ActionResponse response) {
    User user = AuthUtils.getUser();

    response.setView(
        Beans.get(ExtraHoursViewService.class)
            .buildHistoricExtraHoursView(user, user.getEmployee(), user.getActiveCompany())
            .map());
  }

  public void showSubordinateExtraHours(ActionRequest request, ActionResponse response) {
    try {
      User user = AuthUtils.getUser();
      response.setView(
          Beans.get(ExtraHoursViewService.class)
              .buildSubordinateExtraHoursView(user, user.getActiveCompany())
              .map());
    } catch (AxelorException e) {
      TraceBackService.trace(response, e, ResponseMessageType.INFORMATION);
    }
  }

  /* Count Tags displayed on the menu items */
  @CallMethod
  public String extraHoursValidateMenuTag() {

    return Beans.get(HRMenuTagService.class)
        .countRecordsTag(ExtraHours.class, ExtraHoursRepository.STATUS_CONFIRMED);
  }

  // confirming request and sending mail to manager
  public void confirm(ActionRequest request, ActionResponse response) {

    try {
      ExtraHours extraHours = request.getContext().asType(ExtraHours.class);
      extraHours = Beans.get(ExtraHoursRepository.class).find(extraHours.getId());
      Beans.get(ExtraHoursService.class).confirm(extraHours);

      Message message = Beans.get(ExtraHoursService.class).sendConfirmationEmail(extraHours);
      if (message != null && message.getStatusSelect() == MessageRepository.STATUS_SENT) {
        response.setInfo(
            String.format(
                I18n.get("Email sent to %s"),
                Beans.get(MessageServiceBaseImpl.class).getToRecipients(message)));
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    } finally {
      response.setReload(true);
    }
  }

  /**
   * validating request and sending mail to applicant
   *
   * @param request
   * @param response
   * @throws AxelorException
   */
  public void valid(ActionRequest request, ActionResponse response) {

    try {
      ExtraHours extraHours = request.getContext().asType(ExtraHours.class);
      extraHours = Beans.get(ExtraHoursRepository.class).find(extraHours.getId());
      Beans.get(ExtraHoursService.class).validate(extraHours);

      Message message = Beans.get(ExtraHoursService.class).sendValidationEmail(extraHours);
      if (message != null && message.getStatusSelect() == MessageRepository.STATUS_SENT) {
        response.setInfo(
            String.format(
                I18n.get("Email sent to %s"),
                Beans.get(MessageServiceBaseImpl.class).getToRecipients(message)));
      }
      Beans.get(PeriodService.class)
          .checkPeriod(extraHours.getCompany(), extraHours.getValidationDate());

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    } finally {
      response.setReload(true);
    }
  }

  // refusing request and sending mail to applicant
  public void refuse(ActionRequest request, ActionResponse response) {

    try {
      ExtraHours extraHours = request.getContext().asType(ExtraHours.class);
      extraHours = Beans.get(ExtraHoursRepository.class).find(extraHours.getId());
      Beans.get(ExtraHoursService.class).refuse(extraHours);

      Message message = Beans.get(ExtraHoursService.class).sendRefusalEmail(extraHours);
      if (message != null && message.getStatusSelect() == MessageRepository.STATUS_SENT) {
        response.setInfo(
            String.format(
                I18n.get("Email sent to %s"),
                Beans.get(MessageServiceBaseImpl.class).getToRecipients(message)));
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    } finally {
      response.setReload(true);
    }
  }

  // canceling request and sending mail to applicant
  public void cancel(ActionRequest request, ActionResponse response) {
    try {
      ExtraHours extraHours = request.getContext().asType(ExtraHours.class);
      extraHours = Beans.get(ExtraHoursRepository.class).find(extraHours.getId());
      Beans.get(ExtraHoursService.class).cancel(extraHours);

      Message message = Beans.get(ExtraHoursService.class).sendCancellationEmail(extraHours);
      if (message != null && message.getStatusSelect() == MessageRepository.STATUS_SENT) {
        response.setInfo(
            String.format(
                I18n.get("Email sent to %s"),
                Beans.get(MessageServiceBaseImpl.class).getToRecipients(message)));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    } finally {
      response.setReload(true);
    }
  }

  // counting total hours while computing extra hours lines
  public void compute(ActionRequest request, ActionResponse response) {
    try {
      ExtraHours extraHours = request.getContext().asType(ExtraHours.class);
      Beans.get(ExtraHoursService.class).compute(extraHours);
      response.setValue("totalQty", extraHours.getTotalQty());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void updateLineEmployee(ActionRequest request, ActionResponse response) {
    ExtraHours extraHours = request.getContext().asType(ExtraHours.class);
    Beans.get(ExtraHoursService.class).updateLineEmployee(extraHours);
    response.setValue("extraHoursLineList", extraHours.getExtraHoursLineList());
  }

  public void getEmployeeDomain(ActionRequest request, ActionResponse response) {
    response.setAttr(
        "employee", "domain", Beans.get(ExtraHoursDomainService.class).getEmployeeDomain());
  }
}
