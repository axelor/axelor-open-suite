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
package com.axelor.apps.sale.service;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.crm.message.MessageServiceCrmImpl;
import com.axelor.apps.message.db.repo.MessageRepository;
import com.axelor.apps.message.service.SendMailQueueService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.meta.db.repo.MetaAttachmentRepository;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.google.inject.Inject;
import java.util.Map;

public class MessageServiceSaleImpl extends MessageServiceCrmImpl {

  protected SaleOrderRepository saleOrderRep;

  @Inject
  public MessageServiceSaleImpl(
      MetaAttachmentRepository metaAttachmentRepository,
      MessageRepository messageRepository,
      SendMailQueueService sendMailQueueService,
      UserService userService,
      AppBaseService appBaseService,
      SaleOrderRepository saleOrderRep) {
    super(
        metaAttachmentRepository,
        messageRepository,
        sendMailQueueService,
        userService,
        appBaseService);
    this.saleOrderRep = saleOrderRep;
  }

  @Override
  public void fillContext(
      ActionViewBuilder builder, Map<String, Object> contextMap, String model, Long objectId) {

    if (model.equals(SaleOrder.class.getName())) {

      SaleOrder saleOrder = saleOrderRep.find(objectId);

      if (builder != null) {
        builder.context("_relatedTo2Select", Partner.class.getName());
        builder.context(
            "_relatedTo2SelectId", String.valueOf(saleOrder.getClientPartner().getId()));
      } else {
        contextMap.put("_relatedTo2Select", Partner.class.getName());
        contextMap.put("_relatedTo2SelectId", saleOrder.getClientPartner().getId());
      }
    }
  }
}
