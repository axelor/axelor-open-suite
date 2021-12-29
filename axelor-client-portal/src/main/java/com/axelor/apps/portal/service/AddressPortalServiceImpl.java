/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
package com.axelor.apps.portal.service;

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.repo.AddressRepository;
import com.axelor.apps.base.db.repo.CityRepository;
import com.axelor.apps.base.db.repo.CountryRepository;
import com.axelor.apps.base.service.AddressService;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.inject.Beans;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class AddressPortalServiceImpl implements AddressPortalService {

  @Inject AddressRepository addressRepo;
  @Inject AddressService addressService;

  @Override
  public Address create(Map<String, Object> values) {
    return Mapper.toBean(Address.class, processValues(values));
  }

  @Override
  @Transactional
  public Address update(Address target, Map<String, Object> source) {
    Address address = setValues(target, source);
    addressService.autocompleteAddress(address);
    return addressRepo.save(address);
  }

  private Address setValues(Address target, Map<String, Object> source) {
    Mapper mapper = Mapper.of(Address.class);
    for (Map.Entry<String, Object> entry : processValues(source).entrySet()) {
      mapper.set(target, entry.getKey(), entry.getValue());
    }
    return target;
  }

  private Map<String, Object> processValues(Map<String, Object> values) {
    Mapper mapper = Mapper.of(Address.class);
    Map<String, Object> response = new HashMap<>();
    Map<String, ImmutablePair<String, Function<Object, Object>>> extraFields =
        new ImmutableMap.Builder<String, ImmutablePair<String, Function<Object, Object>>>()
            .put("addressL7CountryId", ImmutablePair.of("addressL7Country", this::getCountry))
            .put("cityId", ImmutablePair.of("city", this::getCity))
            .build();

    for (Entry<String, Object> entry : values.entrySet()) {
      String name = entry.getKey();
      Property property = mapper.getProperty(name);
      Object value = entry.getValue();
      if (property == null) {
        Pair<String, Function<Object, Object>> pair = extraFields.get(name);
        if (pair == null) {
          continue;
        }
        name = pair.getLeft();
        value = pair.getRight().apply(value);
      }
      response.put(name, value);
    }
    return response;
  }

  private Object getCountry(Object id) {
    return Beans.get(CountryRepository.class).find(Long.valueOf(id.toString()));
  }

  private Object getCity(Object id) {
    return Beans.get(CityRepository.class).find(Long.valueOf(id.toString()));
  }
}
