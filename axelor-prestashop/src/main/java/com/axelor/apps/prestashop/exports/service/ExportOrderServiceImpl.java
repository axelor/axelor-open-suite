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

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.base.db.AppPrestashop;
import com.axelor.apps.base.db.repo.PriceListLineRepository;
import com.axelor.apps.base.service.AddressService;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.prestashop.db.SaleOrderStatus;
import com.axelor.apps.prestashop.entities.Associations.CartRowsAssociationElement;
import com.axelor.apps.prestashop.entities.Associations.OrderRowsAssociationElement;
import com.axelor.apps.prestashop.entities.PrestashopCart;
import com.axelor.apps.prestashop.entities.PrestashopDelivery;
import com.axelor.apps.prestashop.entities.PrestashopOrder;
import com.axelor.apps.prestashop.entities.PrestashopOrderHistory;
import com.axelor.apps.prestashop.entities.PrestashopOrderInvoice;
import com.axelor.apps.prestashop.entities.PrestashopOrderPayment;
import com.axelor.apps.prestashop.entities.PrestashopOrderRowDetails;
import com.axelor.apps.prestashop.entities.PrestashopResourceType;
import com.axelor.apps.prestashop.service.library.PSWebServiceClient;
import com.axelor.apps.prestashop.service.library.PrestaShopWebserviceException;
import com.axelor.apps.sale.db.AdvancePayment;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.AdvancePaymentRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;

@Singleton
public class ExportOrderServiceImpl implements ExportOrderService {
	private Logger log = LoggerFactory.getLogger(getClass());

	private SaleOrderRepository saleOrderRepo;
	private AddressService addressService;
	private CurrencyService currencyService;
	private UnitConversionService unitConversionService;
	private InvoiceRepository invoiceRepository;

	@Inject
	public ExportOrderServiceImpl(SaleOrderRepository saleOrderRepo, AddressService addressService, InvoiceRepository invoiceRepository,
			CurrencyService currencyService, UnitConversionService unitConversionService) {
		this.saleOrderRepo = saleOrderRepo;
		this.addressService = addressService;
		this.invoiceRepository = invoiceRepository;
		this.currencyService = currencyService;
		this.unitConversionService = unitConversionService;
	}


	@Override
	@Transactional
	public void exportOrder(AppPrestashop appConfig, ZonedDateTime endDate, Writer logBuffer) throws IOException, PrestaShopWebserviceException {
		int done = 0;
		int errors = 0;

		log.debug("Starting orders export to prestashop");
		logBuffer.write(String.format("%n====== ORDERS ======%n"));

		final PSWebServiceClient ws = new PSWebServiceClient(appConfig.getPrestaShopUrl(), appConfig.getPrestaShopKey());

		final StringBuilder filter = new StringBuilder(128);
		final List<Object> params = new ArrayList<>(2);

		filter.append("1 = 1");

		if(endDate != null) {
			filter.append("AND (self.createdOn > ?1 OR self.updatedOn > ?2 OR self.prestaShopId IS NULL)");
			params.add(endDate);
			params.add(endDate);
		}

		if(appConfig.getIsOrderStatus() == Boolean.TRUE) {
			filter.append("AND (self.statusSelect = 1)");
		}

		if(appConfig.getExportNonPrestashopOrders() == Boolean.FALSE) {
			// Only push back orders that come from prestashop
			filter.append("AND (self.prestaShopId IS NOT NULL)");
		}

		orderLoop: // Not very pretty
		for (SaleOrder localOrder : saleOrderRepo.all().filter(filter.toString(), params.toArray()).fetch()) {
			logBuffer.write(String.format("Exporting order #%d (%s) ‑ ", localOrder.getId(), localOrder.getSaleOrderSeq()));
			if(localOrder.getClientPartner().getPrestaShopId() == null) {
				logBuffer.write(String.format(" [WARNING] Customer is not synced yet, skipping%n"));
				continue;
			}
			if(localOrder.getDeliveryAddress() == null) {
				logBuffer.write(String.format(" [WARNING] No delivery address filled, required for prestashop, skipping%n"));
				continue;
			} else if(localOrder.getDeliveryAddress().getPrestaShopId() == null) {
				logBuffer.write(String.format(" [WARNING] Delivery address has not been synced yet, skipping%n"));
				continue;
			}
			if(localOrder.getMainInvoicingAddress() == null) {
				logBuffer.write(String.format(" [WARNING] No invoicing address filled, required for prestashop, skipping%n"));
				continue;
			} else if(localOrder.getMainInvoicingAddress().getPrestaShopId() == null) {
				logBuffer.write(String.format(" [WARNING] Invoicing address has not been synced yet, skipping%n"));
				continue;
			}
			if(localOrder.getCurrency().getPrestaShopId() == null) {
				logBuffer.write(String.format(" [WARNING] Currency has not been synced yet, skipping%n"));
				continue;
			}

			PrestashopOrder remoteOrder;
			PrestashopCart remoteCart;

			// We do not fetch the all remote orders as for other entities since
			// it could lead to memory issues on heavy databases, tradeoff is that
			// we'd have a lot of HTTP roundtrips.
			if(localOrder.getPrestaShopId() != null) {
				logBuffer.write("prestashop id=" + localOrder.getPrestaShopId());
				remoteOrder = ws.fetch(PrestashopResourceType.ORDERS, localOrder.getPrestaShopId());
				if(remoteOrder == null) {
					logBuffer.write(String.format(" [ERROR] Not found remotely%n"));
					log.error("Unable to fetch remote order #{} ({}), something's probably very wrong, skipping",
							localOrder.getPrestaShopId(), localOrder.getSaleOrderSeq());
					++errors;
					continue;
				}
				remoteCart = ws.fetch(PrestashopResourceType.CARTS, remoteOrder.getCartId());
				if(remoteCart == null) {
					logBuffer.write(String.format(" [ERROR] Cart for order #%s (%s) not found remotely, your prestashop installation seems to be in a very bad shape%n", localOrder.getPrestaShopId(), localOrder.getSaleOrderSeq()));
					log.error("Unable to fetch cart #{} (for order {}), something's probably very wrong, skipping",
							remoteOrder.getCartId(), localOrder.getSaleOrderSeq());
					++errors;
					continue;
				}
			} else {
				logBuffer.write("no prestashop id, assuming new order & cart");
				remoteOrder = new PrestashopOrder();
				remoteCart = new PrestashopCart();

				remoteOrder.setCustomerId(localOrder.getClientPartner().getPrestaShopId());
				remoteOrder.setCurrencyId(localOrder.getCurrency().getPrestaShopId());
				remoteOrder.setDeliveryAddressId(localOrder.getDeliveryAddress().getPrestaShopId());
				remoteOrder.setInvoiceAddressId(localOrder.getMainInvoicingAddress().getPrestaShopId());
				remoteOrder.setLanguageId(1); // FIXME Handle language correctly
				remoteOrder.setCarrierId(1); // TODO We should have a way to provide mapping between FreightCarrierModes and PrestaShop carriers
				remoteOrder.setAddDate(localOrder.getCreatedOn());
				if(localOrder.getPaymentCondition() != null) {
					remoteOrder.setPayment(localOrder.getPaymentCondition().getName());
				} else {
					remoteOrder.setPayment(I18n.getBundle(new Locale(StringUtils.defaultString(localOrder.getClientPartner().getLanguageSelect(), "en"))).getString("Unknown"));
				}
				remoteOrder.setModule("ps_checkpayment"); // FIXME make this configurable (through translation table?)
				remoteOrder.setSecureKey(RandomStringUtils.random(32, "0123456789abcdef"));

				remoteCart.setCustomerId(remoteOrder.getCustomerId());
				remoteCart.setCurrencyId(remoteOrder.getCurrencyId());
				remoteCart.setDeliveryAddressId(remoteOrder.getDeliveryAddressId());
				remoteCart.setInvoiceAddressId(remoteOrder.getInvoiceAddressId());
				remoteCart.setLanguageId(remoteOrder.getLanguageId());
				remoteCart.setAddDate(remoteOrder.getAddDate());
			}

			// Rebuild cart from scratch (should we?)

			try {
				// Prestashop expects rate to convert from prestashop'scurrency order currency, it will
				// use it when generating default order details to convert from product's prices (in prestashop
				// currency) to order prices (in order currency)
				remoteOrder.setConversionRate(currencyService.getCurrencyConversionRate(appConfig.getPrestaShopCurrency(), localOrder.getCurrency(), remoteOrder.getAddDate().toLocalDate()));
				logBuffer.write(String.format(", using conversion rate of %f", remoteOrder.getConversionRate()));
			} catch(AxelorException e) {
				logBuffer.write(String.format(" [WARNING] Unable to get currency conversion rate, leaving it untouched"));
			}
			BigDecimal taxIncludedShippingCosts = BigDecimal.ZERO;
			BigDecimal taxExcludedShippingCosts = BigDecimal.ZERO;
			BigDecimal taxIncludedProducts = BigDecimal.ZERO;
			BigDecimal taxExcludedProducts = BigDecimal.ZERO;

			final List<CartRowsAssociationElement> remoteCartRows = remoteCart.getAssociations().getCartRows().getCartRows();
			remoteCartRows.clear();

			// Extract lines containing products and rebuild cart from them
			final List<SaleOrderLine> localRows = new LinkedList<>();
			for(SaleOrderLine localRow : localOrder.getSaleOrderLineList()) {
				if(localRow.getProduct() == null) continue;
				if(localRow.getProduct().getPrestaShopId() == null) {
					logBuffer.write(String.format(" [WARNING] Product %s has not been synced yet, skipping order%n", localRow.getProduct().getCode()));
					continue orderLoop;
				}
				if(localRow.getProduct().getIsShippingCostsProduct()) {
					taxIncludedShippingCosts = taxIncludedShippingCosts.add(localRow.getInTaxTotal());
					taxExcludedShippingCosts = taxExcludedShippingCosts.add(localRow.getExTaxTotal());
				} else {
					taxIncludedProducts = taxIncludedProducts.add(localRow.getInTaxTotal());
					taxExcludedProducts = taxExcludedProducts.add(localRow.getExTaxTotal());
				}
				CartRowsAssociationElement remoteRow = new CartRowsAssociationElement();
				remoteRow.setProductId(localRow.getProduct().getPrestaShopId());
				remoteRow.setQuantity(localRow.getQty().intValue());
				remoteRow.setDeliveryAddressId(localOrder.getDeliveryAddress().getPrestaShopId());
				// TODO Handle variants (productAttribute)
				remoteCartRows.add(remoteRow);
				localRows.add(localRow);
			}

			remoteCart = ws.save(PrestashopResourceType.CARTS, remoteCart);
			// FIXME if something gets wrong here, we end up with a cart being pushed twice ('cause we don't
			// track cart IDs which is pretty easy to fix).

			// Recreate order
			remoteOrder.setCartId(remoteCart.getId());
			remoteOrder.setTotalPaidTaxIncluded(localOrder.getInTaxTotal().setScale(appConfig.getExportPriceScale(), RoundingMode.HALF_UP));
			remoteOrder.setTotalPaidTaxExcluded(localOrder.getExTaxTotal().setScale(appConfig.getExportPriceScale(), RoundingMode.HALF_UP));
			remoteOrder.setTotalPaid(remoteOrder.getTotalPaidTaxIncluded());
			remoteOrder.setTotalPaidReal(remoteOrder.getTotalPaidTaxIncluded());
			remoteOrder.setTotalProductsTaxExcluded(taxExcludedProducts.setScale(appConfig.getExportPriceScale(), RoundingMode.HALF_UP));
			remoteOrder.setTotalProductsTaxIncluded(taxIncludedProducts.setScale(appConfig.getExportPriceScale(), RoundingMode.HALF_UP));
			remoteOrder.setTotalShipping(taxIncludedShippingCosts.setScale(appConfig.getExportPriceScale(), RoundingMode.HALF_UP));
			remoteOrder.setTotalShippingTaxIncluded(taxIncludedShippingCosts.setScale(appConfig.getExportPriceScale(), RoundingMode.HALF_UP));
			remoteOrder.setTotalShippingTaxExcluded(taxExcludedShippingCosts.setScale(appConfig.getExportPriceScale(), RoundingMode.HALF_UP));

			SaleOrderStatus orderStatus = null;

			// FIXME This is too dumb to use with prestashop, we've to enforce things
			// with following statuses (user only provides mapping) :
			//  - awaiting payment (since there's no status for "on hold" on prestashop): no payment nor delivery
			//  - payment accepted:  fully paid and no delivery
			//  - preparation: partially delivered
			//  - shipped: totally delivered
			// Also, only export orders that are confirmed, we may create carts for earlier statuses but this
			// won't have a big interest.
			if(Boolean.TRUE.equals(appConfig.getIsOrderStatus())) {
				for(SaleOrderStatus status : appConfig.getSaleOrderStatusList()) {
					if(status.getAbsStatus() == localOrder.getStatusSelect()) {
						orderStatus = status;
						break;
					}
				}
				if(orderStatus == null) {
					logBuffer.write(String.format(" [WARNING] No mapping for order status %s, leaving untouched%n"));
				} else {
					remoteOrder.setCurrentState(orderStatus.getPrestaShopStatus());
				}
			}

			// TODO Check if recreating on every run is an issue, we could also perform a diff on the order
			List<OrderRowsAssociationElement> remoteOrderRows = remoteOrder.getAssociations().getOrderRows().getOrderRows();
			remoteOrderRows.clear();
			for(SaleOrderLine localRow : localOrder.getSaleOrderLineList()) {
				if(localRow.getProduct() == null) continue;
				OrderRowsAssociationElement remoteRow = new OrderRowsAssociationElement();
				remoteRow.setProductId(localRow.getProduct().getPrestaShopId()); // Validity has already been checked when creating cart
				remoteRow.setProductAttributeId(0); // TODO Handle variants
				remoteRow.setQuantity(localRow.getQty().intValue());
				remoteOrderRows.add(remoteRow);
			}
			if(log.isDebugEnabled()) {
				log.debug(String.format("Rows for order %s: %s", localOrder.getSaleOrderSeq(), remoteOrder.getAssociations().getOrderRows().getOrderRows()));
			}

			if(localOrder.getPrestaShopId() == null) {
				// Let prestashop assign a reference before further processing… Of course,
				// as it would be too simple, save does not fill reference and we have to make
				// an additional fetch()…
				remoteOrder = ws.save(PrestashopResourceType.ORDERS, remoteOrder);
				remoteOrder = ws.fetch(PrestashopResourceType.ORDERS, remoteOrder.getId());
			}


			Integer remoteInvoiceId = generateInvoicingEntities(appConfig, ws, localOrder, remoteOrder, logBuffer);

			if(localOrder.getPrestaShopId() == null) {
				List<SaleOrderLine> rows = new LinkedList<>(localRows);
				// If we just created order, lines have been created from cart
				for(OrderRowsAssociationElement remoteRow : remoteOrder.getAssociations().getOrderRows().getOrderRows()) {
					SaleOrderLine localRow = rows.remove(0);
					localRow.setPrestaShopId(remoteRow.getId());
				}
			}
			localOrder.setPrestaShopId(remoteOrder.getId());

			logBuffer.write(String.format(" [SUCCESS]%n\tExporting lines:%n"));

			exportLines(appConfig, ws, localOrder, localRows, remoteInvoiceId, logBuffer);


			if(orderStatus != null) {
				Map<String, String> historyFilter = new HashMap<>();
				historyFilter.put("id_order", remoteOrder.getId().toString());
				historyFilter.put("id_order_state", orderStatus.getPrestaShopStatus().toString());
				PrestashopOrderHistory history = ws.fetchOne(PrestashopResourceType.ORDER_HISTORIES, historyFilter);
				if(history == null) {
					logBuffer.write(String.format(" — Order status changed, recording new one"));
					history = new PrestashopOrderHistory();
					history.setOrderId(remoteOrder.getId().intValue());
					history.setOrderStateId(orderStatus.getPrestaShopStatus().intValue());
					history = ws.save(PrestashopResourceType.ORDER_HISTORIES, history);
				}
			}

			// We've to save *after* the lines are updated since totalPaid fields are totally ignored and forced
			// to product base price * qty otherwhise.
			remoteOrder = ws.save(PrestashopResourceType.ORDERS, remoteOrder);

			// OK, so we've an order with its lines, but those lines currently have no informations, eg. price was
			// taken from product configuration, so we've to improve them
			++done;
		}

		logBuffer.write(String.format("%n=== END OF ORDERS EXPORT, done: %d, errors: %d ===%n", done, errors));
	}

	private void exportLines(final AppPrestashop appConfig, final PSWebServiceClient ws,
			final SaleOrder order, final List<SaleOrderLine> lines,
			final Integer remoteInvoiceId, final Writer logBuffer) throws PrestaShopWebserviceException, IOException {
		List<PrestashopOrderRowDetails> remoteRows = ws.fetch(PrestashopResourceType.ORDER_DETAILS, Collections.singletonMap("id_order", order.getPrestaShopId().toString()));

		final Map<Integer, PrestashopOrderRowDetails> remoteRowsById = new HashMap<>();
		for(PrestashopOrderRowDetails row : remoteRows) {
			remoteRowsById.put(row.getId(), row);
		}

		// Now, we've to create missing rows (OrderDetail in Prestashop terminology)
		for(SaleOrderLine localRow : lines) {
			logBuffer.write(String.format("\tExporting line #%d (%s) ‑ ", localRow.getId(), localRow.getProductName()));
			PrestashopOrderRowDetails remoteRow;
			if(localRow.getPrestaShopId() == null || remoteRowsById.containsKey(localRow.getPrestaShopId()) == false) {
				if(localRow.getPrestaShopId() != null) {
					logBuffer.write(String.format("[WARNING] no row with id %d found remotely for this order, creating a new one", localRow.getId()));
				}

				// Line was added after first sync, we've to build it from scratch
				remoteRow = new PrestashopOrderRowDetails();
				remoteRow.setOrderId(order.getPrestaShopId());
			} else {
				logBuffer.write(String.format("prestashop id: %d", localRow.getPrestaShopId()));
				// Existing line, just update needed fields
				remoteRow = remoteRowsById.get(localRow.getPrestaShopId());
			}
			remoteRow.setProductId(localRow.getProduct().getPrestaShopId()); // Ensured to be not null by preliminary filtering
			remoteRow.setProductName(localRow.getProductName());
			remoteRow.setProductQuantity(localRow.getQty().intValue());
			remoteRow.setUnitPriceTaxExcluded(localRow.getExTaxTotal().divide(localRow.getQty()).setScale(appConfig.getExportPriceScale(), RoundingMode.HALF_UP));
			remoteRow.setUnitPriceTaxIncluded(localRow.getInTaxTotal().divide(localRow.getQty()).setScale(appConfig.getExportPriceScale(), RoundingMode.HALF_UP));
			remoteRow.setProductPrice(localRow.getPrice().setScale(appConfig.getExportPriceScale(), RoundingMode.HALF_UP));
			if(localRow.getDiscountTypeSelect() != null) {
				switch(localRow.getDiscountTypeSelect()) {
				case PriceListLineRepository.TYPE_PERCENT:
					remoteRow.setDiscountPercent(localRow.getDiscountAmount().setScale(appConfig.getExportPriceScale(), RoundingMode.HALF_UP));
					break;
				case PriceListLineRepository.TYPE_FIXED:
					remoteRow.setDiscountAmount(localRow.getDiscountAmount().setScale(appConfig.getExportPriceScale(), RoundingMode.HALF_UP));
					break;
				}
			}
			remoteRow.setEan13(localRow.getProduct().getEan13());
			remoteRow.setProductReference(localRow.getProduct().getCode());
			if(localRow.getProduct().getGrossWeight() != null) {
				try {
					remoteRow.setProductWeight(unitConversionService.convert(localRow.getProduct().getWeightUnit(), appConfig.getPrestaShopWeightUnit(), localRow.getProduct().getGrossWeight()));
				} catch(AxelorException e) {
					log.error("Exception while converting product weight");
				}
			}
			if(localRow.getProduct().getIsShippingCostsProduct() == Boolean.TRUE) {
				remoteRow.setTotalShippingPriceTaxExcluded(localRow.getExTaxTotal().setScale(appConfig.getExportPriceScale(), RoundingMode.HALF_UP));
				remoteRow.setTotalShippingPriceTaxExcluded(localRow.getInTaxTotal().setScale(appConfig.getExportPriceScale(), RoundingMode.HALF_UP));
			} else {
				remoteRow.setTotalPriceTaxExcluded(localRow.getExTaxTotal().setScale(appConfig.getExportPriceScale(), RoundingMode.HALF_UP));
				remoteRow.setTotalPriceTaxIncluded(localRow.getInTaxTotal().setScale(appConfig.getExportPriceScale(), RoundingMode.HALF_UP));
			}
			remoteRow.setOrderInvoiceId(remoteInvoiceId);
			remoteRow.setShopId(1); // FIXME Handle this through configuration
			// TODO Handle warehouse

			remoteRow = ws.save(PrestashopResourceType.ORDER_DETAILS, remoteRow);
			localRow.setPrestaShopId(remoteRow.getId());

			logBuffer.write(String.format(" [SUCCESS]%n"));
		}
	}

	private List<PrestashopOrderPayment> getPayments(final AppPrestashop appConfig, final SaleOrder order, final String prestashopReference) {
		final List<PrestashopOrderPayment> payments = new LinkedList<>();
		for(AdvancePayment p : order.getAdvancePaymentList()) {
			if(p.getStatusSelect() == AdvancePaymentRepository.STATUS_VALIDATED) {
				PrestashopOrderPayment payment = new PrestashopOrderPayment();
				payment.setOrderReference(prestashopReference);
				payment.setCurrencyId(p.getCurrency().getPrestaShopId());
				payment.setAmount(p.getAmount());
				try {
					payment.setConversionRate(currencyService.getCurrencyConversionRate(order.getCurrency(), appConfig.getPrestaShopCurrency(), p.getAdvancePaymentDate()));
				} catch (AxelorException e) {
					log.warn("Unable to get payment conversion rate, using 1.0");
					payment.setConversionRate(BigDecimal.ONE);
				}
				payment.setAddDate(p.getAdvancePaymentDate().atStartOfDay());
				payments.add(payment);
			}
		}

		for(Invoice invoice : invoiceRepository.all().filter("saleOrder = ?", order.getId()).fetch()) {
			for(InvoicePayment p : invoice.getInvoicePaymentList()) {
				if(p.getStatusSelect() == InvoicePaymentRepository.STATUS_VALIDATED) {
					PrestashopOrderPayment payment = new PrestashopOrderPayment();
					payment.setOrderReference(prestashopReference);
					payment.setCurrencyId(p.getCurrency().getPrestaShopId());
					payment.setAmount(p.getAmount());
					try {
						payment.setConversionRate(currencyService.getCurrencyConversionRate(order.getCurrency(), appConfig.getPrestaShopCurrency(), p.getPaymentDate()));
					} catch (AxelorException e) {
						log.warn("Unable to get payment conversion rate, using 1.0");
						payment.setConversionRate(BigDecimal.ONE);
					}
					payment.setAddDate(p.getPaymentDate().atStartOfDay());
					payments.add(payment);
				}
			}
		}

		return payments;
	}

	/**
	 * Create entities bound to invoicing process (invoices, payments, deliveries).
	 * @param appConfig Prestashop module's configuration
	 * @param ws Webservice instance used for API calls
	 * @param localOrder Local copy of the currently being processed order (fetched
	 * from db)
	 * @param remoteOrder Remote copy of the order (fetched through Prestashop's WS).
	 * Some of its properties will be updated depending on created/updated entities.
	 * @return The id of the invoice bound to the order, or <code>null</code> if no
	 * invoice was generated
	 */
	private Integer generateInvoicingEntities(final AppPrestashop appConfig, final PSWebServiceClient ws, final SaleOrder localOrder, final PrestashopOrder remoteOrder,  final Writer logBuffer) throws PrestaShopWebserviceException, IOException {
		final List<PrestashopOrderPayment> existingPayments = ws.fetch(PrestashopResourceType.ORDER_PAYMENTS, Collections.singletonMap("order_reference", remoteOrder.getReference()));
		PrestashopOrderInvoice remoteInvoice = ws.fetchOne(PrestashopResourceType.ORDER_INVOICES, Collections.singletonMap("id_order", remoteOrder.getId().toString()));
		PrestashopDelivery remoteDelivery = remoteOrder.getDeliveryNumber() != null ? ws.fetchOne(PrestashopResourceType.DELIVERIES, Collections.singletonMap("id", remoteOrder.getDeliveryNumber().toString())) : null;

		if(localOrder.getAmountInvoiced().compareTo(localOrder.getExTaxTotal()) < 0) {
			// Since prestashop partial invoicing/delivery/payment is kind of buggy/unusable,
			// we just put an invoice when the whole order has been invoiced
			for(PrestashopOrderPayment payment : existingPayments) {
				logBuffer.write(String.format(", deleting payment %d (nothing invoiced)", payment.getId()));
				ws.delete(PrestashopResourceType.ORDER_PAYMENTS, payment);
			}
			if(remoteInvoice != null) {
				ws.delete(PrestashopResourceType.ORDER_INVOICES, remoteInvoice);
			}
			if(remoteDelivery != null) {
				ws.delete(PrestashopResourceType.DELIVERIES, remoteDelivery);
			}
			return null;
		}

		if(remoteInvoice == null) {
			remoteInvoice = new PrestashopOrderInvoice();
			remoteInvoice.setOrderId(remoteOrder.getId());
			remoteInvoice.setNote(I18n.get("Invoice created from Axelor"));
			remoteInvoice.setShopAddress(localOrder.getCompany().getName() + "\n" + addressService.computeAddressStr(localOrder.getCompany().getAddress()));
			remoteInvoice.setNumber(ws.getNextInvoiceNumber());
			remoteOrder.setInvoiceNumber(remoteInvoice.getNumber());
		}

		if(remoteInvoice.getTotalPaidTaxExcluded().compareTo(localOrder.getAmountInvoiced()) != 0) {
			log.debug("Adjusting invoiced amount to {}", localOrder.getExTaxTotal());
			remoteInvoice.setTotalPaidTaxIncluded(localOrder.getInTaxTotal().setScale(appConfig.getExportPriceScale(), RoundingMode.HALF_UP));
			remoteInvoice.setTotalPaidTaxExcluded(localOrder.getExTaxTotal().setScale(appConfig.getExportPriceScale(), RoundingMode.HALF_UP));
			remoteInvoice.setTotalProductsTaxExcluded(remoteOrder.getTotalProductsTaxExcluded());
			remoteInvoice.setTotalProductsTaxIncluded(remoteOrder.getTotalProductsTaxIncluded());
			remoteInvoice.setTotalShippingTaxIncluded(remoteOrder.getTotalShippingTaxIncluded());
			remoteInvoice.setTotalShippingTaxExcluded(remoteOrder.getTotalShippingTaxExcluded());
			remoteInvoice = ws.save(PrestashopResourceType.ORDER_INVOICES, remoteInvoice);
			remoteOrder.setInvoiceDate(remoteInvoice.getAddDate());
		}

		final List<PrestashopOrderPayment> neededPayments = getPayments(appConfig, localOrder, remoteOrder.getReference());
		for(ListIterator<PrestashopOrderPayment> it = neededPayments.listIterator() ; it.hasNext() ; ) {
			PrestashopOrderPayment localPayment = it.next();
			for(ListIterator<PrestashopOrderPayment> it2 = existingPayments.listIterator() ; it2.hasNext() ; ) {
				PrestashopOrderPayment remotePayment = it2.next();
				if(remotePayment.getAmount().compareTo(localPayment.getAmount()) == 0 /* We might have checked for addDate too but… PrestaShop ignores it when saving and sets it to now() */) {
					it2.remove();
					it.remove();
					break;
				}
			}
		}
		// So neededPayments now contains payments without remote counterpart, existingPayments contains remote payments with no local counterPart
		for(PrestashopOrderPayment payment : existingPayments) {
			if(log.isDebugEnabled()) {
				log.debug("Deleting extra payment " + payment);
			}
			ws.delete(PrestashopResourceType.ORDER_PAYMENTS, payment);
		}
		for(PrestashopOrderPayment payment : neededPayments) {
			ws.save(PrestashopResourceType.ORDER_PAYMENTS, payment);
		}

		if(localOrder.getDeliveryState() == SaleOrderRepository.STATE_DELIVERED) {
			if(remoteDelivery == null) {
				remoteDelivery = new PrestashopDelivery();
				remoteDelivery.setCarrierId(1); // FIXME Handle a mapping in freight port products?
				remoteDelivery.setShopId(remoteOrder.getShopId());
				remoteDelivery.setShopGroupId(remoteOrder.getShopGroupId());
				remoteDelivery.setPriceRangeId(1);
				remoteDelivery.setWeightRangeId(1);
				if(localOrder.getDeliveryAddress().getAddressL7Country() != null && localOrder.getDeliveryAddress().getAddressL7Country().getPrestaShopZoneId() != null) {
					remoteDelivery.setZoneId(localOrder.getDeliveryAddress().getAddressL7Country().getPrestaShopZoneId());
				} else {
					remoteDelivery.setZoneId(1);
				}

				LocalDate deliveryDate = null;
				for(StockMove move : localOrder.getStockMoveList()) {
					if(deliveryDate == null || (move.getRealDate() != null && deliveryDate.isBefore(move.getRealDate()))) {
						deliveryDate = move.getRealDate();
					}
				}
				if(deliveryDate != null) {
					remoteOrder.setDeliveryDate(deliveryDate.atStartOfDay());
				}
				// TODO set order state to shipped
			}

			// Note that Prestashop provides *no way* to bind a delivery to a payment so
			// delivery sheet will always say "no payment".
			if(remoteDelivery.getId() == null || remoteDelivery.getPrice().compareTo(remoteOrder.getTotalShippingTaxIncluded()) != 0) {
				remoteDelivery.setPrice(remoteOrder.getTotalShippingTaxIncluded());
				remoteDelivery = ws.save(PrestashopResourceType.DELIVERIES, remoteDelivery);
				remoteOrder.setDeliveryNumber(remoteDelivery.getId());
			}
		} else {
			if(remoteDelivery != null) ws.delete(PrestashopResourceType.DELIVERIES, remoteDelivery);
			remoteOrder.setDeliveryNumber(null);
			remoteOrder.setDeliveryDate(null);
		}

		return remoteInvoice.getId();
	}
}
