package com.backbase.ct.bbfuel.configurator;

import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_USE_PFM_CATEGORIES_FOR_TRANSACTIONS;
import static org.apache.http.HttpStatus.SC_CREATED;

import com.backbase.ct.bbfuel.client.accessgroup.UserContextPresentationRestClient;
import com.backbase.ct.bbfuel.client.common.LoginRestClient;
import com.backbase.ct.bbfuel.client.pfm.CategoriesPresentationRestClient;
import com.backbase.ct.bbfuel.client.transaction.TransactionsIntegrationRestClient;
import com.backbase.ct.bbfuel.data.CommonConstants;
import com.backbase.ct.bbfuel.data.TransactionsDataGenerator;
import com.backbase.ct.bbfuel.input.TransactionsReader;
import com.backbase.ct.bbfuel.util.CommonHelpers;
import com.backbase.ct.bbfuel.util.GlobalProperties;
import com.backbase.integration.transaction.external.rest.spec.v2.transactions.TransactionsPostRequestBody;
import com.backbase.presentation.categories.management.rest.spec.v2.categories.SubCategory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionsConfigurator {

    private static GlobalProperties globalProperties = GlobalProperties.getInstance();

    private final TransactionsReader reader = new TransactionsReader();

    private final TransactionsIntegrationRestClient transactionsIntegrationRestClient;
    private final CategoriesPresentationRestClient categoriesPresentationRestClient;
    private final LoginRestClient loginRestClient;
    private final UserContextPresentationRestClient userContextPresentationRestClient;

    public void ingestTransactionsByArrangement(String externalArrangementId, boolean isRetail) {
        List<TransactionsPostRequestBody> transactions = Collections.synchronizedList(new ArrayList<>());
        List<SubCategory> retailCategories = new ArrayList<>();

        if (globalProperties.getBoolean(PROPERTY_USE_PFM_CATEGORIES_FOR_TRANSACTIONS)) {
            loginRestClient.loginBankAdmin();
            userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();
            retailCategories = categoriesPresentationRestClient.retrieveCategories();
        }

        int randomAmount = CommonHelpers
            .generateRandomNumberInRange(globalProperties.getInt(CommonConstants.PROPERTY_TRANSACTIONS_MIN),
                globalProperties.getInt(CommonConstants.PROPERTY_TRANSACTIONS_MAX));

        List<SubCategory> finalCategories = new ArrayList<>(retailCategories);

        if (isRetail) {
            // Add 1 check images per account.
            transactions.add(reader.loadSingleWithCheckImages(externalArrangementId));

            // After that ingest rest of the transactions.
            IntStream.range(0, randomAmount).parallel()
                    .forEach(randomNumber -> transactions.add(
                            reader.loadSingle(externalArrangementId)));
        } else {
            IntStream.range(0, randomAmount).parallel()
                    .forEach(randomNumber -> transactions.add(
                            TransactionsDataGenerator.generateTransactionsPostRequestBody(externalArrangementId, isRetail, finalCategories)));
        }
        transactionsIntegrationRestClient.ingestTransactions(transactions)
            .then()
            .statusCode(SC_CREATED);

        log.info("Transactions [{}] ingested for arrangement [{}]", randomAmount, externalArrangementId);
    }
}
