package com.axelor.apps.base.service.batch;

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.BaseBatch;
import com.axelor.apps.base.db.Country;
import com.axelor.apps.base.db.repo.AddressRepository;
import com.axelor.apps.base.db.repo.BatchRepository;
import com.axelor.apps.base.db.repo.CountryRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.AddressService;
import com.axelor.apps.base.service.administration.AbstractBatch;
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
                .filter("self.addressL7Country.id IN :countryIds")
                .bind("countryIds", countryIdList)
                .order("id")
                .fetch(AbstractBatch.FETCH_LIMIT, offset))
        .isEmpty()) {
      for (Address address : addressList) {
        ++offset;
        try {
          recomputeAddress(address);
          incrementDone();
        } catch (Exception e) {
          TraceBackService.trace(e, BaseExceptionMessage.ADDRESS_TEMPLATE_ERROR, address.getId());
          incrementAnomaly();
        }
      }
      JPA.clear();
      findBatch();
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  private void recomputeAddress(Address address) {
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

  @Override
  protected void setBatchTypeSelect() {
    this.batch.setBatchTypeSelect(BatchRepository.BATCH_TYPE_BASE_BATCH);
  }
}
