package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Address;
import com.axelor.exception.AxelorException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import wslite.json.JSONException;

public interface MapRestService {

  /**
   * Set data response.
   *
   * @param mainNode
   * @param arrayNode
   * @throws AxelorException
   * @throws JSONException
   */
  void setData(ObjectNode mainNode, ArrayNode arrayNode) throws AxelorException, JSONException;

  /**
   * Set error response.
   *
   * @param mainNode
   * @param e
   */
  void setError(ObjectNode mainNode, Exception e);

  /**
   * Make address string.
   *
   * @param address
   * @param objectNode
   * @return
   * @throws AxelorException
   * @throws JSONException
   */
  String makeAddressString(Address address, ObjectNode objectNode)
      throws AxelorException, JSONException;
}
