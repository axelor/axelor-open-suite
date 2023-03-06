/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.web;

import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.account.service.analytic.AnalyticDistributionTemplateService;
import com.axelor.apps.account.service.fixedasset.FixedAssetService;
import com.axelor.exception.ResponseMessageType;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class AnalyticDistributionTemplateController {

  public void validateTemplatePercentages(ActionRequest request, ActionResponse response) {
    try {
      AnalyticDistributionTemplate analyticDistributionTemplate =
          request.getContext().asType(AnalyticDistributionTemplate.class);
      Beans.get(AnalyticDistributionTemplateService.class)
          .validateTemplatePercentages(analyticDistributionTemplate);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void checkTemplateCompany(ActionRequest request, ActionResponse response) {
    try {
      AnalyticDistributionTemplate analyticDistributionTemplate =
          request.getContext().asType(AnalyticDistributionTemplate.class);
      Beans.get(AnalyticDistributionTemplateService.class)
          .checkAnalyticDistributionTemplateCompany(analyticDistributionTemplate);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void calculateAnalyticFixedAsset(ActionRequest request, ActionResponse response) {
    try {
      if (request.getContext().get("fixedAsset") != null) {
        Long fixedAssetId = (Long) Long.valueOf((Integer) request.getContext().get("fixedAsset"));
        FixedAsset fixedAsset = Beans.get(FixedAssetRepository.class).find(fixedAssetId);
        Beans.get(FixedAssetService.class).updateAnalytic(fixedAsset);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void checkAnalyticAccounts(ActionRequest request, ActionResponse response) {
    try {
      AnalyticDistributionTemplate analyticDistributionTemplate =
          request.getContext().asType(AnalyticDistributionTemplate.class);
      Beans.get(AnalyticDistributionTemplateService.class)
          .checkAnalyticAccounts(analyticDistributionTemplate);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }
}
