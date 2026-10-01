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

import com.axelor.apps.bankpayment.xsd.sepa.pain_001_001_09.PostalAddress24;

public final class BankOrderFile00100109AddressAdapter {

  private BankOrderFile00100109AddressAdapter() {}

  public static PostalAddress24 createPostalAddress(StructuredPostalAddress postalAddress) {

    if (postalAddress == null) {
      return null;
    }

    PostalAddress24 sepaPostalAddress = new PostalAddress24();

    sepaPostalAddress.setSubDept(postalAddress.getSubDept());
    sepaPostalAddress.setStrtNm(postalAddress.getStrtNm());
    sepaPostalAddress.setBldgNb(postalAddress.getBldgNb());
    sepaPostalAddress.setBldgNm(postalAddress.getBldgNm());
    sepaPostalAddress.setFlr(postalAddress.getFlr());
    sepaPostalAddress.setPstBx(postalAddress.getPstBx());
    sepaPostalAddress.setRoom(postalAddress.getRoom());
    sepaPostalAddress.setPstCd(postalAddress.getPstCd());
    sepaPostalAddress.setTwnNm(postalAddress.getTwnNm());
    sepaPostalAddress.setTwnLctnNm(postalAddress.getTwnLctnNm());
    sepaPostalAddress.setDstrctNm(postalAddress.getDstrctNm());
    sepaPostalAddress.setCtrySubDvsn(postalAddress.getCtrySubDvsn());
    sepaPostalAddress.setCtry(postalAddress.getCtry());
    sepaPostalAddress.getAdrLine().addAll(postalAddress.getAdrLineList());

    return sepaPostalAddress;
  }
}
