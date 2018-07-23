/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.apps.marketing.web;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.crm.db.repo.LeadRepository;
import com.axelor.apps.marketing.db.TargetList;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.studio.service.FilterService;
import com.google.inject.Inject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TargetListController {

  @Inject private FilterService filterService;

  @Inject private PartnerRepository partnerRepo;

  @Inject private LeadRepository leadRepo;

  public void applyQuery(ActionRequest request, ActionResponse response) {

    TargetList targetList = request.getContext().asType(TargetList.class);

    String partnerFilters = filterService.getSqlFilters(targetList.getPartnerFilterList());
    if (partnerFilters != null) {
      List<Partner> partners = partnerRepo.all().filter(partnerFilters).fetch();
      Set<Partner> partnerSet = new HashSet<Partner>();
      partnerSet.addAll(partners);
      response.setValue("partnerSet", partnerSet);
    }

    String leadFilers = filterService.getSqlFilters(targetList.getLeadFilterList());
    if (leadFilers != null) {
      List<Lead> leads = leadRepo.all().filter(leadFilers).fetch();
      Set<Lead> leadSet = new HashSet<Lead>();
      leadSet.addAll(leads);
      response.setValue("leadSet", leadSet);
    }
  }
}
