package com.inxbtwt.domain.service.feed.chronological;

import com.inxbtwt.db.entity.PostEntity;
import com.inxbtwt.db.repository.PostRepository;
import com.inxbtwt.domain.dto.feed.FeedContext;
import com.inxbtwt.domain.service.feed.FeedStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

/** Feed global de texto: mais recentes primeiro, com desempate pelo ID interno. */
@Component
@RequiredArgsConstructor
public class ChronologicalFeedStrategy implements FeedStrategy {

    private final PostRepository postRepository;

    @Override
    public String key() {
        return "chronological";
    }

    @Override
    public Slice<PostEntity> getFeed(FeedContext context) {
        return postRepository.findByParentIsNullAndDeletedAtIsNullOrderByCreatedAtDescIdDesc(
                PageRequest.of(context.page(), context.size()));
    }
}
