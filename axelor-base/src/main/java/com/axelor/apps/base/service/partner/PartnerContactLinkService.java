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
package com.axelor.apps.base.service.partner;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PartnerContactLink;
import java.util.Map;

public interface PartnerContactLinkService {

  /**
   * Keeps a contact's company links, main company and company memberships consistent before the
   * contact is saved.
   *
   * @throws AxelorException if the same company appears on two links
   */
  void onContactSave(Partner contact) throws AxelorException;

  /**
   * Aligns a company's links with its contact set after the company is saved: a link is created for
   * each new contact, reactivated for a contact selected again, and deleted for a removed contact.
   */
  void afterPartnerSave(Partner partner) throws AxelorException;

  /** Deletes every link of the partner, whether it is the company or the contact of the link. */
  void onPartnerRemove(Partner partner) throws AxelorException;

  /**
   * Returns the contact form values to refresh after a change of the company lines: the reconciled
   * lines, the main company and the contact details mirrored from the main line.
   */
  Map<String, Object> getMainPartnerLinkValuesMap(Partner contact);

  /**
   * Reconciles the main company field with the company links.
   *
   * <p>A star the user just toggled wins. Otherwise the main company field designates the main
   * link, created when missing so that contacts written outside the companies panel keep their
   * company. Otherwise the starred link is the main one. An inactive link is never main. Exactly
   * one link ends up flagged main and the contact mirrors its details; without main link the main
   * company is cleared.
   */
  void updateMainPartnerLink(Partner contact);

  /**
   * Copies the contact's own details onto its main company link, created when missing, then
   * reconciles as {@link #updateMainPartnerLink(Partner)}.
   *
   * <p>For flows where the contact is the source of truth, such as data imports.
   */
  void updateMainPartnerLinkFromContact(Partner contact);

  /** Creates an unsaved active link flagged as main company for the given company. */
  PartnerContactLink createMainPartnerLink(Partner company);
}
