package com.axelor.apps.base.service.address;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.City;
import com.axelor.apps.base.db.Country;
import com.axelor.apps.base.db.Street;
import com.axelor.apps.base.db.repo.AddressRepository;
import com.axelor.apps.base.db.repo.CityRepository;
import com.axelor.apps.base.db.repo.StreetRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;
import java.util.Optional;

public class AddressCreationServiceImpl implements AddressCreationService {

  protected final AddressRepository addressRepository;
  protected final CityRepository cityRepository;
  protected final StreetRepository streetRepository;
  protected final AppBaseService appBaseService;

  @Inject
  public AddressCreationServiceImpl(
      CityRepository cityRepository,
      StreetRepository streetRepository,
      AppBaseService appBaseService,
      AddressRepository addressRepository) {
    this.cityRepository = cityRepository;
    this.streetRepository = streetRepository;
    this.appBaseService = appBaseService;
    this.addressRepository = addressRepository;
  }

  @Override
  public Address createAddress(
      String room,
      String floor,
      String streetName,
      String postBox,
      String zip,
      City city,
      Country country) {

    Address address = new Address();
    address.setRoom(room);
    address.setFloor(floor);
    address.setStreetName(streetName);
    address.setPostBox(postBox);
    address.setCity(city);
    address.setZip(zip);
    address.setCountry(country);
    autocompleteAddress(address);

    return address;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Address createAndSaveAddress(Country country, City city, String zip, String streetName)
      throws AxelorException {
    if (city == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY, I18n.get(BaseExceptionMessage.NO_CITY_FOUND));
    }
    if (zip == null && city.getZip() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY, I18n.get(BaseExceptionMessage.NO_ZIP_FOUND));
    }

    return addressRepository.save(
        createAddress(
            null,
            null,
            streetName,
            null,
            Optional.ofNullable(zip).orElse(city.getZip()),
            city,
            country));
  }

  @Override
  public void autocompleteAddress(Address address) {
    String zip = address.getZip();
    if (zip == null) {
      return;
    }
    Country country = address.getCountry();

    City city = address.getCity();
    if (city == null) {
      List<City> cities = cityRepository.findByZipAndCountry(zip, country).fetch();
      city = cities.size() == 1 ? cities.get(0) : null;
      address.setCity(city);
    }
    address.setAddressL6(city != null ? zip + " " + city.getName() : null);

    if (appBaseService.getAppBase().getStoreStreets()) {
      List<Street> streets =
          streetRepository.all().filter("self.city = :city").bind("city", city).fetch();
      if (streets.size() == 1) {
        Street street = streets.get(0);
        address.setStreet(street);
        String name = street.getName();
        String num = address.getBuildingNumber();
        address.setAddressL4(num != null ? num + " " + name : name);
      } else {
        address.setStreet(null);
        address.setAddressL4(null);
      }
    }
  }
}
