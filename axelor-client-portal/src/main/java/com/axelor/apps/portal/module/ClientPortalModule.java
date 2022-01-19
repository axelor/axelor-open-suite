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
package com.axelor.apps.portal.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.businessproduction.service.SaleOrderWorkflowServiceBusinessProductionImpl;
import com.axelor.apps.client.portal.db.repo.CardRepository;
import com.axelor.apps.client.portal.db.repo.ClientResourceRepository;
import com.axelor.apps.client.portal.db.repo.DiscussionGroupRepository;
import com.axelor.apps.client.portal.db.repo.DiscussionPostRepository;
import com.axelor.apps.client.portal.db.repo.GeneralAnnouncementRepository;
import com.axelor.apps.client.portal.db.repo.IdeaRepository;
import com.axelor.apps.client.portal.db.repo.PortalQuotationRepository;
import com.axelor.apps.portal.db.repo.CardManagementRepository;
import com.axelor.apps.portal.db.repo.ClientResourceManagementRepository;
import com.axelor.apps.portal.db.repo.DiscussionGroupManagementRepository;
import com.axelor.apps.portal.db.repo.DiscussionPostManagementRepository;
import com.axelor.apps.portal.db.repo.GeneralAnnounceManagementRepository;
import com.axelor.apps.portal.db.repo.IdeaManagementRepository;
import com.axelor.apps.portal.db.repo.PortalQuotationManagementRepository;
import com.axelor.apps.portal.db.repo.ProductPortalRepository;
import com.axelor.apps.portal.service.AddressPortalService;
import com.axelor.apps.portal.service.AddressPortalServiceImpl;
import com.axelor.apps.portal.service.CardService;
import com.axelor.apps.portal.service.CardServiceImpl;
import com.axelor.apps.portal.service.ClientViewService;
import com.axelor.apps.portal.service.ClientViewServiceImpl;
import com.axelor.apps.portal.service.DiscussionGroupService;
import com.axelor.apps.portal.service.DiscussionGroupServiceImpl;
import com.axelor.apps.portal.service.MetaFilesPortal;
import com.axelor.apps.portal.service.PartnerPortalService;
import com.axelor.apps.portal.service.PartnerPortalServiceImpl;
import com.axelor.apps.portal.service.PortalQuotationService;
import com.axelor.apps.portal.service.PortalQuotationServiceImpl;
import com.axelor.apps.portal.service.ProductPortalService;
import com.axelor.apps.portal.service.ProductPortalServiceImpl;
import com.axelor.apps.portal.service.SaleOrderPortalService;
import com.axelor.apps.portal.service.SaleOrderPortalServiceImpl;
import com.axelor.apps.portal.service.SaleOrderWorkflowServicePortalImpl;
import com.axelor.apps.portal.service.paybox.PayboxService;
import com.axelor.apps.portal.service.paybox.PayboxServiceImpl;
import com.axelor.apps.portal.service.paypal.PaypalService;
import com.axelor.apps.portal.service.paypal.PaypalServiceImpl;
import com.axelor.apps.portal.service.response.PortalRestResponse;
import com.axelor.apps.portal.service.stripe.StripePaymentService;
import com.axelor.apps.portal.service.stripe.StripePaymentServiceImpl;
import com.axelor.apps.portal.web.MailPortalController;
import com.axelor.apps.portal.web.interceptor.PortalResponseInterceptor;
import com.axelor.apps.production.db.repo.ProductProductionRepository;
import com.axelor.mail.web.MailController;
import com.axelor.meta.MetaFiles;
import com.google.inject.matcher.Matchers;

public class ClientPortalModule extends AxelorModule {

  @Override
  protected void configure() {
    bind(ClientViewService.class).to(ClientViewServiceImpl.class);
    bind(PortalQuotationService.class).to(PortalQuotationServiceImpl.class);
    bind(AddressPortalService.class).to(AddressPortalServiceImpl.class);
    bind(PartnerPortalService.class).to(PartnerPortalServiceImpl.class);
    bind(PartnerPortalService.class).to(PartnerPortalServiceImpl.class);
    bind(SaleOrderPortalService.class).to(SaleOrderPortalServiceImpl.class);
    bind(StripePaymentService.class).to(StripePaymentServiceImpl.class);
    bind(ProductPortalService.class).to(ProductPortalServiceImpl.class);
    bind(SaleOrderWorkflowServiceBusinessProductionImpl.class)
        .to(SaleOrderWorkflowServicePortalImpl.class);
    bind(PaypalService.class).to(PaypalServiceImpl.class);
    bind(PayboxService.class).to(PayboxServiceImpl.class);
    bind(DiscussionGroupService.class).to(DiscussionGroupServiceImpl.class);
    bind(CardService.class).to(CardServiceImpl.class);
    bind(MetaFiles.class).to(MetaFilesPortal.class);
    // intercept all response methods
    bindInterceptor(
        Matchers.any(),
        Matchers.returns(Matchers.subclassesOf(PortalRestResponse.class)),
        new PortalResponseInterceptor());

    bind(DiscussionGroupRepository.class).to(DiscussionGroupManagementRepository.class);
    bind(DiscussionPostRepository.class).to(DiscussionPostManagementRepository.class);
    bind(PortalQuotationRepository.class).to(PortalQuotationManagementRepository.class);
    bind(ClientResourceRepository.class).to(ClientResourceManagementRepository.class);
    bind(IdeaRepository.class).to(IdeaManagementRepository.class);
    bind(CardRepository.class).to(CardManagementRepository.class);
    bind(ProductProductionRepository.class).to(ProductPortalRepository.class);

    bind(MailController.class).to(MailPortalController.class);
    bind(GeneralAnnouncementRepository.class).to(GeneralAnnounceManagementRepository.class);
  }
}
