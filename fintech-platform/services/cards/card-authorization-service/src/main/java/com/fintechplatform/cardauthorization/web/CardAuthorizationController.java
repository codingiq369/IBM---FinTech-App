package com.fintechplatform.cardauthorization.web;

import com.fintechplatform.cardauthorization.domain.CardAuthorization;
import com.fintechplatform.cardauthorization.dto.AuthorizePurchaseRequest;
import com.fintechplatform.cardauthorization.dto.CardAuthorizationResponse;
import com.fintechplatform.cardauthorization.service.CardAuthorizationService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/card-authorizations")
public class CardAuthorizationController {

    private final CardAuthorizationService cardAuthorizationService;

    public CardAuthorizationController(CardAuthorizationService cardAuthorizationService) {
        this.cardAuthorizationService = cardAuthorizationService;
    }

    /**
     * Returns 201 whether the authorization ended up APPROVED or DECLINED —
     * the request to attempt a purchase was well-formed either way, and the
     * body's {@code status} field is how the caller finds out which. A 4xx
     * here means the request itself was invalid (no such card); it never
     * means "the purchase was declined".
     */
    @PostMapping
    public ResponseEntity<CardAuthorizationResponse> authorize(@Valid @RequestBody AuthorizePurchaseRequest request) {
        CardAuthorization authorization = cardAuthorizationService.authorizePurchase(request);
        return ResponseEntity.created(URI.create("/api/card-authorizations/" + authorization.getId()))
                .body(CardAuthorizationResponse.from(authorization));
    }

    @GetMapping("/{id}")
    public CardAuthorizationResponse getById(@PathVariable UUID id) {
        return CardAuthorizationResponse.from(cardAuthorizationService.getById(id));
    }

    @GetMapping
    public List<CardAuthorizationResponse> getByCard(@RequestParam UUID cardId) {
        return cardAuthorizationService.getByCardId(cardId).stream().map(CardAuthorizationResponse::from).toList();
    }
}
