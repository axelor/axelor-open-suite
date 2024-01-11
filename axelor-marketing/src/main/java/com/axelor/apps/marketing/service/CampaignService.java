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
package com.axelor.apps.marketing.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.marketing.db.Campaign;
import com.axelor.meta.db.MetaFile;

public interface CampaignService {

  public MetaFile sendEmail(Campaign campaign);

  public MetaFile sendReminderEmail(Campaign campaign);

  public void generateEvents(Campaign campaign);

  public void generateTargets(Campaign campaign) throws AxelorException;

  public void inviteAllTargets(Campaign campaign);

  public void inviteSelectedTargets(Campaign campaign, Campaign campaignContext);

  public void addParticipatingTargets(Campaign campaign, Campaign campaignContext);

  public void addNotParticipatingTargets(Campaign campaign, Campaign campaignContext);

  public void markLeadPresent(Campaign campaign, Lead lead);

  public void markPartnerPresent(Campaign campaign, Partner partner);
}
