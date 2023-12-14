package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Country;
import com.axelor.apps.base.db.repo.AddressRepository;
import com.axelor.common.csv.CSVFile;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;
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
    header.add("AddressL2");
    header.add("AddressL3");
    header.add("AddressL4");
    header.add("AddressL5");
    header.add("AddressL6");
    header.add("CodeINSEE");

    printer.printRecord(header);
    List<String> items = new ArrayList<>();
    for (Address a : addresses) {

      items.add(a.getId() != null ? a.getId().toString() : "");
      items.add(a.getAddressL2() != null ? a.getAddressL2() : "");
      items.add(a.getAddressL3() != null ? a.getAddressL3() : "");
      items.add(a.getAddressL4() != null ? a.getAddressL4() : "");
      items.add(a.getAddressL5() != null ? a.getAddressL5() : "");
      items.add(a.getAddressL6() != null ? a.getAddressL6() : "");
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
      String addressL2,
      String addressL3,
      String addressL4,
      String addressL5,
      String addressL6,
      Country addressL7Country) {

    return addressRepository
        .all()
        .filter(
            "self.addressL2 = ?1 AND self.addressL3 = ?2 AND self.addressL4 = ?3 "
                + "AND self.addressL5 = ?4 AND self.addressL6 = ?5 AND self.addressL7Country = ?6",
            addressL2,
            addressL3,
            addressL4,
            addressL5,
            addressL6,
            addressL7Country)
        .fetchOne();
  }
}
