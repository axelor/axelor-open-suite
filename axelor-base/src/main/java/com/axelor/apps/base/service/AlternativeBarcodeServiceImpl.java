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
package com.axelor.apps.base.service;

import com.axelor.apps.base.db.AlternativeBarcode;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;

public class AlternativeBarcodeServiceImpl implements AlternativeBarcodeService {

  protected BarcodeGeneratorService barcodeGeneratorService;

  @Inject
  public AlternativeBarcodeServiceImpl(BarcodeGeneratorService barcodeGeneratorService) {
    this.barcodeGeneratorService = barcodeGeneratorService;
  }

  @Override
  public void generateBarcode(AlternativeBarcode alternativeBarcode) {

    boolean addPadding = false;
    Long id = alternativeBarcode.getId();
    MetaFile barcodeFile =
        barcodeGeneratorService.createBarCode(
            id != null ? id : 0l,
            "ProductAlternativeBarCode%d.png",
            alternativeBarcode.getSerialNumber(),
            alternativeBarcode.getBarcodeTypeConfig(),
            addPadding);

    if (barcodeFile != null) {
      alternativeBarcode.setBarCode(barcodeFile);
    }
  }
}
