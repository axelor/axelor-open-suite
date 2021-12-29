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
package com.axelor.apps.portal.web;

import com.axelor.apps.client.portal.db.PortalQuotation;
import com.axelor.apps.client.portal.db.repo.PortalQuotationRepository;
import com.axelor.apps.portal.service.PortalQuotationService;
import com.axelor.apps.portal.translation.ITranslation;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import javax.mail.MessagingException;

public class PortalQuotationController {

  @Inject PortalQuotationService portalQuotationService;

  public void sendConfirmCode(ActionRequest request, ActionResponse response)
      throws MessagingException, AxelorException {

    PortalQuotation portalQuotation = request.getContext().asType(PortalQuotation.class);
    Integer code = portalQuotationService.sendConfirmCode(portalQuotation);

    ActionViewBuilder confirmView =
        ActionView.define(I18n.get(ITranslation.PORTAL_QUATATION))
            .model(PortalQuotation.class.getName())
            .add("form", "portal-quotation-confirm-form")
            .param("popup", "reload")
            .param("show-toolbar", "false")
            .param("show-confirm", "false")
            .param("popup-save", "false")
            .param("forceEdit", "true")
            .context("_showRecord", portalQuotation.getId())
            .context("sentCode", code);

    response.setView(confirmView.map());
    response.setNotify(I18n.get(ITranslation.PORTAL_QUATATION_CODE));
  }

  public void confirmPortalQuotation(ActionRequest request, ActionResponse response)
      throws MessagingException, AxelorException, UnsupportedEncodingException {

    Context context = request.getContext();
    String name =
        URLDecoder.decode(
            context.get("signatureName").toString(), StandardCharsets.UTF_8.toString());

    PortalQuotation portalQuotation = context.asType(PortalQuotation.class);
    portalQuotationService.confirmPortalQuotation(portalQuotation, name);
    response.setCanClose(true);
  }

  @Transactional
  public void cancelPortalQuotation(ActionRequest request, ActionResponse response) {

    PortalQuotation portalQuotation = request.getContext().asType(PortalQuotation.class);
    portalQuotation = Beans.get(PortalQuotationRepository.class).find(portalQuotation.getId());
    portalQuotation.setStatusSelect(SaleOrderRepository.STATUS_CANCELED);
    Beans.get(PortalQuotationRepository.class).save(portalQuotation);
    response.setNotify(I18n.get(ITranslation.PORTAL_QUATATION_CANCEL));
    response.setReload(true);
  }
}
