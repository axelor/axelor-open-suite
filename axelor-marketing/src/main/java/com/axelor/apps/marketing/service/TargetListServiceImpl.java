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
package com.axelor.apps.marketing.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.crm.db.repo.LeadRepository;
import com.axelor.apps.marketing.db.TargetList;
import com.axelor.apps.marketing.db.repo.TargetListRepository;
import com.axelor.apps.marketing.exception.MarketingExceptionMessage;
import com.axelor.i18n.I18n;
import com.axelor.studio.service.filter.FilterJpqlService;
import com.google.inject.Inject;
import java.util.HashSet;
import java.util.Set;

/**
 * This service class use to get filtered Partners and Leads.
 *
 * @author axelor
 */
public class TargetListServiceImpl implements TargetListService {

  protected FilterJpqlService filterJpqlService;
  protected PartnerRepository partnerRepository;
  protected LeadRepository leadRepository;

  @Inject
  public TargetListServiceImpl(
      FilterJpqlService filterJpqlService,
      PartnerRepository partnerRepository,
      LeadRepository leadRepository) {
    this.filterJpqlService = filterJpqlService;
    this.partnerRepository = partnerRepository;
    this.leadRepository = leadRepository;
  }

  @Override
  public String getPartnerQuery(TargetList targetList) {
    String partnerFilters = null;

    if (targetList.getPartnerQueryTypeSelect()
        == TargetListRepository.TARGET_QUERY_TYPE_SELECT_GUIDED) {
      partnerFilters = filterJpqlService.getJpqlFilters(targetList.getPartnerFilterList());
    }
    if (targetList.getPartnerQueryTypeSelect()
        == TargetListRepository.TARGET_QUERY_TYPE_SELECT_MANUAL) {
      partnerFilters = targetList.getPartnerQuery();
    }
    return partnerFilters;
  }

  @Override
  public String getLeadQuery(TargetList targetList) {
    String leadFilters = null;

    if (targetList.getLeadQueryTypeSelect()
        == TargetListRepository.TARGET_QUERY_TYPE_SELECT_GUIDED) {
      leadFilters = filterJpqlService.getJpqlFilters(targetList.getLeadFilterList());
    }
    if (targetList.getLeadQueryTypeSelect()
        == TargetListRepository.TARGET_QUERY_TYPE_SELECT_MANUAL) {
      leadFilters = targetList.getLeadQuery();
    }
    return leadFilters;
  }

  @Override
  public Set<Partner> getAllPartners(Set<TargetList> targetListSet) throws AxelorException {
    Set<Partner> partnerSet = new HashSet<>();

    for (TargetList target : targetListSet) {
      String filter = getPartnerQuery(target);
      if (filter != null) {
        try {
          partnerSet.addAll(partnerRepository.all().filter(filter).fetch());
        } catch (Exception e) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(MarketingExceptionMessage.CAMPAIGN_PARTNER_FILTER));
        }
      }
      partnerSet.addAll(target.getPartnerSet());
    }
    return partnerSet;
  }

  @Override
  public Set<Lead> getAllLeads(Set<TargetList> targetListSet) throws AxelorException {
    Set<Lead> leadSet = new HashSet<>();

    for (TargetList target : targetListSet) {
      String filter = getLeadQuery(target);
      if (filter != null) {
        try {
          leadSet.addAll(leadRepository.all().filter(filter).fetch());
        } catch (Exception e) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(MarketingExceptionMessage.CAMPAIGN_LEAD_FILTER));
        }
      }
      leadSet.addAll(target.getLeadSet());
    }
    return leadSet;
  }
}
