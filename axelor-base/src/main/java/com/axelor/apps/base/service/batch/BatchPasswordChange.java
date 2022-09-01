/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.batch;

import com.axelor.apps.base.db.BaseBatch;
import com.axelor.apps.base.db.repo.BatchRepository;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.auth.db.User;
import com.axelor.auth.db.repo.UserRepository;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.ExceptionOriginRepository;
import com.axelor.exception.service.TraceBackService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;

public class BatchPasswordChange extends AbstractBatch {

  protected UserRepository userRepo;
  protected UserService userService;
  protected AppBaseService appBaseService;

  @Inject
  public BatchPasswordChange(
      UserRepository userRepo, UserService userService, AppBaseService appBaseService) {
    this.userRepo = userRepo;
    this.userService = userService;
    this.appBaseService = appBaseService;
  }

  @Override
  protected void process() {

    BaseBatch baseBatch = batch.getBaseBatch();

    if (baseBatch.getGenerateNewRandomPasswords() || baseBatch.getUpdatePasswordNextLogin()) {

      updateUsers(baseBatch);
    }
  }

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  protected void updateUsers(BaseBatch baseBatch) {

    LocalDate date =
        appBaseService
            .getTodayDate(baseBatch.getCompany())
            .minusDays(baseBatch.getNbOfDaySinceLastUpdate());

    String filter =
        "((cast(self.passwordUpdatedOn as LocalDate) < :date OR self.passwordUpdatedOn IS NULL) AND (self.group IS NULL OR self.group.code != 'admins'))";

    HashMap<String, Object> queryParameters = new HashMap<>();
    queryParameters.put("date", date);

    if (!baseBatch.getAllUsers()) {
      filter += " AND (self.group IN (:groupSet) OR self IN (:userSet))";
      queryParameters.put(
          "groupSet",
          CollectionUtils.isNotEmpty(baseBatch.getGroupSet()) ? baseBatch.getGroupSet() : 0l);
      queryParameters.put(
          "userSet",
          CollectionUtils.isNotEmpty(baseBatch.getUserSet()) ? baseBatch.getUserSet() : 0l);
    }

    List<User> userList;
    Query<User> userQuery =
        userRepo.all().filter(filter.toString()).bind(queryParameters).order("id");

    // No need of offset as user records are updating in loop
    while (!(userList = userQuery.fetch(AbstractBatch.FETCH_LIMIT)).isEmpty()) {
      for (User user : userList) {
        try {
          if (baseBatch.getGenerateNewRandomPasswords()) {
            userService.generateRandomPasswordForUser(user);
            user.setSendEmailUponPasswordChange(true);
          }

          if (baseBatch.getUpdatePasswordNextLogin()) {
            user.setForcePasswordChange(true);
          }
          userRepo.save(user); // processChangedPassword method call in save method for email send
          incrementDone();
        } catch (Exception e) {
          TraceBackService.trace(e, ExceptionOriginRepository.PASSWORD_CHANGE, batch.getId());
          incrementAnomaly();
        }
      }
      batchRepo.save(batch);
      JPA.clear();
    }
  }

  @Override
  protected void stop() {

    String comment = String.format("%s Users processed", batch.getDone());

    super.stop();
    addComment(comment);
  }

  @Override
  protected void setBatchTypeSelect() {
    this.batch.setBatchTypeSelect(BatchRepository.BATCH_TYPE_BASE_BATCH);
  }
}
