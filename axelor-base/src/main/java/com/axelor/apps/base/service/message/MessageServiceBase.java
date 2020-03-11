package com.axelor.apps.base.service.message;

import com.axelor.apps.message.db.EmailAccount;
import com.axelor.apps.message.db.EmailAddress;
import com.axelor.apps.message.db.Message;
import com.axelor.meta.db.MetaFile;
import java.util.List;
import java.util.Set;

public interface MessageServiceBase {

  public Message createMessage(
      String model,
      int id,
      String subject,
      String content,
      EmailAddress fromEmailAddress,
      List<EmailAddress> replyToEmailAddressList,
      List<EmailAddress> toEmailAddressList,
      List<EmailAddress> ccEmailAddressList,
      List<EmailAddress> bccEmailAddressList,
      Set<MetaFile> metaFiles,
      String addressBlock,
      int mediaTypeSelect,
      EmailAccount emailAccount,
      String signature);

  public Message createMessage(
      String content,
      EmailAddress fromEmailAddress,
      String relatedTo1Select,
      long relatedTo1SelectId,
      String relatedTo2Select,
      long relatedTo2SelectId,
      boolean sentByEmail,
      int statusSelect,
      String subject,
      int typeSelect,
      List<EmailAddress> replyToEmailAddressList,
      List<EmailAddress> toEmailAddressList,
      List<EmailAddress> ccEmailAddressList,
      List<EmailAddress> bccEmailAddressList,
      String addressBlock,
      int mediaTypeSelect,
      EmailAccount emailAccount,
      String signature);
}
