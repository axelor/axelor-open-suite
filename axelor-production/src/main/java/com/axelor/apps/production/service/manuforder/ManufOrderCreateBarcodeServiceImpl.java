/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.BarcodeGeneratorService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import javax.validation.ValidationException;

public class ManufOrderCreateBarcodeServiceImpl implements ManufOrderCreateBarcodeService {

  protected BarcodeGeneratorService barcodeGeneratorService;
  protected AppProductionService appProductionService;
  protected MetaFiles metaFiles;

  @Inject
  public ManufOrderCreateBarcodeServiceImpl(
      BarcodeGeneratorService barcodeGeneratorService,
      AppProductionService appProductionService,
      MetaFiles metaFiles) {
    this.barcodeGeneratorService = barcodeGeneratorService;
    this.appProductionService = appProductionService;
    this.metaFiles = metaFiles;
  }

  @Override
  public void createBarcode(ManufOrder manufOrder) {
    String manufOrderSeq = manufOrder.getManufOrderSeq();

    try (InputStream inStream =
        barcodeGeneratorService.createBarCode(
            manufOrderSeq, appProductionService.getAppProduction().getBarcodeTypeConfig(), true)) {

      if (inStream != null) {
        MetaFile barcodeFile =
            metaFiles.upload(
                inStream, String.format("ManufOrderBarcode%d.png", manufOrder.getId()));
        manufOrder.setBarCode(barcodeFile);
      }
    } catch (IOException e) {
      TraceBackService.trace(e);
    } catch (AxelorException e) {
      throw new ValidationException(e.getMessage());
    }
  }
}
