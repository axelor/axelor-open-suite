/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.purchase.service;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.purchase.db.CallTenderOffer;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.utils.helpers.StringHelper;
import com.axelor.utils.helpers.file.CsvHelper;
import com.google.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CallTenderCsvServiceImpl implements CallTenderCsvService {

  protected final AppBaseService appBaseService;

  @Inject
  public CallTenderCsvServiceImpl(AppBaseService appBaseService) {
    this.appBaseService = appBaseService;
  }

  @Override
  public MetaFile generateCsvFile(List<CallTenderOffer> offerList) throws IOException {

    Partner supplier = null;
    String callTenderName = null;
    if (!offerList.isEmpty()) {
      supplier = offerList.get(0).getSupplierPartner();
      callTenderName = offerList.get(0).getCallTender().getName();
    }
    List<String[]> list = getValues(offerList);

    var fileName =
        StringHelper.cutTooLongString(
            String.format(
                "CFT%s-%s-%s",
                callTenderName,
                Optional.ofNullable(supplier).map(Partner::getSimpleFullName).orElse(""),
                DateTimeFormatter.ofPattern("ddMMyyyyhhmm")
                    .format(appBaseService.getTodayDateTime())));
    File file = MetaFiles.createTempFile(fileName, ".csv").toFile();
    fileName += ".csv";

    CsvHelper.csvWriter(file.getParent(), file.getName(), ';', getOfferCsvHeaders(), list);

    try (var inStream = new FileInputStream(file)) {
      return Beans.get(MetaFiles.class).upload(inStream, fileName);
    }
  }

  protected List<String[]> getValues(List<CallTenderOffer> offerList) {
    List<String[]> list = new ArrayList<>();

    for (CallTenderOffer offer : offerList) {
      String[] lineValue =
          new String[] {
            offer.getProduct().getCode(),
            offer.getProduct().getName(),
            offer.getRequestedQty().toString(),
            Optional.ofNullable(offer.getRequestedDate()).map(LocalDate::toString).orElse(""),
            "",
            "",
            ""
          };

      list.add(lineValue);
    }

    return list;
  }

  protected String[] getOfferCsvHeaders() {

    String[] headers = new String[7];
    headers[0] = I18n.get("Product code");
    headers[1] = I18n.get("Product name");
    headers[2] = I18n.get("Requested quantity");
    headers[3] = I18n.get("Requested date");
    headers[4] = I18n.get("Offered quantity");
    headers[5] = I18n.get("Offered date");
    headers[6] = I18n.get("Offered price");
    return headers;
  }
}
