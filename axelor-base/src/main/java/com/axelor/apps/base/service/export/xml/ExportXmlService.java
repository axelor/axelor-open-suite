package com.axelor.apps.base.service.export.xml;

import com.axelor.apps.base.xml.models.ExportedModel;
import java.util.List;

public interface ExportXmlService {

  <T extends ExportedModel> void exportXML(T srcObject,  String fileName, Class<T> classObject)
      throws Exception;

  @SuppressWarnings("unchecked")
  <T extends ExportedModel> void exportXML(
      List<T> srcObjects, String fileName, Class<T>... classObjects) throws Exception;
}
