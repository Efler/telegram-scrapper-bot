package edu.eflerrr.scrapper.service.jdbc;

import edu.eflerrr.scrapper.domain.jdbc.dao.ChatDao;
import edu.eflerrr.scrapper.domain.jdbc.dao.LinkDao;
import edu.eflerrr.scrapper.domain.jdbc.dao.TrackingDao;
import edu.eflerrr.scrapper.domain.jdbc.dto.Chat;
import edu.eflerrr.scrapper.domain.jdbc.dto.Tracking;
import edu.eflerrr.scrapper.exception.DuplicateRegistrationException;
import edu.eflerrr.scrapper.exception.TgChatNotExistException;
import edu.eflerrr.scrapper.service.TgChatService;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(value = "app.service.implementation", havingValue = "jdbc")
@RequiredArgsConstructor
@Slf4j
public class JdbcTgChatService implements TgChatService {

    private final ChatDao chatDao;
    private final LinkDao linkDao;
    private final TrackingDao trackingDao;
    private final String autoUsernamePrefix = "auto-username@";

    @Override
    @Transactional
    public void register(long tgChatId) {
        log.debug("REGISTER IN TG-CHAT-SERVICE (JDBC): tgChatId: {}", tgChatId);
        try {
            chatDao.add(
                new Chat(tgChatId, autoUsernamePrefix + tgChatId)
            );
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateRegistrationException(ex.getMessage());
        }
    }

    @Override
    @Transactional
    public void unregister(long tgChatId) {
        log.debug("UNREGISTER IN TG-CHAT-SERVICE (JDBC): tgChatId: {}", tgChatId);
        try {
            var trackingList = trackingDao.findAllByChatId(tgChatId);
            if (!trackingList.isEmpty()) {
                var linksList = new ArrayList<>(trackingList.stream().map(Tracking::getLinkId).toList());
                for (var linkId : linksList) {
                    if (trackingDao.findAllByLinkId(linkId).size() == 1) {
                        linkDao.delete(
                            linkDao.getLinkById(linkId)
                        );
                    }
                }
            }
        } catch (InvalidDataAccessResourceUsageException ex) {
            throw new TgChatNotExistException(ex.getMessage());
        }
        try {
            chatDao.delete(
                new Chat(tgChatId, autoUsernamePrefix + tgChatId)
            );
        } catch (InvalidDataAccessResourceUsageException ex) {
            throw new TgChatNotExistException(ex.getMessage());
        }
    }
}
