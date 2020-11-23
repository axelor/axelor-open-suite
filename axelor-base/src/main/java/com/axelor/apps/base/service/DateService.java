package com.axelor.apps.base.service;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.inject.Beans;
import java.time.LocalDate;
import java.util.Optional;

public class DateService {

  public LocalDate date() {
    return Beans.get(AppBaseService.class)
        .getTodayDate(
            Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null));
  }
}
