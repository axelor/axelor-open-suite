/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.user;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.TradingName;
import com.axelor.auth.db.Permission;
import com.axelor.auth.db.User;
import com.axelor.meta.CallMethod;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.MetaPermissionRule;
import com.axelor.team.db.Team;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** UserService is a class that implement all methods for user information */
public interface UserService {

  /**
   * Method that return the current connected user
   *
   * @return user the current connected user
   */
  User getUser();

  /**
   * Method that return the id of the current connected user
   *
   * @return user the id of current connected user
   */
  Long getUserId();

  /**
   * Method that return the active company of the current connected user
   *
   * @return Company the active company
   */
  @CallMethod
  Company getUserActiveCompany();

  /**
   * Method that return the Trading name of the current connected user
   *
   * @return Company the active company
   */
  TradingName getTradingName();

  /**
   * Method that return the active company id of the current connected user
   *
   * @return Company the active company id
   */
  Long getUserActiveCompanyId();

  /**
   * Returns the file representing the active company logo of the user, according to the provided
   * theme.
   *
   * <p>If there is no company logo defined for the provided theme, the fallback is the default
   * company logo.
   *
   * @return the logo file
   */
  MetaFile getUserActiveCompanyLogo(String theme);

  /**
   * Returns the link targeting the active company logo of the user, according to the provided
   * theme.
   *
   * <p>If there is no company logo defined for the provided theme, the fallback is the default
   * company logo.
   *
   * @return the logo link
   */
  String getUserActiveCompanyLogoLink(String theme);

  /**
   * Method that return the active team of the current connected user
   *
   * @return Team the active team
   */
  @CallMethod
  Team getUserActiveTeam();

  /**
   * Method that return the active team of the current connected user
   *
   * @return Team the active team id
   */
  @CallMethod
  Long getUserActiveTeamId();

  /**
   * Method that return the partner of the current connected user
   *
   * @return Partner the user partner
   */
  @CallMethod
  Partner getUserPartner();

  void createPartner(User user);

  String getLocalizationCode();

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
   */
  User changeUserPassword(User user, Map<String, Object> values);

  /**
   * Processs changed user password.
   *
   * @param user
   * @throws AxelorException
   * @throws ClassNotFoundException
   * @throws IOException
   */
  void processChangedPassword(User user)
      throws AxelorException, ClassNotFoundException, IOException;

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
   * Setting user's partner
   *
   * @param partner
   * @param user
   * @return
   */
  Partner setUserPartner(Partner partner, User user);

  void generateRandomPasswordForUser(User user);

  List<Permission> getPermissions(User user);

  List<MetaPermissionRule> getMetaPermissionRules(User user);

  void setActiveCompany(User user, Company company);

  void setTradingName(User user, TradingName tradingName);
}
