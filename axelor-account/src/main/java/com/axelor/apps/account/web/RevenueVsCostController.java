package com.axelor.apps.account.web;


import com.axelor.apps.account.util.helpers.AccountInvoiceChartHelper;
import com.axelor.apps.account.util.helpers.models.AccountInvoiceChartConstants;
import com.axelor.apps.account.util.helpers.models.ChartMonthTurnoverDto;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public class RevenueVsCostController {

    public void calculate(ActionRequest request, ActionResponse response) {
        try {
            // Get year from request
            int year = Integer.parseInt(request.getContext().get("year").toString());


            List<DTO> resultList = new ArrayList<DTO>() {{

                addAll(DTO.generateDtoListOutOfMonthDtoList(AccountInvoiceChartHelper.getRevenueByMonths(AccountInvoiceChartConstants.CATEGORY_CODE_ALL).stream().filter(e-> e.year == year).collect(Collectors.toList()), "Revenue"));
                addAll(DTO.generateDtoListOutOfMonthDtoList(AccountInvoiceChartHelper.getCostByMonths(AccountInvoiceChartConstants.CATEGORY_CODE_ALL).stream().filter(e-> e.year == year).collect(Collectors.toList()), "Cost"));
                addAll(DTO.generateDtoListOutOfMonthDtoList(AccountInvoiceChartHelper.getWorkforceCostByMonths(Stream.of(year).collect(Collectors.toSet()), false).stream().filter(e-> e.year == year).collect(Collectors.toList()), "Workforce cost"));
                addAll(DTO.generateDtoListOutOfMonthDtoList(AccountInvoiceChartHelper.getEBITDADTOByMonth().stream().filter(e-> e.year == year).collect(Collectors.toList()), "EBITDA"));
                addAll(DTO.generateDtoListOutOfMonthDtoList(AccountInvoiceChartHelper.getProfitDTOByMonth(AccountInvoiceChartConstants.CATEGORY_CODE_ALL).stream().filter(e-> e.year == year).collect(Collectors.toList()), "Gross"));
            }};
            response.setData(resultList);


        } catch (Exception e) {
            TraceBackService.trace(response, e);
        }
    }

    private static class DTO {
        private int _month_no;
        private String _type;
        private BigDecimal _amount;

        public DTO() {
        }

        public DTO(int _month_no, String _type, String amount) {
            this._month_no = _month_no;
            this._type = _type;
            this._amount = new BigDecimal(amount);
        }

        public int get_month_no() {
            return _month_no;
        }

        public void set_month_no(int _month_no) {
            this._month_no = _month_no;
        }

        public String get_type() {
            return _type;
        }

        public void set_type(String _type) {
            this._type = _type;
        }

        public BigDecimal get_amount() {
            return _amount;
        }

        public void set_amount(BigDecimal _amount) {
            this._amount = _amount;
        }



        public static List<DTO> generateDtoListOutOfMonthDtoList(List<ChartMonthTurnoverDto> dto, String typeName) {
            return dto.stream().map(e-> generateDtoOutOfMonthDto(e, typeName)).collect(Collectors.toList());
        }
        public static DTO generateDtoOutOfMonthDto(ChartMonthTurnoverDto dto, String typeName) {
            DTO result = new DTO();
            result.set_amount(dto.turnOver);
            result.set_month_no(dto.monthNo);
            result.set_type(typeName);
            return result;
        }

        @Override
        public String toString() {
            return "DTO{" +
                    "_month_no=" + _month_no +
                    ", _type='" + _type + '\'' +
                    ", _amount=" + _amount +
                    '}';
        }
    }


}
