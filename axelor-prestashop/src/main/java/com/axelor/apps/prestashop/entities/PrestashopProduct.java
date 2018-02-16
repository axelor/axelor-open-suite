package com.axelor.apps.prestashop.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.w3c.dom.Element;

import com.axelor.apps.prestashop.entities.Associations.CategoriesAssociationsEntry;

@XmlRootElement(name="product")
public class PrestashopProduct extends PrestashopIdentifiableEntity {
	private static List<String> READONLY_ATTRIBUTES = Arrays.asList(
			"manufacturer_name",
			"quantity"
	);
	private Integer manufacturerId;
	private Integer supplierId;
	private Integer defaultCategoryId;
	private boolean newProduct;
	private boolean cacheDefaultAttribute;
	private Integer defaultImageId;
	private Integer defaultCombinationId;
	private Integer taxRulesGroupId;
	private Integer positionInCategory;
	private String type; // simple|pack|virtual
	private Integer defaultShopId;
	private String reference;
	private String supplierReference;
	private String location;
	private BigDecimal width;
	private BigDecimal height;
	private BigDecimal depth;
	private BigDecimal weight;
	private boolean quantityDiscount;
	private String ean13;
	private String isbn;
	private String upc;
	private boolean cacheIsPack;
	private boolean cacheHasAttachments;
	private boolean virtual;
	private boolean saved = true;
	private boolean onSale;
	private boolean onlineOnly;
	private BigDecimal ecotax;
	private Integer minimalQuantity;
	private BigDecimal price;
	private BigDecimal wholesalePrice;
	private String unity;
	private BigDecimal unitPriceRatio;
	private BigDecimal additionalShippingCosts;
	private Integer customizable;
	private Integer textFields;
	private Integer uploadableFiles;
	private boolean active = true;
	private String redirectType;
	private Integer redirectedTypeId;
	private boolean availableForOrder = true;
	private LocalDate availableDate;
	private boolean showCondition;
	private String condition;
	private boolean showPrice = true;
	private boolean indexed = true;
	private String visibility = "both"; // both|catalog|search|none
	private boolean advancedStockManagement;
	private LocalDateTime addDate = LocalDateTime.now();
	private LocalDateTime updateDate = LocalDateTime.now();
	private Integer packStockType;
	private PrestashopTranslatableString metaDescription;
	private PrestashopTranslatableString metaKeywords;
	private PrestashopTranslatableString metaTitle;
	private PrestashopTranslatableString linkRewrite;
	private PrestashopTranslatableString name;
	private PrestashopTranslatableString description;
	private PrestashopTranslatableString shortDescription;
	private PrestashopTranslatableString availableNow;
	private PrestashopTranslatableString availableLater;
	private Associations associations = new Associations();
	private List<Element> additionalProperties = new LinkedList<>();

	public PrestashopProduct() {
		associations.setCategories(new CategoriesAssociationsEntry());
	}

	@XmlElement(name="id_manufacturer")
	public Integer getManufacturerId() {
		return manufacturerId;
	}

	public void setManufacturerId(Integer manufacturerId) {
		this.manufacturerId = manufacturerId;
	}

	@XmlElement(name="id_supplier")
	public Integer getSupplierId() {
		return supplierId;
	}

	public void setSupplierId(Integer supplierId) {
		this.supplierId = supplierId;
	}

	@XmlElement(name="id_category_default")
	public Integer getDefaultCategoryId() {
		return defaultCategoryId;
	}

	public void setDefaultCategoryId(Integer defaultCategoryId) {
		this.defaultCategoryId = defaultCategoryId;
	}

	@XmlElement(name="new")
	public boolean isNewProduct() {
		return newProduct;
	}

	public void setNewProduct(boolean newProduct) {
		this.newProduct = newProduct;
	}

	@XmlElement(name="cache_default_attribute")
	public boolean isCacheDefaultAttribute() {
		return cacheDefaultAttribute;
	}

	public void setCacheDefaultAttribute(boolean cacheDefaultAttribute) {
		this.cacheDefaultAttribute = cacheDefaultAttribute;
	}

	@XmlElement(name="id_default_image")
	public Integer getDefaultImageId() {
		return defaultImageId;
	}

	public void setDefaultImageId(Integer defaultImageId) {
		this.defaultImageId = defaultImageId;
	}

	@XmlElement(name="id_default_combination")
	public Integer getDefaultCombinationId() {
		return defaultCombinationId;
	}

	public void setDefaultCombinationId(Integer defaultCombinationId) {
		this.defaultCombinationId = defaultCombinationId;
	}

	@XmlElement(name="id_tax_rules_group")
	public Integer getTaxRulesGroupId() {
		return taxRulesGroupId;
	}

	public void setTaxRulesGroupId(Integer taxRulesGroupId) {
		this.taxRulesGroupId = taxRulesGroupId;
	}

	@XmlElement(name="position_in_category")
	public Integer getPositionInCategory() {
		return positionInCategory;
	}

	public void setPositionInCategory(Integer positionInCategory) {
		this.positionInCategory = positionInCategory;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@XmlElement(name="id_shop_default")
	public Integer getDefaultShopId() {
		return defaultShopId;
	}

	public void setDefaultShopId(Integer defaultShopId) {
		this.defaultShopId = defaultShopId;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	@XmlElement(name="supplier_reference")
	public String getSupplierReference() {
		return supplierReference;
	}

	public void setSupplierReference(String supplierReference) {
		this.supplierReference = supplierReference;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public BigDecimal getWidth() {
		return width;
	}

	public void setWidth(BigDecimal width) {
		this.width = width;
	}

	public BigDecimal getHeight() {
		return height;
	}

	public void setHeight(BigDecimal height) {
		this.height = height;
	}

	public BigDecimal getDepth() {
		return depth;
	}

	public void setDepth(BigDecimal depth) {
		this.depth = depth;
	}

	public BigDecimal getWeight() {
		return weight;
	}

	public void setWeight(BigDecimal weight) {
		this.weight = weight;
	}

	@XmlElement(name="quantity_discount")
	public boolean isQuantityDiscount() {
		return quantityDiscount;
	}

	public void setQuantityDiscount(boolean quantityDiscount) {
		this.quantityDiscount = quantityDiscount;
	}

	public String getEan13() {
		return ean13;
	}

	public void setEan13(String ean13) {
		this.ean13 = ean13;
	}

	public String getIsbn() {
		return isbn;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}

	public String getUpc() {
		return upc;
	}

	public void setUpc(String upc) {
		this.upc = upc;
	}

	@XmlElement(name="cache_is_pack")
	public boolean isCacheIsPack() {
		return cacheIsPack;
	}

	public void setCacheIsPack(boolean cacheIsPack) {
		this.cacheIsPack = cacheIsPack;
	}

	@XmlElement(name="cache_has_attachments")
	public boolean isCacheHasAttachments() {
		return cacheHasAttachments;
	}

	public void setCacheHasAttachments(boolean cacheHasAttachments) {
		this.cacheHasAttachments = cacheHasAttachments;
	}

	@XmlElement(name="is_virtual")
	public boolean isVirtual() {
		return virtual;
	}

	public void setVirtual(boolean virtual) {
		this.virtual = virtual;
	}

	@XmlElement(name="state")
	public boolean isSaved() {
		return saved;
	}

	public void setSaved(boolean saved) {
		this.saved = saved;
	}

	@XmlElement(name="on_sale")
	public boolean isOnSale() {
		return onSale;
	}

	public void setOnSale(boolean onSale) {
		this.onSale = onSale;
	}

	@XmlElement(name="online_only")
	public boolean isOnlineOnly() {
		return onlineOnly;
	}

	public void setOnlineOnly(boolean onlineOnly) {
		this.onlineOnly = onlineOnly;
	}

	public BigDecimal getEcotax() {
		return ecotax;
	}

	public void setEcotax(BigDecimal ecotax) {
		this.ecotax = ecotax;
	}

	@XmlElement(name="minimal_quantity")
	public Integer getMinimalQuantity() {
		return minimalQuantity;
	}

	public void setMinimalQuantity(Integer minimalQuantity) {
		this.minimalQuantity = minimalQuantity;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	@XmlElement(name="wholesale_price")
	public BigDecimal getWholesalePrice() {
		return wholesalePrice;
	}

	public void setWholesalePrice(BigDecimal wholesalePrice) {
		this.wholesalePrice = wholesalePrice;
	}

	public String getUnity() {
		return unity;
	}

	public void setUnity(String unity) {
		this.unity = unity;
	}

	@XmlElement(name="unit_price_ratio")
	public BigDecimal getUnitPriceRatio() {
		return unitPriceRatio;
	}

	public void setUnitPriceRatio(BigDecimal unitPriceRatio) {
		this.unitPriceRatio = unitPriceRatio;
	}

	@XmlElement(name="additional_shipping_cost")
	public BigDecimal getAdditionalShippingCosts() {
		return additionalShippingCosts;
	}

	public void setAdditionalShippingCosts(BigDecimal additionalShippingCosts) {
		this.additionalShippingCosts = additionalShippingCosts;
	}

	public Integer getCustomizable() {
		return customizable;
	}

	public void setCustomizable(Integer customizable) {
		this.customizable = customizable;
	}

	@XmlElement(name="text_fields")
	public Integer getTextFields() {
		return textFields;
	}

	public void setTextFields(Integer textFields) {
		this.textFields = textFields;
	}

	@XmlElement(name="uploadable_files")
	public Integer getUploadableFiles() {
		return uploadableFiles;
	}

	public void setUploadableFiles(Integer uploadableFiles) {
		this.uploadableFiles = uploadableFiles;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	@XmlElement(name="redirect_type")
	public String getRedirectType() {
		return redirectType;
	}

	public void setRedirectType(String redirectType) {
		this.redirectType = redirectType;
	}

	@XmlElement(name="id_type_redirected")
	public Integer getRedirectedTypeId() {
		return redirectedTypeId;
	}

	public void setRedirectedTypeId(Integer redirectedTypeId) {
		this.redirectedTypeId = redirectedTypeId;
	}

	@XmlElement(name="available_for_order")
	public boolean isAvailableForOrder() {
		return availableForOrder;
	}

	public void setAvailableForOrder(boolean availableForOrder) {
		this.availableForOrder = availableForOrder;
	}

	@XmlElement(name="available_date")
	public LocalDate getAvailableDate() {
		return availableDate;
	}

	public void setAvailableDate(LocalDate availableDate) {
		this.availableDate = availableDate;
	}

	@XmlElement(name="show_condition")
	public boolean isShowCondition() {
		return showCondition;
	}

	public void setShowCondition(boolean showCondition) {
		this.showCondition = showCondition;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	@XmlElement(name="show_price")
	public boolean isShowPrice() {
		return showPrice;
	}

	public void setShowPrice(boolean showPrice) {
		this.showPrice = showPrice;
	}

	public boolean isIndexed() {
		return indexed;
	}

	public void setIndexed(boolean indexed) {
		this.indexed = indexed;
	}

	public String getVisibility() {
		return visibility;
	}

	public void setVisibility(String visibility) {
		this.visibility = visibility;
	}

	@XmlElement(name="advanced_stock_management")
	public boolean isAdvancedStockManagement() {
		return advancedStockManagement;
	}

	public void setAdvancedStockManagement(boolean advancedStockManagement) {
		this.advancedStockManagement = advancedStockManagement;
	}

	@XmlElement(name="date_add")
	public LocalDateTime getAddDate() {
		return addDate;
	}

	public void setAddDate(LocalDateTime addDate) {
		this.addDate = addDate;
	}

	@XmlElement(name="date_upd")
	public LocalDateTime getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(LocalDateTime updateDate) {
		this.updateDate = updateDate;
	}

	@XmlElement(name="pack_stock_type")
	public Integer getPackStockType() {
		return packStockType;
	}

	public void setPackStockType(Integer packStockType) {
		this.packStockType = packStockType;
	}

	@XmlElement(name="meta_description")
	public PrestashopTranslatableString getMetaDescription() {
		return metaDescription;
	}

	public void setMetaDescription(PrestashopTranslatableString metaDescription) {
		this.metaDescription = metaDescription;
	}

	@XmlElement(name="meta_keywords")
	public PrestashopTranslatableString getMetaKeywords() {
		return metaKeywords;
	}

	public void setMetaKeywords(PrestashopTranslatableString metaKeywords) {
		this.metaKeywords = metaKeywords;
	}

	@XmlElement(name="meta_title")
	public PrestashopTranslatableString getMetaTitle() {
		return metaTitle;
	}

	public void setMetaTitle(PrestashopTranslatableString metaTitle) {
		this.metaTitle = metaTitle;
	}

	@XmlElement(name="link_rewrite")
	public PrestashopTranslatableString getLinkRewrite() {
		return linkRewrite;
	}

	public void setLinkRewrite(PrestashopTranslatableString linkRewrite) {
		this.linkRewrite = linkRewrite;
	}

	public PrestashopTranslatableString getName() {
		return name;
	}

	public void setName(PrestashopTranslatableString name) {
		this.name = name;
	}

	public PrestashopTranslatableString getDescription() {
		return description;
	}

	public void setDescription(PrestashopTranslatableString description) {
		this.description = description;
	}

	@XmlElement(name="description_short")
	public PrestashopTranslatableString getShortDescription() {
		return shortDescription;
	}

	public void setShortDescription(PrestashopTranslatableString shortDescription) {
		this.shortDescription = shortDescription;
	}

	@XmlElement(name="available_now")
	public PrestashopTranslatableString getAvailableNow() {
		return availableNow;
	}

	public void setAvailableNow(PrestashopTranslatableString availableNow) {
		this.availableNow = availableNow;
	}

	@XmlElement(name="available_later")
	public PrestashopTranslatableString getAvailableLater() {
		return availableLater;
	}

	public void setAvailableLater(PrestashopTranslatableString availableLater) {
		this.availableLater = availableLater;
	}

	public Associations getAssociations() {
		return associations;
	}

	public void setAssociations(Associations associations) {
		this.associations = associations;
	}

	@XmlAnyElement
	public List<Element> getAdditionalProperties() {
		for(ListIterator<Element> it = additionalProperties.listIterator() ; it.hasNext() ; ) {
			if(READONLY_ATTRIBUTES.contains(it.next().getTagName())) it.remove();
		}
		return additionalProperties;
	}

	public void setAdditionalProperties(List<Element> additionalProperties) {
		this.additionalProperties = additionalProperties;
	}
}
