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

import com.axelor.app.AppSettings;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.theme.MetaThemeFetchService;
import com.axelor.auth.AuthService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.Group;
import com.axelor.auth.db.Permission;
import com.axelor.auth.db.Role;
import com.axelor.auth.db.User;
import com.axelor.auth.db.repo.UserRepository;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.message.db.Template;
import com.axelor.message.service.TemplateMessageService;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.MetaPermission;
import com.axelor.meta.db.MetaPermissionRule;
import com.axelor.meta.db.ThemeLogoMode;
import com.axelor.studio.db.AppBase;
import com.axelor.team.db.Team;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.mail.MessagingException;
import javax.validation.ValidationException;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.exception.TooManyIterationsException;
import org.apache.shiro.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wslite.json.JSONException;

/** UserService is a class that implement all methods for user information */
public class UserServiceImpl implements UserService {

  @Inject private UserRepository userRepo;
  @Inject private MetaFiles metaFiles;
  @Inject private MetaThemeFetchService metaThemeFetchService;

  public static final String DEFAULT_LOCALE = "en";

  public static final String DEFAULT_LOCALIZATION_CODE = "en_GB";

  private static final String PATTERN_ACCES_RESTRICTION =
      "(((?=.*[a-z])(?=.*[A-Z])(?=.*\\d))|((?=.*[a-z])(?=.*[A-Z])(?=.*\\W))|((?=.*[a-z])(?=.*\\d)(?=.*\\W))|((?=.*[A-Z])(?=.*\\d)(?=.*\\W))).{8,}";
  private static final Pattern PATTERN =
      Pattern.compile(
          MoreObjects.firstNonNull(
              AppSettings.get().get("user.password.pattern"), PATTERN_ACCES_RESTRICTION));

  private static final String PATTERN_DESCRIPTION =
      PATTERN.pattern().equals(PATTERN_ACCES_RESTRICTION)
          ? BaseExceptionMessage.USER_PATTERN_MISMATCH_ACCES_RESTRICTION
          : BaseExceptionMessage.USER_PATTERN_MISMATCH_CUSTOM;

  private static final String GEN_CHARS =
      "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";
  private static final Pair<Integer, Integer> GEN_BOUNDS = Pair.of(12, 22);
  private static final int GEN_LOOP_LIMIT = 1000;
  private static final SecureRandom random = new SecureRandom();

  private static final Logger logger =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /**
   * Method that return the current connected user
   *
   * @return user the current connected user
   */
  @Override
  public User getUser() {
    User user = null;
    try {
      user = AuthUtils.getUser();
    } catch (Exception ex) {
    }
    if (user == null) {
      user = userRepo.findByCode("admin");
    }
    return user;
  }

  /**
   * Method that return the id of the current connected user
   *
   * @return user the id of current connected user
   */
  @Override
  public Long getUserId() {

    final User user = this.getUser();

    if (user == null) {
      return null;
    }

    return user.getId();
  }

  /**
   * Method that return the active company of the current connected user
   *
   * @return Company the active company
   */
  @Override
  public Company getUserActiveCompany() {

    User user = getUser();

    if (user == null) {
      return null;
    }

    return user.getActiveCompany();
  }

  /**
   * Method that return the active company id of the current connected user
   *
   * @return Company the active company id
   */
  @Override
  public Long getUserActiveCompanyId() {

    final Company company = this.getUserActiveCompany();

    if (company == null) {
      return null;
    }

    return company.getId();
  }

  @Override
  public MetaFile getUserActiveCompanyLogo(String mode) {
    User user = AuthUtils.getUser();
    Company company = user != null ? this.getUserActiveCompany() : null;

    if (company == null) {
      return null;
    }
    MetaFile logo = null;
    ThemeLogoMode logoMode = metaThemeFetchService.getCurrentThemeLogoMode(user);
    switch (logoMode) {
      case DARK:
        logo = company.getDarkLogo();
        break;
      case LIGHT:
        logo = company.getLightLogo();
        break;
      case NONE:
      default:
        logo = company.getLogo();
        break;
    }
    return Optional.ofNullable(logo).orElse(company.getLogo());
  }

  @Override
  public String getUserActiveCompanyLogoLink(String mode) {
    MetaFile logo = getUserActiveCompanyLogo(mode);

    if (logo == null) {
      return null;
    }

    return metaFiles.getDownloadLink(logo, getUserActiveCompany());
  }

  @Override
  public Optional<Address> getUserActiveCompanyAddress() {
    Company company = getUserActiveCompany();

    if (company != null) {
      return Optional.ofNullable(company.getAddress());
    }

    return Optional.empty();
  }

  /**
   * Method that return the active team of the current connected user
   *
   * @return Team the active team
   */
  @Override
  public Team getUserActiveTeam() {

    final User user = getUser();

    if (user == null) {
      return null;
    }

    return user.getActiveTeam();
  }

  /**
   * Method that return the active team of the current connected user
   *
   * @return Team the active team id
   */
  @Override
  public Long getUserActiveTeamId() {

    final Team team = this.getUserActiveTeam();

    if (team == null) {
      return null;
    }

    return team.getId();
  }

  /**
   * Method that return the partner of the current connected user
   *
   * @return Partner the user partner
   */
  @Override
  public Partner getUserPartner() {

    final User user = getUser();

    if (user == null) {
      return null;
    }

    return user.getPartner();
  }

  @Override
  @Transactional
  public void createPartner(User user) {
    Partner partner = new Partner();
    partner.setPartnerTypeSelect(2);
    partner.setIsContact(true);
    partner.setName(user.getName());
    partner.setFullName(user.getName());
    partner.setTeam(user.getActiveTeam());
    partner.setUser(user);
    Beans.get(PartnerRepository.class).save(partner);

    user.setPartner(partner);
    userRepo.save(user);
  }

  @Override
  public String getLocalizationCode() {
    User user = getUser();
    if (user != null && user.getLocalization() != null) {
      if (!Strings.isNullOrEmpty(user.getLocalization().getCode())) {
        return user.getLocalization().getCode();
      }
    }
    return DEFAULT_LOCALIZATION_CODE;
  }

  @Override
  public boolean matchPasswordPattern(CharSequence password) {
    return PATTERN.matcher(password).matches();
  }

  @Override
  public User changeUserPassword(User user, Map<String, Object> values)
      throws ClassNotFoundException,
          InstantiationException,
          IllegalAccessException,
          MessagingException,
          IOException,
          AxelorException {
    Preconditions.checkNotNull(user, I18n.get("User cannot be null."));
    Preconditions.checkNotNull(values, I18n.get("User context cannot be null."));

    final String oldPassword = (String) values.get("oldPassword");
    final String newPassword = (String) values.get("newPassword");
    final String chkPassword = (String) values.get("chkPassword");

    // no password change
    if (StringUtils.isBlank(newPassword)) {
      return user;
    }

    if (StringUtils.isBlank(oldPassword)) {
      throw new ValidationException(I18n.get("Current user password is not provided."));
    }

    if (!newPassword.equals(chkPassword)) {
      throw new ValidationException(I18n.get("Confirm password doesn't match with new password."));
    }

    if (!matchPasswordPattern(newPassword)) {
      throw new ValidationException(I18n.get(PATTERN_DESCRIPTION));
    }

    final User current = AuthUtils.getUser();
    final AuthService authService = AuthService.getInstance();

    if (!authService.match(oldPassword, current.getPassword())) {
      throw new ValidationException(I18n.get("Current user password is wrong."));
    }

    user.setTransientPassword(newPassword);

    return user;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void processChangedPassword(User user)
      throws AxelorException, ClassNotFoundException, IOException, JSONException {
    Preconditions.checkNotNull(user, I18n.get("User cannot be null."));

    try {
      if (!user.getSendEmailUponPasswordChange()) {
        return;
      }

      AppBase appBase = Beans.get(AppBaseService.class).getAppBase();
      Template template = appBase.getPasswordChangedTemplate();

      if (template == null) {
        throw new AxelorException(
            appBase,
            TraceBackRepository.CATEGORY_NO_VALUE,
            I18n.get("Template for changed password is missing."));
      }

      TemplateMessageService templateMessageService = Beans.get(TemplateMessageService.class);
      templateMessageService.generateAndSendMessage(user, template);

    } finally {
      user.setTransientPassword(null);
    }
  }

  @Override
  public CharSequence generateRandomPassword() {
    for (int genLoopIndex = 0; genLoopIndex < GEN_LOOP_LIMIT; ++genLoopIndex) {
      int len = random.ints(GEN_BOUNDS.getLeft(), GEN_BOUNDS.getRight()).findFirst().getAsInt();
      StringBuilder sb = new StringBuilder(len);

      for (int i = 0; i < len; ++i) {
        sb.append(GEN_CHARS.charAt(random.nextInt(GEN_CHARS.length())));
      }

      String result = sb.toString();

      if (matchPasswordPattern(result)) {
        return result;
      }
    }

    throw new TooManyIterationsException(GEN_LOOP_LIMIT);
  }

  @Override
  public String getPasswordPatternDescription() {
    return I18n.get(PATTERN_DESCRIPTION);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void generateRandomPasswordForUser(User user) {
    AuthService authService = Beans.get(AuthService.class);
    LocalDateTime todayDateTime =
        Beans.get(AppBaseService.class).getTodayDateTime().toLocalDateTime();

    String password = this.generateRandomPassword().toString();
    user.setTransientPassword(password);
    password = authService.encrypt(password);
    user.setPassword(password);
    user.setPasswordUpdatedOn(todayDateTime);

    User loginUser = this.getUser();

    // Update login date in session so that user changing own password doesn't get logged out.
    if (loginUser.equals(user)) {
      Session session = AuthUtils.getSubject().getSession();
      session.setAttribute("com.axelor.internal.loginDate", todayDateTime);
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Partner setUserPartner(Partner partner, User user) {
    partner.setUser(user);
    partner.setTeam(user.getActiveTeam());
    user.setPartner(partner);
    userRepo.save(user);
    return partner;
  }

  @Override
  public TradingName getTradingName() {
    final User user = getUser();

    if (user == null) {
      return null;
    }

    return user.getTradingName();
  }

  @Override
  public List<Permission> getPermissions(User user) {
    List<Permission> permissionList = new ArrayList<>();
    Optional.ofNullable(user.getPermissions()).ifPresent(permissionList::addAll);
    Optional.ofNullable(user.getGroup())
        .ifPresent(group -> permissionList.addAll(group.getPermissions()));
    Optional.ofNullable(user.getRoles())
        .map(
            roles ->
                roles.stream()
                    .map(Role::getPermissions)
                    .flatMap(Set::stream)
                    .collect(Collectors.toList()))
        .ifPresent(permissionList::addAll);
    Optional.ofNullable(user.getGroup())
        .map(Group::getRoles)
        .map(
            roles ->
                roles.stream()
                    .map(Role::getPermissions)
                    .flatMap(Set::stream)
                    .collect(Collectors.toList()))
        .ifPresent(permissionList::addAll);

    return permissionList;
  }

  @Override
  public List<MetaPermissionRule> getMetaPermissionRules(User user) {
    List<MetaPermissionRule> metaPermissionRuleList = new ArrayList<>();
    Optional.ofNullable(user.getMetaPermissions())
        .ifPresent(
            metaPermissions ->
                metaPermissionRuleList.addAll(
                    metaPermissions.stream()
                        .map(MetaPermission::getRules)
                        .flatMap(List::stream)
                        .collect(Collectors.toList())));
    Optional.ofNullable(user.getGroup())
        .map(Group::getMetaPermissions)
        .ifPresent(
            metaPermissions ->
                metaPermissionRuleList.addAll(
                    metaPermissions.stream()
                        .map(MetaPermission::getRules)
                        .flatMap(List::stream)
                        .collect(Collectors.toList())));
    Optional.ofNullable(user.getRoles())
        .map(
            roles ->
                roles.stream()
                    .map(Role::getMetaPermissions)
                    .flatMap(Set::stream)
                    .collect(Collectors.toList()))
        .ifPresent(
            metaPermissions ->
                metaPermissionRuleList.addAll(
                    metaPermissions.stream()
                        .map(MetaPermission::getRules)
                        .flatMap(List::stream)
                        .collect(Collectors.toList())));
    Optional.ofNullable(user.getGroup())
        .map(Group::getRoles)
        .map(
            roles ->
                roles.stream()
                    .map(Role::getMetaPermissions)
                    .flatMap(Set::stream)
                    .collect(Collectors.toList()))
        .ifPresent(
            metaPermissions ->
                metaPermissionRuleList.addAll(
                    metaPermissions.stream()
                        .map(MetaPermission::getRules)
                        .flatMap(List::stream)
                        .collect(Collectors.toList())));

    return metaPermissionRuleList;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void setActiveCompany(User user, Company company) {
    user.setActiveCompany(company);
    user.setTradingName(null);
    userRepo.save(user);
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void setTradingName(User user, TradingName tradingName) {
    user.setTradingName(tradingName);
    userRepo.save(user);
  }
}
