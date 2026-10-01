/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.bankpayment.service.bankorder.file.address;

import com.axelor.apps.bankpayment.db.BankOrderFileFormat;
import com.axelor.apps.bankpayment.db.repo.BankOrderFileFormatRepository;
import com.axelor.apps.bankpayment.exception.BankPaymentExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import java.util.Set;

public final class BankOrderAddressFormatHelper {

  protected static final Set<String> STRUCTURED_FILE_FORMAT_SET =
      Set.of(
          BankOrderFileFormatRepository.FILE_FORMAT_PAIN_001_001_03_SCT,
          BankOrderFileFormatRepository.FILE_FORMAT_PAIN_008_001_02_SBB,
          BankOrderFileFormatRepository.FILE_FORMAT_PAIN_008_001_02_SDD,
          BankOrderFileFormatRepository.FILE_FORMAT_PAIN_001_001_09_SCT,
          BankOrderFileFormatRepository.FILE_FORMAT_PAIN_008_001_08_SBB,
          BankOrderFileFormatRepository.FILE_FORMAT_PAIN_008_001_08_SDD);

  protected static final Set<String> HYBRID_FILE_FORMAT_SET =
      Set.of(
          BankOrderFileFormatRepository.FILE_FORMAT_PAIN_001_001_09_SCT,
          BankOrderFileFormatRepository.FILE_FORMAT_PAIN_008_001_08_SBB,
          BankOrderFileFormatRepository.FILE_FORMAT_PAIN_008_001_08_SDD);

  private BankOrderAddressFormatHelper() {}

  public static boolean isAddressFormatEnabled(String addressFormat) {

    return !StringUtils.isBlank(addressFormat)
        && !BankOrderFileFormatRepository.ADDRESS_FORMAT_NONE.equals(addressFormat);
  }

  public static boolean isAddressFormatEnabled(BankOrderFileFormat bankOrderFileFormat) {

    return bankOrderFileFormat != null
        && isAddressFormatEnabled(bankOrderFileFormat.getAddressFormatSelect());
  }

  public static void checkAddressFormat(BankOrderFileFormat bankOrderFileFormat)
      throws AxelorException {

    if (!isAddressFormatEnabled(bankOrderFileFormat)) {
      return;
    }

    String addressFormat = bankOrderFileFormat.getAddressFormatSelect();
    String orderFileFormat = bankOrderFileFormat.getOrderFileFormatSelect();

    Set<String> supportedFileFormatSet =
        BankOrderFileFormatRepository.ADDRESS_FORMAT_HYBRID.equals(addressFormat)
            ? HYBRID_FILE_FORMAT_SET
            : STRUCTURED_FILE_FORMAT_SET;

    if (orderFileFormat == null || !supportedFileFormatSet.contains(orderFileFormat)) {
      throw new AxelorException(
          bankOrderFileFormat,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BankPaymentExceptionMessage.BANK_ORDER_FILE_FORMAT_ADDRESS_FORMAT_NOT_SUPPORTED),
          addressFormat,
          orderFileFormat);
    }
  }
}
