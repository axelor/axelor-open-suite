package com.axelor.apps.base.service;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.auth.AuthUtils;
import com.axelor.inject.Beans;
import java.time.LocalDate;

public class DateService {

  public LocalDate date() {
    return Beans.get(AppBaseService.class)
        .getTodayDate(AuthUtils.getUser() != null ? AuthUtils.getUser().getActiveCompany() : null);
  }
}
