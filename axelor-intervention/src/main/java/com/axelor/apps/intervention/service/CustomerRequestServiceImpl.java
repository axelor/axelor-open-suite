/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.intervention.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.intervention.db.CustomerRequest;
import com.axelor.apps.intervention.db.Intervention;
import com.axelor.apps.intervention.db.repo.CustomerRequestRepository;
import com.axelor.apps.intervention.service.helper.CustomerRequestHelper;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.saleorder.SaleOrderCreateService;
import com.axelor.auth.AuthUtils;
import com.axelor.message.db.Template;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.Optional;

public class CustomerRequestServiceImpl implements CustomerRequestService {

  protected final InterventionService interventionService;
  protected final CustomerRequestRepository customerRequestRepository;
  protected final SaleOrderRepository saleOrderRepository;
  protected final SaleOrderCreateService saleOrderCreateService;
  protected final AppInterventionService appInterventionService;
  protected final InterventionPartnerService interventionPartnerService;

  @Inject
  public CustomerRequestServiceImpl(
      InterventionService interventionService,
      CustomerRequestRepository customerRequestRepository,
      SaleOrderRepository saleOrderRepository,
      SaleOrderCreateService saleOrderCreateService,
      AppInterventionService appInterventionService,
      InterventionPartnerService interventionPartnerService) {
    this.interventionService = interventionService;
    this.customerRequestRepository = customerRequestRepository;
    this.saleOrderRepository = saleOrderRepository;
    this.saleOrderCreateService = saleOrderCreateService;
    this.appInterventionService = appInterventionService;
    this.interventionPartnerService = interventionPartnerService;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void takeIntoAccount(CustomerRequest request) {
    request.setStatusSelect(CustomerRequestRepository.CUSTOMER_REQUEST_STATUS_TAKEN_INTO_ACCOUNT);
    Optional<Template> template = CustomerRequestHelper.getTemplate(request);
    template.ifPresent(value -> CustomerRequestHelper.generateAndSendEmail(value, request));
    customerRequestRepository.save(request);
  }

  @Override
  public Intervention createAnIntervention(CustomerRequest request) throws AxelorException {
    Intervention intervention = interventionService.create(request);
    if (request
            .getStatusSelect()
            .compareTo(CustomerRequestRepository.CUSTOMER_REQUEST_STATUS_IN_PROGRESS)
        < 0) {
      inProgress(request);
    }
    return intervention;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void inProgress(CustomerRequest request) {
    request.setStatusSelect(CustomerRequestRepository.CUSTOMER_REQUEST_STATUS_IN_PROGRESS);
    customerRequestRepository.save(request);
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public SaleOrder generateSaleOrder(CustomerRequest customerRequest) throws AxelorException {
    Partner contactPartner = customerRequest.getContact();
    SaleOrder saleOrder =
        saleOrderCreateService.createSaleOrder(
            AuthUtils.getUser(),
            customerRequest.getCompany(),
            contactPartner,
            customerRequest.getCompany().getCurrency(),
            null,
            null,
            null,
            null,
            customerRequest.getDeliveredPartner(),
            AuthUtils.getUser().getActiveTeam(),
            null,
            null,
            customerRequest.getTradingName());

    saleOrder.setCompany(customerRequest.getCompany());
    saleOrder.setTradingName(customerRequest.getTradingName());
    saleOrder.setDeliveredPartner(customerRequest.getDeliveredPartner());

    saleOrder.setInvoicedPartner(
        interventionPartnerService.getDefaultInvoicedPartner(
            customerRequest.getDeliveredPartner()));
    saleOrder.setDescription(customerRequest.getDescription());
    saleOrder.setCustomerRequest(customerRequest);
    saleOrder.setTradingName(customerRequest.getTradingName());
    saleOrder = saleOrderRepository.save(saleOrder);
    customerRequest.setSaleQuotations(saleOrder);
    customerRequestRepository.save(customerRequest);
    return saleOrder;
  }
}
