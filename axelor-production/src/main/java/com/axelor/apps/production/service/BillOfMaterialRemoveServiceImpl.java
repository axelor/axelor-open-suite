package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ProdProcess;
import com.axelor.apps.production.db.repo.BillOfMaterialRepository;
import com.axelor.apps.production.db.repo.ProdProcessRepository;
import com.google.inject.Inject;

public class BillOfMaterialRemoveServiceImpl implements BillOfMaterialRemoveService {

  protected final BillOfMaterialCheckService billOfMaterialCheckService;
  protected final BillOfMaterialRepository billOfMaterialRepository;
  protected final ProdProcessRepository prodProcessRepository;

  @Inject
  public BillOfMaterialRemoveServiceImpl(
      BillOfMaterialCheckService billOfMaterialCheckService,
      BillOfMaterialRepository billOfMaterialRepository,
      ProdProcessRepository prodProcessRepository) {
    this.billOfMaterialCheckService = billOfMaterialCheckService;
    this.billOfMaterialRepository = billOfMaterialRepository;
    this.prodProcessRepository = prodProcessRepository;
  }

  @Override
  public void removeBomAndProdProcess(BillOfMaterial oldBillOfMaterial) throws AxelorException {
    ProdProcess oldProdProcess;
    if (oldBillOfMaterial != null) {
      oldProdProcess = oldBillOfMaterial.getProdProcess();
      billOfMaterialCheckService.checkUsedBom(oldBillOfMaterial);
      billOfMaterialRepository.remove(oldBillOfMaterial);
      if (oldProdProcess != null) {
        prodProcessRepository.remove(oldProdProcess);
      }
    }
  }
}
