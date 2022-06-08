/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.crm.service;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.crm.db.LostReason;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;
import java.util.List;
import java.util.Map;

public interface LeadService {

  /**
   * Convert lead into a partner
   *
   * @param lead
   * @return
   * @throws AxelorException
   */
  @Transactional(rollbackOn = {Exception.class})
  public Lead convertLead(Lead lead, Partner partner, Partner contactPartner)
      throws AxelorException;

  /**
   * Get sequence for partner
   *
   * @return
   * @throws AxelorException
   */
  public String getSequence() throws AxelorException;

  /**
   * Assign user company to partner
   *
   * @param partner
   * @return
   */
  public Partner setPartnerCompany(Partner partner);

  public Map<String, String> getSocialNetworkUrl(String name, String firstName, String companyName);

  @SuppressWarnings("rawtypes")
  public Object importLead(Object bean, Map values);

  /**
   * Check if the lead in view has a duplicate.
   *
   * @param lead a context lead object
   * @return if there is a duplicate lead
   */
  public boolean isThereDuplicateLead(Lead lead);

  /**
   * Set the lead status to In Process.
   *
   * @param lead
   * @throws AxelorException if the lead wasn't new nor assigned.
   */
  void startLead(Lead lead) throws AxelorException;

  /**
   * Set the lead to the current user and change status to Assigned.
   *
   * @param lead
   * @throws AxelorException if the lead wasn't new nor assigned.
   */
  void assignToMeLead(Lead lead) throws AxelorException;

  /**
   * Set multiple leads to the current user and change status to Assigned.
   *
   * @param leadList
   * @throws AxelorException if the lead wasn't new nor assigned.
   */
  void assignToMeMultipleLead(List<Lead> leadList) throws AxelorException;

  /**
   * Recycle the lead if it was lost, change the status to In Process.
   *
   * @param lead
   * @throws AxelorException if the lead wasn't lost.
   */
  void recycleLead(Lead lead) throws AxelorException;

  /**
   * Set the lead status to lost and set the lost reason with the given lost reason.
   *
   * @param lead a context lead object
   * @param lostReason the specified lost reason
   */
  public void loseLead(Lead lead, LostReason lostReason) throws AxelorException;

  public String processFullName(String enterpriseName, String name, String firstName);
}
