package com.axelor.apps.contract.service;

import com.axelor.apps.base.db.File;
import java.util.List;

public interface ContractFileService {
  void setDMSFile(File contractFile);

  String getInlineUrl(File contractFile);

  void remove(List<Integer> contractFileIds);
}
