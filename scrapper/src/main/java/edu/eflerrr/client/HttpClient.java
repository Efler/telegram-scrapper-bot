package edu.eflerrr.client;

import edu.eflerrr.client.response.HttpClientResponse;

public interface HttpClient {
    HttpClientResponse fetchResponse(String endpoint);
}
