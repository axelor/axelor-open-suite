package com.axelor.apps.base.service.address.validation;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.PickListEntry;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.address.AddressService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionResponse;
import com.qas.web_2005_02.AddressLineType;
import com.qas.web_2005_02.PicklistEntryType;
import com.qas.web_2005_02.QAAddressType;
import com.qas.web_2005_02.QAPicklistType;
import com.qas.web_2005_02.VerifyLevelType;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddressValidationQASService extends AddressValidationAbstractService {

  public static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Override
  public boolean validateAddress(Address address) throws AxelorException {
    return false;
  }

  public void validateAddress(Address a, ActionResponse response) {
    {
      String search = a.getAddressL4() + " " + a.getAddressL6();
      Map<String, Object> retDict =
          Beans.get(AddressService.class)
              .validate(Beans.get(AppBaseService.class).getAppBase().getQasWsdlUrl(), search);
      LOG.debug("validate retDict = {}", retDict);

      VerifyLevelType verifyLevel = (VerifyLevelType) retDict.get("verifyLevel");

      if (verifyLevel != null && verifyLevel.value().equals("Verified")) {

        QAAddressType address = (QAAddressType) retDict.get("qaAddress");
        String addL1;
        List<AddressLineType> addressLineType = address.getAddressLine();
        addL1 = addressLineType.get(0).getLine();
        response.setValue("addressL2", addressLineType.get(1).getLine());
        response.setValue("addressL3", addressLineType.get(2).getLine());
        response.setValue("addressL4", addressLineType.get(3).getLine());
        response.setValue("addressL5", addressLineType.get(4).getLine());
        response.setValue("addressL6", addressLineType.get(5).getLine());
        response.setValue("inseeCode", addressLineType.get(6).getLine());
        response.setValue("certifiedOk", true);
        response.setValue("pickList", new ArrayList<QAPicklistType>());
        if (addL1 != null) {
          response.setInfo("Ligne 1: " + addL1);
        }
      } else if (verifyLevel != null
          && (verifyLevel.value().equals("Multiple")
              || verifyLevel.value().equals("StreetPartial")
              || verifyLevel.value().equals("InteractionRequired")
              || verifyLevel.value().equals("PremisesPartial"))) {
        LOG.debug("retDict.verifyLevel = {}", retDict.get("verifyLevel"));
        QAPicklistType qaPicklist = (QAPicklistType) retDict.get("qaPicklist");
        List<PickListEntry> pickList = new ArrayList<>();
        if (qaPicklist != null) {
          for (PicklistEntryType p : qaPicklist.getPicklistEntry()) {
            PickListEntry e = new PickListEntry();
            e.setAddress(a);
            e.setMoniker(p.getMoniker());
            e.setScore(p.getScore().toString());
            e.setPostcode(p.getPostcode());
            e.setPartialAddress(p.getPartialAddress());
            e.setPicklist(p.getPicklist());

            pickList.add(e);
          }
        } else if (retDict.get("qaAddress") != null) {
          QAAddressType address = (QAAddressType) retDict.get("qaAddress");
          PickListEntry e = new PickListEntry();
          List<AddressLineType> addressLineType = address.getAddressLine();
          e.setAddress(a);
          e.setL2(addressLineType.get(1).getLine());
          e.setL3(addressLineType.get(2).getLine());
          e.setPartialAddress(addressLineType.get(3).getLine());
          e.setL5(addressLineType.get(4).getLine());
          e.setPostcode(addressLineType.get(5).getLine());
          e.setInseeCode(addressLineType.get(6).getLine());

          pickList.add(e);
        }
        response.setValue("certifiedOk", false);
        response.setValue("pickList", pickList);

      } else if (verifyLevel != null && verifyLevel.value().equals("None")) {
        LOG.debug("address None");
        response.setInfo(I18n.get(BaseExceptionMessage.ADDRESS_3));
      }
    }
  }
}
