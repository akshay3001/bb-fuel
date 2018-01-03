package com.backbase.testing.dataloader.clients.productsummary;

import com.backbase.integration.product.rest.spec.v2.products.ProductsPostRequestBody;
import com.backbase.testing.dataloader.clients.common.RestClient;
import com.backbase.testing.dataloader.utils.GlobalProperties;
import com.backbase.integration.arrangement.rest.spec.v2.arrangements.ArrangementsPostRequestBody;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_ENTITLEMENTS_BASE_URI;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_PRODUCTSUMMARY_BASE_URI;

public class ArrangementsIntegrationRestClient extends RestClient {

    private static GlobalProperties globalProperties = GlobalProperties.getInstance();
    private static final String SERVICE_VERSION = "v2";
    private static final String ARRANGEMENTS_INTEGRATION_SERVICE = "arrangements-integration-service";
    private static final String ENDPOINT_ARRANGEMENTS = "/arrangements";
    private static final String ENDPOINT_PRODUCTS = "/products";

    public ArrangementsIntegrationRestClient() {
        super(globalProperties.getString(PROPERTY_PRODUCTSUMMARY_BASE_URI), SERVICE_VERSION);
        setInitialPath(ARRANGEMENTS_INTEGRATION_SERVICE);
    }

    public Response ingestArrangement(ArrangementsPostRequestBody body) {
        return requestSpec()
                .contentType(ContentType.JSON)
                .body(body)
                .post(getPath(ENDPOINT_ARRANGEMENTS));
    }

    public Response ingestProduct(ProductsPostRequestBody body) {
        return requestSpec()
                .contentType(ContentType.JSON)
                .body(body)
                .post(getPath(ENDPOINT_PRODUCTS));
    }
}
