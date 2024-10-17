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
package com.axelor.apps.base.service.address;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PartnerAddress;
import com.axelor.apps.base.db.PickListEntry;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.MapService;
import com.axelor.common.StringUtils;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.axelor.utils.helpers.address.AddressHelper;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wslite.json.JSONException;

@Singleton
public class AddressServiceImpl implements AddressService {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  protected AddressHelper ads;
  protected MapService mapService;
  protected static final Set<Function<Long, Boolean>> checkUsedFuncs = new LinkedHashSet<>();

  static {
    registerCheckUsedFunc(AddressServiceImpl::checkAddressUsedBase);
  }

  @Inject
  public AddressServiceImpl(AddressHelper ads, MapService mapService) {
    this.ads = ads;
    this.mapService = mapService;
  }

  @Override
  public boolean check(String wsdlUrl) {
    return ads.doCanSearch(wsdlUrl);
  }

  @Override
  public Map<String, Object> validate(String wsdlUrl, String search) {
    return ads.doSearch(wsdlUrl, search);
  }

  @Override
  public com.qas.web_2005_02.Address select(String wsdlUrl, String moniker) {
    return ads.doGetAddress(wsdlUrl, moniker);
  }

  @Override
  public boolean checkAddressUsed(Long addressId) {
    LOG.debug("Address Id to be checked = {}", addressId);
    return checkUsedFuncs.stream().anyMatch(checkUsedFunc -> checkUsedFunc.apply(addressId));
  }

  protected static void registerCheckUsedFunc(Function<Long, Boolean> checkUsedFunc) {
    checkUsedFuncs.add(checkUsedFunc);
  }

  private static boolean checkAddressUsedBase(Long addressId) {
    return JPA.all(PartnerAddress.class).filter("self.address.id = ?1", addressId).fetchOne()
            != null
        || JPA.all(Partner.class).filter("self.mainAddress.id = ?1", addressId).fetchOne() != null
        || JPA.all(PickListEntry.class).filter("self.address.id = ?1", addressId).fetchOne()
            != null;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Optional<Pair<BigDecimal, BigDecimal>> getOrUpdateLatLong(Address address)
      throws AxelorException, JSONException {
    Preconditions.checkNotNull(address, I18n.get(BaseExceptionMessage.ADDRESS_CANNOT_BE_NULL));
    Optional<Pair<BigDecimal, BigDecimal>> latLong = getLatLong(address);

    if (latLong.isPresent()) {
      return latLong;
    }

    return updateLatLong(address);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Optional<Pair<BigDecimal, BigDecimal>> updateLatLong(Address address)
      throws AxelorException, JSONException {
    Preconditions.checkNotNull(address, I18n.get(BaseExceptionMessage.ADDRESS_CANNOT_BE_NULL));

    if (mapService.isConfigured() && StringUtils.notBlank(address.getFullName())) {
      Map<String, Object> result = mapService.getMap(address.getFullName());
      if (result == null) {
        address.setIsValidLatLong(false);
        return Optional.empty();
      }
      address.setIsValidLatLong(true);
      BigDecimal latitude = (BigDecimal) result.get("latitude");
      BigDecimal longitude = (BigDecimal) result.get("longitude");
      setLatLong(address, Pair.of(latitude, longitude));
    }

    return getLatLong(address);
  }

  @Override
  @Transactional
  public void resetLatLong(Address address) {
    Preconditions.checkNotNull(address, I18n.get(BaseExceptionMessage.ADDRESS_CANNOT_BE_NULL));
    setLatLong(address, Pair.of(null, null));
  }

  protected void setLatLong(Address address, Pair<BigDecimal, BigDecimal> latLong) {
    address.setLatit(latLong.getLeft());
    address.setLongit(latLong.getRight());
  }

  protected Optional<Pair<BigDecimal, BigDecimal>> getLatLong(Address address) {
    if (address.getLatit() != null && address.getLongit() != null) {
      return Optional.of(Pair.of(address.getLatit(), address.getLongit()));
    }

    return Optional.empty();
  }

  @Override
  public String computeFullName(Address address) {
    String l2 = address.getAddressL2();
    String l3 = address.getAddressL3();
    String l4 = address.getAddressL4();
    String l5 = address.getAddressL5();
    String l6 = address.getAddressL6();

    return (!Strings.isNullOrEmpty(l2) ? l2 : "")
        + (!Strings.isNullOrEmpty(l3) ? " " + l3 : "")
        + (!Strings.isNullOrEmpty(l4) ? " " + l4 : "")
        + (!Strings.isNullOrEmpty(l5) ? " " + l5 : "")
        + (!Strings.isNullOrEmpty(l6) ? " " + l6 : "");
  }

  @Override
  public String computeAddressStr(Address address) {
    if (address == null) {
      return null;
    }
    return address.getFormattedFullName();
  }
}
