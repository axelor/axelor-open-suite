package com.axelor.apps.contract.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.CurrencyConversionService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import wslite.json.JSONException;

public class ContractLineServiceImpl implements ContractLineService {

    protected AppBaseService appBaseService;
    protected AccountManagementService accountManagementService;
    protected CurrencyConversionService currencyConversionService;

    @Inject
    public ContractLineServiceImpl(AppBaseService appBaseService,
                                   AccountManagementService accountManagementService,
                                   CurrencyConversionService currencyConversionService) {
        this.appBaseService = appBaseService;
        this.accountManagementService = accountManagementService;
        this.currencyConversionService = currencyConversionService;
    }

    @Override
    public ContractLine reset(ContractLine contractLine) {
        if (contractLine == null) {
            return new ContractLine();
        }
        contractLine.setTaxLine(null);
        contractLine.setProductName(null);
        contractLine.setUnit(null);
        contractLine.setPrice(null);
        contractLine.setExTaxTotal(null);
        contractLine.setInTaxTotal(null);
        return contractLine;
    }

    @Override
    public ContractLine update(ContractLine contractLine, Product product) {
        contractLine.setProductName(product.getName());
        if (product.getSalesUnit() != null) {
            contractLine.setUnit(product.getSalesUnit());
        } else {
            contractLine.setUnit(product.getUnit());
        }
        contractLine.setPrice(product.getSalePrice());
        return contractLine;
    }

    @Override
    public ContractLine computePrice(ContractLine contractLine, Contract contract,
                                   Product product) throws AxelorException,
            MalformedURLException, JSONException {
        Preconditions.checkNotNull(contract, I18n.get("Contract can't be " +
                "empty for compute contract line price."));
        Preconditions.checkNotNull(product, "Product can't be " +
                "empty for compute contract line price.");

        // TODO: maybe put tax computing in another method
        TaxLine taxLine = accountManagementService.getTaxLine(
                appBaseService.getTodayDate(), product, contract.getCompany(),
                contract.getPartner().getFiscalPosition(), false);
        contractLine.setTaxLine(taxLine);

        if(taxLine != null && product.getInAti()) {
            BigDecimal price = contractLine.getPrice();
            price = price.divide(taxLine.getValue().add(BigDecimal.ONE), 2,
                    BigDecimal.ROUND_HALF_UP);
            contractLine.setPrice(price);
        }

        BigDecimal price = contractLine.getPrice();
        BigDecimal convert = currencyConversionService.convert(product.getSaleCurrency(), contract.getCurrency());
        contractLine.setPrice(price.multiply(convert));

        return contractLine;
    }

    @Override
    public ContractLine computeTotal(ContractLine contractLine, Product product) {
        BigDecimal taxRate = BigDecimal.ZERO;

        if(contractLine.getTaxLine() != null) {
            taxRate = contractLine.getTaxLine().getValue();
        }

        BigDecimal exTaxTotal = contractLine.getQty()
                .multiply(contractLine.getPrice())
                .setScale(2, RoundingMode.HALF_EVEN);
        contractLine.setExTaxTotal(exTaxTotal);
        BigDecimal inTaxTotal = exTaxTotal.add(exTaxTotal.multiply(taxRate));
        contractLine.setInTaxTotal(inTaxTotal);

        return contractLine;
    }

}
