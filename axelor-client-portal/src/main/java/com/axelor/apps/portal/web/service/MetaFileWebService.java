/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
package com.axelor.apps.portal.web.service;

import com.axelor.db.JpaSecurity;
import com.axelor.db.JpaSecurity.AccessType;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.repo.MetaFileRepository;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.shiro.authz.UnauthorizedException;

@Path("/")
public class MetaFileWebService extends AbstractWebService {

  @GET
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response fetchOne(@PathParam("id") Long id) {
    try {
      Beans.get(JpaSecurity.class).check(AccessType.READ, MetaFile.class, id);
      MetaFile metaFile = Beans.get(MetaFileRepository.class).find(id);
      if (metaFile == null || !MetaFiles.getPath(metaFile).toFile().exists()) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_NO_VALUE,
            I18n.get("File with given id (%s) not found or does not exists"),
            id);
      }
      return Response.ok()
          .entity(MetaFiles.getPath(metaFile).toFile())
          .header("Content-Disposition", "inline;filename=" + metaFile.getFileName())
          .build();
    } catch (UnauthorizedException e) {
      return autherizationFail(e);
    } catch (Exception e) {
      return fail(e);
    }
  }
}
