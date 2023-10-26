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

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.crm.translation.ITranslation;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.db.Model;
import com.axelor.i18n.I18n;
import com.axelor.message.db.EmailAddress;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class LeadDuplicateServiceImpl implements LeadDuplicateService {

  protected Provider<EmailDomainToolService<Lead>> leadEmailDomainToolServiceProvider;
  protected Provider<EmailDomainToolService<Partner>> partnerEmailDomainToolServiceProvider;

  @Inject
  public LeadDuplicateServiceImpl(
      Provider<EmailDomainToolService<Lead>> leadEmailDomainToolServiceProvider,
      Provider<EmailDomainToolService<Partner>> partnerEmailDomainToolServiceProvider) {
    this.leadEmailDomainToolServiceProvider = leadEmailDomainToolServiceProvider;
    this.partnerEmailDomainToolServiceProvider = partnerEmailDomainToolServiceProvider;
  }

  @Override
  public String getDuplicateRecordsFullName(Lead lead) {
    if (Optional.ofNullable(lead)
        .map(Lead::getEmailAddress)
        .map(EmailAddress::getName)
        .filter(StringUtils::notBlank)
        .isEmpty()) {
      return "";
    }
    return String.format(
        "%s%s%s", getDuplicateLeads(lead), getDuplicateContacts(lead), getDuplicateProspects(lead));
  }

  protected String getDuplicateLeads(Lead lead) {
    List<Lead> duplicateLeadList = getLeadsWithSameDomainName(lead);
    if (ObjectUtils.isEmpty(duplicateLeadList)) {
      return "";
    }
    return getHtmlListStr(
        duplicateLeadList, Lead::getFullName, null, I18n.get(ITranslation.CRM_DUPLICATE_LEADS));
  }

  protected List<Lead> getLeadsWithSameDomainName(Lead lead) {

    final EmailDomainToolService<Lead> emailDomainToolService =
        leadEmailDomainToolServiceProvider.get();

    return emailDomainToolService.getEntitiesWithSameEmailAddress(
        Lead.class, lead.getId(), lead.getEmailAddress(), null);
  }

  protected String getDuplicateContacts(Lead lead) {
    List<Partner> duplicateContactList = getPartnersWithSameDomainName(lead);
    if (ObjectUtils.isEmpty(duplicateContactList)) {
      return "";
    }

    return getHtmlListStr(
        duplicateContactList,
        Partner::getFullName,
        Partner::getIsContact,
        I18n.get(ITranslation.CRM_DUPLICATE_CONTACTS));
  }

  protected String getDuplicateProspects(Lead lead) {
    List<Partner> duplicateProspectList = getPartnersWithSameDomainName(lead);
    if (ObjectUtils.isEmpty(duplicateProspectList)) {
      return "";
    }

    return getHtmlListStr(
        duplicateProspectList,
        Partner::getFullName,
        Partner::getIsProspect,
        I18n.get(ITranslation.CRM_DUPLICATE_PROSPECTS));
  }

  protected List<Partner> getPartnersWithSameDomainName(Lead lead) {

    final EmailDomainToolService<Partner> emailDomainToolService =
        partnerEmailDomainToolServiceProvider.get();

    return emailDomainToolService.getEntitiesWithSameEmailAddress(
        Partner.class, null, lead.getEmailAddress(), null);
  }

  protected <T extends Model> String getHtmlListStr(
      List<T> modelList, Function<T, String> mapper, Predicate<T> predicate, String title) {
    String htmlListStr = "";

    if (ObjectUtils.isEmpty(modelList)) {
      return htmlListStr;
    }

    if (predicate == null) {
      predicate = t -> true;
    }

    htmlListStr =
        modelList.stream()
            .filter(predicate)
            .map(mapper)
            .map(item -> "<li>" + item + "</li>")
            .collect(Collectors.joining());

    if (StringUtils.isBlank(htmlListStr)) {
      return htmlListStr;
    }

    return String.format("<b>%s</b><ul>%s</ul>", title, htmlListStr);
  }
}
