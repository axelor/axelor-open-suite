package com.axelor.apps.hr.service;

import com.axelor.meta.db.MetaSchedule;
import com.axelor.meta.schema.actions.ActionView;

public interface SchedulerCreationService {

  ActionView.ActionViewBuilder openWizard(int firstLeaveDayPeriod, int firstLeaveMonthPeriod);

  String createCron(
      int cronMinute, int cronHour, int cronDay, int cronMonth, int leaveReasonTypeSelect);

  MetaSchedule createMetaSchedule(
      String name, String code, String description, int leaveReasonTypeSelect, String cron);

  MetaSchedule updateMetaSchedule(
      String name, String code, String description, int leaveReasonTypeSelect, String cron);
}
