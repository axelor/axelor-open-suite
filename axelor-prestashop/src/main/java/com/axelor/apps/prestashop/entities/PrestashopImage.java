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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Only used as response body when an image is added.
 * PrestaShop provides no way to edit image details (eg. legend)
 * through WebServices at the time being.
 *
 * Please note that only fields relevant to image product are
 * currently implemented.
 */
@XmlRootElement(name="image")
public class PrestashopImage extends PrestashopIdentifiableEntity {
	private Integer productId;
	private Integer position;
	private boolean cover;
	private PrestashopTranslatableString legend;

	@XmlElement(name="id_product")
	public Integer getProductId() {
		return productId;
	}

	public void setProductId(Integer productId) {
		this.productId = productId;
	}

	public Integer getPosition() {
		return position;
	}

	public void setPosition(Integer position) {
		this.position = position;
	}

	public boolean isCover() {
		return cover;
	}

	public void setCover(boolean cover) {
		this.cover = cover;
	}

	public PrestashopTranslatableString getLegend() {
		return legend;
	}

	public void setLegend(PrestashopTranslatableString legend) {
		this.legend = legend;
	}
}
