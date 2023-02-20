package com.axelor.apps.mobilesettings.web;

import com.axelor.app.AppSettings;
import com.axelor.apps.base.db.repo.BarcodeTypeConfigRepository;
import com.axelor.apps.base.service.BarcodeGeneratorService;
import com.axelor.auth.db.User;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaFile;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import wslite.json.JSONObject;

public class UserController {

  public void generateQrCode(ActionRequest request, ActionResponse response) {
    try {
      User user = request.getContext().asType(User.class);
      String url = AppSettings.get().getBaseURL();
      JSONObject json = new JSONObject();
      json.put("url", url);
      json.put("username", user.getCode());
      MetaFile qrCode =
          Beans.get(BarcodeGeneratorService.class)
              .createBarCode(
                  user.getId(),
                  "UserQrCode.png",
                  json.toString(),
                  Beans.get(BarcodeTypeConfigRepository.class).findByName("QR_CODE"),
                  false);
      response.setValue("qrCode", qrCode);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
