/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
package com.axelor.apps.partner.portal.service;

import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.base.service.app.AppService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.client.portal.db.repo.ClientResourceRepository;
import com.axelor.apps.client.portal.db.repo.DiscussionGroupRepository;
import com.axelor.apps.client.portal.db.repo.GeneralAnnouncementRepository;
import com.axelor.apps.client.portal.db.repo.IdeaRepository;
import com.axelor.apps.client.portal.db.repo.PortalIdeaTagRepository;
import com.axelor.apps.client.portal.db.repo.PortalQuotationRepository;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.crm.db.repo.LeadRepository;
import com.axelor.apps.helpdesk.db.repo.TicketRepository;
import com.axelor.apps.portal.service.ClientViewServiceImpl;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.db.JpaSecurity;
import com.axelor.db.Query;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ClientViewPartnerPortalServiceImpl extends ClientViewServiceImpl
    implements ClientViewPartnerPortalService {

  protected LeadRepository leadRepo;

  @Inject
  public ClientViewPartnerPortalServiceImpl(
      SaleOrderRepository saleOrderRepo,
      StockMoveRepository stockMoveRepo,
      ProjectRepository projectRepo,
      TicketRepository ticketRepo,
      InvoiceRepository invoiceRepo,
      ProjectTaskRepository projectTaskRepo,
      JpaSecurity jpaSecurity,
      AppService appService,
      PortalQuotationRepository portalQuotationRepo,
      DiscussionGroupRepository discussionGroupRepo,
      GeneralAnnouncementRepository announcementRepo,
      ClientResourceRepository clientResourceRepo,
      IdeaRepository ideaRepo,
      PortalIdeaTagRepository ideaTagRepo,
      LeadRepository leadRepo) {
    super(
        saleOrderRepo,
        stockMoveRepo,
        projectRepo,
        ticketRepo,
        invoiceRepo,
        projectTaskRepo,
        jpaSecurity,
        appService,
        portalQuotationRepo,
        discussionGroupRepo,
        announcementRepo,
        clientResourceRepo,
        ideaRepo,
        ideaTagRepo);
    this.leadRepo = leadRepo;
  }

  @Override
  public Long getLead() {

    Query<Lead> query = leadRepo.all();
    String filter = "self.assignedPartner.linkedUser.id = :userId";

    List<Long> idList = getUnreadLeads();
    if (ObjectUtils.notEmpty(idList)) {
      filter += " AND self.id NOT IN :ids";
      query.bind("ids", idList);
    }

    return query.filter(filter).bind("userId", getClientUser().getId()).count();
  }

  @Override
  public Long getNewLead() {

    Query<Lead> query = leadRepo.all();
    String filter = "self.assignedPartner.linkedUser.id = :userId";

    List<Long> idList = getUnreadLeads();
    if (ObjectUtils.notEmpty(idList)) {
      filter += " AND self.id IN :ids";
      query.bind("ids", idList);
    } else {
      filter += " AND self.id IN (0)";
    }

    return query.filter(filter).bind("userId", getClientUser().getId()).count();
  }

  @Override
  public List<Long> getUnreadLeads() {

    User currentUser = Beans.get(UserService.class).getUser();
    String ids = currentUser.getLeadUnreadIds();
    if (StringUtils.isBlank(ids)) {
      return new ArrayList<>();
    }

    return Arrays.asList(ids.split(",")).stream()
        .filter(idStr -> StringUtils.notBlank(idStr))
        .map(Long::parseLong)
        .collect(Collectors.toList());
  }
}
