/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.stock.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.db.BirtTemplate;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.birt.template.BirtTemplateService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.stock.db.Inventory;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.repo.InventoryRepository;
import com.axelor.apps.stock.exception.StockExceptionMessage;
import com.axelor.apps.stock.service.InventoryProductService;
import com.axelor.apps.stock.service.InventoryService;
import com.axelor.apps.stock.service.config.StockConfigService;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import org.eclipse.birt.core.exception.BirtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class InventoryController {

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /**
   * Fonction appeler par le bouton imprimer
   *
   * @param request
   * @param response
   * @return
   * @throws BirtException
   * @throws IOException
   */
  public void showInventory(ActionRequest request, ActionResponse response) {
    try {
      Inventory inventory = request.getContext().asType(Inventory.class);
      inventory = Beans.get(InventoryRepository.class).find(inventory.getId());
      BirtTemplate inventoryBirtTemplate =
          Beans.get(StockConfigService.class)
              .getStockConfig(inventory.getCompany())
              .getInventoryBirtTemplate();
      if (ObjectUtils.isEmpty(inventoryBirtTemplate)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(BaseExceptionMessage.BIRT_TEMPLATE_CONFIG_NOT_FOUND));
      }

      String name = I18n.get("Inventory") + " " + inventory.getInventorySeq();
      String outputName = Beans.get(InventoryService.class).computeExportFileName(inventory);
      String fileLink =
          Beans.get(BirtTemplateService.class)
              .generateBirtTemplateLink(
                  inventoryBirtTemplate,
                  inventory,
                  null,
                  outputName,
                  inventoryBirtTemplate.getAttach(),
                  inventory.getFormatSelect());

      logger.debug("Printing " + name);

      response.setView(ActionView.define(name).add("html", fileLink).map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void exportInventory(ActionRequest request, ActionResponse response) {
    try {
      Inventory inventory = request.getContext().asType(Inventory.class);
      inventory = Beans.get(InventoryRepository.class).find(inventory.getId());

      String name = I18n.get("Inventory") + " " + inventory.getInventorySeq();
      MetaFile metaFile = Beans.get(InventoryService.class).exportInventoryAsCSV(inventory);

      response.setView(
          ActionView.define(name)
              .add(
                  "html",
                  "ws/rest/com.axelor.meta.db.MetaFile/"
                      + metaFile.getId()
                      + "/content/download?v="
                      + metaFile.getVersion())
              .map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void importFile(ActionRequest request, ActionResponse response) {
    try {
      Inventory inventory =
          Beans.get(InventoryRepository.class)
              .find(request.getContext().asType(Inventory.class).getId());

      Path filePath = Beans.get(InventoryService.class).importFile(inventory);
      response.setInfo(
          String.format(I18n.get(StockExceptionMessage.INVENTORY_8), filePath.toString()));

      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void planInventory(ActionRequest request, ActionResponse response) {
    try {
      Long id = request.getContext().asType(Inventory.class).getId();
      Inventory inventory = Beans.get(InventoryRepository.class).find(id);
      Beans.get(InventoryService.class).planInventory(inventory);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void startInventory(ActionRequest request, ActionResponse response) {
    try {
      Long id = request.getContext().asType(Inventory.class).getId();
      Inventory inventory = Beans.get(InventoryRepository.class).find(id);
      Beans.get(InventoryService.class).startInventory(inventory);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void completeInventory(ActionRequest request, ActionResponse response) {
    try {
      Long id = request.getContext().asType(Inventory.class).getId();
      Inventory inventory = Beans.get(InventoryRepository.class).find(id);
      Beans.get(InventoryService.class).completeInventory(inventory);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void validateInventory(ActionRequest request, ActionResponse response) {
    try {
      Long id = request.getContext().asType(Inventory.class).getId();
      Inventory inventory = Beans.get(InventoryRepository.class).find(id);
      Beans.get(InventoryService.class).validateInventory(inventory);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void cancel(ActionRequest request, ActionResponse response) {
    try {
      Inventory inventory = request.getContext().asType(Inventory.class);
      inventory = Beans.get(InventoryRepository.class).find(inventory.getId());
      Beans.get(InventoryService.class).cancel(inventory);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void draftInventory(ActionRequest request, ActionResponse response) {
    try {
      Long id = request.getContext().asType(Inventory.class).getId();
      Inventory inventory = Beans.get(InventoryRepository.class).find(id);
      Beans.get(InventoryService.class).draftInventory(inventory);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void fillInventoryLineList(ActionRequest request, ActionResponse response) {
    try {
      Long inventoryId = (Long) request.getContext().get("id");
      if (inventoryId != null) {
        Inventory inventory = Beans.get(InventoryRepository.class).find(inventoryId);
        Boolean succeed = Beans.get(InventoryService.class).fillInventoryLineList(inventory);
        if (succeed == null) {
          response.setInfo(I18n.get(StockExceptionMessage.INVENTORY_9));
        } else {
          if (succeed) {
            response.setNotify(I18n.get(StockExceptionMessage.INVENTORY_10));
          } else {
            response.setNotify(I18n.get(StockExceptionMessage.INVENTORY_11));
          }
        }
      }
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setInventorySequence(ActionRequest request, ActionResponse response) {
    try {

      Inventory inventory = request.getContext().asType(Inventory.class);
      SequenceService sequenceService = Beans.get(SequenceService.class);

      if (sequenceService.isEmptyOrDraftSequenceNumber(inventory.getInventorySeq())) {

        StockLocation stockLocation = inventory.getStockLocation();

        response.setValue(
            "inventorySeq",
            Beans.get(InventoryService.class).getInventorySequence(stockLocation.getCompany()));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void showStockMoves(ActionRequest request, ActionResponse response) {
    try {
      final InventoryService inventoryService = Beans.get(InventoryService.class);
      Inventory inventory = request.getContext().asType(Inventory.class);
      if (Boolean.FALSE.equals(inventoryService.hasRelatedStockMoves(inventory))) {
        response.setInfo(I18n.get("No stock moves found for this inventory."));
        return;
      }

      response.setView(
          ActionView.define(I18n.get("Internal Stock Moves"))
              .model(StockMove.class.getName())
              .add("grid", "stock-move-grid")
              .add("form", "stock-move-form")
              .param("search-filters", "internal-stock-move-filters")
              .domain("self.inventory.id = :inventoryId")
              .context("_showSingle", true)
              .context("inventoryId", inventory.getId())
              .map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void checkDuplicateProduct(ActionRequest request, ActionResponse response) {
    try {
      Inventory inventory = request.getContext().asType(Inventory.class);
      Beans.get(InventoryProductService.class).checkDuplicate(inventory);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }
}
