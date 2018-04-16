package com.axelor.apps.contract.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.exception.AxelorException;

public interface ContractLineService {
    /**
     * Set to null ContractLine fields for form view.
     * @param contractLine to reset.
     * @return ContractLine reset.
     */
    ContractLine reset(ContractLine contractLine);

    /**
     * Fill ContractLine with Product information.
     * @param contractLine to fill.
     * @param product to get information.
     * @return ContractLine filled with Product information.
     */
    ContractLine fill(ContractLine contractLine, Product product);

    /**
     * Compute price and tax of Product to ContractLine.
     * @param contractLine to save price and tax.
     * @param contract to give additional information like Partner and Company.
     * @param product to use for computing.
     * @return ContractLine price and tax computed.
     * @throws AxelorException if a error occurred when we get tax line.
     */
    ContractLine compute(ContractLine contractLine, Contract contract, Product product) throws AxelorException;

    /**
     * Fill and compute ContractLine with Product.
     * @param contractLine to fill and compute.
     * @param contract to give additional information.
     * @param product to use operation.
     * @return ContractLine filled and computed.
     * @throws AxelorException if a error occurred when we get tax line.
     */
    ContractLine fillAndCompute(ContractLine contractLine, Contract contract, Product product) throws AxelorException;

    /**
     * Compute ex and in tax total for ContractLine.
     * @param contractLine to compute ex/in tax total.
     * @param product to get information about computing.
     * @return ContractLine with ex/in tax total computed.
     */
    ContractLine computeTotal(ContractLine contractLine, Product product);
}
