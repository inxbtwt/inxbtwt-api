package com.inxbtwt.domain.service.notification;

import com.inxbtwt.domain.dto.notification.NotificationCommand;
import com.inxbtwt.domain.enums.NotificationChannel;
import com.inxbtwt.infra.exception.domain.DomainException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final Map<NotificationChannel, NotificationSender> senders;
    private final Validator validator;

    public NotificationServiceImpl(List<NotificationSender> implementations, Validator validator) {
        var registry = new EnumMap<NotificationChannel, NotificationSender>(NotificationChannel.class);
        for (var sender : implementations) {
            var channel = sender.channel();
            if (channel == null || registry.putIfAbsent(channel, sender) != null) {
                throw new IllegalStateException("Canal de notificação inválido ou duplicado: " + channel);
            }
        }
        this.senders = Map.copyOf(registry);
        this.validator = validator;
    }

    @Override
    public void send(NotificationCommand command, NotificationChannel channel) {
        if (command == null || channel == null) {
            throw new DomainException("Informe os dados e o canal da notificação.");
        }
        var violations = validator.validate(command);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .distinct()
                    .sorted()
                    .collect(Collectors.joining(" "));
            throw new DomainException(message);
        }
        var sender = senders.get(channel);
        if (sender == null) {
            throw new DomainException("Canal de notificação ainda não configurado: " + channel);
        }
        sender.send(command);
    }
}
