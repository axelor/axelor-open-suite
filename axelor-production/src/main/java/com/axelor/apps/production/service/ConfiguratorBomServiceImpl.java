/**
 * Axelor Business Solutions
 * <p>
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
 * <p>
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.production.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ConfiguratorBOM;
import com.axelor.apps.production.db.ProdProcess;
import com.axelor.apps.production.db.repo.BillOfMaterialRepository;
import com.axelor.apps.production.db.repo.ConfiguratorBOMRepository;
import com.axelor.apps.sale.db.Configurator;
import com.axelor.apps.sale.db.repo.ConfiguratorRepository;
import com.axelor.apps.sale.service.ConfiguratorService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.axelor.rpc.JsonContext;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import java.math.BigDecimal;

public class ConfiguratorBomServiceImpl implements ConfiguratorBomService {

    protected ConfiguratorBOMRepository configuratorBOMRepo;

    @Inject
    ConfiguratorBomServiceImpl(ConfiguratorBOMRepository configuratorBOMRepo) {
        this.configuratorBOMRepo = configuratorBOMRepo;
    }

    @Override
    @Transactional(rollbackOn = {Exception.class, AxelorException.class})
    public BillOfMaterial generateBillOfMaterial(ConfiguratorBOM configuratorBOM, JsonContext attributes)
            throws AxelorException {
        ConfiguratorService configuratorService =
                Beans.get(ConfiguratorService.class);
        String name;
        Product product;
        BigDecimal qty;
        Unit unit;
        ProdProcess prodProcess;

        if (configuratorBOM.getDefNameAsFormula()) {
            name = configuratorService.computeFormula(
                    configuratorBOM.getNameFormula(),
                    attributes
            ).toString();
        } else {
            name = configuratorBOM.getName();
        }
        if (configuratorBOM.getDefProductAsFormula()) {
            product = (Product) configuratorService.computeFormula(
                    configuratorBOM.getProductFormula(),
                    attributes
            );
        } else {
            product = configuratorBOM.getProduct();
        }
        if (configuratorBOM.getDefQtyAsFormula()) {
            qty = new BigDecimal(
                    configuratorService.computeFormula(
                            configuratorBOM.getQtyFormula(),
                            attributes
                    ).toString()
            );
        } else {
            qty = configuratorBOM.getQty();
        }
        if (configuratorBOM.getDefUnitAsFormula()) {
            unit = (Unit) configuratorService.computeFormula(
                    configuratorBOM.getUnitFormula(),
                    attributes
            );
        } else {
            unit = configuratorBOM.getUnit();
        }
        if (configuratorBOM.getDefProdProcessAsFormula()) {
            prodProcess = (ProdProcess) configuratorService.computeFormula(
                    configuratorBOM.getProdProcessFormula(), attributes);
        } else if (configuratorBOM.getDefProdProcessAsConfigurator()) {
            //TODO
            prodProcess = null;
        }
        else {
            prodProcess = configuratorBOM.getProdProcess();
        }

        BillOfMaterial billOfMaterial = new BillOfMaterial();
        billOfMaterial.setName(name);
        billOfMaterial.setProduct(product);
        billOfMaterial.setQty(qty);
        billOfMaterial.setUnit(unit);
        billOfMaterial.setProdProcess(prodProcess);
        billOfMaterial = Beans.get(BillOfMaterialRepository.class)
                .save(billOfMaterial);
        configuratorBOM.setBillOfMaterialId(billOfMaterial.getId());
        configuratorBOMRepo.save(configuratorBOM);
        return billOfMaterial;
    }
}
