/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.hr.service.config;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.hr.db.HRConfig;
import com.axelor.apps.hr.db.LeaveReason;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.i18n.I18n;
import com.axelor.message.db.Template;

public class HRConfigService {

  public HRConfig getHRConfig(Company company) throws AxelorException {
    HRConfig hrConfig = company.getHrConfig();
    if (hrConfig == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.HR_CONFIG),
          company.getName());
    }
    return hrConfig;
  }

  public Sequence getExpenseSequence(HRConfig hrConfig) throws AxelorException {
    Sequence sequence = hrConfig.getExpenseSequence();
    if (sequence == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.HR_CONFIG_NO_EXPENSE_SEQUENCE),
          hrConfig.getCompany().getName());
    }
    return sequence;
  }

  public LeaveReason getLeaveReason(HRConfig hrConfig) throws AxelorException {
    LeaveReason leaveReason = hrConfig.getToJustifyLeaveReason();
    if (leaveReason == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.HR_CONFIG_LEAVE_REASON),
          hrConfig.getCompany().getName());
    }
    return leaveReason;
  }

  public Product getKilometricExpenseProduct(HRConfig hrConfig) throws AxelorException {
    Product kilometricExpenseProduct = hrConfig.getKilometricExpenseProduct();
    if (kilometricExpenseProduct == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.HR_CONFIG_EXPENSE_TYPE),
          hrConfig.getCompany().getName());
    }
    return kilometricExpenseProduct;
  }

  // EXPENSE

  public Template getSentExpenseTemplate(HRConfig hrConfig) throws AxelorException {
    Template sentExpenseTemplate = hrConfig.getSentExpenseTemplate();
    if (sentExpenseTemplate == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.HR_CONFIG_SENT_EXPENSE_TEMPLATE),
          hrConfig.getCompany().getName());
    }
    return sentExpenseTemplate;
  }

  public Template getValidatedExpenseTemplate(HRConfig hrConfig) throws AxelorException {
    Template validatedExpenseTemplate = hrConfig.getValidatedExpenseTemplate();
    if (validatedExpenseTemplate == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.HR_CONFIG_VALIDATED_EXPENSE_TEMPLATE),
          hrConfig.getCompany().getName());
    }
    return validatedExpenseTemplate;
  }

  public Template getRefusedExpenseTemplate(HRConfig hrConfig) throws AxelorException {
    Template refusedExpenseTemplate = hrConfig.getRefusedExpenseTemplate();
    if (refusedExpenseTemplate == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.HR_CONFIG_REFUSED_EXPENSE_TEMPLATE),
          hrConfig.getCompany().getName());
    }
    return refusedExpenseTemplate;
  }

  public Template getCanceledExpenseTemplate(HRConfig hrConfig) throws AxelorException {
    Template refusedExpenseTemplate = hrConfig.getCanceledExpenseTemplate();
    if (refusedExpenseTemplate == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.HR_CONFIG_CANCELED_EXPENSE_TEMPLATE),
          hrConfig.getCompany().getName());
    }
    return refusedExpenseTemplate;
  }

  // LEAVE REQUEST

  public Template getSentLeaveTemplate(HRConfig hrConfig) throws AxelorException {
    Template sentLeaveTemplate = hrConfig.getSentLeaveTemplate();
    if (sentLeaveTemplate == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.HR_CONFIG_SENT_LEAVE_TEMPLATE),
          hrConfig.getCompany().getName());
    }
    return sentLeaveTemplate;
  }

  public Template getValidatedLeaveTemplate(HRConfig hrConfig) throws AxelorException {
    Template validatedLeaveTemplate = hrConfig.getValidatedLeaveTemplate();
    if (validatedLeaveTemplate == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.HR_CONFIG_VALIDATED_LEAVE_TEMPLATE),
          hrConfig.getCompany().getName());
    }
    return validatedLeaveTemplate;
  }

  public Template getRefusedLeaveTemplate(HRConfig hrConfig) throws AxelorException {
    Template refusedLeaveTemplate = hrConfig.getRefusedLeaveTemplate();
    if (refusedLeaveTemplate == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.HR_CONFIG_REFUSED_LEAVE_TEMPLATE),
          hrConfig.getCompany().getName());
    }
    return refusedLeaveTemplate;
  }

  public Template getCanceledLeaveTemplate(HRConfig hrConfig) throws AxelorException {
    Template refusedLeaveTemplate = hrConfig.getCanceledLeaveTemplate();
    if (refusedLeaveTemplate == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.HR_CONFIG_CANCELED_LEAVE_TEMPLATE),
          hrConfig.getCompany().getName());
    }
    return refusedLeaveTemplate;
  }

  // EXTRA HOURS

  public Template getSentExtraHoursTemplate(HRConfig hrConfig) throws AxelorException {
    Template sentExtraHoursTemplate = hrConfig.getSentExtraHoursTemplate();
    if (sentExtraHoursTemplate == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.HR_CONFIG_SENT_EXTRA_HOURS_TEMPLATE),
          hrConfig.getCompany().getName());
    }
    return sentExtraHoursTemplate;
  }

  public Template getValidatedExtraHoursTemplate(HRConfig hrConfig) throws AxelorException {
    Template validatedExtraHoursTemplate = hrConfig.getValidatedExtraHoursTemplate();
    if (validatedExtraHoursTemplate == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.HR_CONFIG_VALIDATED_EXTRA_HOURS_TEMPLATE),
          hrConfig.getCompany().getName());
    }
    return validatedExtraHoursTemplate;
  }

  public Template getRefusedExtraHoursTemplate(HRConfig hrConfig) throws AxelorException {
    Template refusedExtraHoursTemplate = hrConfig.getRefusedExtraHoursTemplate();
    if (refusedExtraHoursTemplate == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.HR_CONFIG_REFUSED_EXTRA_HOURS_TEMPLATE),
          hrConfig.getCompany().getName());
    }
    return refusedExtraHoursTemplate;
  }

  public Template getCanceledExtraHoursTemplate(HRConfig hrConfig) throws AxelorException {
    Template refusedExtraHoursTemplate = hrConfig.getCanceledExtraHoursTemplate();
    if (refusedExtraHoursTemplate == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.HR_CONFIG_CANCELED_EXTRA_HOURS_TEMPLATE),
          hrConfig.getCompany().getName());
    }
    return refusedExtraHoursTemplate;
  }
}
