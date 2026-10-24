package com.inxbtwt.infra.controller.notification;

import com.inxbtwt.domain.dto.api.ApiResponse;
import com.inxbtwt.domain.dto.notification.NotificationPageResponse;
import com.inxbtwt.domain.dto.notification.NotificationResponse;
import com.inxbtwt.domain.dto.notification.UnreadNotificationCountResponse;
import com.inxbtwt.domain.service.notification.NotificationInboxService;
import com.inxbtwt.infra.controller.util.ApiControllerUtils;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationInboxService inbox;

    @GetMapping
    public ResponseEntity<ApiResponse<NotificationPageResponse>> getNotifications(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ApiControllerUtils.ok("Notificações encontradas.", inbox.getNotifications(page, size));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<UnreadNotificationCountResponse>> countUnread() {
        return ApiControllerUtils.ok("Notificações não lidas contadas.",
                new UnreadNotificationCountResponse(inbox.countUnread()));
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(@PathVariable UUID notificationId) {
        return ApiControllerUtils.ok("Notificação marcada como lida.", inbox.markAsRead(notificationId));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        inbox.markAllAsRead();
        return ApiControllerUtils.ok("Notificações marcadas como lidas.", null);
    }
}
