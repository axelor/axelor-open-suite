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
import com.axelor.script.ScriptAllowed;
import com.axelor.team.db.Team;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** UserService is a class that implement all methods for user information */
@ScriptAllowed
public interface UserService {

  /**
   * Return the current connected user.
   *
   * @return user the current connected user
   */
  User getUser();

  /**
   * Return the id of the current connected user.
   *
   * @return user the id of current connected user
   */
  Long getUserId();

  /**
   * Return the active company of the current connected user.
   *
   * @return Company the active company
   */
  @CallMethod
  Company getUserActiveCompany();

  /**
   * Return the trading name of the current connected user.
   *
   * @return Company the active company
   */
  TradingName getTradingName();

  /**
   * Return the active company id of the current connected user.
   *
   * @return Company the active company id
   */
  Long getUserActiveCompanyId();

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
  MetaFile getUserActiveCompanyLogo(String mode);

  /**
   * Returns the download URL for the current user’s company logo based on their theme, falling back
   * to the default logo if the theme-specific one is unavailable.
   *
   * @param mode theme mode indicator (unused)
   * @return the logo’s download URL, or null if no logo (or company) is available
   */
  String getUserActiveCompanyLogoLink(String mode);

  /**
   * Return the active team of the current connected user.
   *
   * @return Team the active team
   */
  @CallMethod
  Team getUserActiveTeam();

  /**
   * Return the active team ID of the current connected user.
   *
   * @return Team the active team id
   */
  @CallMethod
  Long getUserActiveTeamId();

  /**
   * Return the partner of the current connected user.
   *
   * @return Partner the user partner
   */
  @CallMethod
  Partner getUserPartner();

  void createPartner(User user);

  String getLocalizationCode();

  /**
   * Get the active company address of the user.
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
   * Trigger post processes after a change of the user password.
   *
   * @param user
   * @throws AxelorException
   * @throws ClassNotFoundException
   * @throws IOException
   */
  void processChangedPassword(User user)
      throws AxelorException, ClassNotFoundException, IOException;

  /**
   * Return whether the password matches the pattern in {@code user.password.pattern} property, with
   * a fallback to the default password pattern if property is not set.
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
   * Set the partner of the user.
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
