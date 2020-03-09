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
package com.axelor.apps.base.service.user;

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.meta.CallMethod;
import com.axelor.meta.db.MetaFile;
import com.axelor.team.db.Team;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.mail.MessagingException;

/** UserService is a class that implement all methods for user information */
public interface UserService {

  /**
   * Method that return the current connected user
   *
   * @return user the current connected user
   */
  public User getUser();

  /**
   * Method that return the id of the current connected user
   *
   * @return user the id of current connected user
   */
  public Long getUserId();

  /**
   * Method that return the active company of the current connected user
   *
   * @return Company the active company
   */
  @CallMethod
  public Company getUserActiveCompany();

  /**
   * Method that return the active company id of the current connected user
   *
   * @return Company the active company id
   */
  public Long getUserActiveCompanyId();

  /**
   * Method that return the active team of the current connected user
   *
   * @return Team the active team
   */
  public MetaFile getUserActiveCompanyLogo();

  /**
   * Method that return company logo link
   *
   * @return the logo Link
   */
  public String getUserActiveCompanyLogoLink();

  /**
   * Method that return the active team of the current connected user
   *
   * @return Team the active team
   */
  @CallMethod
  public Team getUserActiveTeam();

  /**
   * Method that return the active team of the current connected user
   *
   * @return Team the active team id
   */
  @CallMethod
  public Long getUserActiveTeamId();

  /**
   * Method that return the partner of the current connected user
   *
   * @return Partner the user partner
   */
  @CallMethod
  public Partner getUserPartner();

  @Transactional
  public void createPartner(User user);

  public String getLanguage();

  /**
   * Get user's active company address.
   *
   * @return
   */
  Optional<Address> getUserActiveCompanyAddress();

  /**
   * Change user password.
   *
   * @param user
   * @param values
   * @return
   * @throws ClassNotFoundException
   * @throws InstantiationException
   * @throws IllegalAccessException
   * @throws MessagingException
   * @throws IOException
   * @throws AxelorException
   */
  User changeUserPassword(User user, Map<String, Object> values)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException,
          MessagingException, IOException, AxelorException;

  /**
   * Processs changed user password.
   *
   * @param user
   * @throws ClassNotFoundException
   * @throws InstantiationException
   * @throws IllegalAccessException
   * @throws MessagingException
   * @throws IOException
   * @throws AxelorException
   */
  void processChangedPassword(User user)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException,
          MessagingException, IOException, AxelorException;

  /**
   * Match password with configured pattern.
   *
   * @param password
   * @return
   */
  boolean matchPasswordPattern(CharSequence password);

  /**
   * Generate a random password.
   *
   * @return
   */
  CharSequence generateRandomPassword();

  /**
   * Get password pattern description.
   *
   * @return
   */
  @CallMethod
  String getPasswordPatternDescription();

  /**
   * Verify current connected user's password
   *
   * @param password
   * @return
   */
  boolean verifyCurrentUserPassword(String password);

  @Transactional
  public void generateRandomPasswordForUsers(List<Long> userIds);
}
