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

import java.util.ArrayList;
import java.util.List;

public class StructuredPostalAddress {

  protected String subDept;
  protected String strtNm;
  protected String bldgNb;
  protected String bldgNm;
  protected String flr;
  protected String pstBx;
  protected String room;
  protected String pstCd;
  protected String twnNm;
  protected String twnLctnNm;
  protected String dstrctNm;
  protected String ctrySubDvsn;
  protected String ctry;
  protected List<String> adrLineList = new ArrayList<>();

  public String getSubDept() {
    return subDept;
  }

  public void setSubDept(String subDept) {
    this.subDept = subDept;
  }

  public String getStrtNm() {
    return strtNm;
  }

  public void setStrtNm(String strtNm) {
    this.strtNm = strtNm;
  }

  public String getBldgNb() {
    return bldgNb;
  }

  public void setBldgNb(String bldgNb) {
    this.bldgNb = bldgNb;
  }

  public String getBldgNm() {
    return bldgNm;
  }

  public void setBldgNm(String bldgNm) {
    this.bldgNm = bldgNm;
  }

  public String getFlr() {
    return flr;
  }

  public void setFlr(String flr) {
    this.flr = flr;
  }

  public String getPstBx() {
    return pstBx;
  }

  public void setPstBx(String pstBx) {
    this.pstBx = pstBx;
  }

  public String getRoom() {
    return room;
  }

  public void setRoom(String room) {
    this.room = room;
  }

  public String getPstCd() {
    return pstCd;
  }

  public void setPstCd(String pstCd) {
    this.pstCd = pstCd;
  }

  public String getTwnNm() {
    return twnNm;
  }

  public void setTwnNm(String twnNm) {
    this.twnNm = twnNm;
  }

  public String getTwnLctnNm() {
    return twnLctnNm;
  }

  public void setTwnLctnNm(String twnLctnNm) {
    this.twnLctnNm = twnLctnNm;
  }

  public String getDstrctNm() {
    return dstrctNm;
  }

  public void setDstrctNm(String dstrctNm) {
    this.dstrctNm = dstrctNm;
  }

  public String getCtrySubDvsn() {
    return ctrySubDvsn;
  }

  public void setCtrySubDvsn(String ctrySubDvsn) {
    this.ctrySubDvsn = ctrySubDvsn;
  }

  public String getCtry() {
    return ctry;
  }

  public void setCtry(String ctry) {
    this.ctry = ctry;
  }

  public List<String> getAdrLineList() {
    return adrLineList;
  }

  public void setAdrLineList(List<String> adrLineList) {
    this.adrLineList = adrLineList;
  }
}
