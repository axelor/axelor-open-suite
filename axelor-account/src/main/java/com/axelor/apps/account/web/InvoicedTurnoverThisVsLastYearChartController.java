package com.axelor.apps.account.web;

import com.axelor.apps.account.util.helpers.AccountInvoiceChartHelper;
import com.axelor.apps.account.util.helpers.models.AccountInvoiceChartConstants;
import com.axelor.apps.account.util.helpers.models.ChartYearTurnoverDto;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public class InvoicedTurnoverThisVsLastYearChartController {

  /**
   * Function which called on chart, to get data Data sets into response from @request takes param
   * 'invoiceType'. Accepted invoiceTypes are: 'WORKFORCE_COST', 'COST', 'REVENUE', 'GROSS_PROFIT'
   * and 'EBITDA' from @request takes params 'companyCode'. Accepted companyCodes are: 'ALL', 'IT',
   * 'MAIN', 'TRN' and 'COMM'
   *
   * <p>Algorithm: 1) Get year and month for future calculations 2) Get parameters from request
   */
  public void calculate(ActionRequest request, ActionResponse response) {
    try {

      // Get Year and month
      int currentYear = LocalDateTime.now().getYear();

      // Get field values
      String operation = request.getContext().get("operation").toString();
      String companyCode = request.getContext().get("companyCode").toString();

      List<ChartYearTurnoverDto> resultList = new ArrayList<>();
      // InvoiceLineComparator.AccountChartHelper.getDataFromSql();
      // Fill data depending on operation select
      switch (operation) {
        case AccountInvoiceChartConstants.REVENUE:
          resultList = AccountInvoiceChartHelper.getRevenueByYears(companyCode);
          break;
        case AccountInvoiceChartConstants.COST:
          resultList = AccountInvoiceChartHelper.getCostByYears(companyCode);
          break;
        case AccountInvoiceChartConstants.CALCULATE_GROSS_PROFIT:
          resultList = AccountInvoiceChartHelper.getProfitDTOByYear(companyCode);
          break;
        case AccountInvoiceChartConstants.WORKFORCE_COST:
          resultList =
              AccountInvoiceChartHelper.getWorkforceCostByYears(
                  Stream.of(currentYear, currentYear - 1)
                      .collect(Collectors.toCollection(HashSet::new)),
                  false);
          break;
        case AccountInvoiceChartConstants.CALCULATE_EBITDA:
          resultList = AccountInvoiceChartHelper.getEBITDADTOByYear();
          break;
      }

      // Filter data by last 2 years
      resultList =
          resultList.stream()
              .filter(e -> currentYear - 1 == e.year || currentYear == e.year)
              .collect(Collectors.toList());

      // Set result list data to response
      response.setData(resultList);

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
