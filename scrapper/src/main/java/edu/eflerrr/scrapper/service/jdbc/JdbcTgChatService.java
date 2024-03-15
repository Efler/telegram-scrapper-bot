package edu.eflerrr.scrapper.service.jdbc;

import edu.eflerrr.scrapper.domain.dao.ChatDao;
import edu.eflerrr.scrapper.domain.dao.LinkDao;
import edu.eflerrr.scrapper.domain.dao.TrackingDao;
import edu.eflerrr.scrapper.domain.dto.Chat;
import edu.eflerrr.scrapper.domain.dto.Tracking;
import edu.eflerrr.scrapper.exception.DuplicateRegistrationException;
import edu.eflerrr.scrapper.exception.TgChatNotExistException;
import edu.eflerrr.scrapper.service.TgChatService;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(value = "app.service.implementation", havingValue = "jdbc")
@RequiredArgsConstructor
public class JdbcTgChatService implements TgChatService {

    private final ChatDao chatDao;
    private final LinkDao linkDao;
    private final TrackingDao trackingDao;
    private final String autoUsernamePrefix = "auto-username@";

    @Override
    public void register(long tgChatId) {
        try {
            chatDao.add(
                new Chat(tgChatId, autoUsernamePrefix + tgChatId)
            );
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateRegistrationException(ex.getMessage());
        }
    }

    @Override
    public void unregister(long tgChatId) {
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
