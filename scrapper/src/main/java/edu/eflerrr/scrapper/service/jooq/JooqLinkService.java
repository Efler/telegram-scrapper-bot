package edu.eflerrr.scrapper.service.jooq;

import edu.eflerrr.scrapper.domain.jdbc.dto.Link;
import edu.eflerrr.scrapper.exception.DuplicateLinkPostException;
import edu.eflerrr.scrapper.exception.LinkNotFoundException;
import edu.eflerrr.scrapper.exception.TgChatNotExistException;
import edu.eflerrr.scrapper.service.LinkService;
import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.transaction.annotation.Transactional;
import static edu.eflerrr.jooqcodegen.generated.Tables.CHAT;
import static edu.eflerrr.jooqcodegen.generated.Tables.LINK;
import static edu.eflerrr.jooqcodegen.generated.Tables.TRACKING;
import static edu.eflerrr.scrapper.configuration.TimeConstants.MIN_DATE_TIME;

@RequiredArgsConstructor
@Slf4j
public class JooqLinkService implements LinkService {

    private final DSLContext dsl;
    private final String autoUsernamePrefix = "auto-username@";
    private final String chatNotFoundErrorMessage = "Chat not found!";

    @Override
    @Transactional
    public Link add(long tgChatId, URI url) {
        log.debug("ADD IN LINK-SERVICE (JOOQ): tgChatId: {}, url: {}", tgChatId, url);
        if (!dsl.fetchExists(
            dsl.selectFrom(CHAT)
                .where(CHAT.ID.eq(tgChatId)
                    .and(CHAT.USERNAME.eq(autoUsernamePrefix + tgChatId))
                ))
        ) {
            throw new TgChatNotExistException(chatNotFoundErrorMessage);
        }

        if (!dsl.fetchExists(
            dsl.selectFrom(LINK)
                .where(LINK.URL.eq(url.toString())))
        ) {
            dsl.insertInto(LINK)
                .set(LINK.URL, url.toString())
                .set(LINK.CREATED_AT, OffsetDateTime.now(ZoneOffset.UTC))
                .set(LINK.CHECKED_AT, MIN_DATE_TIME)
                .set(LINK.UPDATED_AT, OffsetDateTime.now(ZoneOffset.UTC))
                .execute();
        }

        Long linkId = dsl.select(LINK.ID)
            .from(LINK)
            .where(LINK.URL.eq(url.toString()))
            .fetchOne(LINK.ID);

        if (dsl.fetchExists(
            dsl.selectFrom(TRACKING)
                .where(TRACKING.CHAT_ID.eq(tgChatId)
                    .and(TRACKING.LINK_ID.eq(linkId))
                ))
        ) {
            throw new DuplicateLinkPostException("Tracking already exists!");
        }

        dsl.insertInto(TRACKING)
            .set(TRACKING.CHAT_ID, tgChatId)
            .set(TRACKING.LINK_ID, linkId)
            .execute();

        return dsl.selectFrom(LINK)
            .where(LINK.ID.eq(linkId))
            .fetchOneInto(Link.class);
    }

    @Override
    @Transactional
    public Link delete(long tgChatId, URI url) {
        log.debug("DELETE IN LINK-SERVICE (JOOQ): tgChatId: {}, url: {}", tgChatId, url);
        Long linkId = dsl.select(LINK.ID)
            .from(LINK)
            .where(LINK.URL.eq(url.toString()))
            .fetchOneInto(Long.class);
        if (linkId == null) {
            throw new LinkNotFoundException("Link not found!");
        }

        if (!dsl.fetchExists(
            dsl.selectFrom(CHAT)
                .where(CHAT.ID.eq(tgChatId)
                    .and(CHAT.USERNAME.eq(autoUsernamePrefix + tgChatId))
                ))
        ) {
            throw new TgChatNotExistException(chatNotFoundErrorMessage);
        }

        var checker = dsl.deleteFrom(TRACKING)
            .where(TRACKING.CHAT_ID.eq(tgChatId).and(TRACKING.LINK_ID.eq(linkId)))
            .execute();
        if (checker == 0) {
            throw new LinkNotFoundException("Tracking not found!");
        }

        Link deletedLink = dsl.selectFrom(LINK)
            .where(LINK.ID.eq(linkId))
            .fetchOneInto(Link.class);

        if (deletedLink != null
            && dsl.fetchCount(TRACKING, TRACKING.LINK_ID.eq(linkId)) == 0) {
            dsl.deleteFrom(LINK)
                .where(LINK.ID.eq(linkId))
                .execute();
        }

        return deletedLink;
    }

    @Override
    @Transactional
    public List<Link> listAll(long tgChatId) {
        log.debug("LIST-ALL IN LINK-SERVICE (JOOQ): tgChatId: {}", tgChatId);
        if (!dsl.fetchExists(
            dsl.selectFrom(CHAT)
                .where(CHAT.ID.eq(tgChatId)
                    .and(CHAT.USERNAME.eq(autoUsernamePrefix + tgChatId))
                ))
        ) {
            throw new TgChatNotExistException(chatNotFoundErrorMessage);
        }

        return dsl.select(
                LINK.ID, LINK.URL,
                LINK.CREATED_AT, LINK.CHECKED_AT, LINK.UPDATED_AT
            )
            .from(LINK)
            .join(TRACKING).on(TRACKING.LINK_ID.eq(LINK.ID))
            .where(TRACKING.CHAT_ID.eq(tgChatId))
            .fetchInto(Link.class);
    }

}
