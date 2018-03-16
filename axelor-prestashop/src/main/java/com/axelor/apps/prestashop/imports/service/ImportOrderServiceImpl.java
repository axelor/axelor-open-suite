/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.apps.prestashop.imports.service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;

import com.axelor.apps.account.db.PaymentCondition;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.PaymentConditionRepository;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.AppPrestashop;
import com.axelor.apps.base.db.CancelReason;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.AddressRepository;
import com.axelor.apps.base.db.repo.AppPrestashopRepository;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.CurrencyRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.prestashop.db.SaleOrderStatus;
import com.axelor.apps.prestashop.entities.PrestashopResourceType;
import com.axelor.apps.prestashop.exception.IExceptionMessage;
import com.axelor.apps.prestashop.service.library.PSWebServiceClient;
import com.axelor.apps.prestashop.service.library.PSWebServiceClient.Options;
import com.axelor.apps.prestashop.service.library.PrestaShopWebserviceException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.saleorder.SaleOrderWorkflowService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import wslite.json.JSONArray;
import wslite.json.JSONException;
import wslite.json.JSONObject;

public class ImportOrderServiceImpl implements ImportOrderService {

    private final String shopUrl;
	private final String key;
	private final boolean isStatus;
	private final List<SaleOrderStatus> saleOrderStatus;
	private final PaymentMode paymentMode;


	@Inject
	private PaymentConditionRepository paymentConditionRepo;

	@Inject
	private CompanyRepository companyRepo;

	@Inject
	private SaleOrderRepository saleOrderRepo;

	@Inject
	private SaleOrderWorkflowService saleOrderWorkflowService;

	/**
	 * Initialization
	 */
	public ImportOrderServiceImpl() {
		AppPrestashop prestaShopObj = Beans.get(AppPrestashopRepository.class).all().fetchOne();
		shopUrl = prestaShopObj.getPrestaShopUrl();
		key = prestaShopObj.getPrestaShopKey();
		isStatus = prestaShopObj.getIsOrderStatus();
		saleOrderStatus = prestaShopObj.getSaleOrderStatusList();
		paymentMode = prestaShopObj.getPaymentMode();
	}

	/**
	 * Get only order ids which is in draft state
	 *
	 * @return order ids which is in draft state
	 * @throws PrestaShopWebserviceException
	 * @throws JSONException
	 */
	public List<Integer> getDraftOrderIds() throws PrestaShopWebserviceException, JSONException {

		List<Integer> orderIds = new ArrayList<>();
		List<Integer> currentStatus = new ArrayList<>();

		for(SaleOrderStatus orderStatus : saleOrderStatus) {
			if(orderStatus.getAbsStatus() == 1) {
				currentStatus.add(orderStatus.getPrestaShopStatus());
			}
		}

		PSWebServiceClient ws = new PSWebServiceClient(shopUrl, key);
		HashMap<String, String> filter = new HashMap<>();
		Options options = new Options();
		options.setResourceType(PrestashopResourceType.ORDERS);

		for(Integer id : currentStatus) {
			filter.put("current_state", id.toString());
			options.setFilter(filter);
			JSONObject schema =  ws.getJson(options);

			if(schema != null) {
				JSONArray jsonMainArr = schema.getJSONArray("orders");

				for (int i = 0; i < jsonMainArr.length(); i++) {
				     JSONObject childJSONObject = jsonMainArr.getJSONObject(i);
				     orderIds.add(childJSONObject.getInt("id"));
				}
			}
		}
		return orderIds;
	}

	/**
	 * Manage address of customer
	 *
	 * @param deliveryAddressId id of address which is on prestashop
	 * @param invoiceAddressId id of address which is on prestashop
	 * @param order current sale order
	 * @return object of sale order
	 */
	public SaleOrder manageAddresses(String deliveryAddressId, String invoiceAddressId, SaleOrder order) {

		Address deliveryAddress = null;
		Address invoiceAddress = null;

		if (deliveryAddressId != null) {
			deliveryAddress = Beans.get(AddressRepository.class).all().filter("self.prestaShopId = ?" , deliveryAddressId).fetchOne();
			order.setDeliveryAddress(deliveryAddress);
			order.setDeliveryAddressStr(deliveryAddress.getAddressL4() + "\n" + deliveryAddress.getAddressL5()
				+ "\n" + deliveryAddress.getAddressL6() + "\n" + deliveryAddress.getAddressL7Country().getName());
		}

		if(invoiceAddressId != null) {
			invoiceAddress = Beans.get(AddressRepository.class).all().filter("self.prestaShopId = ?" , invoiceAddressId).fetchOne();
			order.setMainInvoicingAddress(invoiceAddress);
			order.setMainInvoicingAddressStr(invoiceAddress.getAddressL4() + "\n" + invoiceAddress.getAddressL5()
				+ "\n" + invoiceAddress.getAddressL6() + "\n" + invoiceAddress.getAddressL7Country().getName());
		}

		return order;
	}

	/**
	 * Update status of sale order on prestashop
	 *
	 * @param order current sale order
	 * @param status sale order status
	 * @return object of sale order
	 * @throws Exception
	 */
	public SaleOrder updateOrderStatus(SaleOrder order, int status) throws Exception {

		for(SaleOrderStatus saleOrderStatus : saleOrderStatus) {
			if(status == saleOrderStatus.getPrestaShopStatus()) {
				if(saleOrderStatus.getAbsStatus() == 1) {
					order.setStatusSelect(saleOrderStatus.getAbsStatus());

				} else if (saleOrderStatus.getAbsStatus() == 2) {
					order.setManualUnblock(true);
					saleOrderWorkflowService.finalizeSaleOrder(order);
					order.setStatusSelect(saleOrderStatus.getAbsStatus());

				} else if (saleOrderStatus.getAbsStatus() == 3) {
					order.setManualUnblock(true);
					saleOrderWorkflowService.finalizeSaleOrder(order);
					saleOrderWorkflowService.confirmSaleOrder(order);
					order.setStatusSelect(saleOrderStatus.getAbsStatus());

				} else if (saleOrderStatus.getAbsStatus() == 4) {
					order.setManualUnblock(true);
					saleOrderWorkflowService.finalizeSaleOrder(order);
					saleOrderWorkflowService.confirmSaleOrder(order);
					order.setStatusSelect(saleOrderStatus.getAbsStatus());

				} else if (saleOrderStatus.getAbsStatus() == 5) {
					CancelReason cancelReason = new CancelReason();
					cancelReason.setName("From prestashop");
					order.setCancelReason(cancelReason);
					order.setCancelReasonStr("From prestashop");
					saleOrderWorkflowService.cancelSaleOrder(order, order.getCancelReason(), order.getCancelReasonStr());
					order.setStatusSelect(saleOrderStatus.getAbsStatus());

				} else {
					order.setStatusSelect(1);
				}
			}
		}
		return order;
	}

	@SuppressWarnings("deprecation")
	@Override
	@Transactional
	public BufferedWriter importOrder(BufferedWriter bwImport)
			throws IOException, PrestaShopWebserviceException, TransformerException, JAXBException, JSONException {

		Integer done = 0;
		Integer anomaly = 0;
		bwImport.newLine();
		bwImport.write("-----------------------------------------------");
		bwImport.newLine();
		bwImport.write("Order");
		Long clientPartner = 0l;
		Integer prestashopId = null;
		List<Integer> orderIds = null;

		if(isStatus == true) {
			PSWebServiceClient ws = new PSWebServiceClient(shopUrl,key);
			orderIds = ws.fetchApiIds(PrestashopResourceType.ORDERS);
		} else {
			orderIds = this.getDraftOrderIds();
		}

		for (Integer id : orderIds) {
			try {
				PSWebServiceClient ws = new PSWebServiceClient(shopUrl, key);
				Options options = new Options();
				options.setResourceType(PrestashopResourceType.ORDERS);
				options.setRequestedId(id);
				JSONObject schema = ws.getJson(options);

				prestashopId = schema.getJSONObject("order").getInt("id");
				SaleOrder order = null;
				Partner partner = null;
				Integer status = null;
				Currency currency = null;
				order = Beans.get(SaleOrderRepository.class).all().filter("self.prestaShopId = ?", prestashopId).fetchOne();

				if(order == null) {
					order = new SaleOrder();
					order.setPrestaShopId(prestashopId);
				}

				if (!schema.getJSONObject("order").getString("id_customer").isEmpty()) {
					clientPartner = Long.parseLong(schema.getJSONObject("order").getString("id_customer"));
				} else {
					throw new AxelorException(I18n.get(IExceptionMessage.INVALID_CUSTOMER), IException.NO_VALUE);
				}

				partner = Beans.get(PartnerRepository.class).all().filter("self.prestaShopId = ?", clientPartner).fetchOne();
				status = Integer.parseInt(schema.getJSONObject("order").getString("current_state"));
				currency = Beans.get(CurrencyRepository.class).all().filter("self.prestaShopId = ?" , schema.getJSONObject("order").getString("id_currency")).fetchOne();

				if(partner == null)
					throw new AxelorException(I18n.get(IExceptionMessage.INVALID_CUSTOMER), IException.NO_VALUE);

				if(currency == null)
					throw new AxelorException(I18n.get(IExceptionMessage.INVALID_CURRENCY), IException.NO_VALUE);

				String deliveryAddressId = schema.getJSONObject("order").getString("id_address_delivery");
				String invoiceAddressId = schema.getJSONObject("order").getString("id_address_invoice");
				String paymentCondition = schema.getJSONObject("order").getString("payment");
				Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse( schema.getJSONObject("order").getString("date_add"));
				String formattedDate = new SimpleDateFormat("yyyy-MM-dd").format(date);
				PaymentCondition paymentConditionObj = paymentConditionRepo.findByName(paymentCondition);

				if(paymentConditionObj == null) {
					paymentConditionObj = new PaymentCondition();
					paymentConditionObj.setCode(paymentCondition);
					paymentConditionObj.setName(paymentCondition);
				}

				order.setClientPartner(partner);
				order = this.manageAddresses(deliveryAddressId, invoiceAddressId, order);
				order.setExTaxTotal(new BigDecimal(schema.getJSONObject("order").getString("total_paid_tax_excl")).setScale(2, RoundingMode.HALF_UP));
				order.setTaxTotal(new BigDecimal(schema.getJSONObject("order").getString("total_wrapping_tax_incl")).setScale(2, RoundingMode.HALF_UP));
				order.setInTaxTotal(new BigDecimal(schema.getJSONObject("order").getString("total_paid")).setScale(2, RoundingMode.HALF_UP));
				order.setCreationDate(LocalDate.parse(formattedDate));
				order.setOrderDate(LocalDate.parse(formattedDate));
				order.setExternalReference(schema.getJSONObject("order").getString("reference"));
				order.setPaymentMode(paymentMode);
				order.setPaymentCondition(paymentConditionObj);
				Company company = companyRepo.find(1L);
				order.setCompany(company);
				order.setCurrency(currency);
				order = this.updateOrderStatus(order, status);
				saleOrderRepo.persist(order);
				saleOrderRepo.save(order);
				done++;
			} catch (AxelorException e) {
				bwImport.newLine();
				bwImport.newLine();
				bwImport.write("Id - " + id + " " + e.getMessage());
				anomaly++;
				continue;
			} catch (Exception e) {
				bwImport.newLine();
				bwImport.newLine();
				bwImport.write("Id - " + id + " " + e.getMessage());
				anomaly++;
				continue;
			}
		}

		bwImport.newLine();
		bwImport.newLine();
		bwImport.write("Succeed : " + done + " " + "Anomaly : " + anomaly);
		return bwImport;
	}
}
