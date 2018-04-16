package com.axelor.apps.contract.service;

import java.net.MalformedURLException;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.exception.AxelorException;

import wslite.json.JSONException;

public interface ContractLineService {
    ContractLine reset(ContractLine contractLine);
    ContractLine update(ContractLine contractLine, Product product);
    ContractLine computePrice(ContractLine contractLine, Contract contract, Product product) throws AxelorException, MalformedURLException, JSONException;
    ContractLine computeTotal(ContractLine contractLine, Product product);
}
