
package com.axelor.apps.sale.rest.dto;

import com.axelor.apps.base.db.Company;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestPostStructure;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;


public class SaleOrderPostRequest extends RequestPostStructure {

    @NotNull
    @Min(0)
    private Long  clientPartner;


    public Long getClientPartner() {
        return clientPartner;
    }

    public void setClientPartner(Long clientPartner) {
        this.clientPartner = clientPartner;
    }

    public Company fetchClientPartner() {
        if (clientPartner == null || clientPartner == 0L) {
            return null;
        }
        return ObjectFinder.find(Company.class, clientPartner, ObjectFinder.NO_VERSION);
    }




}
