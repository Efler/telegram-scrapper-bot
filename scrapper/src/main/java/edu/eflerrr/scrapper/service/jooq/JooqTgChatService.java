package edu.eflerrr.scrapper.service.jooq;

import edu.eflerrr.scrapper.exception.DuplicateRegistrationException;
import edu.eflerrr.scrapper.exception.TgChatNotExistException;
import edu.eflerrr.scrapper.service.TgChatService;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static edu.eflerrr.jooqcodegen.generated.Tables.CHAT;
import static edu.eflerrr.jooqcodegen.generated.Tables.LINK;
import static edu.eflerrr.jooqcodegen.generated.Tables.TRACKING;

@Service
@ConditionalOnProperty(value = "app.service.implementation", havingValue = "jooq")
@RequiredArgsConstructor
@Slf4j
public class JooqTgChatService implements TgChatService {

    private final DSLContext dsl;
    private final String autoUsernamePrefix = "auto-username@";

    @Override
    @Transactional
    public void register(long tgChatId) {
        log.debug("REGISTER IN TG-CHAT-SERVICE (JOOQ): tgChatId: {}", tgChatId);
        try {
            dsl.insertInto(CHAT)
                .set(CHAT.ID, tgChatId)
                .set(CHAT.USERNAME, autoUsernamePrefix + tgChatId)
                .set(CHAT.CREATED_AT, OffsetDateTime.now(ZoneOffset.UTC))
                .execute();
        } catch (DuplicateKeyException ex) {
            throw new DuplicateRegistrationException(ex.getMessage());
        }
    }

    @Override
    @Transactional
    public void unregister(long tgChatId) {
        log.debug("UNREGISTER IN TG-CHAT-SERVICE (JOOQ): tgChatId: {}", tgChatId);
        var trackingList = dsl.selectFrom(TRACKING)
            .where(TRACKING.CHAT_ID.eq(tgChatId))
            .fetch();
        if (!trackingList.isEmpty()) {
            for (var tracking : trackingList) {
                var linkId = tracking.getLinkId();
                if (dsl.fetchCount(TRACKING, TRACKING.LINK_ID.eq(linkId)) == 1) {
                    dsl.deleteFrom(LINK)
                        .where(LINK.ID.eq(linkId))
                        .execute();
                }
            }
        }
        var checker = dsl.deleteFrom(CHAT)
            .where(CHAT.ID.eq(tgChatId))
            .execute();
        if (checker == 0) {
            throw new TgChatNotExistException("Chat not found!");
        }
    }

}
