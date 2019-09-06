/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.web;

import com.axelor.app.AppSettings;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Wizard;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.tool.ModelTool;
import com.axelor.auth.db.User;
import com.axelor.auth.db.repo.UserRepository;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.validation.ValidationException;

@Singleton
public class UserController {
  protected static final Map<String, String> UNIQUE_MESSAGES =
      ImmutableMap.of("code", IExceptionMessage.USER_CODE_ALREADY_EXISTS);

  @Transactional
  public void setUserPartner(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();

      if (context.get("user_id") != null) {
        UserRepository userRepo = Beans.get(UserRepository.class);
        Partner partner =
            Beans.get(PartnerRepository.class).find(context.asType(Partner.class).getId());
        User user = userRepo.find(((Integer) context.get("user_id")).longValue());
        user.setPartner(partner);
        userRepo.save(user);
      }
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }

  public void applyApplicationMode(ActionRequest request, ActionResponse response) {
    String applicationMode = AppSettings.get().get("application.mode", "prod");
    if ("dev".equals(applicationMode)) {
      response.setAttr("testingPanel", "hidden", false);
    }
  }

  public void validate(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();
      User user = request.getContext().asType(User.class);
      Map<String, String> errors = ModelTool.getUniqueErrors(user, UNIQUE_MESSAGES);

      if (!errors.isEmpty()) {
        response.setErrors(errors);
        return;
      }

      UserService userService = Beans.get(UserService.class);
      user = userService.changeUserPassword(user, context);

      response.setValue("transientPassword", user.getTransientPassword());
    } catch (ValidationException e) {
      response.setError(e.getMessage());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void generateRandomPassword(ActionRequest request, ActionResponse response) {
    try {
      UserService userService = Beans.get(UserService.class);
      CharSequence password = userService.generateRandomPassword();

      response.setValue("newPassword", password);
      response.setValue("chkPassword", password);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void validatePassword(ActionRequest request, ActionResponse response) {
    try {
      UserService userService = Beans.get(UserService.class);
      String newPassword =
          MoreObjects.firstNonNull((String) request.getContext().get("newPassword"), "");
      boolean valid = userService.matchPasswordPattern(newPassword);

      response.setAttr("passwordPatternDescriptionLabel", "hidden", valid);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @SuppressWarnings("unchecked")
  public void openGenerateRandomPasswordsWizard(ActionRequest request, ActionResponse response) {
    try {
      List<Long> userIds = (List<Long>) request.getContext().get("_ids");
      if (ObjectUtils.isEmpty(userIds)) {
        response.setError(I18n.get(IExceptionMessage.RECORD_NONE_SELECTED));
        return;
      }

      ActionViewBuilder view =
          ActionView.define(I18n.get("Generate random passwords"))
              .model(Wizard.class.getName())
              .add("form", "generate-random-passwords-wizard")
              .param("popup", "reload")
              .param("show-toolbar", "false")
              .param("show-confirm", "false")
              .param("popup-save", "false")
              .context("_userIds", userIds);

      response.setView(view.map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @SuppressWarnings("unchecked")
  public void generateRandomPasswords(ActionRequest request, ActionResponse response) {
    try {
      List<Long> ids =
          Lists.transform(
              (List) request.getContext().get("_userIds"),
              new Function<Object, Long>() {
                @Nullable
                @Override
                public Long apply(@Nullable Object input) {
                  return Long.parseLong(input.toString());
                }
              });

      String currentUserPassword = (String) request.getContext().get("currentUserPassword");
      boolean isValidUser =
          Beans.get(UserService.class).verifyCurrentUserPassword(currentUserPassword);

      if (!isValidUser) {
        response.setError(I18n.get("Current user password is wrong."));
        return;
      }

      Beans.get(UserService.class).generateRandomPasswordForUsers(ids);
      response.setCanClose(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
