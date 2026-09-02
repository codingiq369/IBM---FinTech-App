package com.fintechplatform.cardauthorization.client;

import com.fintechplatform.cardauthorization.service.CardNotFoundException;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Component
public class CardManagementClient {

    private final RestClient restClient;

    public CardManagementClient(@Qualifier("cardManagementServiceClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public CardResponse getCard(UUID cardId) {
        try {
            return restClient.get().uri("/api/cards/{id}", cardId).retrieve().body(CardResponse.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new CardNotFoundException(cardId);
        }
    }
}
