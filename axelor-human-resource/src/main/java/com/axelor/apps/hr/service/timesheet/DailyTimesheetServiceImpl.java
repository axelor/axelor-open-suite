/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.hr.service.timesheet;

import com.axelor.apps.base.db.ICalendarEvent;
import com.axelor.apps.base.db.repo.ICalendarEventRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.DailyTimesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.axelor.apps.project.db.Project;
import com.axelor.auth.db.User;
import com.axelor.mail.db.MailMessage;
import com.axelor.mail.db.repo.MailMessageRepository;
import com.axelor.team.db.TeamTask;
import com.axelor.team.db.repo.TeamTaskRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;

public class DailyTimesheetServiceImpl implements DailyTimesheetService {

  public AppBaseService appBaseService;
  public TimesheetLineRepository timesheetLineRepository;
  public MailMessageRepository mailMessageRepository;
  public TeamTaskRepository teamTaskRepository;
  public TimesheetLineService timesheetLineService;
  public ICalendarEventRepository iCalendarEventRepository;

  @Inject
  public DailyTimesheetServiceImpl(
      AppBaseService appBaseService,
      TimesheetLineRepository timesheetLineRepository,
      MailMessageRepository mailMessageRepository,
      TeamTaskRepository teamTaskRepository,
      TimesheetLineService timesheetLineService,
      ICalendarEventRepository iCalendarEventRepository) {
    this.appBaseService = appBaseService;
    this.timesheetLineRepository = timesheetLineRepository;
    this.mailMessageRepository = mailMessageRepository;
    this.teamTaskRepository = teamTaskRepository;
    this.timesheetLineService = timesheetLineService;
    this.iCalendarEventRepository = iCalendarEventRepository;
  }

  @Override
  public void updateFromTimesheets(DailyTimesheet dailyTimesheet) {

    LocalDate todayDate = appBaseService.getTodayDate();
    User dailyTimesheetUser = dailyTimesheet.getDailyTimesheetUser();

    List<TimesheetLine> timesheetLineList =
        timesheetLineRepository
            .all()
            .filter(
                "self.user = ?1 and self.date = ?2 and self.timesheet != null and (self.dailyTimesheet = null or self.dailyTimesheet != ?3)",
                dailyTimesheetUser,
                todayDate,
                dailyTimesheet)
            .fetch();

    for (TimesheetLine timesheetLine : timesheetLineList) {
      dailyTimesheet.addDailyTimesheetLineListItem(timesheetLine);
    }
  }

  @Override
  public void updateFromActivities(DailyTimesheet dailyTimesheet) {

    LocalDate todayDate = appBaseService.getTodayDate();
    User dailyTimesheetUser = dailyTimesheet.getDailyTimesheetUser();
    List<Long> taskIdList = new ArrayList<>();
    List<Long> projectIdList = new ArrayList<>();

    List<TimesheetLine> dailyTimesheetLineList = dailyTimesheet.getDailyTimesheetLineList();

    if (CollectionUtils.isNotEmpty(dailyTimesheetLineList)) {

      for (TimesheetLine timesheetLine : dailyTimesheetLineList) {

        if (timesheetLine.getProject() != null && timesheetLine.getTeamTask() != null) {
          taskIdList.add(timesheetLine.getTeamTask().getId());
        } else if (timesheetLine.getProject() != null && timesheetLine.getTeamTask() == null) {
          projectIdList.add(timesheetLine.getProject().getId());
        }
      }
    }

    // Update from activities

    List<MailMessage> mailMessageList =
        mailMessageRepository
            .all()
            .filter(
                "self.relatedModel = ?1 and self.author = ?2 and (date(self.createdOn) >= ?3 or date(self.updatedOn) >= ?3)",
                TeamTask.class.getName(),
                dailyTimesheetUser,
                todayDate)
            .fetch();

    for (MailMessage mailMessage : mailMessageList) {

      if (!taskIdList.contains(mailMessage.getRelatedId())) {
        TeamTask teamTask = teamTaskRepository.find(mailMessage.getRelatedId());

        if (teamTask != null) {
          createTimesheetLine(
              teamTask, teamTask.getProject(), dailyTimesheetUser, todayDate, dailyTimesheet, null);
          taskIdList.add(mailMessage.getRelatedId());
        }
      }
    }

    // Update from favourites

    Set<TeamTask> favouriteTaskSet = dailyTimesheetUser.getFavouriteTaskSet();

    if (CollectionUtils.isNotEmpty(favouriteTaskSet)) {

      for (TeamTask teamTask : favouriteTaskSet) {

        if (!taskIdList.contains(teamTask.getId())) {
          createTimesheetLine(
              teamTask, teamTask.getProject(), dailyTimesheetUser, todayDate, dailyTimesheet, null);
          taskIdList.add(teamTask.getId());
        }
      }
    }

    Set<Project> favouriteProjectSet = dailyTimesheetUser.getFavouriteProjectSet();

    if (CollectionUtils.isNotEmpty(favouriteProjectSet)) {

      for (Project project : favouriteProjectSet) {

        if (!projectIdList.contains(project.getId())) {
          createTimesheetLine(null, project, dailyTimesheetUser, todayDate, dailyTimesheet, null);
          projectIdList.add(project.getId());
        }
      }
    }
  }

  @Override
  public void updateFromEvents(DailyTimesheet dailyTimesheet) {

    LocalDate todayDate = appBaseService.getTodayDate();
    User dailyTimesheetUser = dailyTimesheet.getDailyTimesheetUser();

    List<ICalendarEvent> iCalendarEventList =
        iCalendarEventRepository
            .all()
            .filter(
                "self.user = ?1 and date(self.startDateTime) <= ?2 and date(self.endDateTime) >= ?2",
                dailyTimesheetUser,
                todayDate)
            .fetch();

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyy HH:mm");

    for (ICalendarEvent iCalendarEvent : iCalendarEventList) {
      createTimesheetLine(
          null,
          null,
          dailyTimesheetUser,
          todayDate,
          dailyTimesheet,
          iCalendarEvent.getSubject()
              + ": "
              + formatter.format(iCalendarEvent.getStartDateTime())
              + " - "
              + formatter.format(iCalendarEvent.getEndDateTime()));
    }
  }

  @Transactional
  public void createTimesheetLine(
      TeamTask teamTask,
      Project project,
      User dailyTimesheetUser,
      LocalDate todayDate,
      DailyTimesheet dailyTimesheet,
      String comments) {

    TimesheetLine timesheetLine =
        timesheetLineService.createTimesheetLine(
            project, null, dailyTimesheetUser, todayDate, null, BigDecimal.ZERO, comments);

    timesheetLine.setDailyTimesheet(dailyTimesheet);
    timesheetLine.setTeamTask(teamTask);

    timesheetLineRepository.save(timesheetLine);
  }
}
