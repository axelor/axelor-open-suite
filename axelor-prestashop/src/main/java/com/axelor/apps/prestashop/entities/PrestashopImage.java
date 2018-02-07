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
