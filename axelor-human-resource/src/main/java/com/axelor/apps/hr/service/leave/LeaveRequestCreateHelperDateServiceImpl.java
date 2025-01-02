package com.axelor.apps.hr.service.leave;

import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.repo.LeaveRequestRepository;
import com.axelor.apps.hr.service.WorkingDayService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public class LeaveRequestCreateHelperDateServiceImpl
    implements LeaveRequestCreateHelperDateService {

  protected final WorkingDayService workingDayService;

  @Inject
  public LeaveRequestCreateHelperDateServiceImpl(WorkingDayService workingDayService) {
    this.workingDayService = workingDayService;
  }

  @Override
  public LocalDate computeNextStartDate(LocalDate toDate, int endOnSelect, int nextStartOnSelect) {
    Employee employee =
        Optional.ofNullable(AuthUtils.getUser()).map(User::getEmployee).orElse(null);
    if (endOnSelect == LeaveRequestRepository.SELECT_MORNING) {
      if (nextStartOnSelect == LeaveRequestRepository.SELECT_MORNING) {
        do {
          toDate = toDate.plusDays(1);
        } while (!workingDayService.isWorkingDay(employee, toDate));
      } else {
        return toDate;
      }
    }

    if (endOnSelect == LeaveRequestRepository.SELECT_AFTERNOON) {
      do {
        toDate = toDate.plusDays(1);
      } while (!workingDayService.isWorkingDay(employee, toDate));
      return toDate;
    }
    return toDate;
  }

  @Override
  public LocalDate computeNextToDate(LocalDate fromDate, BigDecimal duration, int startOnSelect) {
    Employee employee =
        Optional.ofNullable(AuthUtils.getUser()).map(User::getEmployee).orElse(null);

    if (startOnSelect == LeaveRequestRepository.SELECT_MORNING) {
      return getNextToDateFromMorningStart(fromDate, duration, employee);
    }

    if (startOnSelect == LeaveRequestRepository.SELECT_AFTERNOON) {
      return getNextToDateFromAfternoonStart(fromDate, duration, employee);
    }
    return null;
  }

  protected LocalDate getNextToDateFromMorningStart(
      LocalDate fromDate, BigDecimal duration, Employee employee) {
    if (duration.compareTo(BigDecimal.valueOf(1)) <= 0) {
      return fromDate;
    }
    int counter = 0;
    if (duration.remainder(BigDecimal.ONE).signum() == 0) {
      while (counter != duration.intValue() - 1) {
        fromDate = fromDate.plusDays(1);
        if (workingDayService.isWorkingDay(employee, fromDate)) {
          counter++;
        }
      }
    } else {
      while (counter != duration.intValue()) {
        fromDate = fromDate.plusDays(1);
        if (workingDayService.isWorkingDay(employee, fromDate)) {
          counter++;
        }
      }
    }
    return fromDate;
  }

  protected LocalDate getNextToDateFromAfternoonStart(
      LocalDate fromDate, BigDecimal duration, Employee employee) {
    if (duration.compareTo(BigDecimal.valueOf(0.5)) == 0) {
      return fromDate;
    }
    if (duration.compareTo(BigDecimal.ONE) == 0) {
      return fromDate.plusDays(1);
    }

    int counter = 0;
    while (counter != duration.intValue()) {
      fromDate = fromDate.plusDays(1);
      if (workingDayService.isWorkingDay(employee, fromDate)) {
        counter++;
      }
    }
    return fromDate;
  }

  @Override
  public int computeNextStartOnSelect(int endOfSelect) {
    if (endOfSelect == LeaveRequestRepository.SELECT_MORNING) {
      return LeaveRequestRepository.SELECT_AFTERNOON;
    } else {
      return LeaveRequestRepository.SELECT_MORNING;
    }
  }

  @Override
  public int computeEndOnSelect(BigDecimal duration, int startOnSelect) {
    if (startOnSelect == LeaveRequestRepository.SELECT_MORNING) {
      if (duration.remainder(BigDecimal.ONE).signum() == 0) {
        return LeaveRequestRepository.SELECT_AFTERNOON;
      } else {
        return LeaveRequestRepository.SELECT_MORNING;
      }
    }

    if (startOnSelect == LeaveRequestRepository.SELECT_AFTERNOON) {
      if (duration.remainder(BigDecimal.ONE).signum() == 0) {
        return LeaveRequestRepository.SELECT_MORNING;
      } else {
        return LeaveRequestRepository.SELECT_AFTERNOON;
      }
    }

    return 0;
  }
}
