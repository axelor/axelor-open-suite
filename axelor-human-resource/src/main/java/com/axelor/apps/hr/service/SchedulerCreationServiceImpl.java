package com.axelor.apps.hr.service;

import com.axelor.apps.hr.db.HrBatch;
import com.axelor.apps.hr.db.repo.HrBatchRepository;
import com.axelor.apps.hr.db.repo.LeaveReasonRepository;
import com.axelor.apps.hr.service.app.AppHumanResourceService;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaSchedule;
import com.axelor.meta.db.repo.MetaScheduleRepository;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.studio.db.AppLeave;
import com.axelor.utils.db.Wizard;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class SchedulerCreationServiceImpl implements SchedulerCreationService {

  protected HrBatchRepository hrBatchRepository;
  protected MetaScheduleRepository metaScheduleRepository;
  protected AppHumanResourceService appHumanResourceService;

  private static final String BATCH_JOB_SELECT = "com.axelor.apps.base.job.BatchJob";
  private static final String HR_BATCH_SELECT = "com.axelor.apps.hr.service.batch.HrBatchService";

  @Inject
  public SchedulerCreationServiceImpl(
      HrBatchRepository hrBatchRepository,
      MetaScheduleRepository metaScheduleRepository,
      AppHumanResourceService appHumanResourceService) {
    this.hrBatchRepository = hrBatchRepository;
    this.metaScheduleRepository = metaScheduleRepository;
    this.appHumanResourceService = appHumanResourceService;
  }

  @Transactional
  public MetaSchedule createMetaSchedule(
      String name, String code, String description, int leaveReasonTypeSelect, String cron) {
    code = code.toUpperCase();
    createHrBatch(code, description, leaveReasonTypeSelect);
    AppLeave appLeave = appHumanResourceService.getAppLeave();

    MetaSchedule schedule = createScheduler(name, code, description, cron);
    setSchedule(leaveReasonTypeSelect, appLeave, schedule);
    return schedule;
  }

  @Override
  @Transactional
  public MetaSchedule updateMetaSchedule(
      String name, String code, String description, int leaveReasonTypeSelect, String cron) {
    code = code.toUpperCase();
    AppLeave appLeave = appHumanResourceService.getAppLeave();
    MetaSchedule schedule = getSchedule(leaveReasonTypeSelect, appLeave);
    updateHrBatch(code, schedule);
    updateScheduler(schedule, name, code, description, cron);

    return schedule;
  }

  @Transactional
  protected void updateHrBatch(String code, MetaSchedule schedule) {
    HrBatch hrBatch = hrBatchRepository.findByCode(schedule.getBatchCode());
    hrBatch.setCode(code);
    hrBatchRepository.save(hrBatch);
  }

  protected void setSchedule(int leaveReasonTypeSelect, AppLeave appLeave, MetaSchedule schedule) {
    if (leaveReasonTypeSelect == LeaveReasonRepository.TYPE_SELECT_EVERY_MONTH) {
      appLeave.setMonthlySchedule(schedule);
    }

    if (leaveReasonTypeSelect == LeaveReasonRepository.TYPE_SELECT_EVERY_YEAR) {
      appLeave.setAnnualSchedule(schedule);
    }
  }

  protected MetaSchedule getSchedule(int leaveReasonTypeSelect, AppLeave appLeave) {
    MetaSchedule schedule = null;

    if (leaveReasonTypeSelect == LeaveReasonRepository.TYPE_SELECT_EVERY_MONTH) {
      schedule = appLeave.getMonthlySchedule();
    }

    if (leaveReasonTypeSelect == LeaveReasonRepository.TYPE_SELECT_EVERY_YEAR) {
      schedule = appLeave.getAnnualSchedule();
    }
    return schedule;
  }

  @Override
  public String createCron(int cronHour, int cronDay, int cronMonth, int leaveReasonTypeSelect) {
    String hour = String.valueOf(cronHour);
    String day = String.valueOf(cronDay);
    String month = String.valueOf(cronMonth);
    String weekday = "*";

    if (leaveReasonTypeSelect == LeaveReasonRepository.TYPE_SELECT_EVERY_MONTH) {
      month = "*";
    }

    return "0" + " " + hour + " " + day + " " + month + " " + weekday + " ?";
  }

  @Transactional
  protected MetaSchedule createScheduler(
      String name, String code, String description, String cron) {
    MetaSchedule metaSchedule = new MetaSchedule(name);
    metaSchedule.setCron(cron);
    metaSchedule.setJob(BATCH_JOB_SELECT);
    metaSchedule.setBatchServiceSelect(HR_BATCH_SELECT);
    metaSchedule.setBatchCode(code);
    metaSchedule.setDescription(description);
    return metaScheduleRepository.save(metaSchedule);
  }

  @Transactional
  protected void updateScheduler(
      MetaSchedule metaSchedule, String name, String code, String description, String cron) {
    metaSchedule.setName(name);
    metaSchedule.setCron(cron);
    metaSchedule.setBatchCode(code);
    metaSchedule.setDescription(description);
    metaScheduleRepository.save(metaSchedule);
  }

  @Transactional
  protected void createHrBatch(String code, String description, int leaveReasonTypeSelect) {
    HrBatch hrBatch = new HrBatch(code);
    hrBatch.setActionSelect(HrBatchRepository.ACTION_INCREMENT_LEAVE);
    hrBatch.setLeaveReasonTypeSelect(leaveReasonTypeSelect);
    hrBatch.setDescription(description);
    hrBatchRepository.save(hrBatch);
  }

  public ActionView.ActionViewBuilder openWizard(
      int firstLeaveDayPeriod, int firstLeaveMonthPeriod) {
    ActionView.ActionViewBuilder actionViewBuilder =
        ActionView.define(I18n.get("Create scheduler"));
    actionViewBuilder.model(Wizard.class.getName());
    actionViewBuilder.add("form", "scheduler-creation-wizard-form");
    actionViewBuilder.param("popup", "reload");
    actionViewBuilder.param("show-toolbar", "false");
    actionViewBuilder.param("show-confirm", "false");
    actionViewBuilder.param("width", "large");
    actionViewBuilder.param("popup-save", "false");
    actionViewBuilder.context("_firstLeaveDayPeriod", firstLeaveDayPeriod);
    actionViewBuilder.context("_firstLeaveMonthPeriod", firstLeaveMonthPeriod);
    return actionViewBuilder;
  }
}
