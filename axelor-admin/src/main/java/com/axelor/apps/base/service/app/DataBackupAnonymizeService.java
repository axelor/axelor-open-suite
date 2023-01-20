package com.axelor.apps.base.service.app;

import com.axelor.apps.base.db.AnonymizerLine;
import com.axelor.apps.base.db.DataBackup;
import com.axelor.db.mapper.Property;
import com.axelor.exception.AxelorException;
import java.util.List;

public interface DataBackupAnonymizeService {
  List<AnonymizerLine> searchAnonymizerLines(
      DataBackup dataBackup, Property property, String metaModelName);

  String anonymizeMetaModelData(
      Property property, String metaModelName, Object value, List<AnonymizerLine> anonymizerLines)
      throws AxelorException;

  List<String> csvComputeAnonymizedFullname(List<String> dataArr, List<String> headerArr);

  List<String> csvAnonymizeImportId(List<String> dataArr, List<String> headerArr, byte[] salt);
}
