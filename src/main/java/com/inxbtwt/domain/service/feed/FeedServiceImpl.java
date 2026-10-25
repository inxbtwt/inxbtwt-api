package com.inxbtwt.domain.service.feed;

import com.inxbtwt.domain.dto.feed.FeedContext;
import com.inxbtwt.domain.dto.feed.http.FeedResponse;
import com.inxbtwt.domain.mapper.post.PostMapper;
import com.inxbtwt.domain.util.UserAuthUtil;
import com.inxbtwt.infra.exception.domain.DomainException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Orquestra o feed sem definir as regras de recomendação.
 *
 * <p>Registra as implementações de {@link FeedStrategy} injetadas pelo Spring,
 * rejeitando chaves vazias ou duplicadas na inicialização. A estratégia padrão
 * vem de {@code app.feed.default-strategy}. Para criar outra versão, adicione
 * um bean com chave própria; o controller e o formato da resposta permanecem iguais.
 *
 * <p>Autentica o solicitante, valida a paginação e delega a seleção à estratégia.
 * O mapeamento preserva a ordem devolvida, sem reordenar posts por data ou ID.
 * Usa {@code Slice} para indicar a próxima página sem exigir uma contagem global,
 * que pode não ser adequada a algoritmos de recomendação.
 */
@Service
public class FeedServiceImpl implements FeedService {

    private final UserAuthUtil userAuthUtil;
    private final PostMapper postMapper;
    private final Map<String, FeedStrategy> strategies;
    private final String defaultStrategy;

    public FeedServiceImpl(UserAuthUtil userAuthUtil, PostMapper postMapper,
            List<FeedStrategy> strategies,
            @Value("${app.feed.default-strategy:chronological}") String defaultStrategy) {
        this.userAuthUtil = userAuthUtil;
        this.postMapper = postMapper;
        var registered = new HashMap<String, FeedStrategy>();
        for (var strategy : strategies) {
            String key = strategy.key();
            if (key == null || key.isBlank()) {
                throw new IllegalStateException("Toda estratégia de feed deve ter uma chave.");
            }
            String normalizedKey = normalize(key);
            if (registered.putIfAbsent(normalizedKey, strategy) != null) {
                throw new IllegalStateException("Estratégia de feed duplicada: " + normalizedKey);
            }
        }
        this.strategies = Map.copyOf(registered);
        if (defaultStrategy == null || defaultStrategy.isBlank()
                || !this.strategies.containsKey(normalize(defaultStrategy))) {
            throw new IllegalStateException("A estratégia padrão do feed não está registrada.");
        }
        this.defaultStrategy = normalize(defaultStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public FeedResponse getFeed(String strategy, int page, int size) {
        var userId = userAuthUtil.getUserId();
        if (page < 0 || size < 1 || size > 100) {
            throw new DomainException("Informe página não negativa e tamanho entre 1 e 100.");
        }
        String key = strategy == null || strategy.isBlank() ? defaultStrategy : normalize(strategy);
        var selected = strategies.get(key);
        if (selected == null) {
            throw new DomainException("Estratégia de feed não reconhecida: " + key);
        }

        var posts = selected.getFeed(new FeedContext(userId, page, size));
        var content = postMapper.toResponses(posts.getContent());
        return new FeedResponse(key, content, posts.getNumber(), posts.getSize(),
                posts.hasNext(), posts.hasPrevious());
    }

    private static String normalize(String key) {
        return key.trim().toLowerCase(Locale.ROOT);
    }
}
