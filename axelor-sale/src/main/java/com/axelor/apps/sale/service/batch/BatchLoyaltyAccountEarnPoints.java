package com.axelor.apps.sale.service.batch;

import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.sale.db.LoyaltyAccount;
import com.axelor.apps.sale.db.repo.LoyaltyAccountRepository;
import com.axelor.apps.sale.exception.SaleExceptionMessage;
import com.axelor.apps.sale.service.loyalty.LoyaltyAccountService;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchLoyaltyAccountEarnPoints extends BatchStrategy {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject
  protected BatchLoyaltyAccountEarnPoints(
      LoyaltyAccountService loyaltyAccountService,
      LoyaltyAccountRepository loyaltyAccountRepository) {
    super(loyaltyAccountService, loyaltyAccountRepository);
  }

  @Override
  protected void process() {
    int offset = 0;
    List<LoyaltyAccount> loyaltyAccountList;
    Query<LoyaltyAccount> loyaltyAccountQuery =
        loyaltyAccountRepository
            .all()
            .order("id")
            .filter("self.pointsBalance != self.futurePointsBalance");
    while (!(loyaltyAccountList = loyaltyAccountQuery.fetch(FETCH_LIMIT, offset)).isEmpty()) {
      findBatch();
      for (LoyaltyAccount loyaltyAccount : loyaltyAccountList) {
        ++offset;
        try {
          loyaltyAccountService.acquirePoints(
              loyaltyAccount, batch.getSaleBatch().getLoyaltyAccountPointsAcquiringDelay());
          updateLoyaltyAccount(loyaltyAccount);
        } catch (Exception e) {
          TraceBackService.trace(
              new Exception(
                  String.format(I18n.get("Loyalty account with id %s"), loyaltyAccount.getId()), e),
              "Loyalty account",
              batch.getId());
          incrementAnomaly();

          LOG.error(
              SaleExceptionMessage.BATCH_LOYALTY_ACCOUNT_EARN_POINTS_3, loyaltyAccount.getId());
        }
      }
      JPA.clear();
    }
  }

  /**
   * As {@code batch} entity can be detached from the session, call {@code Batch.find()} get the
   * entity in the persistent context. Warning : {@code batch} entity have to be saved before.
   */
  @Override
  protected void stop() {

    String comment = I18n.get(SaleExceptionMessage.BATCH_LOYALTY_ACCOUNT_EARN_POINTS_1) + " ";
    comment +=
        String.format(
            "\t* %s " + I18n.get(SaleExceptionMessage.BATCH_LOYALTY_ACCOUNT_EARN_POINTS_2) + "\n",
            batch.getDone());
    comment +=
        String.format("\t" + I18n.get(BaseExceptionMessage.BASE_BATCH_3), batch.getAnomaly());

    super.stop();
    addComment(comment);
  }
}
