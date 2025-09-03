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

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.BaseBatch;
import com.axelor.apps.base.db.Country;
import com.axelor.apps.base.db.repo.AddressRepository;
import com.axelor.apps.base.db.repo.CountryRepository;
import com.axelor.apps.base.db.repo.ExceptionOriginRepository;
import com.axelor.apps.base.service.address.AddressService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchCountryAddressRecompute extends BatchStrategy {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  protected CountryRepository countryRepository;
  protected AddressRepository addressRepository;
  protected AddressService addressService;

  @Inject
  public BatchCountryAddressRecompute(
      CountryRepository countryRepository,
      AddressRepository addressRepository,
      AddressService addressService) {
    this.countryRepository = countryRepository;
    this.addressRepository = addressRepository;
    this.addressService = addressService;
  }

  @Override
  protected void process() throws SQLException {
    HashMap<String, Object> queryParameters = new HashMap<>();
    BaseBatch baseBatch = batch.getBaseBatch();
    String filter = "self IS NOT NULL";

    if (!baseBatch.getAllCountries()) {
      filter = "self IN (:countrySet)";
      queryParameters.put(
          "countrySet",
          CollectionUtils.isNotEmpty(baseBatch.getCountrySet()) ? baseBatch.getCountrySet() : 0L);
    }

    int offset = 0;
    List<Address> addressList;
    Query<Country> countryQuery =
        countryRepository.all().filter(filter).bind(queryParameters).order("id");
    List<Long> countryIdList =
        countryQuery.select("id").fetch(0, 0).stream()
            .map(m -> (Long) m.get("id"))
            .collect(Collectors.toList());

    while (!(addressList =
            addressRepository
                .all()
                .filter("self.country.id IN :countryIds")
                .bind("countryIds", countryIdList)
                .order("id")
                .fetch(getFetchLimit(), offset))
        .isEmpty()) {
      for (Address address : addressList) {
        ++offset;
        try {
          address = addressRepository.find(address.getId());
          recomputeAddress(address);
          incrementDone();
        } catch (Exception e) {
          TraceBackService.trace(e, ExceptionOriginRepository.ADDRESS_RECOMPUTE, batch.getId());
          incrementAnomaly();
        }
      }
      JPA.clear();
      findBatch();
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void recomputeAddress(Address address) {
    addressRepository.save(address);
  }

  @Override
  protected void stop() {

    String comment =
        String.format(
            I18n.get("%s address processed", "%s addresses processed", batch.getDone()),
            batch.getDone());

    super.stop();
    addComment(comment);
  }
}
