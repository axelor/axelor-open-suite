package com.axelor.apps.hr.service.leave;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.ICalendarEvent;
import com.axelor.apps.base.db.repo.ICalendarEventRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.LeaveLine;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.repo.LeaveLineRepository;
import com.axelor.apps.hr.db.repo.LeaveReasonRepository;
import com.axelor.apps.hr.db.repo.LeaveRequestRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LeaveRequestServiceImpl implements LeaveRequestService {

  protected LeaveRequestManagementService leaveRequestManagementService;
  protected LeaveLineService leaveLineService;
  protected LeaveRequestEventService leaveRequestEventService;
  protected LeaveLineRepository leaveLineRepository;
  protected LeaveRequestRepository leaveRequestRepository;
  protected ICalendarEventRepository iCalendarEventRepository;
  protected AppBaseService appBaseService;

  @Inject
  public LeaveRequestServiceImpl(
      LeaveRequestManagementService leaveRequestManagementService,
      LeaveLineService leaveLineService,
      LeaveRequestEventService leaveRequestEventService,
      LeaveLineRepository leaveLineRepository,
      LeaveRequestRepository leaveRequestRepository,
      ICalendarEventRepository iCalendarEventRepository,
      AppBaseService appBaseService) {
    this.leaveRequestManagementService = leaveRequestManagementService;
    this.leaveLineService = leaveLineService;
    this.leaveRequestEventService = leaveRequestEventService;
    this.leaveLineRepository = leaveLineRepository;
    this.leaveRequestRepository = leaveRequestRepository;
    this.iCalendarEventRepository = iCalendarEventRepository;
    this.appBaseService = appBaseService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void confirm(LeaveRequest leaveRequest) throws AxelorException {

    checkCompany(leaveRequest);
    if (leaveRequest.getLeaveReason().getManageAccumulation()) {
      leaveRequestManagementService.manageSentLeaves(leaveRequest);
    }

    leaveRequest.setStatusSelect(LeaveRequestRepository.STATUS_AWAITING_VALIDATION);
    leaveRequest.setRequestDate(appBaseService.getTodayDate(leaveRequest.getCompany()));

    leaveRequestRepository.save(leaveRequest);

    if (leaveRequest.getLeaveReason().getManageAccumulation()) {
      leaveLineService.updateDaysToValidate(leaveLineService.getLeaveLine(leaveRequest));
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void refuse(LeaveRequest leaveRequest) throws AxelorException {

    checkCompany(leaveRequest);
    if (leaveRequest.getLeaveReason().getManageAccumulation()) {
      leaveRequestManagementService.manageRefuseLeaves(leaveRequest);
    }

    leaveRequest.setStatusSelect(LeaveRequestRepository.STATUS_REFUSED);
    leaveRequest.setRefusedBy(AuthUtils.getUser());
    leaveRequest.setRefusalDateTime(
        appBaseService.getTodayDateTime(leaveRequest.getCompany()).toLocalDateTime());

    leaveRequestRepository.save(leaveRequest);
    if (leaveRequest.getLeaveReason().getManageAccumulation()) {
      leaveLineService.updateDaysToValidate(leaveLineService.getLeaveLine(leaveRequest));
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void validate(LeaveRequest leaveRequest) throws AxelorException {

    checkCompany(leaveRequest);
    if (leaveRequest.getLeaveReason().getUnitSelect() == LeaveReasonRepository.UNIT_SELECT_DAYS) {
      isOverlapped(leaveRequest);
    }
    if (leaveRequest.getLeaveReason().getManageAccumulation()) {
      leaveRequestManagementService.manageValidateLeaves(leaveRequest);
    }

    leaveRequest.setStatusSelect(LeaveRequestRepository.STATUS_VALIDATED);
    leaveRequest.setValidatedBy(AuthUtils.getUser());
    leaveRequest.setValidationDateTime(
        appBaseService.getTodayDateTime(leaveRequest.getCompany()).toLocalDateTime());

    LeaveLine leaveLine = leaveLineService.getLeaveLine(leaveRequest);
    if (leaveLine != null) {
      leaveRequest.setQuantityBeforeValidation(leaveLine.getQuantity());
    }
    leaveRequestRepository.save(leaveRequest);

    if (leaveRequest.getLeaveReason().getManageAccumulation()) {
      leaveLineService.updateDaysToValidate(leaveLine);
    }
    leaveRequestEventService.createEvents(leaveRequest);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void cancel(LeaveRequest leaveRequest) throws AxelorException {

    checkCompany(leaveRequest);
    if (leaveRequest.getLeaveReason().getManageAccumulation()) {
      leaveRequestManagementService.manageCancelLeaves(leaveRequest);
    }

    if (leaveRequest.getIcalendarEvent() != null) {
      ICalendarEvent event = leaveRequest.getIcalendarEvent();
      leaveRequest.setIcalendarEvent(null);
      iCalendarEventRepository.remove(iCalendarEventRepository.find(event.getId()));
    }
    leaveRequest.setStatusSelect(LeaveRequestRepository.STATUS_CANCELED);
    leaveRequestRepository.save(leaveRequest);

    if (leaveRequest.getLeaveReason().getManageAccumulation()) {
      leaveLineService.updateDaysToValidate(leaveLineService.getLeaveLine(leaveRequest));
    }
  }

  protected void isOverlapped(LeaveRequest leaveRequest) throws AxelorException {
    List<LeaveRequest> leaveRequestList =
        leaveRequestRepository
            .all()
            .filter(
                "self.employee = ?1 AND self.statusSelect = ?2",
                leaveRequest.getEmployee(),
                LeaveRequestRepository.STATUS_VALIDATED)
            .fetch();
    for (LeaveRequest leaveRequest2 : leaveRequestList) {
      if (isOverlapped(leaveRequest, leaveRequest2)) {
        throw new AxelorException(
            leaveRequest,
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(HumanResourceExceptionMessage.LEAVE_REQUEST_DATES_OVERLAPPED));
      }
    }
  }

  protected boolean isOverlapped(LeaveRequest request1, LeaveRequest request2) {

    if (isDatesNonOverlapped(request1, request2)
        || isSelectsNonOverlapped(request1, request2)
        || isSelectsNonOverlapped(request2, request1)) {
      return false;
    }

    return true;
  }

  protected boolean isDatesNonOverlapped(LeaveRequest request1, LeaveRequest request2) {
    return request2.getToDateT().isBefore(request1.getFromDateT())
        || request1.getToDateT().isBefore(request2.getFromDateT())
        || request1.getToDateT().isBefore(request1.getFromDateT())
        || request2.getToDateT().isBefore(request2.getFromDateT());
  }

  protected boolean isSelectsNonOverlapped(LeaveRequest request1, LeaveRequest request2) {
    return request1.getEndOnSelect() == LeaveRequestRepository.SELECT_MORNING
        && request2.getStartOnSelect() == LeaveRequestRepository.SELECT_AFTERNOON
        && request1.getToDateT().isEqual(request2.getFromDateT());
  }

  protected void checkCompany(LeaveRequest leaveRequest) throws AxelorException {

    if (ObjectUtils.isEmpty(leaveRequest.getCompany())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(HumanResourceExceptionMessage.LEAVE_REQUEST_NO_COMPANY));
    }
  }

  public List<LeaveRequest> getLeaves(Employee employee, LocalDate date) {
    List<LeaveRequest> leavesList = new ArrayList<>();
    List<LeaveRequest> leaves =
        leaveRequestRepository
            .all()
            .filter(
                "self.employee = :employee AND self.statusSelect IN (:awaitingValidation,:validated)")
            .bind("employee", employee)
            .bind("awaitingValidation", LeaveRequestRepository.STATUS_AWAITING_VALIDATION)
            .bind("validated", LeaveRequestRepository.STATUS_VALIDATED)
            .fetch();

    if (ObjectUtils.notEmpty(leaves)) {
      for (LeaveRequest leave : leaves) {
        LocalDate from = leave.getFromDateT().toLocalDate();
        LocalDate to = leave.getToDateT().toLocalDate();
        if ((from.isBefore(date) && to.isAfter(date)) || from.isEqual(date) || to.isEqual(date)) {
          leavesList.add(leave);
        }
      }
    }
    return leavesList;
  }

  @Override
  public boolean willHaveEnoughDays(LeaveRequest leaveRequest) {

    LocalDateTime todayDate = appBaseService.getTodayDateTime().toLocalDateTime();
    LocalDateTime beginDate = leaveRequest.getFromDateT();

    int interval =
        (beginDate.getYear() - todayDate.getYear()) * 12
            + beginDate.getMonthValue()
            - todayDate.getMonthValue();
    LeaveLine leaveLine =
        leaveLineRepository
            .all()
            .filter("self.leaveReason = :leaveReason AND self.employee = :employee")
            .bind("leaveReason", leaveRequest.getLeaveReason())
            .bind("employee", leaveRequest.getEmployee())
            .fetchOne();
    if (leaveLine == null) {
      if (leaveRequest.getLeaveReason() != null
          && !leaveRequest.getLeaveReason().getManageAccumulation()) {
        return true;
      }

      return false;
    }

    BigDecimal num =
        leaveLine
            .getQuantity()
            .add(
                leaveRequest
                    .getEmployee()
                    .getWeeklyPlanning()
                    .getLeaveCoef()
                    .multiply(leaveRequest.getLeaveReason().getDefaultDayNumberGain())
                    .multiply(BigDecimal.valueOf(interval)));

    return leaveRequest.getDuration().compareTo(num) <= 0;
  }

  @Override
  public String getLeaveCalendarDomain(User user) {

    StringBuilder domain = new StringBuilder("self.statusSelect = 3");
    Employee employee = user.getEmployee();

    if (employee == null || !employee.getHrManager()) {
      domain.append(
          " AND (self.employee.managerUser.id = :userId OR self.employee.user.id = :userId)");
    }

    return domain.toString();
  }

  @Override
  public boolean isLeaveDay(Employee employee, LocalDate date) {
    return ObjectUtils.notEmpty(getLeaves(employee, date));
  }
}
