package com.inxbtwt.domain.service.feed;

import com.inxbtwt.db.entity.PostEntity;
import com.inxbtwt.domain.dto.feed.FeedContext;
import org.springframework.data.domain.Slice;

/**
 * Define uma versão de seleção e ordenação das publicações do feed.
 *
 * <p>Implemente esta interface como bean Spring para registrar uma recomendação.
 * A chave deve ser única e estável, por exemplo {@code recommended-v1}.
 * Cada estratégia controla sua consulta e deve respeitar a página e o tamanho
 * recebidos, mantendo a ordem recomendada e os metadados de continuação.
 * Carregue os autores dos posts na consulta para evitar consultas adicionais
 * durante o mapeamento. O usuário do contexto vem da sessão, nunca do cliente.
 */
public interface FeedStrategy {

    String key();

    Slice<PostEntity> getFeed(FeedContext context);
}
