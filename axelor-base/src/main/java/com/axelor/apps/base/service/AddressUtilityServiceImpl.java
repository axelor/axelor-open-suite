package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Country;
import com.axelor.apps.base.db.repo.AddressRepository;
import com.axelor.common.csv.CSVFile;
import com.google.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddressUtilityServiceImpl implements AddressUtilityService {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  protected AddressRepository addressRepository;

  @Inject
  public AddressUtilityServiceImpl(AddressRepository addressRepository) {
    this.addressRepository = addressRepository;
  }

  @Override
  public int export(String path) throws IOException {
    List<Address> addresses = addressRepository.all().filter("self.certifiedOk IS FALSE").fetch();

    File tempFile = new File(path);
    CSVFile csvFormat = CSVFile.DEFAULT.withDelimiter('|').withFirstRecordAsHeader();
    CSVPrinter printer = csvFormat.write(tempFile);

    List<String> header = new ArrayList<>();
    header.add("Id");
    header.add("AddressL1");
    header.add("Room");
    header.add("Floor");
    header.add("Street Name");
    header.add("Postbox");
    header.add("CodeINSEE");

    printer.printRecord(header);
    List<String> items = new ArrayList<>();
    for (Address a : addresses) {

      items.add(a.getId() != null ? a.getId().toString() : "");
      items.add(a.getRoom() != null ? a.getRoom() : "");
      items.add(a.getFloor() != null ? a.getFloor() : "");
      items.add(a.getStreetName() != null ? a.getStreetName() : "");
      items.add(a.getPostBox() != null ? a.getPostBox() : "");
      items.add(a.getInseeCode() != null ? a.getInseeCode() : "");

      printer.printRecord(items);
      items.clear();
    }
    printer.close();
    LOG.info("{} exported", path);

    return addresses.size();
  }

  @Override
  public Address getAddress(
      String room, String floor, String streetName, String postBox, Country country) {

    return addressRepository
        .all()
        .filter(
            "self.room = ?1 AND self.floor = ?2 AND self.streetName = ?3 "
                + "AND self.postBox = ?4 AND self.country = ?6",
            room,
            floor,
            streetName,
            postBox,
            country)
        .fetchOne();
  }
}
