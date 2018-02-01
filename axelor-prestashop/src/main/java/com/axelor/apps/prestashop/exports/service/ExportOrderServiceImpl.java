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
package com.axelor.apps.prestashop.exports.service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.axelor.apps.base.db.AppPrestashop;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.repo.CurrencyRepository;
import com.axelor.apps.prestashop.db.Associations;
import com.axelor.apps.prestashop.db.Cart_row;
import com.axelor.apps.prestashop.db.Cart_rows;
import com.axelor.apps.prestashop.db.Carts;
import com.axelor.apps.prestashop.db.Order_histories;
import com.axelor.apps.prestashop.db.Order_row;
import com.axelor.apps.prestashop.db.Order_rows;
import com.axelor.apps.prestashop.db.Orders;
import com.axelor.apps.prestashop.db.Prestashop;
import com.axelor.apps.prestashop.db.SaleOrderStatus;
import com.axelor.apps.prestashop.exception.IExceptionMessage;
import com.axelor.apps.prestashop.service.library.PSWebServiceClient;
import com.axelor.apps.prestashop.service.library.PrestaShopWebserviceException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;

@Singleton
public class ExportOrderServiceImpl implements ExportOrderService {
	private SaleOrderRepository saleOrderRepo;
	private CurrencyRepository currencyRepo;

	@Inject
	public ExportOrderServiceImpl(SaleOrderRepository saleOrderRepo, CurrencyRepository currencyRepo) {
		this.saleOrderRepo = saleOrderRepo;
		this.currencyRepo = currencyRepo;
	}


	/**
	 * Get the cart id of sale order from prestashop.
	 *
	 * @param saleOrder current saleOrder object
	 * @return prestashop Id of current saleOrder
	 * @throws PrestaShopWebserviceException
	 */
	public String getCartId(AppPrestashop appConfig, SaleOrder saleOrder) throws PrestaShopWebserviceException {
		String cart_id = "";
		PSWebServiceClient ws = new PSWebServiceClient(appConfig.getPrestaShopUrl() + "/api/orders/" + saleOrder.getPrestaShopId(), appConfig.getPrestaShopKey());
		Map<String, Object> opt = new HashMap<>();
		opt.put("resource", "orders");
		Document schema = ws.get(opt);
		NodeList list = schema.getChildNodes();

		for (int i = 0; i < list.getLength(); i++) {
			if (list.item(i).getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) list.item(i);
				if (element.getElementsByTagName("id").item(0).getTextContent().toString()
						.equals(saleOrder.getPrestaShopId())) {
					cart_id = element.getElementsByTagName("id_cart").item(0).getTextContent().toString();
					break;
				}
			}
		}
		return cart_id;
	}

	@Override
	@Transactional
	public void exportOrder(AppPrestashop appConfig, ZonedDateTime endDate, BufferedWriter bwExport)
			throws IOException, TransformerConfigurationException, TransformerException, ParserConfigurationException,
			SAXException, PrestaShopWebserviceException, JAXBException, TransformerFactoryConfigurationError {
		int done = 0;
		int anomaly = 0;
		String schema = null;
		Document document = null;

		bwExport.newLine();
		bwExport.write("-----------------------------------------------");
		bwExport.newLine();
		bwExport.write("Order");

		final StringBuilder filter = new StringBuilder(128);
		final List<Object> params = new ArrayList<>(2);

		if(endDate != null) {
			filter.append("(self.createdOn > ?1 OR self.updatedOn > ?2 OR self.prestaShopId IS NULL)");
			params.add(endDate);
			params.add(endDate);
		}

		if(appConfig.getIsOrderStatus() == Boolean.TRUE) {
			if(filter.length() > 0) filter.append(" AND ");
			filter.append("self.statusSelect = 1");
		}

		if(appConfig.getExportNonPrestashopOrders() == Boolean.FALSE) {
			// Only push back orders that come from prestashop
			if(filter.length() > 0) filter.append(" AND ");
			filter.append("self.prestaShopId IS NOT NULL");
		}

		for (SaleOrder saleOrder : Beans.get(SaleOrderRepository.class).all().filter(filter.toString(), params.toArray(new Object[] {})).fetch()) {

			List<Cart_row> cartRowList = new ArrayList<Cart_row>();
			String id_customer = "";
			String id_address_delivery = "";
			String id_address_invoice = "";
			String secure_key = "";
			String cartId = "";
			String id_currency = "";

			if (saleOrder.getPrestaShopId() != null) {
				cartId = getCartId(appConfig, saleOrder);
			}

			try {

				if (!saleOrder.getClientPartner().getPrestaShopId().isEmpty()) {

					id_customer = saleOrder.getClientPartner().getPrestaShopId();
					id_address_delivery = saleOrder.getDeliveryAddress().getPrestaShopId();
					id_address_invoice =  saleOrder.getMainInvoicingAddress().getPrestaShopId();
					Currency currency = currencyRepo.findByCode(saleOrder.getCurrency().getCode());
					id_currency = currency.getPrestaShopId();

					Carts cart = new Carts();
					cart.setId(cartId);
					cart.setId_shop_group("1");
					cart.setId_shop("1");
					cart.setId_carrier("1");
					cart.setId_currency(id_currency);
					cart.setId_lang("1");

					if(id_address_delivery == null) {
						throw new AxelorException(IException.NO_VALUE, I18n.get(IExceptionMessage.INVALID_ADDRESS));
					} else {
						cart.setId_address_delivery(id_address_delivery);
					}

					cart.setId_address_invoice(id_address_invoice);
					cart.setId_customer(id_customer.toString());
					cart.setSecure_key(secure_key.toString());

					for(SaleOrderLine line: saleOrder.getSaleOrderLineList()) {
						if(line.getProduct() != null) {
							Cart_row cart_row = new Cart_row();
							cart_row.setId_product(line.getProduct().getPrestaShopId());
							cart_row.setId_product_attribute("0");
							cart_row.setId_address_delivery(id_address_delivery);
							cart_row.setQuantity(line.getQty().toString());
							cartRowList.add(cart_row);
						}
					}

					Cart_rows cartRows = new Cart_rows();
					cartRows.setCart_row(cartRowList);

					Associations associations = new Associations();
					associations.setCart_rows(cartRows);
					cart.setAssociations(associations);

					Prestashop prestaShop = new Prestashop();
					prestaShop.setPrestashop(cart);

					StringWriter sw = new StringWriter();
					JAXBContext contextObj = JAXBContext.newInstance(Prestashop.class);
					Marshaller marshallerObj = contextObj.createMarshaller();
					marshallerObj.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
					marshallerObj.marshal(prestaShop, sw);
					schema = sw.toString();

					HashMap<String, Object> opt = new HashMap<String, Object>();
					opt.put("resource", "carts");
					opt.put("postXml", schema);

					if (saleOrder.getPrestaShopId() == null) {
						PSWebServiceClient ws = new PSWebServiceClient(appConfig.getPrestaShopUrl() + "/api/carts?schema=blank", appConfig.getPrestaShopKey());
						document = ws.add(opt);
						cartId = document.getElementsByTagName("id").item(0).getTextContent();

					} else {
						PSWebServiceClient ws = new PSWebServiceClient(appConfig.getPrestaShopUrl(), appConfig.getPrestaShopKey());
						opt.put("id", cartId);
						document = ws.edit(opt);
						cartId = document.getElementsByTagName("id").item(0).getTextContent();
					}

					this.createOrder(appConfig, saleOrder, id_address_delivery, id_address_invoice, cartId, id_currency, id_customer);
				}

				done++;

			} catch (AxelorException e) {
				bwExport.newLine();
				bwExport.newLine();
				bwExport.write("Id - " + saleOrder.getId().toString() + " " + e.getMessage());
				anomaly++;
				continue;

			} catch (Exception e) {
				bwExport.newLine();
				bwExport.newLine();
				bwExport.write("Id - " + saleOrder.getId().toString() + " " + e.getMessage());
				anomaly++;
				continue;
			}
		}

		bwExport.newLine();
		bwExport.newLine();
		bwExport.write("Succeed : " + done + " " + "Anomaly : " + anomaly);
	}

	/**
	 * Create Sale Order on prestashop
	 *
	 * @param appConfig Module configuration
	 * @param saleOrder current saleOrder
	 * @param id_address_delivery of prestashop's address module
	 * @param id_address_invoice  of prestashop's address module
	 * @param cartId current order's cat id
	 * @param id_currency of prestashop's currency
	 * @param id_customer of prestashop's customer
	 * @throws PrestaShopWebserviceException
	 * @throws TransformerException
	 * @throws JAXBException
	 */
	public void createOrder(AppPrestashop appConfig, SaleOrder saleOrder, String id_address_delivery, String id_address_invoice,
			String cartId, String id_currency, String id_customer) throws PrestaShopWebserviceException, TransformerException, JAXBException {

		List<Order_row> orderRowList = new ArrayList<Order_row>();
		Document document;
		String orderId = null;

		Orders order = new Orders();
		order.setId(saleOrder.getPrestaShopId());
		order.setId_shop("1");
		order.setId_shop_group("1");

		order.setId_address_delivery(id_address_delivery);
		order.setId_address_invoice(id_address_invoice);
		order.setId_cart(cartId);
		order.setId_currency(id_currency);
		order.setId_lang("1");
		order.setId_customer(id_customer);
		order.setId_carrier("1");
		order.setTotal_paid_tax_incl(saleOrder.getExTaxTotal().setScale(2, RoundingMode.HALF_UP).toString());
		order.setTotal_wrapping_tax_incl(saleOrder.getTaxTotal().setScale(2, RoundingMode.HALF_UP).toString());
		order.setTotal_paid(saleOrder.getInTaxTotal().setScale(2, RoundingMode.HALF_UP).toString());
		order.setTotal_paid_tax_excl(saleOrder.getExTaxTotal().setScale(2, RoundingMode.HALF_UP).toString());
		order.setTotal_paid_real(saleOrder.getExTaxTotal().setScale(2, RoundingMode.HALF_UP).toString());
		order.setTotal_products_wt(saleOrder.getExTaxTotal().setScale(2, RoundingMode.HALF_UP).toString());
		order.setTotal_shipping("0");
		order.setTotal_products(saleOrder.getExTaxTotal().setScale(2, RoundingMode.HALF_UP).toString());
		order.setTotal_shipping_tax_excl("00.0");
		order.setConversion_rate("0.00");
		order.setModule("ps_checkpayment");
		order.setPayment(saleOrder.getPaymentCondition().getName());

		if(saleOrder.getPrestaShopId() == null) {

			for(SaleOrderLine line: saleOrder.getSaleOrderLineList()) {
				if(line.getProduct() != null) {
					Order_row order_row = new Order_row();
					order_row.setProduct_id(line.getProduct().getPrestaShopId());
					orderRowList.add(order_row);
				}
			}

			Order_rows orderRows = new Order_rows();
			orderRows.setOrder_row(orderRowList);

			Associations associations = new Associations();
			associations.setOrder_rows(orderRows);
			order.setAssociations(associations);

			Prestashop prestaShop = new Prestashop();
			prestaShop.setPrestashop(order);

			StringWriter sw = new StringWriter();
			JAXBContext contextObj = JAXBContext.newInstance(Prestashop.class);
			Marshaller marshallerObj = contextObj.createMarshaller();
			marshallerObj.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshallerObj.marshal(prestaShop, sw);
			String schema = sw.toString();

			PSWebServiceClient ws = new PSWebServiceClient(appConfig.getPrestaShopUrl() + "/api/orders?schema=blank", appConfig.getPrestaShopKey());
			Map<String, Object> opt = new HashMap<>();
			opt.put("resource", "orders");
			opt.put("postXml", schema);
			document = ws.add(opt);

			orderId = document.getElementsByTagName("id").item(0).getTextContent();

		} else {

			Prestashop prestaShop = new Prestashop();
			prestaShop.setPrestashop(order);

			StringWriter sw = new StringWriter();
			JAXBContext contextObj = JAXBContext.newInstance(Prestashop.class);
			Marshaller marshallerObj = contextObj.createMarshaller();
			marshallerObj.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshallerObj.marshal(prestaShop, sw);
			String schema = sw.toString();

			Map<String, Object> opt = new HashMap<>();
			opt.put("resource", "orders");
			opt.put("postXml", schema);
			opt.put("id", saleOrder.getPrestaShopId());
			PSWebServiceClient ws = new PSWebServiceClient(appConfig.getPrestaShopUrl(), appConfig.getPrestaShopUrl());
			document = ws.edit(opt);
			orderId = document.getElementsByTagName("id").item(0).getTextContent();
		}

		Order_histories histories = new Order_histories();
		histories.setId_order(orderId);

		for(SaleOrderStatus orderStatus : appConfig.getSaleOrderStatusList()) {
			if(orderStatus.getAbsStatus() == saleOrder.getStatusSelect()) {
				histories.setId_order_state(orderStatus.getPrestaShopStatus().toString());
				break;
			}
		}

		Prestashop prestaShop = new Prestashop();
		prestaShop.setPrestashop(histories);

		StringWriter sw = new StringWriter();
		JAXBContext contextObj = JAXBContext.newInstance(Prestashop.class);
		Marshaller marshallerObj = contextObj.createMarshaller();
		marshallerObj.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshallerObj.marshal(prestaShop, sw);
		String schema = sw.toString();

		PSWebServiceClient ws = new PSWebServiceClient(appConfig.getPrestaShopUrl() + "/api/order_histories?schema=blank", appConfig.getPrestaShopKey());
		Map<String, Object> opt = new HashMap<>();
		opt.put("resource", "order_histories");
		opt.put("postXml", schema);
		ws.add(opt);

		saleOrder.setPrestaShopId(orderId);
		saleOrderRepo.save(saleOrder);
	}
}
