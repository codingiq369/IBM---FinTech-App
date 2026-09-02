package com.fintechplatform.cardmanagement.web;

import com.fintechplatform.cardmanagement.domain.Card;
import com.fintechplatform.cardmanagement.dto.CardResponse;
import com.fintechplatform.cardmanagement.dto.IssueCardRequest;
import com.fintechplatform.cardmanagement.service.CardService;
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
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping
    public ResponseEntity<CardResponse> issueCard(@Valid @RequestBody IssueCardRequest request) {
        Card card = cardService.issueCard(request);
        return ResponseEntity.created(URI.create("/api/cards/" + card.getId())).body(CardResponse.from(card));
    }

    @PostMapping("/{id}/activate")
    public CardResponse activate(@PathVariable UUID id) {
        return CardResponse.from(cardService.activateCard(id));
    }

    @PostMapping("/{id}/block")
    public CardResponse block(@PathVariable UUID id) {
        return CardResponse.from(cardService.blockCard(id));
    }

    @GetMapping("/{id}")
    public CardResponse getById(@PathVariable UUID id) {
        return CardResponse.from(cardService.getById(id));
    }

    @GetMapping
    public List<CardResponse> search(
            @RequestParam(required = false) UUID accountId, @RequestParam(required = false) UUID customerId) {
        List<Card> cards;
        if (accountId != null) {
            cards = cardService.getByAccountId(accountId);
        } else if (customerId != null) {
            cards = cardService.getByCustomerId(customerId);
        } else {
            cards = List.of();
        }
        return cards.stream().map(CardResponse::from).toList();
    }
}
