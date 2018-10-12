package com.backbase.ct.bbfuel.configurator;

import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_ROOT_ENTITLEMENTS_ADMIN;
import static com.backbase.ct.bbfuel.data.CommonConstants.PROPERTY_USE_PFM_CATEGORIES_FOR_TRANSACTIONS;
import static com.backbase.ct.bbfuel.util.CommonHelpers.getRandomFromStringList;
import static org.apache.http.HttpStatus.SC_CREATED;

import com.backbase.ct.bbfuel.client.accessgroup.UserContextPresentationRestClient;
import com.backbase.ct.bbfuel.client.common.LoginRestClient;
import com.backbase.ct.bbfuel.client.transaction.TransactionsIntegrationRestClient;
import com.backbase.ct.bbfuel.client.pfm.CategoriesPresentationRestClient;
import com.backbase.ct.bbfuel.data.CommonConstants;
import com.backbase.ct.bbfuel.data.TransactionsDataGenerator;
import com.backbase.ct.bbfuel.util.CommonHelpers;
import com.backbase.ct.bbfuel.util.GlobalProperties;
import com.backbase.integration.transaction.external.rest.spec.v2.transactions.TransactionsPostRequestBody;
import com.backbase.presentation.categories.management.rest.spec.v2.categories.id.CategoryGetResponseBody;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionsConfigurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionsConfigurator.class);
    private static GlobalProperties globalProperties = GlobalProperties.getInstance();

    private final TransactionsIntegrationRestClient transactionsIntegrationRestClient;
    private final CategoriesPresentationRestClient categoriesPresentationRestClient;
    private final LoginRestClient loginRestClient;
    private final UserContextPresentationRestClient userContextPresentationRestClient;
    private String rootEntitlementsAdmin = globalProperties.getString(PROPERTY_ROOT_ENTITLEMENTS_ADMIN);
    private Random random = new Random();

    public void ingestTransactionsByArrangement(String externalArrangementId) {
        List<TransactionsPostRequestBody> transactions = Collections.synchronizedList(new ArrayList<>());
        List<String> categoryNames = new ArrayList<>();

        if (globalProperties.getBoolean(PROPERTY_USE_PFM_CATEGORIES_FOR_TRANSACTIONS)) {
            loginRestClient.login(rootEntitlementsAdmin, rootEntitlementsAdmin);
            userContextPresentationRestClient.selectContextBasedOnMasterServiceAgreement();
            categoryNames = categoriesPresentationRestClient.retrieveCategories()
                .stream()
                .map(CategoryGetResponseBody::getCategoryName)
                .collect(Collectors.toList());
        }

        int randomAmount = CommonHelpers
            .generateRandomNumberInRange(globalProperties.getInt(CommonConstants.PROPERTY_TRANSACTIONS_MIN),
                globalProperties.getInt(CommonConstants.PROPERTY_TRANSACTIONS_MAX));
        List<String> finalCategoryNames = categoryNames;

        IntStream.range(0, randomAmount).parallel()
            .forEach(randomNumber -> {
                String categoryName = null;
                if (globalProperties.getBoolean(PROPERTY_USE_PFM_CATEGORIES_FOR_TRANSACTIONS)) {
                    categoryName = getRandomFromStringList(finalCategoryNames);
                }

                transactions.add(
                    TransactionsDataGenerator.generateTransactionsPostRequestBody(externalArrangementId, categoryName));
            });

        transactionsIntegrationRestClient.ingestTransactions(transactions)
            .then()
            .statusCode(SC_CREATED);

        LOGGER.info("Transactions [{}] ingested for arrangement [{}]", randomAmount, externalArrangementId);
    }
}
