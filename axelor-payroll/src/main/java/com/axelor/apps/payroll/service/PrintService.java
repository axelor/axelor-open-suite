package com.axelor.apps.payroll.service;

import com.axelor.apps.payroll.db.ProcessedEmployeePayrollData;
import com.axelor.mail.MailException;
import com.axelor.mail.service.MailServiceImpl;
import com.axelor.report.ReportGenerator;
import com.google.inject.Inject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.*;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.eclipse.birt.core.exception.BirtException;

public class PrintService {
  @Inject private ReportGenerator generator;
  @Inject private MailServiceImpl mailService;

  public void print(
      String design, Map<String, Object> params, ProcessedEmployeePayrollData pEmpData)
      throws IOException, BirtException, MailException {

    // Create Report file
    String parentDirName = "Payslip Reports";
    File parentDir = new File(parentDirName);
    if (!parentDir.exists()) {
      parentDir.mkdirs();
    }

    String dirName =
        parentDir.getAbsolutePath() + "/" + pEmpData.getMonth() + "_" + pEmpData.getYear();
    File dir = new File(dirName);
    if (!dir.exists()) {
      dir.mkdirs();
    }

    String fileName =
        dir.getAbsolutePath()
            + "/Payslip_"
            + pEmpData.getEmployee().getEmployee().getContactPartner().getFullName()
            + "_"
            + pEmpData.getMonth()
            + "_"
            + pEmpData.getYear()
            + ".pdf";
    File file = new File(fileName);
    Files.deleteIfExists(file.toPath());
    file.createNewFile();
    OutputStream output = new FileOutputStream(file);

    try {
      generator.generate(output, design, "pdf", params, Locale.getDefault());
    } finally {
      output.close();
    }

    // Send email
    String host = "smtp.gmail.com";

    Properties properties = System.getProperties();
    properties.setProperty("mail.smtp.auth", "true");
    properties.setProperty("mail.smtp.starttls.enable", "true");
    properties.setProperty("mail.smtp.host", host);
    properties.setProperty("mail.smtp.port", "587");

    String myAccountEmail = "excenitlimited@gmail.com";
    String password = "nvegmltmqakrzbjo";

    Session session =
        Session.getDefaultInstance(
            properties,
            new Authenticator() {
              @Override
              protected PasswordAuthentication getPasswordAuthentication() {
                // TODO Auto-generated method stub
                return new PasswordAuthentication(myAccountEmail, password);
              }
            });

    try {
      // Configure the message
      MimeMessage message = new MimeMessage(session);
      message.setFrom(new InternetAddress(myAccountEmail));
      message.addRecipient(
          Message.RecipientType.TO,
          new InternetAddress(
              pEmpData
                  .getEmployee()
                  .getEmployee()
                  .getContactPartner()
                  .getEmailAddress()
                  .getAddress()));

      message.setSubject("Pay for " + pEmpData.getMonth() + " " + pEmpData.getYear());
      String[] name = pEmpData.getEmployee().getEmployee().getName().split("-", 2);

      // Add the message
      BodyPart messageBodyPart = new MimeBodyPart();
      messageBodyPart.setText(
          "Dear "
              + name[1]
              + ",\n"
              + "\n"
              + "Kindly find attached your payslip for "
              + pEmpData.getMonth()
              + " "
              + pEmpData.getYear()
              + ".Your bank account has been credited accordingly.\n"
              + "\n"
              + "Regards.");

      Multipart multipart = new MimeMultipart();
      multipart.addBodyPart(messageBodyPart);

      // Add attachment
      messageBodyPart = new MimeBodyPart();
      DataSource source = new FileDataSource(fileName);
      messageBodyPart.setDataHandler(new DataHandler(source));
      fileName =
          "Payslip_"
              + pEmpData.getEmployee().getEmployee().getName()
              + "_"
              + pEmpData.getMonth()
              + "_"
              + pEmpData.getYear()
              + ".pdf";
      messageBodyPart.setFileName(fileName);
      multipart.addBodyPart(messageBodyPart);

      message.setContent(multipart);

      Transport.send(message);

    } catch (MessagingException mex) {
      mex.printStackTrace();
    }
  }
}
