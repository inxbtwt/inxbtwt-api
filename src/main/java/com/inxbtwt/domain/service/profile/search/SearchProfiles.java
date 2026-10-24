package com.inxbtwt.domain.service.profile.search;

import com.inxbtwt.db.entity.UserDataEntity;
import com.inxbtwt.db.repository.UserDataRepository;
import com.inxbtwt.domain.dto.profile.http.ProfileSummaryResponse;
import com.inxbtwt.domain.util.UserAuthUtil;
import com.inxbtwt.infra.exception.domain.user.UserInvalidException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Executa a busca paginada de perfis para usuários autenticados.
 *
 * <p>Rejeita termos nulos ou em branco e remove os espaços nas extremidades antes
 * de consultar. Cada implementação de {@link ProfileSearchCriterion} registrada
 * no Spring transforma o termo em uma {@link Specification}. Os critérios são
 * combinados com OR: basta outro usuário atender a um deles para aparecer no resultado.
 * A ausência de critérios configurados gera uma {@link IllegalStateException}.
 * O UUID do usuário autenticado é excluído com AND sobre o conjunto de critérios,
 * antes da paginação e da contagem, inclusive quando o termo é seu próprio username.
 *
 * <p>Atualmente, {@link UsernameSearchCriterion} procura o termo em qualquer parte
 * do username, sem distinguir maiúsculas de minúsculas. Os caracteres de escape
 * e os curingas do LIKE são tratados literalmente. Por exemplo, {@code luc}
 * encontra {@code lucas123}; {@code lucs} não encontra {@code lucas} por aproximação.
 * O nome de exibição ainda não participa da busca.
 *
 * <p>Para adicionar buscas por nome ou outros critérios, registre uma nova
 * implementação de {@link ProfileSearchCriterion} como componente Spring.
 * Ela será combinada com as demais regras, sem alterar o endpoint. Estratégias
 * de prioridade, fallback ou ordenação por relevância exigem adaptar a composição.
 *
 * <p>A consulta é executada no banco com a paginação e a ordenação do
 * {@link Pageable} recebido. Cada resultado é convertido em
 * {@link ProfileSummaryResponse}, contendo UUID, nome, username e URL da foto,
 * sem expor dados de contato ou credenciais.
 */
@Service
@RequiredArgsConstructor
public class SearchProfiles implements SearchProfilesUseCase {

    private final UserDataRepository userRepository;
    private final UserAuthUtil userAuthUtil;
    private final List<ProfileSearchCriterion> searchCriteria;

    @Override
    @Transactional(readOnly = true)
    public Page<ProfileSummaryResponse> searchProfiles(String query, Pageable pageable) {
        var currentUserId = userAuthUtil.getUserId();
        if (query == null || query.isBlank()) {
            throw new UserInvalidException("Informe um username para pesquisar.");
        }

        String search = query.trim();
        Specification<UserDataEntity> specification = searchCriteria.stream()
                .map(criterion -> criterion.toSpecification(search))
                .reduce(Specification::or)
                .orElseThrow(() -> new IllegalStateException("Nenhum critério de pesquisa de perfil configurado."));

        Specification<UserDataEntity> otherUsers = (root, criteriaQuery, builder) ->
                builder.notEqual(root.get("userId"), currentUserId);

        return userRepository.findAll(specification.and(otherUsers), pageable)
                .map(user -> new ProfileSummaryResponse(user.getUserId(), user.getName(),
                        user.getUsername(), user.getPhotoUrl()));
    }
}
