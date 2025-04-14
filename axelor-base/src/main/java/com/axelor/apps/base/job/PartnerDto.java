package com.axelor.apps.base.job;

public class PartnerDto {
    private Long id;
    private String registrationCode;
    private String name;
    private String taxNbr;
    private String addressL4;

    public PartnerDto(Long id, String registrationCode, String name, String taxNbr, String addressL4) {
        this.id = id;
        this.registrationCode = registrationCode;
        this.name = name;
        this.taxNbr = taxNbr;
        this.addressL4 = addressL4;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRegistrationCode() {
        return registrationCode;
    }

    public void setRegistrationCode(String registrationCode) {
        this.registrationCode = registrationCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTaxNbr() {
        return taxNbr;
    }

    public void setTaxNbr(String taxNbr) {
        this.taxNbr = taxNbr;
    }

    public String getAddressL4() {
        return addressL4;
    }

    public void setAddressL4(String addressL4) {
        this.addressL4 = addressL4;
    }

}
