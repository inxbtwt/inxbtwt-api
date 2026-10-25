package com.inxbtwt.domain.dto.feed;

import java.util.UUID;

/** Contexto confiável da consulta, incluindo o usuário obtido da sessão autenticada. */
public record FeedContext(UUID userId, int page, int size) {
}
