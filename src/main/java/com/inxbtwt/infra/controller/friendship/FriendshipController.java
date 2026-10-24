package com.inxbtwt.infra.controller.friendship;

import com.inxbtwt.domain.dto.api.ApiResponse;
import com.inxbtwt.domain.dto.friendship.FriendCountResponse;
import com.inxbtwt.domain.dto.friendship.FriendshipPageResponse;
import com.inxbtwt.domain.dto.friendship.FriendshipResponse;
import com.inxbtwt.domain.enums.FriendRequestDirection;
import com.inxbtwt.domain.service.friendship.FriendshipService;
import com.inxbtwt.infra.controller.util.ApiControllerUtils;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/friendships")
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendshipService friendships;

    @GetMapping
    public ResponseEntity<ApiResponse<FriendshipPageResponse>> getFriends(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ApiControllerUtils.ok("Amigos encontrados.", friendships.getFriends(page, size));
    }

    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<FriendshipPageResponse>> getRequests(
            @RequestParam(defaultValue = "RECEIVED") FriendRequestDirection direction,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ApiControllerUtils.ok("Solicitações encontradas.", friendships.getRequests(direction, page, size));
    }

    @GetMapping("/count")
    public ResponseEntity<ApiResponse<FriendCountResponse>> countFriends() {
        return ApiControllerUtils.ok("Amigos contados.", new FriendCountResponse(friendships.countFriends()));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<FriendshipResponse>> getState(@PathVariable UUID userId) {
        return ApiControllerUtils.ok("Relação de amizade encontrada.", friendships.getState(userId));
    }

    @PostMapping("/{userId}/request")
    public ResponseEntity<ApiResponse<FriendshipResponse>> request(@PathVariable UUID userId) {
        return ApiControllerUtils.ok("Estado da solicitação verificado.", friendships.request(userId));
    }

    @PatchMapping("/{userId}/accept")
    public ResponseEntity<ApiResponse<FriendshipResponse>> accept(@PathVariable UUID userId) {
        return ApiControllerUtils.ok("Solicitação aceita.", friendships.accept(userId));
    }

    @PatchMapping("/{userId}/decline")
    public ResponseEntity<ApiResponse<FriendshipResponse>> decline(@PathVariable UUID userId) {
        return ApiControllerUtils.ok("Solicitação recusada.", friendships.decline(userId));
    }

    @DeleteMapping("/{userId}/cancel")
    public ResponseEntity<ApiResponse<FriendshipResponse>> cancel(@PathVariable UUID userId) {
        return ApiControllerUtils.ok("Solicitação cancelada.", friendships.cancel(userId));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<FriendshipResponse>> remove(@PathVariable UUID userId) {
        return ApiControllerUtils.ok("Amizade desfeita.", friendships.remove(userId));
    }
}
