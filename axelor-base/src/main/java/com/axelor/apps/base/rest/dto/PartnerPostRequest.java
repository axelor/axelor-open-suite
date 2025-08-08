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
package com.axelor.apps.base.rest.dto;

import com.axelor.apps.base.db.Partner;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestPostStructure;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class PartnerPostRequest extends RequestPostStructure {

  private Integer partnerTypeSelect;

  private Integer titleSelect;

  private String firstName;

  @NotBlank private String name;

  @Min(0)
  private Long mainPartnerId;

  private String description;

  private boolean isContact = false;

  private boolean isCustomer = false;

  private boolean isSupplier = false;

  private boolean isProspect = false;

  public Integer getPartnerTypeSelect() {
    return partnerTypeSelect;
  }

  public void setPartnerTypeSelect(Integer partnerTypeSelect) {
    this.partnerTypeSelect = partnerTypeSelect;
  }

  public Integer getTitleSelect() {
    return titleSelect;
  }

  public void setTitleSelect(Integer titleSelect) {
    this.titleSelect = titleSelect;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Long getMainPartnerId() {
    return mainPartnerId;
  }

  public void setMainPartnerId(Long mainPartnerId) {
    this.mainPartnerId = mainPartnerId;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean getIsContact() {
    return isContact;
  }

  public void setIsContact(boolean isContact) {
    this.isContact = isContact;
  }

  public boolean getIsCustomer() {
    return isCustomer;
  }

  public void setIsCustomer(boolean isCustomer) {
    this.isCustomer = isCustomer;
  }

  public boolean getIsSupplier() {
    return isSupplier;
  }

  public void setIsSupplier(boolean isSupplier) {
    this.isSupplier = isSupplier;
  }

  public boolean getIsProspect() {
    return isProspect;
  }

  public void setIsProspect(boolean isProspect) {
    this.isProspect = isProspect;
  }

  public Partner fetchMainPartner() {
    if (mainPartnerId == null || mainPartnerId == 0L) {
      return null;
    }
    return ObjectFinder.find(Partner.class, mainPartnerId, ObjectFinder.NO_VERSION);
  }
}
