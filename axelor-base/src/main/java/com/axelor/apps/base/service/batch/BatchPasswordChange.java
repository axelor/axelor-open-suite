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
import com.axelor.apps.base.service.administration.AbstractBatch;
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
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import javax.mail.MessagingException;
import org.apache.commons.collections4.CollectionUtils;
import wslite.json.JSONException;

public class BatchPasswordChange extends AbstractBatch {

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

    if (baseBatch.getGenerateNewRandomPasswords() || baseBatch.getUpdatePasswordNextLogin()) {

      updateUsers(baseBatch);
    }
  }

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  protected void updateUsers(BaseBatch baseBatch) {

    LocalDate date = LocalDate.now().minusDays(baseBatch.getNbOfDaySinceLastUpdate());

    String filter = "";
    String query =
        "(cast(self.passwordUpdatedOn as LocalDate) < :date OR self.passwordUpdatedOn IS NULL)";

    boolean isGroupSetNotEmpty = CollectionUtils.isNotEmpty(baseBatch.getGroupSet());
    boolean isUserSetNotEmpty = CollectionUtils.isNotEmpty(baseBatch.getUserSet());
    Boolean allUsers = baseBatch.getAllUsers();

    if (allUsers) {
      filter = query;
    } else if (isGroupSetNotEmpty && isUserSetNotEmpty) {
      filter = query + " AND (self.group IN (:groupSet) OR self IN (:userSet))";
    } else if (isGroupSetNotEmpty) {
      filter = query + " AND self.group IN (:groupSet)";
    } else if (isUserSetNotEmpty) {
      filter = query + " AND self IN (:userSet)";
    } else {
      filter = "self.id = 0";
    }

    HashMap<String, Object> queryParameters = new HashMap<>();
    queryParameters.put("date", date);
    queryParameters.put("groupSet", baseBatch.getGroupSet());
    queryParameters.put("userSet", baseBatch.getUserSet());

    List<User> userList;
    Query<User> userQuery =
        userRepo.all().filter(filter.toString()).bind(queryParameters).order("id");

    int offset = 0;

    while (!(userList = userQuery.fetch(AbstractBatch.FETCH_LIMIT, offset)).isEmpty()) {
      for (User user : userList) {
        try {
          offset++;
          if (baseBatch.getGenerateNewRandomPasswords()) {
            user = userService.generateRandomPasswordForUser(user);
            user.setSendEmailUponPasswordChange(true);
            userService.processChangedPassword(user);
          }

          if (baseBatch.getUpdatePasswordNextLogin()) {
            user.setForcePasswordChange(true);
          }
          userRepo.save(user);
          incrementDone();
        } catch (ClassNotFoundException
            | InstantiationException
            | IllegalAccessException
            | MessagingException
            | IOException
            | AxelorException
            | JSONException e) {
          TraceBackService.trace(e, ExceptionOriginRepository.PASSWORD_CHANGE, batch.getId());
          incrementAnomaly();
        }
      }
      JPA.clear();
    }
  }

  @Override
  protected void stop() {

    String comment = String.format("%s Users processed", batch.getDone());

    super.stop();
    addComment(comment);
  }
}
