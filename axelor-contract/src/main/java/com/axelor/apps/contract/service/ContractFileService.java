package com.axelor.apps.contract.service;

import com.axelor.apps.contract.db.ContractFile;
import java.util.List;

public interface ContractFileService {
  void setDMSFile(ContractFile contractFile);

  String getInlineUrl(ContractFile contractFile);

  void remove(List<Integer> contractFileIds);
}
