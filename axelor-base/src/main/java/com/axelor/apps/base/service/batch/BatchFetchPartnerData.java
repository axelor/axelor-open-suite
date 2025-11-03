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
package com.axelor.apps.base.service.batch;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.base.service.partner.api.PartnerGenerateService;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;

public class BatchFetchPartnerData extends BatchStrategy {

  private static final int FETCH_SIZE = 10;

  protected final PartnerRepository partnerRepository;
  protected final PartnerGenerateService partnerGenerateService;

  @Inject
  public BatchFetchPartnerData(
      PartnerRepository partnerRepository, PartnerGenerateService partnerGenerateService) {
    this.partnerRepository = partnerRepository;
    this.partnerGenerateService = partnerGenerateService;
  }

  @Override
  protected void process() {
    int count = 0;
    int offset = 0;

    String query =
        "SELECT xPartner.id"
            + " FROM Partner xPartner"
            + " WHERE xPartner.registrationCode IS NOT NULL"
            + " ORDER BY xPartner.id";

    List<Long> partnerIds;

    while (ObjectUtils.notEmpty(
        partnerIds =
            JPA.em().createQuery(query, Long.class).setFirstResult(offset).getResultList())) {

      for (Long partnerId : partnerIds) {
        try {
          processOneElement(partnerId);
          incrementDone();
        } catch (Exception e) {
          TraceBackService.trace(e, "", batch.getId());
          incrementAnomaly();
        } finally {
          offset++;
          if (++count % FETCH_SIZE == 0) {
            JPA.clear();
          }
        }
      }
    }
  }

  @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
  public void processOneElement(Long partnerId) throws AxelorException {
    Partner partner = partnerRepository.find(partnerId);
    partnerGenerateService.configurePartner(partner, partner.getRegistrationCode());
  }
}
