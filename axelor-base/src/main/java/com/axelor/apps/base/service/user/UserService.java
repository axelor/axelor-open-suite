/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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

/** Service for managing user-related operations. */
@ScriptAllowed
public interface UserService {

  /**
   * Returns the current connected user.
   *
   * @return the current connected user
   */
  User getUser();

  /**
   * Returns the id of the current connected user.
   *
   * @return the id of the current connected user
   */
  Long getUserId();

  /**
   * Returns the active company of the current connected user.
   *
   * @return the active company
   */
  @CallMethod
  Company getUserActiveCompany();

  /**
   * Returns the trading name of the current connected user.
   *
   * @return the trading name
   */
  TradingName getTradingName();

  /**
   * Returns the active company id of the current connected user.
   *
   * @return the active company id
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
   * Returns the active team of the current connected user.
   *
   * @return the active team
   */
  @CallMethod
  Team getUserActiveTeam();

  /**
   * Returns the active team id of the current connected user.
   *
   * @return the active team id
   */
  @CallMethod
  Long getUserActiveTeamId();

  /**
   * Returns the partner of the current connected user.
   *
   * @return the user partner
   */
  @CallMethod
  Partner getUserPartner();

  /**
   * Creates a partner for the given user.
   *
   * @param user the user for which to create a partner
   */
  void createPartner(User user);

  /**
   * Returns the localization code of the current connected user.
   *
   * @return the localization code, or default code if not set
   */
  String getLocalizationCode();

  /**
   * Returns the active company address of the current connected user.
   *
   * @return the active company address, or empty if not available
   */
  Optional<Address> getUserActiveCompanyAddress();

  /**
   * Changes the user password.
   *
   * @param user the user whose password will be changed
   * @param values map containing oldPassword, newPassword, and chkPassword
   * @return the updated user
   */
  User changeUserPassword(User user, Map<String, Object> values);

  /**
   * Triggers post-processes after a change of the user password.
   *
   * @param user the user whose password was changed
   * @throws AxelorException if template is missing
   * @throws ClassNotFoundException if class not found during template processing
   * @throws IOException if I/O error occurs during template processing
   */
  void processChangedPassword(User user)
      throws AxelorException, ClassNotFoundException, IOException;

  /**
   * Returns whether the password matches the pattern in {@code user.password.pattern} property,
   * with a fallback to the default password pattern if property is not set.
   *
   * @param password the password to validate
   * @return true if the password matches the pattern, false otherwise
   */
  boolean matchPasswordPattern(CharSequence password);

  /**
   * Generates a random password that matches the password pattern.
   *
   * @return the generated password
   */
  CharSequence generateRandomPassword();

  /**
   * Returns the password pattern description.
   *
   * @return the password pattern description
   */
  @CallMethod
  String getPasswordPatternDescription();

  /**
   * Sets the partner of the user.
   *
   * @param partner the partner to set
   * @param user the user
   */
  void setUserPartner(Partner partner, User user);

  /**
   * Generates a random password for the given user and updates their password.
   *
   * @param user the user for which to generate a password
   */
  void generateRandomPasswordForUser(User user);

  /**
   * Returns all permissions for the given user, including those from groups and roles.
   *
   * @param user the user
   * @return the list of permissions
   */
  List<Permission> getPermissions(User user);

  /**
   * Returns all meta permission rules for the given user, including those from groups and roles.
   *
   * @param user the user
   * @return the list of meta permission rules
   */
  List<MetaPermissionRule> getMetaPermissionRules(User user);

  /**
   * Sets the active company for the given user.
   *
   * @param user the user
   * @param company the company to set as active
   */
  void setActiveCompany(User user, Company company);

  /**
   * Sets the trading name for the given user.
   *
   * @param user the user
   * @param tradingName the trading name to set
   */
  void setTradingName(User user, TradingName tradingName);
}
