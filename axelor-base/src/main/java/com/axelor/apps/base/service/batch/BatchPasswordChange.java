/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.batch;

import com.axelor.apps.base.db.BaseBatch;
import com.axelor.apps.base.db.repo.BaseBatchRepository;
import com.axelor.apps.base.db.repo.BatchRepository;
import com.axelor.apps.base.db.repo.ExceptionOriginRepository;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.auth.db.User;
import com.axelor.auth.db.repo.UserRepository;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchPasswordChange extends BatchStrategy {

  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected UserRepository userRepo;
  protected UserService userService;

  @Inject
  public BatchPasswordChange(UserRepository userRepo, UserService userService) {
    this.userRepo = userRepo;
    this.userService = userService;
  }

  @Override
  protected void process() {

    BaseBatch baseBatch = batch.getBaseBatch();

    if (baseBatch.getPasswordChangeActionSelect() == 0) {
      return;
    }
    LocalDate date =
        appBaseService
            .getTodayDate(baseBatch.getCompany())
            .minusDays(baseBatch.getNbOfDaySinceLastUpdate());

    log.debug("date: {}", date);

    String filter =
        "((self.passwordUpdatedOn IS NULL OR cast(self.passwordUpdatedOn as LocalDate) < :date) "
            + "AND (self.group IS NULL OR self.group.code != 'admins'))";

    HashMap<String, Object> queryParameters = new HashMap<>();
    queryParameters.put("date", date);

    if (!baseBatch.getAllUsers()) {
      filter += " AND (self.group IN (:groupSet) OR self IN (:userSet))";
      queryParameters.put(
          "groupSet",
          CollectionUtils.isNotEmpty(baseBatch.getGroupSet()) ? baseBatch.getGroupSet() : 0L);
      queryParameters.put(
          "userSet",
          CollectionUtils.isNotEmpty(baseBatch.getUserSet()) ? baseBatch.getUserSet() : 0L);
    }

    int offset = 0;
    List<User> userList;
    Query<User> userQuery = userRepo.all().filter(filter).bind(queryParameters).order("id");
    List<Long> userIdList =
        userQuery.select("id").fetch(0, 0).stream()
            .map(m -> (Long) m.get("id"))
            .collect(
                Collectors
                    .toList()); // have to do this because the processed users potentially cannot be
    // queried again.

    while (!(userList =
            userRepo
                .all()
                .filter("self.id IN :userIds")
                .bind("userIds", userIdList)
                .order("id")
                .fetch(AbstractBatch.FETCH_LIMIT, offset))
        .isEmpty()) {
      for (User user : userList) {
        ++offset;
        try {
          // incrementDone is called inside generatePassword(User user)
          generatePassword(user);
        } catch (Exception e) {
          TraceBackService.trace(e, ExceptionOriginRepository.PASSWORD_CHANGE, batch.getId());
          incrementAnomaly();
        }
      }
      JPA.clear();
      findBatch();
    }
  }

  @Transactional
  protected void generatePassword(User user) {
    log.debug(
        "Generating password for user {}, current time {}, user password time {}",
        user.getCode(),
        LocalDateTime.now(),
        user.getPasswordUpdatedOn());
    BaseBatch baseBatch = batch.getBaseBatch();
    int passwordChangeActionSelect = baseBatch.getPasswordChangeActionSelect();
    if (passwordChangeActionSelect == BaseBatchRepository.PASSWORD_CHANGE_ACTION_GENERATE
        || passwordChangeActionSelect
            == BaseBatchRepository.PASSWORD_CHANGE_ACTION_GENERATE_AND_FORCE_UPDATE) {
      userService.generateRandomPasswordForUser(user);
      user.setSendEmailUponPasswordChange(true);
    }

    if (passwordChangeActionSelect == BaseBatchRepository.PASSWORD_CHANGE_ACTION_FORCE_UPDATE
        || passwordChangeActionSelect
            == BaseBatchRepository.PASSWORD_CHANGE_ACTION_GENERATE_AND_FORCE_UPDATE) {
      user.setForcePasswordChange(true);
    }
    userRepo.save(user); // processChangedPassword method call in save method for email send
    updateUser(user);
  }

  @Override
  protected void stop() {

    String comment =
        String.format(
            I18n.get("%s user processed", "%s users processed", batch.getDone()), batch.getDone());

    super.stop();
    addComment(comment);
  }

  @Override
  protected void setBatchTypeSelect() {
    this.batch.setBatchTypeSelect(BatchRepository.BATCH_TYPE_BASE_BATCH);
  }
}
