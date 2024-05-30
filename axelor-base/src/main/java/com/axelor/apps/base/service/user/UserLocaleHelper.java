package com.axelor.apps.base.service.user;

import com.axelor.auth.AuthUtils;
import com.axelor.meta.CallMethod;
import com.google.common.base.Strings;
import java.math.BigDecimal;
import java.util.Locale;

public class UserLocaleHelper {

  @CallMethod
  public static String formatNumberWithLocale(BigDecimal decimal) {
    String language = AuthUtils.getUser().getLanguage();
    if (!Strings.isNullOrEmpty(language)) {
      return java.text.NumberFormat.getInstance(Locale.forLanguageTag(language)).format(decimal);
    }
    return java.text.NumberFormat.getInstance(Locale.US).format(decimal);
  }
}
