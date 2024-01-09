package com.axelor.apps.hr.service.timesheet;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.hr.db.HRConfig;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.app.AppHumanResourceService;
import com.axelor.apps.hr.service.config.HRConfigService;
import com.axelor.auth.AuthUtils;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.message.db.Message;
import com.axelor.message.db.Template;
import com.axelor.message.service.TemplateMessageService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import wslite.json.JSONException;

public class TimesheetWorkflowServiceImpl implements TimesheetWorkflowService {

  protected AppHumanResourceService appHumanResourceService;
  protected HRConfigService hrConfigService;
  protected TemplateMessageService templateMessageService;
  protected TimesheetRepository timesheetRepository;

  @Inject
  public TimesheetWorkflowServiceImpl(
      AppHumanResourceService appHumanResourceService,
      HRConfigService hrConfigService,
      TemplateMessageService templateMessageService,
      TimesheetRepository timesheetRepository) {
    this.appHumanResourceService = appHumanResourceService;
    this.hrConfigService = hrConfigService;
    this.templateMessageService = templateMessageService;
    this.timesheetRepository = timesheetRepository;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void confirm(Timesheet timesheet) throws AxelorException {
    this.fillToDate(timesheet);
    this.validateDates(timesheet);

    timesheet.setStatusSelect(TimesheetRepository.STATUS_CONFIRMED);
    timesheet.setSentDateTime(
        appHumanResourceService.getTodayDateTime(timesheet.getCompany()).toLocalDateTime());
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Message sendConfirmationEmail(Timesheet timesheet)
      throws AxelorException, ClassNotFoundException, IOException, JSONException {

    HRConfig hrConfig = hrConfigService.getHRConfig(timesheet.getCompany());
    Template template = hrConfig.getSentTimesheetTemplate();

    if (hrConfig.getTimesheetMailNotification() && template != null) {
      return templateMessageService.generateAndSendMessage(timesheet, template);
    }

    return null;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Message confirmAndSendConfirmationEmail(Timesheet timesheet)
      throws AxelorException, ClassNotFoundException, IOException, JSONException {
    confirm(timesheet);
    return sendConfirmationEmail(timesheet);
  }

  @Override
  @Transactional
  public void validate(Timesheet timesheet) {
    timesheet.setIsCompleted(true);
    timesheet.setStatusSelect(TimesheetRepository.STATUS_VALIDATED);
    timesheet.setValidatedBy(AuthUtils.getUser());
    timesheet.setValidationDateTime(
        appHumanResourceService.getTodayDateTime(timesheet.getCompany()).toLocalDateTime());
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Message sendValidationEmail(Timesheet timesheet)
      throws AxelorException, ClassNotFoundException, IOException, JSONException {

    HRConfig hrConfig = hrConfigService.getHRConfig(timesheet.getCompany());
    Template template = hrConfig.getValidatedTimesheetTemplate();

    if (hrConfig.getTimesheetMailNotification() && template != null) {

      return templateMessageService.generateAndSendMessage(timesheet, template);
    }

    return null;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Message validateAndSendValidationEmail(Timesheet timesheet)
      throws AxelorException, ClassNotFoundException, IOException, JSONException {
    validate(timesheet);
    return sendValidationEmail(timesheet);
  }

  @Override
  @Transactional
  public void refuse(Timesheet timesheet) {

    timesheet.setStatusSelect(TimesheetRepository.STATUS_REFUSED);
    timesheet.setRefusedBy(AuthUtils.getUser());
    timesheet.setRefusalDateTime(
        appHumanResourceService.getTodayDateTime(timesheet.getCompany()).toLocalDateTime());
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Message sendRefusalEmail(Timesheet timesheet)
      throws AxelorException, ClassNotFoundException, IOException, JSONException {

    HRConfig hrConfig = hrConfigService.getHRConfig(timesheet.getCompany());
    Template template = hrConfig.getRefusedTimesheetTemplate();

    if (hrConfig.getTimesheetMailNotification() && template != null) {

      return templateMessageService.generateAndSendMessage(timesheet, template);
    }

    return null;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Message refuseAndSendRefusalEmail(Timesheet timesheet)
      throws AxelorException, ClassNotFoundException, IOException, JSONException {
    refuse(timesheet);
    return sendRefusalEmail(timesheet);
  }

  @Override
  @Transactional
  public void cancel(Timesheet timesheet) {
    timesheet.setStatusSelect(TimesheetRepository.STATUS_CANCELED);
  }

  @Override
  @Transactional
  public void draft(Timesheet timesheet) {
    timesheet.setStatusSelect(TimesheetRepository.STATUS_DRAFT);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Message sendCancellationEmail(Timesheet timesheet)
      throws AxelorException, ClassNotFoundException, IOException, JSONException {

    HRConfig hrConfig = hrConfigService.getHRConfig(timesheet.getCompany());
    Template template = hrConfig.getCanceledTimesheetTemplate();

    if (hrConfig.getTimesheetMailNotification() && template != null) {

      return templateMessageService.generateAndSendMessage(timesheet, template);
    }

    return null;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Message cancelAndSendCancellationEmail(Timesheet timesheet)
      throws AxelorException, ClassNotFoundException, IOException, JSONException {
    cancel(timesheet);
    return sendCancellationEmail(timesheet);
  }

  /**
   * Checks validity of dates related to the timesheet.
   *
   * @param timesheet
   * @throws AxelorException if
   *     <ul>
   *       <li>fromDate of the timesheet is null
   *       <li>toDate of the timesheet is null
   *       <li>timesheetLineList of the timesheet is null or empty
   *       <li>date of a timesheet line is null
   *       <li>date of a timesheet line is before fromDate or after toDate of the timesheet
   *     </ul>
   */
  protected void validateDates(Timesheet timesheet) throws AxelorException {

    List<TimesheetLine> timesheetLineList = timesheet.getTimesheetLineList();
    LocalDate fromDate = timesheet.getFromDate();
    LocalDate toDate = timesheet.getToDate();

    if (fromDate == null) {
      throw new AxelorException(
          timesheet,
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(HumanResourceExceptionMessage.TIMESHEET_NULL_FROM_DATE));

    } else if (toDate == null) {
      throw new AxelorException(
          timesheet,
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(HumanResourceExceptionMessage.TIMESHEET_NULL_TO_DATE));

    } else if (ObjectUtils.isEmpty(timesheetLineList)) {
      throw new AxelorException(
          timesheet,
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(HumanResourceExceptionMessage.TIMESHEET_TIMESHEET_LINE_LIST_IS_EMPTY));

    } else {

      for (TimesheetLine timesheetLine : timesheetLineList) {
        LocalDate timesheetLineDate = timesheetLine.getDate();
        if (timesheetLineDate == null) {
          throw new AxelorException(
              timesheetLine,
              TraceBackRepository.CATEGORY_MISSING_FIELD,
              I18n.get(HumanResourceExceptionMessage.TIMESHEET_LINE_NULL_DATE),
              timesheetLineList.indexOf(timesheetLine) + 1);
        }
      }
    }
  }

  /**
   * If the toDate field of the timesheet is empty, fill it with the last timesheet line date.
   *
   * @param timesheet
   * @throws AxelorException
   */
  protected void fillToDate(Timesheet timesheet) throws AxelorException {
    if (timesheet.getToDate() == null) {

      List<TimesheetLine> timesheetLineList = timesheet.getTimesheetLineList();
      if (timesheetLineList.isEmpty()) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_NO_VALUE,
            I18n.get(HumanResourceExceptionMessage.TIMESHEET_TIMESHEET_LINE_LIST_IS_EMPTY));
      }

      LocalDate timesheetLineLastDate = timesheetLineList.get(0).getDate();
      if (timesheetLineLastDate == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            I18n.get(HumanResourceExceptionMessage.TIMESHEET_LINE_NULL_DATE),
            1);
      }

      for (TimesheetLine timesheetLine : timesheetLineList.subList(1, timesheetLineList.size())) {
        LocalDate timesheetLineDate = timesheetLine.getDate();
        if (timesheetLineDate == null) {
          throw new AxelorException(
              timesheetLine,
              TraceBackRepository.CATEGORY_MISSING_FIELD,
              I18n.get(HumanResourceExceptionMessage.TIMESHEET_LINE_NULL_DATE),
              timesheetLineList.indexOf(timesheetLine) + 1);
        }
        if (timesheetLineDate.isAfter(timesheetLineLastDate)) {
          timesheetLineLastDate = timesheetLineDate;
        }
      }

      timesheet.setToDate(timesheetLineLastDate);
    }
  }
}
