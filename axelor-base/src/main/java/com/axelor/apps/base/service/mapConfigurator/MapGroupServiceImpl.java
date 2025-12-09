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
package com.axelor.apps.base.service.mapConfigurator;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.MapGroup;
import com.axelor.apps.base.service.address.AddressService;
import com.axelor.db.JPA;
import com.axelor.db.QueryBinder;
import com.axelor.db.mapper.Mapper;
import com.axelor.meta.db.MetaField;
import com.axelor.rpc.Context;
import com.axelor.script.GroovyScriptHelper;
import com.axelor.script.ScriptBindings;
import com.google.common.base.Strings;
import jakarta.inject.Inject;
import jakarta.persistence.Query;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.tuple.Pair;

public class MapGroupServiceImpl implements MapGroupService {

  protected AddressService addressService;

  @Inject
  public MapGroupServiceImpl(AddressService addressService) {
    this.addressService = addressService;
  }

  @Override
  public List<Map<String, Object>> computeData(MapGroup mapGroup)
      throws AxelorException, ClassNotFoundException {
    if (mapGroup.getMetaModel() == null) {
      return Collections.emptyList();
    }
    String metaModel = mapGroup.getMetaModel().getFullName();
    String queryStr = "SELECT self FROM " + metaModel + " self";
    if (!Strings.isNullOrEmpty(mapGroup.getFilter())) {
      queryStr += " WHERE " + mapGroup.getFilter();
    }
    Query query = JPA.em().createQuery(queryStr);

    QueryBinder.of(query)
        .bind(new ScriptBindings(new Context(Class.forName(metaModel))))
        .setCacheable();
    List<?> records = query.getResultList();
    List<Map<String, Object>> data = new ArrayList<>();

    for (Object record : records) {
      Map<String, Object> map = Mapper.toMap(record);
      Long id = (Long) map.get("id");
      Pair<BigDecimal, BigDecimal> latLong = getLatLong(map, mapGroup.getAddressField());
      String cardContent = evalCardContent(map, mapGroup.getCardContent());

      Map<String, Object> entry = new HashMap<>();
      entry.put("recordId", id);
      entry.put("latitude", latLong.getLeft());
      entry.put("longitude", latLong.getRight());
      entry.put("cardContent", cardContent);
      data.add(entry);
    }
    return data;
  }

  protected Pair<BigDecimal, BigDecimal> getLatLong(Map<String, Object> map, MetaField addressField)
      throws AxelorException {
    if (addressField != null) {
      Address address = (Address) map.get(addressField.getName());
      if (address != null) {
        Optional<Pair<BigDecimal, BigDecimal>> latLong = addressService.getOrUpdateLatLong(address);
        if (latLong.isPresent()) {
          return Pair.of(latLong.get().getLeft(), latLong.get().getRight());
        }
      }
    }
    return Pair.of(BigDecimal.ZERO, BigDecimal.ZERO);
  }

  protected String evalCardContent(Map<String, Object> recordMap, String cardContent) {
    if (Strings.isNullOrEmpty(cardContent)) {
      return "";
    }
    ScriptBindings bindings = new ScriptBindings(recordMap);
    Object eval = new GroovyScriptHelper(bindings).eval(cardContent);
    return eval != null ? eval.toString() : "";
  }
}
