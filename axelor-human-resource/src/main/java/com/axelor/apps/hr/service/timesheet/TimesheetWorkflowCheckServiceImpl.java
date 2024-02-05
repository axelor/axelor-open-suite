package com.axelor.apps.hr.service.timesheet;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.app.AppHumanResourceService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;

public class TimesheetWorkflowCheckServiceImpl implements TimesheetWorkflowCheckService {

  protected AppHumanResourceService appHumanResourceService;
  protected TimesheetLineGenerationService timesheetLineGenerationService;
  protected PeriodService periodService;

  @Inject
  public TimesheetWorkflowCheckServiceImpl(
      AppHumanResourceService appHumanResourceService,
      TimesheetLineGenerationService timesheetLineGenerationService,
      PeriodService periodService) {
    this.appHumanResourceService = appHumanResourceService;
    this.timesheetLineGenerationService = timesheetLineGenerationService;
    this.periodService = periodService;
  }

  @Override
  public void confirmCheck(Timesheet timesheet) throws AxelorException {
    int statusSelect = timesheet.getStatusSelect();
    if (statusSelect != TimesheetRepository.STATUS_DRAFT) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(HumanResourceExceptionMessage.TIMESHEET_CONFIRM_COMPLETE_WRONG_STATUS));
    }
  }

  @Override
  public void validateCheck(Timesheet timesheet) throws AxelorException {
    int statusSelect = timesheet.getStatusSelect();
    if (statusSelect != TimesheetRepository.STATUS_CONFIRMED) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(HumanResourceExceptionMessage.TIMESHEET_VALIDATE_WRONG_STATUS));
    }

    timesheetLineGenerationService.checkEmptyPeriod(timesheet);
    periodService.checkPeriod(
        timesheet.getCompany(), timesheet.getToDate(), timesheet.getFromDate());
  }

  @Override
  public void refuseCheck(Timesheet timesheet) throws AxelorException {
    validationNeededConfigNotEnabled();
    int statusSelect = timesheet.getStatusSelect();
    if (statusSelect != TimesheetRepository.STATUS_CONFIRMED) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(HumanResourceExceptionMessage.TIMESHEET_REFUSE_WRONG_STATUS));
    }
  }

  @Override
  public void cancelCheck(Timesheet timesheet) throws AxelorException {
    validationNeededConfigNotEnabled();
    int statusSelect = timesheet.getStatusSelect();
    if (statusSelect == TimesheetRepository.STATUS_CANCELED) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(HumanResourceExceptionMessage.TIMESHEET_CANCEL_WRONG_STATUS));
    }
  }

  protected void validationNeededConfigNotEnabled() throws AxelorException {
    boolean needValidation = appHumanResourceService.getAppTimesheet().getNeedValidation();
    if (!needValidation) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(HumanResourceExceptionMessage.TIMESHEET_VALIDATION_NEEDED_NOT_ENABLED));
    }
  }
}
