/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.SupplierCatalog;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.purchase.db.IPurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.apps.sale.db.ISaleOrder;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.stock.db.Location;
import com.axelor.apps.stock.db.LocationLine;
import com.axelor.apps.stock.db.MinStockRules;
import com.axelor.apps.stock.db.repo.LocationLineRepository;
import com.axelor.apps.stock.db.repo.LocationRepository;
import com.axelor.apps.stock.db.repo.MinStockRulesRepository;
import com.axelor.apps.stock.service.MinStockRulesService;
import com.axelor.apps.supplychain.db.Mrp;
import com.axelor.apps.supplychain.db.MrpFamily;
import com.axelor.apps.supplychain.db.MrpForecast;
import com.axelor.apps.supplychain.db.MrpLine;
import com.axelor.apps.supplychain.db.MrpLineOrigin;
import com.axelor.apps.supplychain.db.MrpLineType;
import com.axelor.apps.supplychain.db.repo.MrpForecastRepository;
import com.axelor.apps.supplychain.db.repo.MrpLineRepository;
import com.axelor.apps.supplychain.db.repo.MrpLineTypeRepository;
import com.axelor.apps.supplychain.db.repo.MrpRepository;
import com.axelor.apps.supplychain.exception.IExceptionMessage;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;


public class MrpServiceImpl implements MrpService  {
	
	private final Logger log = LoggerFactory.getLogger( getClass() );
	
	protected MrpRepository mrpRepository;
	protected LocationRepository locationRepository;
	protected ProductRepository productRepository;
	protected LocationLineRepository locationLineRepository;
	protected MrpLineTypeRepository mrpLineTypeRepository;
	protected PurchaseOrderLineRepository purchaseOrderLineRepository;
	protected SaleOrderLineRepository saleOrderLineRepository;
	protected MrpLineRepository mrpLineRepository;
	protected MinStockRulesService minStockRulesService;
	protected MrpLineService mrpLineService;
	protected MrpForecastRepository mrpForecastRepository;
	
	protected LocalDate today;
	
	protected List<Location> locationList = Lists.newArrayList();
	protected Map<Product,Integer> productMap = Maps.newHashMap();
	protected Mrp mrp;
	
	
	@Inject
	public MrpServiceImpl(GeneralService generalService, MrpRepository mrpRepository, LocationRepository locationRepository, 
			ProductRepository productRepository, LocationLineRepository locationLineRepository, MrpLineTypeRepository mrpLineTypeRepository,
			PurchaseOrderLineRepository purchaseOrderLineRepository, SaleOrderLineRepository saleOrderLineRepository, MrpLineRepository mrpLineRepository,
			MinStockRulesService minStockRulesService, MrpLineService mrpLineService, MrpForecastRepository mrpForecastRepository)  {
		
		this.mrpRepository = mrpRepository;
		this.locationRepository = locationRepository;
		this.productRepository = productRepository;
		this.locationLineRepository = locationLineRepository;
		this.mrpLineTypeRepository = mrpLineTypeRepository;
		this.purchaseOrderLineRepository = purchaseOrderLineRepository;
		this.saleOrderLineRepository = saleOrderLineRepository;
		this.mrpLineRepository = mrpLineRepository;
		this.minStockRulesService = minStockRulesService;
		this.mrpLineService = mrpLineService;
		this.mrpForecastRepository = mrpForecastRepository;
		
		this.today = generalService.getTodayDate();
	}
	
	public void runCalculation(Mrp mrp) throws AxelorException  {
		
		this.startMrp(mrpRepository.find(mrp.getId()));
		this.completeMrp(mrpRepository.find(mrp.getId()));
		this.doCalulation(mrpRepository.find(mrp.getId()));
	}
	
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	protected void startMrp(Mrp mrp)  {
		
		log.debug("Start MRP");
		
		mrp.setStatusSelect(MrpRepository.STATUS_CALCULATION_STARTED);
		
		//TODO check that the different types used for purchase/manufOrder proposal are in stock type
		//TODO check that all types exist + override the method on production module
		
		mrp.clearMrpLineList();
		
		mrpRepository.save(mrp);
	}
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void reset(Mrp mrp)  {
		
		mrp.setStatusSelect(MrpRepository.STATUS_DRAFT);
		
		mrp.clearMrpLineList();

		mrpRepository.save(mrp);

	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	protected void completeMrp(Mrp mrp) throws AxelorException  {
		
		log.debug("Complete MRP");
		
		// Initialize 
		this.mrp = mrp;
		this.locationList = this.getAllLocationAndSubLocation(mrp.getLocation());
		this.assignProductAndLevel(this.getProductList());
		
		// Get the stock for each product on each location
		this.createAvailableStockMrpLines();
		
		this.createPurchaseMrpLines();
		
		this.createSaleOrderMrpLines();
		
		this.createSaleForecastMrpLines();
		
		mrpRepository.save(mrp);
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	protected void doCalulation(Mrp mrp) throws AxelorException  {
		
		log.debug("Do calculation");
		
		mrpRepository.save(mrp);
		
		this.checkInsufficientCumulativeQty();
		
//		this.consolidateMrp(mrp);
		
		mrp.setStatusSelect(MrpRepository.STATUS_CALCULATION_ENDED);
		
	}
	
	
	protected void checkInsufficientCumulativeQty() throws AxelorException  {
		
		for(int level = 0; level <= this.getMaxLevel(); level++)  {
			
			for(Product product : this.getProductList(level))  {
				
				this.checkInsufficientCumulativeQty(product);
			
			}
			
		}
		
	}
	
	
	/**
	 * Get the list of product for a level
	 * @param level
	 * @return
	 */
	protected List<Product> getProductList(int level)  {
		
		List<Product> productList = Lists.newArrayList();
		
		for(Product product : this.productMap.keySet())  {
			
			if(this.productMap.get(product) == level)  {
				productList.add(product);
			}
			
		}
		
		return productList;
		
	}
	
	
	protected int getMaxLevel()  {
		
		int maxLevel = 0;
		
		for(int level : this.productMap.values())  {
			
			if(level > maxLevel)  {  maxLevel = level;  }
			
		}

		return maxLevel;
	}
	
	
	protected void checkInsufficientCumulativeQty(Product product) throws AxelorException  {
		
		boolean doASecondPass = false;
		
		this.computeCumulativeQty(product);

		List<MrpLine> mrpLineList = mrpLineRepository.all().filter("self.mrp = ?1 AND self.product = ?2", mrp, product).order("maturityDate").order("mrpLineType.typeSelect").order("mrpLineType.sequence").order("id").fetch();

		for(MrpLine mrpLine : mrpLineList)  {
			
			BigDecimal cumulativeQty = mrpLine.getCumulativeQty();
			
			MrpLineType mrpLineType = mrpLine.getMrpLineType();
			
			boolean isProposalElement = this.isProposalElement(mrpLineType);
			
			BigDecimal minQty = mrpLine.getMinQty();
			
			if(mrpLine.getMrpLineType().getElementSelect() != MrpLineTypeRepository.ELEMENT_AVAILABLE_STOCK 
					&& (!isProposalElement || (isProposalElement && mrpLineType.getTypeSelect() == MrpLineTypeRepository.TYPE_OUT))
					&& cumulativeQty.compareTo(mrpLine.getMinQty()) == -1)  {  
					
				log.debug("Cumulative qty ({} < {}) is insufficient for product ({}) at the maturity date ({})", cumulativeQty, minQty, product.getFullName(), mrpLine.getMaturityDate());
				
				BigDecimal reorderQty = minQty.subtract(cumulativeQty);
				
				MinStockRules minStockRules = minStockRulesService.getMinStockRules(product, mrpLine.getLocation(), MinStockRulesRepository.TYPE_FUTURE);
				
				if(minStockRules != null)  {   reorderQty = reorderQty.max(minStockRules.getReOrderQty());  }
				
				MrpLineType mrpLineTypeProposal = this.getMrpLineTypeForProposal(minStockRules);
				
				this.createProposalMrpLine(product, mrpLineTypeProposal, reorderQty, mrpLine.getLocation(), mrpLine.getMaturityDate(), mrpLine.getMrpLineOriginList(), mrpLine.getRelatedToSelectName());
				
				doASecondPass = true;
				
				break;
			}
		}
			
		if(doASecondPass)  {
			mrpRepository.save(mrp);

			this.checkInsufficientCumulativeQty(product);
		}
		
	}
	
	
	public MrpLine getPreviousProposalMrpLine(Product product, MrpLineType mrpLineType, Location location, LocalDate maturityDate)  {
		
		LocalDate startPeriodDate = maturityDate;
		
		MrpFamily mrpFamily = product.getMrpFamily();
		
		if(mrpFamily != null)  {  
			
			if(mrpFamily.getDayNb() == 0)  {  return null;  }
			
			startPeriodDate = maturityDate.minusDays(mrpFamily.getDayNb());
		
		}
		
		return mrpLineRepository.all().filter("self.mrp = ?1 AND self.product = ?2 AND self.mrpLineType = ?3 AND self.location = ?4 AND self.maturityDate > ?5 AND self.maturityDate <= ?6",
				mrp, product, mrpLineType, location, startPeriodDate, maturityDate).fetchOne();
		
	}
	
	
	protected void createProposalMrpLine(Product product, MrpLineType mrpLineType, BigDecimal reorderQty, Location location, LocalDate maturityDate, List<MrpLineOrigin> mrpLineOriginList, String relatedToSelectName) throws AxelorException {
		
		if(mrpLineType.getElementSelect() == MrpLineTypeRepository.ELEMENT_PURCHASE_PROPOSAL)  {
			maturityDate = maturityDate.minusDays(product.getSupplierDeliveryTime());
			reorderQty = reorderQty.max(this.getSupplierCatalogMinQty(product));
		}
		
		MrpLine mrpLine = this.getPreviousProposalMrpLine(product, mrpLineType, location, maturityDate);
		
		if(mrpLine != null)  {
			mrpLine.setQty(mrpLine.getQty().add(reorderQty));
			mrpLine.setRelatedToSelectName(null);

		}
		else  {
			
			mrpLine = mrpLineRepository.save(this.createMrpLine(product, mrpLineType, reorderQty, maturityDate, BigDecimal.ZERO, location));
			mrp.addMrpLineListItem(mrpLine);
			mrpLine.setRelatedToSelectName(relatedToSelectName);

		}
		
		this.copyMrpLineOrigins(mrpLine, mrpLineOriginList);

	}
	
	protected BigDecimal getSupplierCatalogMinQty(Product product)  {
		
		Partner supplierPartner = product.getDefaultSupplierPartner();
		
		if(supplierPartner != null)  {
		
			for(SupplierCatalog supplierCatalog : product.getSupplierCatalogList())  {
				
				if(supplierCatalog.getSupplierPartner().equals(supplierPartner))  {
					return supplierCatalog.getMinQty();
				}
			}
		}
		return BigDecimal.ZERO;
	}
	
	
	protected MrpLineType getMrpLineTypeForProposal(MinStockRules minStockRules) throws AxelorException  {
		
		return this.getMrpLineType(MrpLineTypeRepository.ELEMENT_PURCHASE_PROPOSAL);
		
	}
	
	
	protected void consolidateMrp()  {
		
		List<MrpLine> mrpLineList = mrpLineRepository.all().filter("self.mrp = ?1", mrp).order("self.product.code").order("maturityDate").order("mrpLineType.typeSelect").order("mrpLineType.sequence").order("id").fetch();

		Map<List<Object>, MrpLine> map = Maps.newHashMap();
		MrpLine consolidateMrpLine = null;
		List<Object> keys = new ArrayList<Object>();

		for (MrpLine mrpLine : mrpLineList){

			MrpLineType mrpLineType = mrpLine.getMrpLineType();
			
			keys.clear();
			keys.add(mrpLineType);
			keys.add(mrpLine.getProduct());
			keys.add(mrpLine.getMaturityDate());
			keys.add(mrpLine.getLocation());

			if (map.containsKey(keys))  {

				consolidateMrpLine = map.get(keys);
				consolidateMrpLine.setQty(consolidateMrpLine.getQty().add(mrpLine.getQty()));
				consolidateMrpLine.setCumulativeQty(consolidateMrpLine.getCumulativeQty().add(mrpLine.getCumulativeQty()));

			}
			else {
				map.put(keys, mrpLine);
			}

		}
		
		mrp.getMrpLineList().clear();
		
		mrp.getMrpLineList().addAll(map.values());
		
	}
	
	
	protected boolean isProposalElement(MrpLineType mrpLineType)  {
		
		if(mrpLineType.getElementSelect() == MrpLineTypeRepository.ELEMENT_PURCHASE_PROPOSAL)  {
		
			return true;
			
		}
		
		return false;
	}
	
	
	protected void computeCumulativeQty()  {
		
		for(Product product : this.productMap.keySet())  {
		
			this.computeCumulativeQty(product);
			
		}
		
	}
	
	
	protected void computeCumulativeQty(Product product)  {
		
		List<MrpLine> mrpLineList = mrpLineRepository.all().filter("self.mrp = ?1 AND self.product = ?2", mrp, product).order("maturityDate").order("mrpLineType.typeSelect").order("mrpLineType.sequence").order("id").fetch();

		BigDecimal previousCumulativeQty = BigDecimal.ZERO;
		
		for(MrpLine mrpLine : mrpLineList)  {
			
			if(mrpLine.getMrpLineType().getElementSelect() == MrpLineTypeRepository.ELEMENT_AVAILABLE_STOCK)  {
				
				mrpLine.setCumulativeQty(mrpLine.getQty());
			}
			else  {
				
				mrpLine.setCumulativeQty(previousCumulativeQty.add(mrpLine.getQty()));
				
			}
			
			previousCumulativeQty = mrpLine.getCumulativeQty();
			
			log.debug("Cumulative qty is ({}) for product ({}) and move ({}) at the maturity date ({})", 
					previousCumulativeQty, mrpLine.getProduct().getFullName(), mrpLine.getMrpLineType().getName(), mrpLine.getMaturityDate());
			
		}
		
	}
	
	
	// achat ferme
	protected void createPurchaseMrpLines() throws AxelorException  {
		
		MrpLineType purchaseProposalMrpLineType = this.getMrpLineType(MrpLineTypeRepository.ELEMENT_PURCHASE_ORDER);
		
		// TODO : Manage the case where order is partially delivered
		List<PurchaseOrderLine> purchaseOrderLineList = purchaseOrderLineRepository.all()
				.filter("self.product in (?1) AND self.purchaseOrder.location in (?2) AND self.purchaseOrder.receiptState = ?3 "
						+ "AND self.purchaseOrder.statusSelect = ?4", 
						this.productMap.keySet(), this.locationList, IPurchaseOrder.STATE_NOT_RECEIVED, IPurchaseOrder.STATUS_VALIDATED).fetch();
		
		for(PurchaseOrderLine purchaseOrderLine : purchaseOrderLineList)  {
			
			PurchaseOrder purchaseOrder = purchaseOrderLine.getPurchaseOrder();
			
			LocalDate maturityDate = purchaseOrderLine.getEstimatedDelivDate();
			
			if(maturityDate == null)  {  maturityDate = purchaseOrder.getDeliveryDate();  }
			if(maturityDate == null)  {  maturityDate = purchaseOrder.getOrderDate();  }
			
			if(this.isBeforeEndDate(maturityDate))  {
				Unit unit = purchaseOrderLine.getProduct().getUnit();
				BigDecimal qty = purchaseOrderLine.getQty();
				if(!unit.equals(purchaseOrderLine.getUnit())){
					qty = Beans.get(UnitConversionService.class).convertWithProduct(purchaseOrderLine.getUnit(), unit, qty, purchaseOrderLine.getProduct());
				}
				mrp.addMrpLineListItem(this.createMrpLine(purchaseOrderLine.getProduct(), purchaseProposalMrpLineType, qty, maturityDate, BigDecimal.ZERO, purchaseOrder.getLocation(), purchaseOrderLine));
			}
		}
	}
	
	
	// Vente ferme
	protected void createSaleOrderMrpLines() throws AxelorException  {
		
		MrpLineType saleForecastMrpLineType = this.getMrpLineType(MrpLineTypeRepository.ELEMENT_SALE_ORDER);
		
		// TODO : Manage the case where order is partially delivered
		List<SaleOrderLine> saleOrderLineList = new ArrayList<>();
		
		if(mrp.getSaleOrderLineSet().isEmpty())  {
			
			saleOrderLineList.addAll(saleOrderLineRepository.all()
				.filter("self.product in (?1) AND self.saleOrder.location in (?2) AND self.saleOrder.deliveryState = ?3 "
						+ "AND self.saleOrder.statusSelect = ?4", 
						this.productMap.keySet(), this.locationList, SaleOrderRepository.STATE_NOT_DELIVERED, ISaleOrder.STATUS_ORDER_CONFIRMED).fetch());
			
		}
		else  {
			
			saleOrderLineList.addAll(mrp.getSaleOrderLineSet());
			
		}
		
		for(SaleOrderLine saleOrderLine : saleOrderLineList)  {
			
			SaleOrder saleOrder = saleOrderLine.getSaleOrder();
			
			LocalDate maturityDate = saleOrderLine.getEstimatedDelivDate();
			
			if(maturityDate == null)  {  maturityDate = saleOrder.getDeliveryDate();  }
			if(maturityDate == null)  {  maturityDate = saleOrder.getOrderDate();  }
			if(maturityDate == null)  {  maturityDate = saleOrder.getCreationDate();  }
			
			if(this.isBeforeEndDate(maturityDate))  {
				Unit unit = saleOrderLine.getProduct().getUnit();
				BigDecimal qty = saleOrderLine.getQty();
				if(!unit.equals(saleOrderLine.getUnit())){
					qty = Beans.get(UnitConversionService.class).convertWithProduct(saleOrderLine.getUnit(), unit, qty, saleOrderLine.getProduct());
				}
				mrp.addMrpLineListItem(this.createMrpLine(saleOrderLine.getProduct(), saleForecastMrpLineType, qty, maturityDate, BigDecimal.ZERO, saleOrder.getLocation(), saleOrderLine));
			}
			
		}
	}
	
	protected void createSaleForecastMrpLines() throws AxelorException  {
		
		MrpLineType saleForecastMrpLineType = this.getMrpLineType(MrpLineTypeRepository.ELEMENT_SALE_FORECAST);
		
		List<MrpForecast> mrpForecastList = new ArrayList<>();
		
		if(mrp.getMrpForecastSet().isEmpty())  {
			
			mrpForecastList.addAll(mrpForecastRepository.all()
					.filter("self.product in (?1) AND self.location in (?2) AND self.forecastDate >= ?3", 
							this.productMap.keySet(), this.locationList, today, today).fetch());
			
		}
		else  {
			mrpForecastList.addAll(mrp.getMrpForecastSet());
		}
		
		for(MrpForecast mrpForecast : mrpForecastList)  {
			
			LocalDate maturityDate = mrpForecast.getForecastDate();
			
			if(this.isBeforeEndDate(maturityDate))  {
				Unit unit = mrpForecast.getProduct().getUnit();
				BigDecimal qty = mrpForecast.getQty();
				if(!unit.equals(mrpForecast.getUnit())){
					qty = Beans.get(UnitConversionService.class).convertWithProduct(mrpForecast.getUnit(), unit, qty, mrpForecast.getProduct());
				}
				mrp.addMrpLineListItem(
						this.createMrpLine(mrpForecast.getProduct(), saleForecastMrpLineType, qty, maturityDate, BigDecimal.ZERO, mrpForecast.getLocation(), mrpForecast));
			}
		}
	}
	
	
	public boolean isBeforeEndDate(LocalDate maturityDate)  {
		
		if(maturityDate != null && !maturityDate.isBefore(today) && (mrp.getEndDate() == null || !maturityDate.isAfter(mrp.getEndDate())))  {
			
			return true;
		}
		
		return false;
	}
	
	
	protected void createAvailableStockMrpLines() throws AxelorException  {
		
		MrpLineType availableStockMrpLineType = this.getMrpLineType(MrpLineTypeRepository.ELEMENT_AVAILABLE_STOCK);
		
		for(Product product : this.productMap.keySet())  {
			
			for(Location location : this.locationList)  {
				
				mrp.addMrpLineListItem(this.createAvailableStockMrpLine(product, location, availableStockMrpLineType));
				
			}
		}
	}
	
	protected MrpLine createAvailableStockMrpLine(Product product, Location location, MrpLineType availableStockMrpLineType)  {
		
		BigDecimal qty = BigDecimal.ZERO;
		
		LocationLine locationLine = this.getLocationLine(product, location);
		
		if(locationLine != null)  {
			
			qty = locationLine.getCurrentQty();
		}
		
		return this.createMrpLine(product, availableStockMrpLineType, qty, today, qty, location);
	}
	
	
	protected MrpLineType getMrpLineType(int elementSelect) throws AxelorException  {
		
		MrpLineType mrpLineType =  mrpLineTypeRepository.all().filter("self.elementSelect = ?1", elementSelect).fetchOne();
		
		if(mrpLineType != null)  {
			
			return mrpLineType;
		}
		

		throw new AxelorException(
				String.format(I18n.get(IExceptionMessage.MRP_MISSING_MRP_LINE_TYPE), elementSelect),
				IException.CONFIGURATION_ERROR);
		
		//TODO get the right label in fact of integer value
	
	}
	
	protected LocationLine getLocationLine(Product product, Location location)  {

		return locationLineRepository.all().filter("self.location = ?1 AND self.product = ?2", location, product).fetchOne();
		
	}
		

	protected Set<Product> getProductList() throws AxelorException  {
		
		Set<Product> productSet = Sets.newHashSet();
		
		if(!mrp.getProductSet().isEmpty())  {
			
			productSet.addAll(mrp.getProductSet());
			
		}
		
		if(!mrp.getProductCategorySet().isEmpty())  {
			
			productSet.addAll(productRepository.all().filter("self.productCategory in (?1) AND self.productTypeSelect = ?2 AND self.excludeFromMrp = false", 
					mrp.getProductCategorySet(), ProductRepository.PRODUCT_TYPE_STORABLE).fetch());
		
		}
		
		if(!mrp.getProductFamilySet().isEmpty())  {
			
			productSet.addAll(productRepository.all().filter("self.productFamily in (?1) AND self.productTypeSelect = ?2 AND self.excludeFromMrp = false", 
					mrp.getProductFamilySet(), ProductRepository.PRODUCT_TYPE_STORABLE).fetch());
			
		}
		
			
		for(SaleOrderLine saleOrderLine : mrp.getSaleOrderLineSet())  {
			
			productSet.add(saleOrderLine.getProduct());
		}
		
		for(MrpForecast mrpForecast : mrp.getMrpForecastSet())  {
				
			productSet.add(mrpForecast.getProduct());

		}

		if(productSet.isEmpty())  {
			throw new AxelorException(
					String.format(I18n.get(IExceptionMessage.MRP_NO_PRODUCT)),
					IException.CONFIGURATION_ERROR);
		}
		
		return productSet;
		
	}
	
	public boolean isMrpProduct(Product product)  {
		
		if(product != null && !product.getExcludeFromMrp() && product.getProductTypeSelect().equals(ProductRepository.PRODUCT_TYPE_STORABLE))  {
			
			return true;
		}
		
		return false;
		
	}
	
	
	protected void assignProductAndLevel(Set<Product> productList)  {
		
		for(Product product : productList)  {
			
			this.assignProductAndLevel(product);
			
		}
		
	}
	
	
	protected void assignProductAndLevel(Product product)  {
		
		log.debug("Add of the product : {}", product.getFullName());
		this.productMap.put(product, 0);
		
	}
	
	
	protected MrpLine createMrpLine(Product product,  MrpLineType mrpLineType, BigDecimal qty, LocalDate maturityDate, BigDecimal cumulativeQty, Location location, Model... models)  {
		
		return mrpLineService.createMrpLine(product, this.productMap.get(product), mrpLineType, qty, maturityDate, cumulativeQty, location, models);
		
	}
	
	
	protected void copyMrpLineOrigins(MrpLine mrpLine, List<MrpLineOrigin> mrpLineOriginList)  {
		
		if(mrpLineOriginList != null)  {
			
			for(MrpLineOrigin mrpLineOrigin : mrpLineOriginList)  {
				
				mrpLine.addMrpLineOriginListItem(mrpLineService.copyMrpLineOrigin(mrpLineOrigin));

			}
			
		}
		
	}
	
	
	protected List<Location> getAllLocationAndSubLocation(Location location)  {
	
		List<Location> subLocationList =  locationRepository.all().filter("self.parent = ?1", location).fetch();
	
		for(Location subLocation : subLocationList)  {
			
			subLocationList.addAll(this.getAllLocationAndSubLocation(subLocation));
		
		}
		
		subLocationList.add(location);
		
		return subLocationList;
	}	
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void generateProposals(Mrp mrp) throws AxelorException  {
		
		for(MrpLine mrpLine : mrp.getMrpLineList())  {
			
			mrpLineService.generateProposal(mrpLine);
			
		}
		
		
		
	}

	
	
}





