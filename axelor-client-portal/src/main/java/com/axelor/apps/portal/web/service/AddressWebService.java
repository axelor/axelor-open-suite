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
package com.axelor.apps.portal.web.service;

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PartnerAddress;
import com.axelor.apps.base.db.repo.AddressRepository;
import com.axelor.apps.portal.service.AddressPortalService;
import com.axelor.apps.portal.service.PartnerPortalService;
import com.axelor.apps.portal.service.response.PortalRestResponse;
import com.axelor.apps.portal.service.response.ResponseGeneratorFactory;
import com.axelor.apps.portal.service.response.generator.ResponseGenerator;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JpaSecurity;
import com.axelor.db.JpaSecurity.AccessType;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/")
public class AddressWebService extends AbstractWebService {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public PortalRestResponse fetch(
      @QueryParam("sort") String sort,
      @QueryParam("page") int page,
      @QueryParam("limit") int limit) {

    final String filter = "self.partner = :partner";
    Map<String, Object> params = ImmutableMap.of("partner", getPartner());
    List<Address> addresses =
        fetch(PartnerAddress.class, filter, params, sort, limit, page).stream()
            .map(PartnerAddress::getAddress)
            .distinct()
            .collect(Collectors.toList());
    Beans.get(JpaSecurity.class)
        .check(
            AccessType.READ,
            Address.class,
            addresses.stream().map(Address::getId).toArray(Long[]::new));
    ResponseGenerator generator = ResponseGeneratorFactory.of(Address.class.getName());
    List<Map<String, Object>> data =
        addresses.stream().map(generator::generate).collect(Collectors.toList());
    return new PortalRestResponse().setData(data).success();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{id}")
  public PortalRestResponse fetch(@PathParam("id") Long id) throws AxelorException {

    final String filter = "self.partner = :partner and self.address.id = :id";
    Partner partner = getPartner();
    Map<String, Object> params = ImmutableMap.of("partner", partner, "id", id);
    List<PartnerAddress> partnerAddress = fetch(PartnerAddress.class, filter, params, null, 0, 1);
    if (ObjectUtils.isEmpty(partnerAddress)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get("Address with given id (%s) not found for partner (%s)"),
          id,
          partner.getName());
    }
    Address address = partnerAddress.get(0).getAddress();
    Beans.get(JpaSecurity.class).check(AccessType.READ, Address.class, address.getId());
    Map<String, Object> data =
        ResponseGeneratorFactory.of(Address.class.getName()).generate(address);
    return new PortalRestResponse().setData(data).success();
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public PortalRestResponse create(Map<String, Object> values) {
    Beans.get(JpaSecurity.class).check(AccessType.CREATE, Address.class);
    Partner partner = getPartner();

    Address address = Beans.get(AddressPortalService.class).create(values);
    Beans.get(PartnerPortalService.class).addPartnerAddress(partner, address);
    Map<String, Object> data =
        ResponseGeneratorFactory.of(Address.class.getName()).generate(address);
    return new PortalRestResponse().setData(data).success();
  }

  @POST
  @Path("/{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public PortalRestResponse update(@PathParam("id") Long id, Map<String, Object> values)
      throws AxelorException {
    Beans.get(JpaSecurity.class).check(AccessType.WRITE, Address.class, id);
    Address address = find(id);
    address = Beans.get(AddressPortalService.class).update(address, values);
    Map<String, Object> data =
        ResponseGeneratorFactory.of(Address.class.getName()).generate(address);
    return new PortalRestResponse().setData(data).success();
  }

  @DELETE
  @Path("/{id}")
  public void remove(@PathParam("id") Long id) throws AxelorException {
    Beans.get(PartnerPortalService.class).removePartnerAddress(getPartner(), find(id));
  }

  private Address find(Long id) throws AxelorException {
    AddressRepository addressRepository = Beans.get(AddressRepository.class);
    Address address = addressRepository.find(id);
    if (address == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE, I18n.get("Address with id (%s) not found"), id);
    }
    return address;
  }
}
