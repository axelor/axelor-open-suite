package com.axelor.apps.hr.web;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import com.axelor.apps.hr.db.DPAE;
import com.axelor.apps.hr.db.repo.DPAERepository;
import com.axelor.apps.hr.exception.IExceptionMessage;
import com.axelor.apps.hr.service.dpae.DPAEService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class DPAEController {

  public void export(ActionRequest request, ActionResponse response) {

    DPAE dpae = request.getContext().asType(DPAE.class);
    dpae = Beans.get(DPAERepository.class).find(dpae.getId());

    DPAEService dpaeService = Beans.get(DPAEService.class);
    
    List<String> errors  = dpaeService.checkDPAEValidity(dpae);
    
    if (CollectionUtils.isNotEmpty(errors)) {
      
      String errorMessage = "<ul>";
      
      errorMessage += errors.stream().map(fieldName -> "<li>" + String.format( I18n.get( IExceptionMessage.DPAE_FIELD_INVALID), I18n.get(fieldName) ) + "</li>" ).collect(Collectors.joining("")); 
       
      errorMessage += "</ul>";
      
      response.setError(errorMessage);
      return;
      
    }
    
    
    
    dpaeService.exportSingle(dpae);
    
    
    

    if (dpae.getMetaFile() != null) {
      response.setView(
          ActionView.define("Export")
              .add(
                  "html",
                  "ws/rest/com.axelor.meta.db.MetaFile/"
                      + dpae.getMetaFile().getId()
                      + "/content/download?v="
                      + dpae.getMetaFile().getVersion())
              .param("download", "true")
              .map());
      response.setReload(true);
    }
  }
}
