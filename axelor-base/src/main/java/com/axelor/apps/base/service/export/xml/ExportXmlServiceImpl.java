package com.axelor.apps.base.service.export.xml;

import com.axelor.apps.base.xml.models.ExportedModel;
import com.axelor.exception.service.TraceBackService;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

public class ExportXmlServiceImpl implements ExportXmlService {

  @Override
  public <T extends ExportedModel> void exportXML(
      T srcObject, String fileName, Class<T> classObject) throws Exception {

    JAXBContext jc = JAXBContext.newInstance(classObject);
    Marshaller jaxbMarshaller = jc.createMarshaller();

    jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
    jaxbMarshaller.marshal(srcObject, new File(fileName));
  }

  @Override
  public <T extends ExportedModel> void exportXML(
      List<T> srcObjects, String fileName, Class<T>... classObjects) throws Exception {

    JAXBContext jc = JAXBContext.newInstance(classObjects);
    Marshaller jaxbMarshaller = jc.createMarshaller();
    jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
    
    try (FileOutputStream fileOutputStream = new FileOutputStream(new File(fileName), true)) {
      boolean jaxbFragment = false;
      
      srcObjects.forEach(
          object -> {
            try {
              jaxbMarshaller.marshal(object, fileOutputStream);
              if (!jaxbFragment) {
            	  jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
              }

            } catch (JAXBException e) {
              TraceBackService.trace(e);
            }
          });
      fileOutputStream.flush();
    }
  }
}
