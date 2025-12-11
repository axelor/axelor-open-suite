package com.axelor.apps.businessproject.service.extracharges;

import com.axelor.apps.hr.db.ExtrachargeType;
import com.axelor.i18n.I18n;
import java.util.List;

public final class ExtraChargeConstants {

  private ExtraChargeConstants() {
    // Utility Class
  }

  // Product codes
  public static final String SATURDAY_PRODUCT_CODE = "SATURDAY";
  public static final String SUNDAY_PRODUCT_CODE = "SUNDAY";
  public static final String HOLIDAY_PRODUCT_CODE = "HOLIDAY";
  public static final String EMERGENCY_PRODUCT_CODE = "EMERGENCY";
  public static final String NIGHTSHIFT_PRODUCT_CODE = "NIGHTSHIFT";

  public static final String TIMESHEET_INVOICE_LINE_SOURCE_TYPE = "TIMESHEET";
  public static final String EXPENSE_INVOICE_LINE_SOURCE_TYPE = "EXPENSE";
  public static final String EXTRACHARGE_INVOICE_LINE_SOURCE_TYPE = "EXTRACHARGE";

  public static final List<String> INVOICE_LINE_SOURCE_TYPES =
      List.of(
          TIMESHEET_INVOICE_LINE_SOURCE_TYPE,
          EXPENSE_INVOICE_LINE_SOURCE_TYPE,
          EXTRACHARGE_INVOICE_LINE_SOURCE_TYPE);
  public static final List<String> EXTRA_CHARGE_CODES_EXCEPT_NIGHT =
      List.of(
          SATURDAY_PRODUCT_CODE, SUNDAY_PRODUCT_CODE, HOLIDAY_PRODUCT_CODE, EMERGENCY_PRODUCT_CODE);

  /** Get extra charge product code */
  public static String getExtraChargeProductCode(ExtrachargeType type) {
    switch (type) {
      case SATURDAY:
        return SATURDAY_PRODUCT_CODE;
      case SUNDAY:
        return SUNDAY_PRODUCT_CODE;
      case HOLIDAY:
        return HOLIDAY_PRODUCT_CODE;
      case NIGHT:
        return NIGHTSHIFT_PRODUCT_CODE;
      default:
        throw new IllegalArgumentException("Unknow Extra Charge: " + type);
    }
  }

  /** Get display name for extra charge type */
  public static String getDisplayName(ExtrachargeType type) {
    switch (type) {
      case SATURDAY:
        return I18n.get("Saturday Extra Charge");
      case SUNDAY:
        return I18n.get("Sunday Extra Charge");
      case HOLIDAY:
        return I18n.get("Holiday Extra Charge");
      case NIGHT:
        return I18n.get("Night Shift Extra Charge");
      default:
        return I18n.get(type.toString());
    }
  }
}
