package com.axelor.apps.bankpayment.rest;

import com.axelor.apps.bankpayment.service.bankorder.BankOrderEncryptionService;
import com.axelor.apps.base.AxelorException;
import com.axelor.auth.AuthService;
import com.axelor.common.http.ContentDisposition;
import com.axelor.db.JPA;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

@Path("/aos/bankorder")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class BankOrderRestController {

  protected AuthService authService;
  protected BankOrderEncryptionService bankOrderEncryptionService;

  @Inject
  public BankOrderRestController(
      AuthService authService, BankOrderEncryptionService bankOrderEncryptionService) {
    this.authService = authService;
    this.bankOrderEncryptionService = bankOrderEncryptionService;
  }

  @GET
  @Path("file-download/{password}/{id}")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response download(@PathParam("id") Long id, @PathParam("password") String password)
      throws IOException, AxelorException {

    String encryptionPassword = bankOrderEncryptionService.checkAndGetEncryptionPassword();

    byte[] decode = Base64.getUrlDecoder().decode(password);
    String decodedPassword = new String(decode, StandardCharsets.UTF_8);

    if (password == null || !authService.match(encryptionPassword, decodedPassword)) {
      return Response.status(Response.Status.FORBIDDEN).build();
    }

    MetaFile bankOrderGeneratedFile = JPA.find(MetaFile.class, id);

    if (bankOrderGeneratedFile == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    File bankOrderFile = MetaFiles.getPath(bankOrderGeneratedFile).toFile();
    String fileName = bankOrderFile.getName();

    byte[] decrypt = bankOrderEncryptionService.getDecryptedBytes(bankOrderFile);
    return Response.ok(
            new StreamingOutput() {
              @Override
              public void write(OutputStream output) throws IOException, WebApplicationException {
                try (BufferedOutputStream bufferedOutput = new BufferedOutputStream(output)) {
                  bufferedOutput.write(decrypt);
                  bufferedOutput.flush();
                }
              }
            })
        .header(
            "Content-Disposition",
            ContentDisposition.attachment().filename(fileName).build().toString())
        .build();
  }
}
