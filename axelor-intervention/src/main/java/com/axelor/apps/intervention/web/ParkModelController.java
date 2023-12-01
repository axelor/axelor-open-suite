package com.axelor.apps.intervention.web;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.intervention.db.Equipment;
import com.axelor.apps.intervention.db.ParkModel;
import com.axelor.apps.intervention.service.ParkModelService;
import com.axelor.db.JpaRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.axelor.utils.helpers.MapHelper;
import com.axelor.utils.helpers.context.ActionViewHelper;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;

public class ParkModelController {

  public static final String FIELD_EQUIPMENT_MODEL_LIST = "_xEquipmentModelList";

  protected static boolean setParkAndModels(ActionResponse response, Context context) {
    Long parkModelId = null;
    if (context.get("_parkModelId") != null) {
      parkModelId = MapHelper.get(context, Long.class, "_parkModelId");
    } else {
      ParkModel parkModel = MapHelper.get(context, ParkModel.class, "_xParkModel");
      if (parkModel != null) {
        parkModelId = parkModel.getId();
      }
    }

    if (parkModelId == null) {
      response.setValue("$_xEquipmentModelList", null);
      return false;
    }

    response.setValue("$_xParkModel", JpaRepository.of(ParkModel.class).find(parkModelId));

    List<Map<String, Object>> equipmentModelList =
        Beans.get(ParkModelService.class)
            .getEquipmentList(JpaRepository.of(ParkModel.class).find(parkModelId));
    response.setValue("$_xEquipmentModelList", equipmentModelList);
    return true;
  }

  public void parkModelChange(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();
      setParkAndModels(response, context);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setDefaults(ActionRequest request, ActionResponse response) {

    try {
      Context context = request.getContext();

      if (setParkAndModels(response, context)) {
        response.setAttr("$_xParkModel", "readonly", true);
      }

      if (context.get("_partnerId") != null) {
        Long partnerId = MapHelper.get(context, Long.class, "_partnerId");
        response.setValue("$_xPartner", JpaRepository.of(Partner.class).find(partnerId));
        response.setAttr("$_xPartner", "readonly", true);
      }

      if (context.get("_contractId") != null) {
        Long contractId = MapHelper.get(context, Long.class, "_contractId");
        response.setValue("$_xContract", JpaRepository.of(Contract.class).find(contractId));
        response.setAttr("$_xContract", "readonly", true);
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @SuppressWarnings("unchecked")
  public void generateEquipments(ActionRequest request, ActionResponse response) {
    try {
      if (!(request.getContext().get(FIELD_EQUIPMENT_MODEL_LIST) instanceof List<?>)
          || CollectionUtils.isEmpty(
              (List<?>) request.getContext().get(FIELD_EQUIPMENT_MODEL_LIST))) {
        return;
      }

      ParkModel parkModel = MapHelper.get(request.getContext(), ParkModel.class, "_xParkModel");
      Partner partner = MapHelper.get(request.getContext(), Partner.class, "_xPartner");
      Contract contract = MapHelper.get(request.getContext(), Contract.class, "_xContract");
      LocalDate commissioningDate =
          MapHelper.get(request.getContext(), LocalDate.class, "_xCommissioningDate");
      LocalDate customerWarrantyOnPartEndDate =
          MapHelper.get(request.getContext(), LocalDate.class, "_xCustomerWarrantyOnPartEndDate");
      LocalDate customerMoWarrantyEndDate =
          MapHelper.get(request.getContext(), LocalDate.class, "_xCustomerMoWarrantyEndDate");

      List<Map<String, Object>> equipmentModels =
          (List<Map<String, Object>>) request.getContext().get(FIELD_EQUIPMENT_MODEL_LIST);

      Map<Long, Integer> quantitiesMap = new HashMap<>();

      for (Map<String, Object> equipmentModel : equipmentModels) {
        quantitiesMap.put(
            MapHelper.get(equipmentModel, Long.class, "id"),
            MapHelper.get(equipmentModel, Integer.class, "qtyToGenerate"));
      }

      List<Long> ids =
          Beans.get(ParkModelService.class)
              .generateEquipments(
                  parkModel,
                  partner,
                  commissioningDate,
                  customerWarrantyOnPartEndDate,
                  customerMoWarrantyEndDate,
                  contract,
                  quantitiesMap);

      response.setCanClose(true);
      if (CollectionUtils.isEmpty(ids)) {
        response.setNotify(I18n.get("Nothing has been generated."));
        return;
      }
      response.setView(
          ActionViewHelper.build(
                  Equipment.class, "self.id in :ids", "equipment-grid", "equipment-form")
              .context("ids", ids)
              .map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
