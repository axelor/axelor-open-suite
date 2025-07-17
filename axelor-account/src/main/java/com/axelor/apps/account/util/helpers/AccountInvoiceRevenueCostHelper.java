package com.axelor.apps.account.util.helpers;

import com.axelor.apps.account.util.helpers.models.ChartMonthTurnoverDto;
import com.axelor.apps.account.util.helpers.models.ChartYearTurnoverDto;
import com.axelor.db.JPA;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Query;

public class AccountInvoiceRevenueCostHelper {
  private static final String SQL_GET_COST_DATA =
      "SELECT SUM(CASE _invoice.operation_type_select\n"
          + "                                       WHEN 1 THEN _invoice.ex_tax_total -- Supplier purchase\n"
          + "                                       WHEN 2 THEN -_invoice.ex_tax_total -- Supplier refund\n"
          + "                            END)                                AS _turn_over,       to_char(_invoice.invoice_date, 'yyyy') AS _year_no,\n"
          + "       to_char(_invoice.invoice_date, 'MM')   AS _month_no\n"
          + "                        FROM account_invoice AS _invoice\n"
          + "                                 left outer join base_company company on _invoice.company = company.id\n"
          + "                        WHERE _invoice.status_select = 3\n"
          + "                          -- Where operation is Supplier purchase or Supplier refund\n"
          + "                          AND (_invoice.operation_type_select = 1 OR _invoice.operation_type_select = 2)\n"
          + "                          -- If categoryCode is 'ALL' get all records, if not then filter by given code\n"
          + "                          AND CASE\n"
          + "                                  WHEN ?1 != 'ALL' THEN company.code = ?1\n"
          + "                                  ELSE TRUE\n"
          + "                            END\n"
          + "GROUP BY _month_no, _year_no\n"
          + "ORDER BY _month_no";

  private static final String SQL_GET_REVENUE_DATA =
      "SELECT SUM(CASE _invoice.operation_type_select\n"
          + "               WHEN 3 THEN _line.ex_tax_total -- Client sale\n"
          + "               WHEN 4 THEN -_line.ex_tax_total -- Client refund\n"
          + "    END)                                        AS _turn_over,\n"
          + "       to_char(_invoice.invoice_date, 'yyyy') AS _year_no,\n"
          + "       to_char(_invoice.invoice_date, 'MM')   AS _month_no\n"
          + "FROM account_invoice AS _invoice\n"
          + "         left outer join account_invoice_line AS _line ON _invoice.id = _line.invoice\n"
          + "         left outer join base_product AS _bp ON _line.product = _bp.id\n"
          + "         left outer join base_product_category AS _bpc ON _bp.product_category = _bpc.id\n"
          + "\n"
          + "WHERE _invoice.status_select = 3\n"
          + "  -- Where operation is Client purchase or Client refund\n"
          + "  AND (_invoice.operation_type_select = 3 OR _invoice.operation_type_select = 4)\n"
          + "  -- If categoryCode is 'ALL' get all records, if not then filter by given code\n"
          + "  AND CASE\n"
          + "          WHEN ?1 != 'ALL' THEN _bpc.code = ?1\n"
          + "          ELSE TRUE\n"
          + "    END\n"
          + "GROUP BY _month_no, _year_no\n"
          + "ORDER BY _month_no";

  public static List<ChartYearTurnoverDto> getRevenueByYears(String companyCode) {
    return ChartYearTurnoverDto.mapMonthDtoToListToYearDto(getRevenueByMonths(companyCode));
  }

  public static List<ChartYearTurnoverDto> getCostByYears(String companyCode) {

    return ChartYearTurnoverDto.mapMonthDtoToListToYearDto(getCostByMonths(companyCode));
  }

  public static List<ChartMonthTurnoverDto> getRevenueByMonths(String companyCode) {
    List<ChartMonthTurnoverDto> resultList = getData(SQL_GET_REVENUE_DATA, companyCode);
    return resultList;
  }

  public static List<ChartMonthTurnoverDto> getCostByMonths(String companyCode) {
    List<ChartMonthTurnoverDto> resultList = getData(SQL_GET_COST_DATA, companyCode);
    return resultList;
  }

  /**
   * Get data from database
   *
   * @param sqlQuery SQL query
   * @param companyCode Company code
   * @return List of DTO
   */
  private static List<ChartMonthTurnoverDto> getData(String sqlQuery, String companyCode) {

    List<ChartMonthTurnoverDto> dtoList = new ArrayList<>();

    Query nativeQuery = JPA.em().createNativeQuery(sqlQuery);
    // Set params based on companyCode (Company)
    nativeQuery.setParameter(1, companyCode);

    // Execute query and get data
    List<Object[]> resultList = nativeQuery.getResultList();

    // Fill yearDataMap with data
    // For each
    for (Object[] record : resultList) {
      // Create DTO object out of record data (
      ChartMonthTurnoverDto dto =
          ChartMonthTurnoverDto.generateDTO(
              record[0].toString(), record[1].toString(), record[2].toString());
      dtoList.add(dto);
    }

    return dtoList;
  }
}
