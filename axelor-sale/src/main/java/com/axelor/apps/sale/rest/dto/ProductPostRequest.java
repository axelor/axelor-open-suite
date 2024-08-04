package com.axelor.apps.sale.rest.dto;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestPostStructure;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class ProductPostRequest extends RequestPostStructure {

    @NotNull
    @Min(0)
    private Long productId;
    private Long companyId;

    private Long partnerId;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId= productId;
    }
    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId= companyId;
    }

    public Long getPartnerId() {
        return partnerId;
    }

    public void setPartner(Long partnerId) {
        this.partnerId = partnerId;
    }

    public Product fetchProduct() {
        if (productId== null || productId == 0L) {
            return null;
        }
        return ObjectFinder.find(Product.class, productId, ObjectFinder.NO_VERSION);
    }
    public Company fetchCompany() {
        if (companyId== null || companyId == 0L) {
            return null;
        }
        return ObjectFinder.find(Company.class, companyId, ObjectFinder.NO_VERSION);
    }

    public Partner fetchPartner() {
        if (partnerId == null || partnerId == 0L) {
            return null;
        }
        return ObjectFinder.find(Partner.class, partnerId, ObjectFinder.NO_VERSION);
    }
}
