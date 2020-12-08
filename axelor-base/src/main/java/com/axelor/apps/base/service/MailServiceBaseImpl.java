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
package com.axelor.apps.base.service;

import static com.axelor.common.StringUtils.isBlank;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.message.service.MailServiceMessageImpl;
import com.axelor.auth.db.User;
import com.axelor.auth.db.repo.UserRepository;
import com.axelor.db.Model;
import com.axelor.db.Query;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.mail.MailException;
import com.axelor.mail.db.MailAddress;
import com.axelor.mail.db.MailFollower;
import com.axelor.mail.db.MailMessage;
import com.axelor.mail.db.repo.MailFollowerRepository;
import com.google.common.base.Joiner;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Singleton;
import javax.mail.internet.InternetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class MailServiceBaseImpl extends MailServiceMessageImpl {
  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject AppBaseService appBaseService;

  @Override
  public Model resolve(String email) {
    final UserRepository users = Beans.get(UserRepository.class);
    final User user =
        users.all().filter("self.partner.emailAddress.address = ?1", email).fetchOne();
    if (user != null) {
      return user;
    }
    final PartnerRepository partners = Beans.get(PartnerRepository.class);
    final Partner partner =
        partners.all().filter("self.emailAddress.address = ?1", email).fetchOne();
    if (partner != null) {
      return partner;
    }
    return super.resolve(email);
  }

  @Override
  public List<InternetAddress> findEmails(String matching, List<String> selected, int maxResult) {

    // Users
    List<String> selectedWithoutNull = new ArrayList<String>(selected);
    for (int i = 0; i < selected.size(); i++) {
      if (Strings.isNullOrEmpty(selected.get(i))) selectedWithoutNull.remove(i);
    }

    final List<String> where = new ArrayList<>();
    final Map<String, Object> params = new HashMap<>();

    where.add(
        "((self.partner is not null AND self.partner.emailAddress is not null) OR (self.email is not null))");

    if (!isBlank(matching)) {
      where.add(
          "(LOWER(self.partner.emailAddress.address) like LOWER(:email) OR LOWER(self.partner.fullName) like LOWER(:email) OR LOWER(self.email) like LOWER(:email) OR LOWER(self.name) like LOWER(:email))");
      params.put("email", "%" + matching + "%");
    }
    if (selectedWithoutNull != null && !selectedWithoutNull.isEmpty()) {
      where.add("self.partner.emailAddress.address not in (:selected)");
      params.put("selected", selectedWithoutNull);
    }

    final String filter = Joiner.on(" AND ").join(where);
    final Query<User> query = Query.of(User.class);

    if (!isBlank(filter)) {
      query.filter(filter);
      query.bind(params);
    }

    final List<InternetAddress> addresses = new ArrayList<>();
    for (User user : query.fetch(maxResult)) {
      try {
        if (user.getPartner() != null
            && user.getPartner().getEmailAddress() != null
            && !Strings.isNullOrEmpty(user.getPartner().getEmailAddress().getAddress())) {
          final InternetAddress item =
              new InternetAddress(
                  user.getPartner().getEmailAddress().getAddress(), user.getFullName());
          addresses.add(item);
          selectedWithoutNull.add(user.getPartner().getEmailAddress().getAddress());
        } else if (!Strings.isNullOrEmpty(user.getEmail())) {
          final InternetAddress item = new InternetAddress(user.getEmail(), user.getFullName());
          addresses.add(item);
          selectedWithoutNull.add(user.getEmail());
        }

      } catch (UnsupportedEncodingException e) {
        TraceBackService.trace(e);
      }
    }

    // Partners

    final List<String> where2 = new ArrayList<>();
    final Map<String, Object> params2 = new HashMap<>();

    where2.add("self.emailAddress is not null");

    if (!isBlank(matching)) {
      where2.add(
          "(LOWER(self.emailAddress.address) like LOWER(:email) OR LOWER(self.fullName) like LOWER(:email))");
      params2.put("email", "%" + matching + "%");
    }
    if (selectedWithoutNull != null && !selectedWithoutNull.isEmpty()) {
      where2.add("self.emailAddress.address not in (:selected)");
      params2.put("selected", selectedWithoutNull);
    }

    final String filter2 = Joiner.on(" AND ").join(where2);
    final Query<Partner> query2 = Query.of(Partner.class);

    if (!isBlank(filter2)) {
      query2.filter(filter2);
      query2.bind(params2);
    }

    for (Partner partner : query2.fetch(maxResult)) {
      try {
        if (partner.getEmailAddress() != null
            && !Strings.isNullOrEmpty(partner.getEmailAddress().getAddress())) {
          final InternetAddress item =
              new InternetAddress(partner.getEmailAddress().getAddress(), partner.getFullName());
          addresses.add(item);
        }
      } catch (UnsupportedEncodingException e) {
        TraceBackService.trace(e);
      }
    }

    return addresses;
  }

  @Override
  protected Set<String> recipients(MailMessage message, Model entity) {
    final Set<String> recipients = new LinkedHashSet<>();
    final MailFollowerRepository followers = Beans.get(MailFollowerRepository.class);
    String entityName = entity.getClass().getName();

    if (message.getRecipients() != null) {
      for (MailAddress address : message.getRecipients()) {
        recipients.add(address.getAddress());
      }
    }

    for (MailFollower follower : followers.findAll(message)) {
      if (follower.getArchived()) {
        continue;
      }
      User user = follower.getUser();
      if (user != null) {
        if (!(user.getReceiveEmails()
            && user.getFollowedMetaModelSet().stream()
                .anyMatch(x -> x.getFullName().equals(entityName)))) {
          continue;
        } else {
          Partner partner = user.getPartner();
          if (partner != null && partner.getEmailAddress() != null) {
            recipients.add(partner.getEmailAddress().getAddress());
          } else if (user.getEmail() != null) {
            recipients.add(user.getEmail());
          }
        }
      } else {

        if (follower.getEmail() != null) {
          recipients.add(follower.getEmail().getAddress());
        } else {
          log.info("No email address found for follower : " + follower);
        }
      }
    }
    return Sets.filter(recipients, Predicates.notNull());
  }

  @Override
  public void send(MailMessage message) throws MailException {
    if (appBaseService.isApp("base")) {
      Boolean activateSendingEmail = appBaseService.getAppBase().getActivateSendingEmail();
      if (activateSendingEmail == null || !activateSendingEmail) {
        return;
      }
      super.send(message);
    }
  }
}
