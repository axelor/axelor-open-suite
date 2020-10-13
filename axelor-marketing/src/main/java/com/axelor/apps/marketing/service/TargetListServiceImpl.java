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
package com.axelor.apps.marketing.service;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.crm.db.repo.LeadRepository;
import com.axelor.apps.marketing.db.TargetList;
import com.axelor.apps.marketing.exception.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
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

  @Inject private FilterJpqlService filterJpqlService;

  @Inject private PartnerRepository partnerRepo;

  @Inject private LeadRepository leadRepo;

  @Override
  public String getPartnerQuery(TargetList targetList) {
    String partnerFilters = null;

    if (targetList.getPartnerQueryTypeSelect() == 0) {
      partnerFilters = filterJpqlService.getJpqlFilters(targetList.getPartnerFilterList());
    }
    if (targetList.getPartnerQueryTypeSelect() == 1) {
      partnerFilters = targetList.getPartnerQuery();
    }
    return partnerFilters;
  }

  @Override
  public String getLeadQuery(TargetList targetList) {
    String leadFilters = null;

    if (targetList.getLeadQueryTypeSelect() == 0) {
      leadFilters = filterJpqlService.getJpqlFilters(targetList.getLeadFilterList());
    }
    if (targetList.getLeadQueryTypeSelect() == 1) {
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
          partnerSet.addAll(partnerRepo.all().filter(filter).fetch());
        } catch (Exception e) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(IExceptionMessage.CAMPAIGN_PARTNER_FILTER));
        }
      }
      for (Partner partner : target.getPartnerSet()) {
        partnerSet.add(partner);
      }
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
          leadSet.addAll(leadRepo.all().filter(filter).fetch());
        } catch (Exception e) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(IExceptionMessage.CAMPAIGN_LEAD_FILTER));
        }
      }
      for (Lead lead : target.getLeadSet()) {
        leadSet.add(lead);
      }
    }
    return leadSet;
  }
}
