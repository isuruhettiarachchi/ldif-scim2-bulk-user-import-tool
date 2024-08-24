package org.wso2.ldif.scim2.handlers;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import static org.wso2.ldif.scim2.constants.Constants.PATH_SEPARATOR;

public class ApiHandler {

    private static final String SCIM_USER_ENDPOINT = "scim2/Users";

    public HttpResponse addUser(String userJSON, String host, String accessToken) {

        try {
            HttpPost request = getHttpPostRequest(accessToken, host, SCIM_USER_ENDPOINT);
            StringEntity entity = new StringEntity(userJSON, ContentType.APPLICATION_JSON);
            request.setEntity(entity);

            try (CloseableHttpClient client = HttpClientBuilder.create()
                    .setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build())
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .build()) {

                return client.execute(request);
            } catch (IOException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
                throw new RuntimeException("Error occurred while importing the users.", e);
            }
        } catch (URISyntaxException e) {
            System.out.println(e);
        }

        return null;
    }

    private static HttpPost getHttpPostRequest(String accessToken, String host, String path)
            throws URISyntaxException {

        String requestPath = "https://" + host + PATH_SEPARATOR + path;
        URIBuilder builder = new URIBuilder(requestPath);
        HttpPost request = new HttpPost(builder.build());

        request.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        request.addHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString());
        String authHeader = "Bearer " + accessToken;
        request.addHeader(HttpHeaders.AUTHORIZATION, authHeader);

        return request;
    }
}
