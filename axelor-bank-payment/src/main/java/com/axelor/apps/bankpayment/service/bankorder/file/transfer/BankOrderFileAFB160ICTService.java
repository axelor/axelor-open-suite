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
package com.axelor.apps.bankpayment.service.bankorder.file.transfer;

import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.base.AxelorException;
import com.google.inject.Inject;

public class BankOrderFileAFB160ICTService extends BankOrderFileAFB160Service {

  @Inject
  public BankOrderFileAFB160ICTService(BankOrder bankOrder) throws AxelorException {

    super(bankOrder);
  }

  /**
   * B1. Code opération La liste des codes opération possibles est indiquée page 4 au paragraphe
   * "types de virements pouvant être émis par la clientèle". Les codes utilisés doivent faire
   * l'objet d'un accord contractuel avec la banque réceptrice.
   *
   * @return
   */
  @Override
  protected String getB1Area() {
    return OPERATION_TREASURY_TRANSFER;
  }

  @Override
  protected String getSenderEArea() {
    return null;
  }

  @Override
  protected String getC11Area() {
    return null;
  }

  @Override
  protected String getB3Area() {
    return null;
  }
}
