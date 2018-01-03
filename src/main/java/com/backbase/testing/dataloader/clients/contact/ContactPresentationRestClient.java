package com.backbase.testing.dataloader.clients.contact;

import com.backbase.presentation.contact.rest.spec.v2.contacts.ContactsPostRequestBody;
import com.backbase.testing.dataloader.clients.common.RestClient;
import com.backbase.testing.dataloader.utils.GlobalProperties;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_GATEWAY_PATH;
import static com.backbase.testing.dataloader.data.CommonConstants.PROPERTY_INFRA_BASE_URI;

public class ContactPresentationRestClient extends RestClient {

    private static GlobalProperties globalProperties = GlobalProperties.getInstance();
    private static final String SERVICE_VERSION = "v2";
    private static final String CONTACT_PRESENTATION_SERVICE = "contact-presentation-service";
    private static final String ENDPOINT_CONTACTS = "/contacts";

    public ContactPresentationRestClient() {
        super(globalProperties.getString(PROPERTY_INFRA_BASE_URI), SERVICE_VERSION);
        setInitialPath(globalProperties.getString(PROPERTY_GATEWAY_PATH) + "/" + CONTACT_PRESENTATION_SERVICE);
    }

    public Response createContact(ContactsPostRequestBody body) {
        return requestSpec()
                .contentType(ContentType.JSON)
                .body(body)
                .post(getPath(ENDPOINT_CONTACTS));
    }
}
