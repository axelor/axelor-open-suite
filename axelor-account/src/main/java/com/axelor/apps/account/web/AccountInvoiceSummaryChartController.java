package com.axelor.apps.account.web;

import com.axelor.apps.account.util.helpers.AccountInvoiceChartHelper;
import com.axelor.apps.account.util.helpers.models.AccountInvoiceChartConstants;
import com.axelor.apps.account.util.helpers.models.ChartMonthTurnoverDto;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class AccountInvoiceSummaryChartController {

  public void calculate(ActionRequest request, ActionResponse response) {
    try {
      // Get field values
      String companyCode = request.getContext().get("companyCode").toString();
      String operation = request.getContext().get("operation").toString();

      List<ChartMonthTurnoverDto> resultList = new ArrayList<>();

      // Fill data depending on operation select
      switch (operation) {
        case AccountInvoiceChartConstants.REVENUE:
          resultList = AccountInvoiceChartHelper.getRevenueByMonths(companyCode);

          break;
        case AccountInvoiceChartConstants.COST:
          resultList = AccountInvoiceChartHelper.getCostByMonths(companyCode);
          break;
        case AccountInvoiceChartConstants.CALCULATE_GROSS_PROFIT:
          resultList = AccountInvoiceChartHelper.getProfitDTOByMonth(companyCode);
          break;
        case AccountInvoiceChartConstants.WORKFORCE_COST:
          List<ChartMonthTurnoverDto> revenueDTO =
              AccountInvoiceChartHelper.getRevenueByMonths(companyCode);
          Set<Integer> yearSet = revenueDTO.stream().map(d -> d.year).collect(Collectors.toSet());
          resultList = AccountInvoiceChartHelper.getWorkforceCostByMonths(yearSet, false);
          break;
        case AccountInvoiceChartConstants.CALCULATE_EBITDA:
          resultList = AccountInvoiceChartHelper.getEBITDADTOByMonth();
          break;
      }
      // Set result list data to response

      response.setData(resultList);

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
