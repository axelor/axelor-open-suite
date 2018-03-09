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
package com.axelor.apps.prestashop.app;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.axelor.apps.base.db.AppPrestashop;
import com.axelor.apps.prestashop.entities.Prestashop;
import com.axelor.apps.prestashop.entities.PrestashopResourceType;
import com.axelor.apps.prestashop.entities.xlink.ApiContainer;
import com.axelor.apps.prestashop.entities.xlink.XlinkEntry;
import com.axelor.apps.prestashop.service.library.PSWebServiceClient;
import com.axelor.apps.prestashop.service.library.PSWebServiceClient.Options;
import com.axelor.i18n.I18n;
import com.google.common.collect.Sets;

import groovy.xml.XmlUtil;

public class AppPrestaShopServiceImpl implements AppPrestaShopService {
	// Static list of required xlinks, this could be made dynamic and built
	// from module configuration.
	private static final HashSet<PrestashopResourceType> REQUIRED_XLINKS = Sets.newHashSet(
			PrestashopResourceType.ADDRESSES,
			PrestashopResourceType.CARTS,
			PrestashopResourceType.PRODUCT_CATEGORIES,
			PrestashopResourceType.COUNTRIES,
			PrestashopResourceType.CURRENCIES,
			PrestashopResourceType.CUSTOMERS,
			PrestashopResourceType.DELIVERIES,
			PrestashopResourceType.IMAGES,
			PrestashopResourceType.LANGUAGES,
			PrestashopResourceType.ORDER_DETAILS,
			PrestashopResourceType.ORDER_HISTORIES,
			PrestashopResourceType.ORDER_INVOICES,
			PrestashopResourceType.ORDER_PAYMENTS,
			PrestashopResourceType.ORDERS,
			PrestashopResourceType.PRODUCTS,
			PrestashopResourceType.STOCK_AVAILABLES
	);

	private Logger logger = LoggerFactory.getLogger(getClass());
	/**
	 * Check connection with prestashop
	 *
	 * @param appConfig object of AppPrestashop contains configuration details
	 * @return true or false
	 */
	@Override
	public void checkAccess(AppPrestashop appConfig, final List<String> errors, final List<String> warnings, final List<String> info) {
		try {
			if(validateUrl(appConfig) == false) {
				errors.add(I18n.get("URL is invalid, it should not be empty nor contain the trailing slash"));
				return;
			}
			PSWebServiceClient ws = new PSWebServiceClient(appConfig.getPrestaShopUrl() + "/api", appConfig.getPrestaShopKey());

			Options options = new Options();
			options.setFullUrl(appConfig.getPrestaShopUrl() + "/api");

			// FIXME Should be moved to PSWSC instead of blindly translating php to Java
			final Document doc = ws.get(options);
			Prestashop envelop = (Prestashop) JAXBContext.newInstance("com.axelor.apps.prestashop.entities:com.axelor.apps.prestashop.entities.xlink")
					.createUnmarshaller()
					.unmarshal(doc);
			if(envelop == null) {
				errors.add(I18n.get("Server returned an unparseable response, see server logs for details"));
				logger.warn("Unparseable response from server while trying to check Prestashop's access rights: " + XmlUtil.serialize(doc.getDocumentElement()));
				return;
			}

			if((envelop.getContent() instanceof ApiContainer) == false) {
				errors.add(I18n.get("Server returned an invalid response, see server logs for details"));
				logger.warn("Invalid response from server while trying to check Prestashop's access rights: " + XmlUtil.serialize(doc.getDocumentElement()));
				return;
			}

			ApiContainer api = envelop.getContent();

			@SuppressWarnings("unchecked")
			Set<PrestashopResourceType> requiredEntries = (HashSet<PrestashopResourceType>)REQUIRED_XLINKS.clone();
			for(XlinkEntry entry : api.getXlinkEntries()) {
				if(requiredEntries.remove(entry.getEntryType())) {
					if(entry.isRead() == false) {
						errors.add(String.format(I18n.get("GET permission is missing for entity %s, related entities cannot be read"), entry.getEntryType().getLabel()));
					}
					if(entry.isCreate() == false) {
						warnings.add(String.format(I18n.get("POST permission is missing for entity %s, related entities won't be created"), entry.getEntryType().getLabel()));
					}
					if(entry.isUpdate() == false) {
						warnings.add(String.format(I18n.get("PUT permission is missing for entity %s, related entities won't be updated"), entry.getEntryType().getLabel()));
					}
					if(entry.getEntryType() == PrestashopResourceType.ORDER_DETAILS && entry.isDelete() == false) {
						warnings.add(I18n.get("DELETE permission is missing for entity order_details, orders wont be correctly updated"));
					}
				} else {
					info.add(String.format(I18n.get("Extra permission for %s is uneeded"), entry.getEntryType().getLabel()));
				}
			}
			if(requiredEntries.size() != 0) {
				StringBuilder sb = new StringBuilder();
				for(PrestashopResourceType t : requiredEntries) {
					if(sb.length() > 0) sb.append(", ");
					sb.append(t.getLabel());
				}
				errors.add(String.format(I18n.get("Missing access rights for entity %s", "Missing access rights for entities %s", requiredEntries.size()), sb.toString()));
			}
			for(Element e : api.getUnknownEntries()) {
				info.add(String.format(I18n.get("Extra permission for %s is uneeded"), e.getNodeName()));
			}

		} catch (Exception e) {
			errors.add(I18n.get("An error occured while checking Prestashop access rights, see server logs for details"));
			logger.error("An error occured while checking Prestashop access rights", e);
		}
	}

	@Override
	public boolean validateUrl(AppPrestashop ps) {

		String url = null;
		url = ps.getPrestaShopUrl();

		return StringUtils.isNotBlank(url) && url.endsWith("/") == false;
	}

}
