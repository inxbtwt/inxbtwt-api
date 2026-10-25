package com.inxbtwt.domain.service.post;

import com.inxbtwt.infra.exception.domain.DomainException;

import java.util.regex.Pattern;

/** Extrai somente o ID público; HTML, scripts e URLs recebidos nunca são executados ou acessados. */
public final class TenorEmbedParser {

    private static final Pattern EMBED_ID = Pattern.compile(
            "data-postid\\s*=\\s*[\"']([0-9]{1,20})[\"']", Pattern.CASE_INSENSITIVE);
    private static final Pattern LINK_ID = Pattern.compile(
            "https://tenor\\.com/(?:embed/([0-9]{1,20})|view/[A-Za-z0-9_%~.-]+-([0-9]{1,20}))/?",
            Pattern.CASE_INSENSITIVE);

    private TenorEmbedParser() {
    }

    public static String parse(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        if (value.length() <= 5000) {
            var embed = EMBED_ID.matcher(value);
            if (value.contains("tenor-gif-embed") && embed.find()) {
                return embed.group(1);
            }
            var link = LINK_ID.matcher(value.strip());
            if (link.matches()) {
                return link.group(1) == null ? link.group(2) : link.group(1);
            }
        }
        throw new DomainException("Cole um código de incorporação ou link válido do Tenor.");
    }
}
