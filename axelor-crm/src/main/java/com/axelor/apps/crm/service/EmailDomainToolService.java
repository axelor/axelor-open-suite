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
package com.axelor.apps.crm.service;

import com.axelor.apps.crm.service.app.AppCrmService;
import com.axelor.common.ObjectUtils;
import com.axelor.db.EntityHelper;
import com.axelor.db.Model;
import com.axelor.db.Query;
import com.axelor.message.db.EmailAddress;
import com.google.inject.Inject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmailDomainToolService<T extends Model> {

  protected AppCrmService appCrmService;

  @Inject
  public EmailDomainToolService(AppCrmService appCrmService) {
    this.appCrmService = appCrmService;
  }

  /**
   * Fetch entities that have the same email domain (for example, given an entity which have the
   * email john@axelor.com, we will search for other entities with an email ending
   * with @axelor.com). <br>
   * Warning: this method will fail with a runtime exception if the given model does not have a
   * field called emailAddress which is a EmailAddress.
   *
   * @param model the model with the email
   * @param emailAddress the referent email address from which we will query other emails with the
   *     same domain
   * @param supplementaryFilter an optional supplementary filter to exclude more entities in the
   *     query
   * @return a list of entities with the same domain
   */
  public List<T> getEntitiesWithSameEmailAddress(
      T model, EmailAddress emailAddress, String supplementaryFilter) {
    return Query.of(EntityHelper.getEntityClass(model))
        .filter(this.computeFilterEmailOnDomain(emailAddress, supplementaryFilter))
        .bind(this.computeParameterForFilter(model, emailAddress))
        .fetch();
  }

  /**
   * Compute the filter to query a model on his email domain. <br>
   * Warning: this method will fail with a runtime exception if the given model does not have a
   * field called emailAddress which is a EmailAddress.
   *
   * @param emailAddress the referent email address from which we will query other emails with the
   *     same domain
   * @param supplementaryFilter an optional supplementary filter to exclude more entities in the
   *     query
   * @return the computed filter
   */
  public String computeFilterEmailOnDomain(EmailAddress emailAddress, String supplementaryFilter) {

    String emailDomainToIgnore = appCrmService.getAppCrm().getEmailDomainToIgnore();
    if (isEmailAddressEmptyOrInvalid(emailAddress)
        || (!ObjectUtils.isEmpty(emailDomainToIgnore)
            && Arrays.stream(emailDomainToIgnore.split(","))
                .anyMatch(
                    emailDomain ->
                        emailDomain.matches(computeEntityDomainNameStr(emailAddress.getName()))))) {
      // false condition to always return an empty list
      return "1 != 1";
    }

    StringBuilder stringBuilder =
        new StringBuilder(
            "self.emailAddress IS NOT NULL"
                + " AND SUBSTRING(self.emailAddress.address,"
                + "      LOCATE('@',self.emailAddress.address)+1) IN :domainName"
                + " AND self.id != :id");

    if (ObjectUtils.notEmpty(supplementaryFilter)) {
      stringBuilder.append(" AND ");
      stringBuilder.append(supplementaryFilter);
    }
    return stringBuilder.toString();
  }

  /**
   * Compute the parameters for the filter on domain. <br>
   * Warning: this method will fail with a runtime exception if the given model does not have a
   * field called emailAddress which is a EmailAddress.
   *
   * @param model the model with the email
   * @param emailAddress the referent email address from which we will query other emails with the
   *     same domain
   * @return a map with the parameters needed to execute the query filter.
   */
  public Map<String, Object> computeParameterForFilter(T model, EmailAddress emailAddress) {
    Map<String, Object> params = new HashMap<>();

    if (isEmailAddressEmptyOrInvalid(emailAddress)) {
      return params;
    }

    params.put("domainName", computeEntityDomainNameStr(emailAddress.getName()));
    params.put("id", model.getId());
    return params;
  }

  protected boolean isEmailAddressEmptyOrInvalid(EmailAddress emailAddress) {
    return emailAddress == null
        || ObjectUtils.isEmpty(emailAddress.getName())
        || !emailAddress.getName().contains("@");
  }

  protected String computeEntityDomainNameStr(String emailAddressName) {
    return emailAddressName.split("@")[1].replace("]", "");
  }
}
