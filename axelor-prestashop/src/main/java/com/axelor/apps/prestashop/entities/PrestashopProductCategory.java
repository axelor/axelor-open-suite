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
package com.axelor.apps.prestashop.entities;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.w3c.dom.Element;

@XmlRootElement(name="category")
public class PrestashopProductCategory extends PrestashopIdentifiableEntity {
	// We could fetch this from synopsis schema… but maybe this is overkill,
	// I've no idea of the rate at which scheme changes
	private static final List<String> READONLY_FIELDS = Arrays.asList(
		"level_depth",
		"nb_products_recursive"
	);

	private Integer parentId;
	private boolean active = true;
	private Integer defaultShopId;
	private boolean rootCategory;
	private Integer position;
	private LocalDateTime addDate = LocalDateTime.now();
	private LocalDateTime updateDate = LocalDateTime.now();
	private PrestashopTranslatableString name;
	private PrestashopTranslatableString linkRewrite;
	private PrestashopTranslatableString description;
	private PrestashopTranslatableString metaTitle;
	private PrestashopTranslatableString metaDescription;
	private PrestashopTranslatableString metaKeywords;
	private List<Element> additionalProperties = new LinkedList<>();

	@XmlElement(name="id_parent")
	public Integer getParentId() {
		return parentId;
	}

	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	@XmlElement(name="id_shop_default")
	public Integer getDefaultShopId() {
		return defaultShopId;
	}

	public void setDefaultShopId(Integer defaultShopId) {
		this.defaultShopId = defaultShopId;
	}

	@XmlElement(name="is_root_category")
	public boolean isRootCategory() {
		return rootCategory;
	}

	public void setRootCategory(boolean rootCategory) {
		this.rootCategory = rootCategory;
	}

	public Integer getPosition() {
		return position;
	}

	public void setPosition(Integer position) {
		this.position = position;
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

	public PrestashopTranslatableString getName() {
		return name;
	}

	public void setName(PrestashopTranslatableString name) {
		this.name = name;
	}

	@XmlElement(name="link_rewrite")
	public PrestashopTranslatableString getLinkRewrite() {
		return linkRewrite;
	}

	public void setLinkRewrite(PrestashopTranslatableString linkRewrite) {
		this.linkRewrite = linkRewrite;
	}

	public PrestashopTranslatableString getDescription() {
		return description;
	}

	public void setDescription(PrestashopTranslatableString description) {
		this.description = description;
	}

	@XmlElement(name="meta_title")
	public PrestashopTranslatableString getMetaTitle() {
		return metaTitle;
	}

	public void setMetaTitle(PrestashopTranslatableString metaTitle) {
		this.metaTitle = metaTitle;
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

	@XmlAnyElement
	public List<Element> getAdditionalProperties() {
		// Remove elements known as read-only
		for(ListIterator<Element> it = additionalProperties.listIterator() ; it.hasNext() ; ) {
			if(READONLY_FIELDS.contains(it.next().getTagName())) it.remove();
		}
		return additionalProperties;
	}

	public void setAdditionalProperties(List<Element> additionalProperties) {
		this.additionalProperties = additionalProperties;
	}
}
