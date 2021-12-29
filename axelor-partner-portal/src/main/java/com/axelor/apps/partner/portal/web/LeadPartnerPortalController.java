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
package com.axelor.apps.partner.portal.web;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.crm.db.repo.LeadRepository;
import com.axelor.apps.partner.portal.db.LeadPartnerComment;
import com.axelor.apps.partner.portal.db.repo.LeadPartnerCommentRepository;
import com.axelor.apps.partner.portal.service.ClientViewPartnerPortalService;
import com.axelor.apps.partner.portal.service.LeadPartnerPortalService;
import com.axelor.auth.db.User;
import com.axelor.auth.db.repo.UserRepository;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.mail.db.MailMessage;
import com.axelor.mail.db.repo.MailMessageRepository;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class LeadPartnerPortalController {

  @Inject UserService userService;

  @Transactional
  public void assignToPartner(ActionRequest request, ActionResponse response) {

    Context context = request.getContext();
    Lead lead = context.asType(Lead.class);
    lead = Beans.get(LeadRepository.class).find(lead.getId());
    if (!context.containsKey("partner") || context.get("partner") == null) {
      return;
    }

    Partner partner = (Partner) request.getContext().get("partner");
    partner = Beans.get(PartnerRepository.class).find(partner.getId());
    lead.setAssignedPartner(partner);
    Beans.get(LeadPartnerPortalService.class).manageUnreadLead(lead, partner.getLinkedUser());

    CopyOnWriteArrayList<MailMessage> discussions =
        new CopyOnWriteArrayList<>(
            Beans.get(MailMessageRepository.class)
                .all()
                .filter("self.relatedModel = :relatedModel AND self.relatedId = :relatedId")
                .bind("relatedModel", Lead.class.getName())
                .bind("relatedId", lead.getId())
                .fetch());
    if (ObjectUtils.isEmpty(discussions)) {
      return;
    }

    LeadPartnerComment comment = lead.getPartnerComment();
    if (comment == null) {
      comment = new LeadPartnerComment();
      comment.setName("Comments");
      comment.setLead(lead);
      lead.setPartnerComment(comment);
      Beans.get(LeadPartnerCommentRepository.class).save(comment);
    }
    for (MailMessage discussion : discussions) {
      discussion.setRelatedId(comment.getId());
      discussion.setRelatedModel(comment.getClass().getName());
      Beans.get(MailMessageRepository.class).save(discussion);
    }

    Beans.get(LeadRepository.class).save(lead);
    response.setReload(true);
  }

  @Transactional
  public void markRead(ActionRequest request, ActionResponse response) {

    Lead lead = request.getContext().asType(Lead.class);
    Long id = lead.getId();
    if (id == null) {
      return;
    }

    User currentUser = Beans.get(UserService.class).getUser();
    String ids =
        StringUtils.notBlank(currentUser.getLeadUnreadIds()) ? currentUser.getLeadUnreadIds() : "";

    List<String> idList = new ArrayList<String>(Arrays.asList(ids.split(",")));
    String idStr = id.toString();
    if (!ObjectUtils.isEmpty(idList) && idList.contains(idStr)) {
      idList.remove(idStr);
      currentUser.setLeadUnreadIds(String.join(",", idList));
      Beans.get(UserRepository.class).save(currentUser);
    }
  }

  public void showNewLead(ActionRequest request, ActionResponse response) {

    User currentUser = userService.getUser();
    ActionViewBuilder actionBuilder =
        ActionView.define(I18n.get("Leads"))
            .model(Lead.class.getName())
            .add("grid", "partner-portal-lead-grid")
            .add("form", "lead-form")
            .param("search-filters", "lead-filters")
            .context("_myActiveTeam", userService.getUserActiveTeam())
            .context(
                "todayDate",
                Beans.get(AppBaseService.class).getTodayDate(currentUser.getActiveCompany()))
            .context("_internalUserId", currentUser.getId());

    String domain = "self.assignedPartner.linkedUser.id = :_internalUserId";
    List<Long> ids = Beans.get(ClientViewPartnerPortalService.class).getUnreadLeads();
    if (ObjectUtils.notEmpty(ids)) {
      domain += " AND self.id IN :ids";
      actionBuilder.context("ids", ids);
    } else {
      domain += " AND self.id IN (0)";
    }

    response.setView(actionBuilder.domain(domain).map());
  }

  public void showCurrentLead(ActionRequest request, ActionResponse response) {

    User currentUser = userService.getUser();
    ActionViewBuilder actionBuilder =
        ActionView.define(I18n.get("Leads"))
            .model(Lead.class.getName())
            .add("grid", "partner-portal-lead-grid")
            .add("form", "lead-form")
            .param("search-filters", "lead-filters")
            .context("_myActiveTeam", userService.getUserActiveTeam())
            .context(
                "todayDate",
                Beans.get(AppBaseService.class).getTodayDate(currentUser.getActiveCompany()))
            .context("_internalUserId", currentUser.getId());

    String domain = "self.assignedPartner.linkedUser.id = :_internalUserId";
    List<Long> ids = Beans.get(ClientViewPartnerPortalService.class).getUnreadLeads();
    if (ObjectUtils.notEmpty(ids)) {
      domain += " AND self.id NOT IN :ids";
      actionBuilder.context("ids", ids);
    }

    response.setView(actionBuilder.domain(domain).map());
  }
}
