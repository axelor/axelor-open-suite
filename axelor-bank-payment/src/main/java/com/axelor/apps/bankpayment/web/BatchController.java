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
package com.axelor.apps.bankpayment.web;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.bankpayment.service.batch.BatchBankPaymentService;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.repo.BatchRepository;
import com.axelor.exception.ResponseMessageType;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class BatchController {

    public void createBankOrder(ActionRequest request, ActionResponse response) {
        try {
            Batch batch = request.getContext().asType(Batch.class);
            batch = Beans.get(BatchRepository.class).find(batch.getId());

            Beans.get(BatchBankPaymentService.class).createBankOrder(batch);

            response.setReload(true);

            ActionViewBuilder actionViewBuilder = ActionView.define(I18n.get("Bank order"));
            actionViewBuilder.model(MoveLine.class.getName());
            actionViewBuilder.add("form", "bank-order-form");
            actionViewBuilder.context("_showRecord", batch.getBankOrder().getId());

            response.setView(actionViewBuilder.map());
        } catch (Exception e) {
            TraceBackService.trace(response, e, ResponseMessageType.ERROR);
        }
    }

}
