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
package com.axelor.apps.hr.service.extra.hours;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.ExtraHours;
import com.axelor.apps.hr.db.repo.ExtraHoursRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.utils.db.Wizard;
import jakarta.inject.Inject;
import java.util.List;

public class ExtraHoursViewServiceImpl implements ExtraHoursViewService {

  protected ExtraHoursRepository extraHoursRepository;

  @Inject
  public ExtraHoursViewServiceImpl(ExtraHoursRepository extraHoursRepository) {
    this.extraHoursRepository = extraHoursRepository;
  }

  @Override
  public ActionViewBuilder buildEditExtraHoursView(User user, Company company) {
    List<ExtraHours> extraHoursList =
        extraHoursRepository
            .all()
            .filter(
                "self.employee.user.id = :userId AND self.company = :company AND self.statusSelect = :statusDraft")
            .bind("statusDraft", ExtraHoursRepository.STATUS_DRAFT)
            .bind("userId", user.getId())
            .bind("company", company)
            .fetch();
    if (extraHoursList.isEmpty()) {
      return buildExtraHoursRequestFormView(I18n.get("Extra Hours"));
    } else if (extraHoursList.size() == 1) {
      return buildSingleRecordEditView(extraHoursList.get(0).getId());
    } else {
      return ActionView.define(I18n.get("ExtraHours"))
          .model(Wizard.class.getName())
          .add("form", "popup-extra-hours-form")
          .param("forceEdit", "true")
          .param("popup", "true")
          .param("show-toolbar", "false")
          .param("show-confirm", "false")
          .param("popup-save", "false");
    }
  }

  @Override
  public ActionViewBuilder buildEditSelectedExtraHoursView(Long extraHoursId) {
    return buildSingleRecordEditView(extraHoursId);
  }

  @Override
  public ActionViewBuilder buildSubordinateExtraHoursView(User user, Company company)
      throws AxelorException {
    if (company == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.NO_ACTIVE_COMPANY));
    }

    String domain =
        "self.employee.managerUser.employee.managerUser = :_user AND self.company = :_activeCompany AND self.statusSelect = :statusConfirmed";

    long nbExtraHours =
        extraHoursRepository
            .all()
            .filter(domain)
            .bind("_user", user)
            .bind("_activeCompany", company)
            .bind("statusConfirmed", ExtraHoursRepository.STATUS_CONFIRMED)
            .count();

    if (nbExtraHours == 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(HumanResourceExceptionMessage.NO_SUBORDINATE_EXTRA_HOURS_TO_VALIDATE));
    }

    return buildExtraHoursListView(I18n.get("Extra hours to be Validated by your subordinates"))
        .domain(domain)
        .context("_user", user)
        .context("_activeCompany", company)
        .context("statusConfirmed", ExtraHoursRepository.STATUS_CONFIRMED);
  }

  @Override
  public ActionViewBuilder buildHistoricExtraHoursView(
      User user, Employee employee, Company company) {
    String domain =
        "self.company = :_activeCompany AND (self.statusSelect = :statusValidated OR self.statusSelect = :statusRefused)";

    ActionViewBuilder actionView =
        buildExtraHoursListView(I18n.get("Historic colleague extra hours"))
            .domain(domain)
            .context("_activeCompany", company)
            .context("statusValidated", ExtraHoursRepository.STATUS_VALIDATED)
            .context("statusRefused", ExtraHoursRepository.STATUS_REFUSED);

    if (employee == null || !employee.getHrManager()) {
      actionView.domain(domain + " AND self.employee.managerUser = :_user").context("_user", user);
    }

    return actionView;
  }

  protected ActionViewBuilder buildSingleRecordEditView(Long extraHoursId) {
    return buildExtraHoursRequestFormView(I18n.get("Extra hours"))
        .param("forceEdit", "true")
        .context("_showRecord", String.valueOf(extraHoursId));
  }

  protected ActionViewBuilder buildExtraHoursRequestFormView(String title) {
    return ActionView.define(title)
        .model(ExtraHours.class.getName())
        .add("form", "extra-hours-request-form")
        .context("_isEmployeeReadOnly", true);
  }

  protected ActionViewBuilder buildExtraHoursListView(String title) {
    return ActionView.define(title)
        .model(ExtraHours.class.getName())
        .add("grid", "extra-hours-grid")
        .add("form", "extra-hours-form")
        .param("search-filters", "extra-hours-filters");
  }
}
