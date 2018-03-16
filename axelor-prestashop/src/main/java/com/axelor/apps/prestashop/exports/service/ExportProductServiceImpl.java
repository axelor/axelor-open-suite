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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.AppPrestashop;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.prestashop.entities.Associations.AvailableStocksAssociationElement;
import com.axelor.apps.prestashop.entities.Associations.AvailableStocksAssociationsEntry;
import com.axelor.apps.prestashop.entities.Associations.CategoriesAssociationElement;
import com.axelor.apps.prestashop.entities.PrestashopAvailableStock;
import com.axelor.apps.prestashop.entities.PrestashopImage;
import com.axelor.apps.prestashop.entities.PrestashopProduct;
import com.axelor.apps.prestashop.entities.PrestashopProductCategory;
import com.axelor.apps.prestashop.entities.PrestashopResourceType;
import com.axelor.apps.prestashop.entities.PrestashopTranslatableString;
import com.axelor.apps.prestashop.service.library.PSWebServiceClient;
import com.axelor.apps.prestashop.service.library.PrestaShopWebserviceException;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.meta.MetaFiles;
import com.google.common.base.Objects;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;

@Singleton
public class ExportProductServiceImpl implements ExportProductService {
	private Logger log = LoggerFactory.getLogger(getClass());

	private ProductRepository productRepo;
	private UnitConversionService unitConversionService;
	private CurrencyService currencyService;

	@Inject
	public ExportProductServiceImpl(ProductRepository productRepo, UnitConversionService unitConversionService, CurrencyService currencyService) {
		this.productRepo = productRepo;
		this.unitConversionService = unitConversionService;
		this.currencyService = currencyService;
	}

	@Override
	@Transactional
	public void exportProduct(AppPrestashop appConfig, ZonedDateTime endDate, Writer logBuffer)
			throws IOException, PrestaShopWebserviceException {
		int done = 0;
		int errors = 0;

		logBuffer.write(String.format("%n====== PRODUCTS ======%n"));

		if(appConfig.getPrestaShopLengthUnit() == null || appConfig.getPrestaShopWeightUnit() == null) {
			logBuffer.write(String.format("[ERROR] Prestashop module isn't fully configured%n"));
			return;
		}

		final List<Object> params = new ArrayList<>(2);
		final StringBuilder filter = new StringBuilder("1 = 1");
		if(endDate != null) {
			filter.append(" AND (self.createdOn > ?1 OR self.updatedOn > ?2 OR self.prestaShopId = null)");
			params.add(endDate);
			params.add(endDate);
		}
		if(appConfig.getExportNonSoldProducts() == Boolean.FALSE) {
			filter.append(" AND (self.sellable = true)");
		}

		final PSWebServiceClient ws = new PSWebServiceClient(appConfig.getPrestaShopUrl(), appConfig.getPrestaShopKey());

		final PrestashopProduct defaultProduct = ws.fetchDefault(PrestashopResourceType.PRODUCTS);
		final PrestashopProductCategory remoteRootCategory = ws.fetchOne(PrestashopResourceType.PRODUCT_CATEGORIES, Collections.singletonMap("is_root_category", "1"));

		final List<PrestashopProduct> remoteProducts = ws.fetchAll(PrestashopResourceType.PRODUCTS);
		final Map<Integer, PrestashopProduct> productsById = new HashMap<>();
		final Map<String, PrestashopProduct> productsByReference = new HashMap<>();
		for(PrestashopProduct p : remoteProducts) {
			productsById.put(p.getId(), p);
			productsByReference.put(p.getReference(), p);
		}
		final int language = (appConfig.getTextsLanguage().getPrestaShopId() == null ? 1 : appConfig.getTextsLanguage().getPrestaShopId());

		final LocalDate today = LocalDate.now();

		for (Product localProduct : productRepo.all().filter(filter.toString(), params.toArray()).fetch()) {
			try {
				final String cleanedReference = localProduct.getCode().replaceAll("[<>;={}]", ""); // took from Prestashop's ValidateCore::isReference
				logBuffer.write(String.format("Exporting product %s (%s/%s) – ", localProduct.getName(), localProduct.getCode(), cleanedReference));

				if(localProduct.getParentProduct() != null) {
					logBuffer.write(String.format("[ERROR] Product is a variant, these are not handled right now, skipping%n"));
					continue;
				} else if(localProduct.getProductVariantConfig() != null) {
					logBuffer.write(String.format("[ERROR] Product has variants, which are not handled right now, skipping%n"));
					continue;
				} else if(localProduct.getIsPack() == Boolean.TRUE) {
					// FIXME fairly easy to fix through product_bundle association + set type to pack
					logBuffer.write(String.format("[ERROR] Product is a pack, these are not handled right now, skipping%n"));
					continue;
				}

				PrestashopProduct remoteProduct;
				if(localProduct.getPrestaShopId() != null) {
					logBuffer.write("prestashop id=" + localProduct.getPrestaShopId());
					remoteProduct = productsById.get(localProduct.getPrestaShopId());
					if(remoteProduct == null) {
						logBuffer.write(String.format(" [ERROR] Not found remotely%n"));
						log.error("Unable to fetch remote product #{} ({}), something's probably very wrong, skipping",
								localProduct.getPrestaShopId(), localProduct.getCode());
						++errors;
						continue;
					} else if(cleanedReference.equals(remoteProduct.getReference()) == false) {
						log.error("Remote product #{} has not the same reference as the local one ({} vs {}), skipping",
								localProduct.getPrestaShopId(), remoteProduct.getReference(), cleanedReference);
						logBuffer.write(String.format(" [ERROR] reference mismatch: %s vs %s%n", remoteProduct.getReference(), cleanedReference));
						++errors;
						continue;
					}
				} else {
					remoteProduct = productsByReference.get(cleanedReference);
					if(remoteProduct == null) {
						logBuffer.write("no ID and reference not found, creating");
						remoteProduct = new PrestashopProduct();
						remoteProduct.setReference(cleanedReference);
						PrestashopTranslatableString str = defaultProduct.getName().clone();
						str.clearTranslations(localProduct.getName());
						remoteProduct.setName(str);

						str = defaultProduct.getDescription().clone();
						str.clearTranslations(localProduct.getDescription());
						remoteProduct.setDescription(str);

						str = defaultProduct.getLinkRewrite().clone();
						// Normalization taken from PrestaShop's JavaScript str2url function
						str.clearTranslations(Normalizer.normalize(
								String.format("%s-%s", localProduct.getCode(), localProduct.getName()), Normalizer.Form.NFKD)
								.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
								.toLowerCase()
								.replaceAll("[^a-z0-9\\s\\'\\:/\\[\\]\\-]", "")
								.replaceAll("[\\s\\'\\:/\\[\\]-]+", " ")
								.replaceAll(" ", "-"));
						// TODO Should we update when product name changes?
						remoteProduct.setLinkRewrite(str);
						remoteProduct.setPositionInCategory(0);
					} else {
						logBuffer.write(String.format("found remotely using its reference %s", cleanedReference));
					}
				}

				if(remoteProduct.getId() == null || appConfig.getPrestaShopMasterForProducts() == Boolean.FALSE) {
					// Here comes the real fun…
					if(localProduct.getProductCategory() != null && localProduct.getProductCategory().getPrestaShopId() != null) {
						remoteProduct.setDefaultCategoryId(localProduct.getProductCategory().getPrestaShopId());
					} else {
						remoteProduct.setDefaultCategoryId(remoteRootCategory.getId());
					}

					final int defaultCategoryId = remoteProduct.getDefaultCategoryId();
					if(remoteProduct.getAssociations().getCategories().getAssociations().stream().anyMatch(c -> c.getId() == defaultCategoryId) == false) {
						CategoriesAssociationElement e = new CategoriesAssociationElement();
						e.setId(defaultCategoryId);
						remoteProduct.getAssociations().getCategories().getAssociations().add(e);
					}

					if(localProduct.getSalePrice() != null) {
						if(localProduct.getSaleCurrency() != null) {
							try {
								remoteProduct.setPrice(currencyService.getAmountCurrencyConvertedAtDate(
										localProduct.getSaleCurrency(),
										appConfig.getPrestaShopCurrency(),
										localProduct.getSalePrice(),
										today).setScale(appConfig.getExportPriceScale(), BigDecimal.ROUND_HALF_UP));
							} catch(AxelorException e) {
								logBuffer.write(" [WARNING] Unable to convert sale price, check your currency convsersion rates");
							}
						} else {
							remoteProduct.setPrice(localProduct.getSalePrice().setScale(appConfig.getExportPriceScale(), BigDecimal.ROUND_HALF_UP));
						}
					}
					if(localProduct.getPurchasePrice() != null) {
						if(localProduct.getPurchaseCurrency() != null) {
							try {
								remoteProduct.setWholesalePrice(currencyService.getAmountCurrencyConvertedAtDate(
										localProduct.getPurchaseCurrency(),
										appConfig.getPrestaShopCurrency(),
										localProduct.getPurchasePrice(),
										today).setScale(appConfig.getExportPriceScale(), BigDecimal.ROUND_HALF_UP));
							} catch(AxelorException e) {
								logBuffer.write(" [WARNING] Unable to convert purchase price, check your currency convsersion rates");
							}
						} else {
							remoteProduct.setWholesalePrice(localProduct.getPurchasePrice().setScale(appConfig.getExportPriceScale(), BigDecimal.ROUND_HALF_UP));
						}
					}
					if(localProduct.getLengthUnit() != null) {
						remoteProduct.setWidth(convert(appConfig.getPrestaShopLengthUnit(), localProduct.getLengthUnit(), localProduct.getWidth()));
						remoteProduct.setHeight(convert(appConfig.getPrestaShopLengthUnit(), localProduct.getLengthUnit(), localProduct.getHeight()));
						remoteProduct.setDepth(convert(appConfig.getPrestaShopLengthUnit(), localProduct.getLengthUnit(), localProduct.getLength()));
					} else {
						// assume homogeneous units
						remoteProduct.setWidth(localProduct.getWidth());
						remoteProduct.setHeight(localProduct.getHeight());
						remoteProduct.setDepth(localProduct.getLength());
					}
					BigDecimal weight = localProduct.getGrossWeight() == null ? localProduct.getNetWeight() : localProduct.getGrossWeight();
					if(localProduct.getWeightUnit() != null) {
						remoteProduct.setWeight(unitConversionService.convert(appConfig.getPrestaShopWeightUnit(), localProduct.getWeightUnit(), weight));
					} else {
						remoteProduct.setWeight(weight);
					}

					// FIXME handle language correctly, only override value for appConfig.textsLanguage
					remoteProduct.getName().setTranslation(language, localProduct.getName());
					remoteProduct.getDescription().setTranslation(language, localProduct.getDescription());
					remoteProduct.setEan13(localProduct.getEan13());
					if(localProduct.getSalesUnit() != null) {
						remoteProduct.setUnity(localProduct.getSalesUnit().getLabelToPrinting());
					} else if(localProduct.getUnit() != null) {
						remoteProduct.setUnity(localProduct.getUnit().getLabelToPrinting());
					}
					remoteProduct.setVirtual(ProductRepository.PRODUCT_TYPE_SERVICE.equals(localProduct.getProductTypeSelect()));
					// TODO Should we handle supplier?

					remoteProduct.setUpdateDate(LocalDateTime.now());
					remoteProduct = ws.save(PrestashopResourceType.PRODUCTS, remoteProduct);

					if((remoteProduct.getDefaultImageId() == null || remoteProduct.getDefaultImageId() == 0) && localProduct.getPicture() != null) {
						logBuffer.write(" – no image stored, adding a new one");
						try(InputStream is = new FileInputStream(MetaFiles.getPath(localProduct.getPicture()).toFile())) {
							PrestashopImage image = ws.addImage(PrestashopResourceType.PRODUCTS, remoteProduct, is);
							remoteProduct.setDefaultImageId(image.getId());
						}
					}

					// FIXME we should have a specific batch for this
					AvailableStocksAssociationsEntry availableStocks = remoteProduct.getAssociations().getAvailableStocks();
					if(availableStocks == null || availableStocks.getStock().size() == 0) {
						logBuffer.write(" [WARNING] No stock for this product, skipping stock update");
					} else if(availableStocks.getStock().size() > 1 || Objects.equal(availableStocks.getStock().get(0).getProductAttributeId(), 0) == false) {
						logBuffer.write(" [WARNING] Remote product appears to have variants, skipping");
					} else {
						AvailableStocksAssociationElement availableStockRef = availableStocks.getStock().get(0);
						PrestashopAvailableStock availableStock = ws.fetch(PrestashopResourceType.STOCK_AVAILABLES, availableStockRef.getId());
						if(availableStock.isDependsOnStock()) {
							logBuffer.write(" [WARNING] Remote product uses advanced stock management features, not updating stock");
						} else {
							BigDecimal currentStock =  (BigDecimal)JPA.em().createQuery(
									"SELECT SUM(line.realQty) " +
									"FROM StockMoveLine line " +
									"JOIN line.stockMove move " +
									"JOIN move.fromStockLocation fromLocation " +
									"JOIN move.toStockLocation toLocation " +
									"WHERE move.statusSelect = 3 AND " +
									"(fromLocation.typeSelect = 1 or toLocation.typeSelect = 1) and line.product = :product")
							.setParameter("product", localProduct)
							.getSingleResult();
							if(currentStock == null) currentStock = BigDecimal.ZERO;
							availableStock.setQuantity(currentStock.intValue());
							ws.save(PrestashopResourceType.STOCK_AVAILABLES, availableStock);
						}
					}

					localProduct.setPrestaShopId(remoteProduct.getId());
				} else {
					logBuffer.write("remote product exists and PrestaShop is master for products, leaving untouched");
				}
				logBuffer.write(String.format(" [SUCCESS]%n"));
				++done;
			} catch (AxelorException | PrestaShopWebserviceException e) {
				logBuffer.write(String.format(" [ERROR] %s (full trace is in application logs)%n", e.getLocalizedMessage()));
				log.error(String.format("Exception while synchronizing product #%d (%s)", localProduct.getId(), localProduct.getName()), e);
				++errors;
			}
		}

		logBuffer.write(String.format("%n=== END OF PRODUCTS EXPORT, done: %d, errors: %d ===%n", done, errors));
	}

	// Null-safe version of UnitConversionService::Convert (feel free to integrate to base method).
	private BigDecimal convert(Unit from, Unit to, BigDecimal value) throws AxelorException {
		if(value == null) return null;
		return unitConversionService.convert(from, to, value);
	}
}
