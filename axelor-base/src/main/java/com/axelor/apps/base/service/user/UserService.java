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
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.mail.MessagingException;
import wslite.json.JSONException;

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
   * Method that return the Trading name of the current connected user
   *
   * @return Company the active company
   */
  public TradingName getTradingName();

  /**
   * Method that return the active company id of the current connected user
   *
   * @return Company the active company id
   */
  public Long getUserActiveCompanyId();

  /**
   * Retrieves the appropriate company logo for the currently authenticated user, based on the
   * user's selected theme logo mode (dark or light).
   *
   * <p>If the user's theme specifies a dark or light logo mode, the corresponding logo is returned.
   * If the selected logo is not available or the theme mode is invalid, the default company logo is
   * returned as a fallback.
   *
   * @param mode an theme mode indicator (currently unused)
   * @return the selected {@link MetaFile} logo, or the default company logo if none is set or
   *     applicable
   */
  public MetaFile getUserActiveCompanyLogo(String mode);

  /**
   * Returns the download URL for the current user’s company logo based on their theme, falling back
   * to the default logo if the theme-specific one is unavailable.
   *
   * @param mode theme mode indicator (unused)
   * @return the logo’s download URL, or null if no logo (or company) is available
   */
  public String getUserActiveCompanyLogoLink(String mode);

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

  public String getLocalizationCode();

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
      throws ClassNotFoundException,
          InstantiationException,
          IllegalAccessException,
          MessagingException,
          IOException,
          AxelorException;

  /**
   * Processs changed user password.
   *
   * @param user
   * @throws ClassNotFoundException
   * @throws IOException
   * @throws AxelorException
   */
  void processChangedPassword(User user)
      throws AxelorException, ClassNotFoundException, IOException, JSONException;

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
  @Transactional
  public Partner setUserPartner(Partner partner, User user);

  public void generateRandomPasswordForUser(User user);

  List<Permission> getPermissions(User user);

  List<MetaPermissionRule> getMetaPermissionRules(User user);

  void setActiveCompany(User user, Company company);

  void setTradingName(User user, TradingName tradingName);
}
