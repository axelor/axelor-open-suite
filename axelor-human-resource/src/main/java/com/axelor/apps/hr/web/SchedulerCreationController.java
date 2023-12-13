package com.axelor.apps.hr.web;

import com.axelor.apps.hr.service.SchedulerCreationService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaSchedule;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;

public class SchedulerCreationController {

  public void createScheduler(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();
    String name = (String) context.get("name");
    String code = (String) context.get("code");
    String description = (String) context.get("description");
    int leaveReasonTypeSelect = (int) context.get("leaveReasonTypeSelect");
    int cronMinute = (int) context.get("cronMinute");
    int cronHour = (int) context.get("cronHour");
    int cronDay = (int) context.get("cronDay");
    int cronMonth = (int) context.get("cronMonth");

    SchedulerCreationService schedulerCreationService = Beans.get(SchedulerCreationService.class);

    String cron =
        schedulerCreationService.createCron(
            cronMinute, cronHour, cronDay, cronMonth, leaveReasonTypeSelect);
    MetaSchedule metaSchedule =
        schedulerCreationService.createMetaSchedule(
            name, code, description, leaveReasonTypeSelect, cron);

    openScheduler(response, metaSchedule);
  }

  public void updateScheduler(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();
    String name = (String) context.get("name");
    String code = (String) context.get("code");
    String description = (String) context.get("description");
    int leaveReasonTypeSelect = (int) context.get("leaveReasonTypeSelect");
    int cronMinute = (int) context.get("cronMinute");
    int cronHour = (int) context.get("cronHour");
    int cronDay = (int) context.get("cronDay");
    int cronMonth = (int) context.get("cronMonth");

    SchedulerCreationService schedulerCreationService = Beans.get(SchedulerCreationService.class);

    String cron =
        schedulerCreationService.createCron(
            cronMinute, cronHour, cronDay, cronMonth, leaveReasonTypeSelect);
    MetaSchedule metaSchedule =
        schedulerCreationService.updateMetaSchedule(
            name, code, description, leaveReasonTypeSelect, cron);

    openScheduler(response, metaSchedule);
  }

  protected void openScheduler(ActionResponse response, MetaSchedule metaSchedule) {
    if (metaSchedule != null) {
      response.setView(
          ActionView.define(I18n.get("Meta Schedule"))
              .model(MetaSchedule.class.getName())
              .add("grid", "meta-schedule-grid")
              .add("form", "meta-schedule-form")
              .param("search-filters", "expense-filters")
              .param("forceEdit", "true")
              .context("_showRecord", String.valueOf(metaSchedule.getId()))
              .map());
      response.setCanClose(true);
    }
  }
}
