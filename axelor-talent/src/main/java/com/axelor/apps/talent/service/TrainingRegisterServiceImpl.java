/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.talent.service;

import com.axelor.apps.base.db.Duration;
import com.axelor.apps.base.db.repo.ICalendarEventRepository;
import com.axelor.apps.base.service.DurationService;
import com.axelor.apps.crm.db.Event;
import com.axelor.apps.crm.db.repo.EventRepository;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.apps.talent.db.Skill;
import com.axelor.apps.talent.db.TrainingRegister;
import com.axelor.apps.talent.db.TrainingSession;
import com.axelor.apps.talent.db.TrainingSkill;
import com.axelor.apps.talent.db.repo.TrainingRegisterRepository;
import com.axelor.apps.talent.db.repo.TrainingSessionRepository;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;

public class TrainingRegisterServiceImpl implements TrainingRegisterService {

  @Inject protected TrainingRegisterRepository trainingRegisterRepo;

  @Inject protected EventRepository eventRepo;

  @Inject protected TrainingSessionRepository trainingSessionRepo;
  public static final String RELATED_TO_TRAINING_REGISTER =
      "com.axelor.apps.talent.db.TrainingRegister";

  @Transactional
  @Override
  public Event plan(TrainingRegister trainingRegister) {

    trainingRegister.setStatusSelect(1);

    trainingRegisterRepo.save(trainingRegister);

    Event event = generateMeeting(trainingRegister);

    return eventRepo.save(event);
  }

  protected Event generateMeeting(TrainingRegister trainingRegister) {

    Event event = new Event();
    event.setTypeSelect(ICalendarEventRepository.TYPE_MEETING);
    event.setStartDateTime(trainingRegister.getFromDate());
    event.setEndDateTime(trainingRegister.getToDate());
    event.setSubject(trainingRegister.getTraining().getName());
    event.setUser(trainingRegister.getEmployee().getUser());
    event.setCalendar(trainingRegister.getCalendar());
    event.setRelatedToSelect(RELATED_TO_TRAINING_REGISTER);
    event.setRelatedToSelectId(trainingRegister.getId());
    if (trainingRegister.getTrainingSession() != null) {
      event.setLocation(trainingRegister.getTrainingSession().getLocation());
    }
    return event;
  }

  @Transactional
  @Override
  public void complete(TrainingRegister trainingRegister) {

    trainingRegister.setStatusSelect(2);

    Set<Skill> skills = trainingRegister.getTraining().getSkillSet();
    if (CollectionUtils.isNotEmpty(skills)) {

      Employee employee = trainingRegister.getEmployee();

      skills.forEach(
          skill -> {
            TrainingSkill trainingSkill = new TrainingSkill();

            trainingSkill.setSkill(skill);

            LocalDate date = trainingRegister.getToDate().toLocalDate();
            trainingSkill.setGraduationDate(date);

            Duration validityDuration = skill.getValidityDuration();
            if (validityDuration != null) {
              LocalDate endOfValidityDate =
                  Beans.get(DurationService.class).computeDuration(validityDuration, date);
              trainingSkill.setEndOfValidityDate(endOfValidityDate);
            }

            employee.addTrainingSkillListItem(trainingSkill);
          });

      Beans.get(EmployeeRepository.class).save(employee);
    }

    trainingRegisterRepo.save(trainingRegister);
  }

  @Transactional
  @Override
  public void cancel(TrainingRegister trainingRegister) {

    trainingRegister.setStatusSelect(3);

    trainingRegisterRepo.save(trainingRegister);
  }

  @Transactional
  @Override
  public void updateEventCalendar(TrainingRegister trainingRegister) {

    Event event =
        eventRepo
            .all()
            .filter(
                "self.relatedToSelect = ?1 AND self.relatedToSelectId = ?2",
                RELATED_TO_TRAINING_REGISTER,
                trainingRegister.getId())
            .fetchOne();

    if (event != null) {

      event.setCalendar(trainingRegister.getCalendar());
      eventRepo.save(event);
    }
  }

  @Override
  @Transactional
  public String massTrainingRegisterCreation(
      ArrayList<LinkedHashMap<String, Object>> employeeList, TrainingSession trainingSession) {

    List<Long> eventsIds = new ArrayList<>();

    for (LinkedHashMap<String, Object> employeeMap : employeeList) {

      Employee employee =
          Beans.get(EmployeeRepository.class)
              .find(Long.parseLong(employeeMap.get("id").toString()));

      if (employee.getUser() == null) {
        continue;
      }

      TrainingRegister trainingRegister = new TrainingRegister();
      trainingRegister.setTraining(trainingSession.getTraining());
      trainingRegister.setFromDate(trainingSession.getFromDate());
      trainingRegister.setToDate(trainingSession.getToDate());
      trainingRegister.setTrainingSession(trainingSession);
      trainingRegister.setEmployee(employee);
      trainingRegister.setRating(trainingSession.getOverallRatingToApply());

      Event event = this.plan(trainingRegister);

      trainingRegister.getEventList().add(event);

      eventsIds.add(event.getId());

      trainingSession.getTrainingRegisterList().add(trainingRegister);
    }

    trainingSessionRepo.save(trainingSession);

    return eventsIds.toString().replace("[", "(").replace("]", ")");
  }
}
