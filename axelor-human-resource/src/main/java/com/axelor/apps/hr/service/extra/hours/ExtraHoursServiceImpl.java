/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.hr.service.extra.hours;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.ExtraHours;
import com.axelor.apps.hr.db.ExtraHoursLine;
import com.axelor.apps.hr.db.HRConfig;
import com.axelor.apps.hr.db.repo.ExtraHoursRepository;
import com.axelor.apps.hr.exception.IExceptionMessage;
import com.axelor.apps.hr.service.config.HRConfigService;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.service.TemplateMessageService;
import com.axelor.auth.AuthUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.mail.MessagingException;
import wslite.json.JSONException;

public class ExtraHoursServiceImpl implements ExtraHoursService {

  protected ExtraHoursRepository extraHoursRepo;
  protected AppBaseService appBaseService;
  protected HRConfigService hrConfigService;
  protected TemplateMessageService templateMessageService;

  @Inject
  public ExtraHoursServiceImpl(
      ExtraHoursRepository extraHoursRepo,
      AppBaseService appBaseService,
      HRConfigService hrConfigService,
      TemplateMessageService templateMessageService) {

    this.extraHoursRepo = extraHoursRepo;
    this.appBaseService = appBaseService;
    this.hrConfigService = hrConfigService;
    this.templateMessageService = templateMessageService;
  }

  @Transactional(rollbackOn = {Exception.class})
  public void cancel(ExtraHours extraHours) throws AxelorException {

    if (extraHours.getStatusSelect() == null
        || extraHours.getStatusSelect() == ExtraHoursRepository.STATUS_CANCELED) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.EXTRA_HOURS_CANCEL_WRONG_STATUS));
    }

    extraHours.setStatusSelect(ExtraHoursRepository.STATUS_CANCELED);
    extraHoursRepo.save(extraHours);
  }

  public Message sendCancellationEmail(ExtraHours extraHours)
      throws AxelorException, ClassNotFoundException, InstantiationException,
          IllegalAccessException, MessagingException, IOException, JSONException {

    HRConfig hrConfig = hrConfigService.getHRConfig(extraHours.getCompany());

    if (hrConfig.getTimesheetMailNotification()) {

      return templateMessageService.generateAndSendMessage(
          extraHours, hrConfigService.getCanceledExtraHoursTemplate(hrConfig));
    }

    return null;
  }

  @Transactional(rollbackOn = {Exception.class})
  public void confirm(ExtraHours extraHours) throws AxelorException {

    if (extraHours.getStatusSelect() == null
        || extraHours.getStatusSelect() != ExtraHoursRepository.STATUS_DRAFT) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.EXTRA_HOURS_CONFIRM_WRONG_STATUS));
    }

    extraHours.setStatusSelect(ExtraHoursRepository.STATUS_CONFIRMED);
    extraHours.setSentDate(appBaseService.getTodayDate(extraHours.getCompany()));

    extraHoursRepo.save(extraHours);
  }

  public Message sendConfirmationEmail(ExtraHours extraHours)
      throws AxelorException, ClassNotFoundException, InstantiationException,
          IllegalAccessException, MessagingException, IOException, JSONException {

    HRConfig hrConfig = hrConfigService.getHRConfig(extraHours.getCompany());

    if (hrConfig.getExtraHoursMailNotification()) {

      return templateMessageService.generateAndSendMessage(
          extraHours, hrConfigService.getSentExtraHoursTemplate(hrConfig));
    }

    return null;
  }

  @Transactional(rollbackOn = {Exception.class})
  public void validate(ExtraHours extraHours) throws AxelorException {

    if (extraHours.getStatusSelect() == null
        || extraHours.getStatusSelect() != ExtraHoursRepository.STATUS_CONFIRMED) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.EXTRA_HOURS_VALIDATE_WRONG_STATUS));
    }

    extraHours.setStatusSelect(ExtraHoursRepository.STATUS_VALIDATED);
    extraHours.setValidatedBy(AuthUtils.getUser());
    extraHours.setValidationDate(appBaseService.getTodayDate(extraHours.getCompany()));

    extraHoursRepo.save(extraHours);
  }

  public Message sendValidationEmail(ExtraHours extraHours)
      throws AxelorException, ClassNotFoundException, InstantiationException,
          IllegalAccessException, MessagingException, IOException, JSONException {

    HRConfig hrConfig = hrConfigService.getHRConfig(extraHours.getCompany());

    if (hrConfig.getExtraHoursMailNotification()) {

      return templateMessageService.generateAndSendMessage(
          extraHours, hrConfigService.getValidatedExtraHoursTemplate(hrConfig));
    }

    return null;
  }

  @Transactional(rollbackOn = {Exception.class})
  public void refuse(ExtraHours extraHours) throws AxelorException {

    if (extraHours.getStatusSelect() == null
        || extraHours.getStatusSelect() != ExtraHoursRepository.STATUS_CONFIRMED) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.EXTRA_HOURS_REFUSE_WRONG_STATUS));
    }

    extraHours.setStatusSelect(ExtraHoursRepository.STATUS_REFUSED);
    extraHours.setRefusedBy(AuthUtils.getUser());
    extraHours.setRefusalDate(appBaseService.getTodayDate(extraHours.getCompany()));

    extraHoursRepo.save(extraHours);
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void draft(ExtraHours extraHours) throws AxelorException {

    List<Integer> authorizedStatus = new ArrayList<>();
    authorizedStatus.add(ExtraHoursRepository.STATUS_REFUSED);
    authorizedStatus.add(ExtraHoursRepository.STATUS_CANCELED);

    if (extraHours.getStatusSelect() == null
        || !authorizedStatus.contains(extraHours.getStatusSelect())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.EXTRA_HOURS_DRAFT_WRONG_STATUS));
    }

    extraHours.setStatusSelect(ExtraHoursRepository.STATUS_DRAFT);
  }

  public Message sendRefusalEmail(ExtraHours extraHours)
      throws AxelorException, ClassNotFoundException, InstantiationException,
          IllegalAccessException, MessagingException, IOException, JSONException {

    HRConfig hrConfig = hrConfigService.getHRConfig(extraHours.getCompany());

    if (hrConfig.getExtraHoursMailNotification()) {

      return templateMessageService.generateAndSendMessage(
          extraHours, hrConfigService.getRefusedExtraHoursTemplate(hrConfig));
    }

    return null;
  }

  @Override
  public void compute(ExtraHours extraHours) {

    BigDecimal totalQty = BigDecimal.ZERO;
    List<ExtraHoursLine> extraHoursLines = extraHours.getExtraHoursLineList();

    for (ExtraHoursLine extraHoursLine : extraHoursLines) {
      totalQty = totalQty.add(extraHoursLine.getQty());
    }

    extraHours.setTotalQty(totalQty);
  }
}
