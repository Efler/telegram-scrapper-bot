package edu.eflerrr.scrapper.service.jdbc;

import edu.eflerrr.scrapper.domain.jdbc.dao.ChatDao;
import edu.eflerrr.scrapper.domain.jdbc.dao.LinkDao;
import edu.eflerrr.scrapper.domain.jdbc.dao.TrackingDao;
import edu.eflerrr.scrapper.domain.jdbc.dto.Chat;
import edu.eflerrr.scrapper.domain.jdbc.dto.Link;
import edu.eflerrr.scrapper.domain.jdbc.dto.Tracking;
import edu.eflerrr.scrapper.exception.DuplicateLinkPostException;
import edu.eflerrr.scrapper.exception.LinkNotFoundException;
import edu.eflerrr.scrapper.exception.TgChatNotExistException;
import edu.eflerrr.scrapper.service.LinkService;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(value = "app.service.implementation", havingValue = "jdbc")
@RequiredArgsConstructor
@Slf4j
public class JdbcLinkService implements LinkService {

    private final ChatDao chatDao;
    private final LinkDao linkDao;
    private final TrackingDao trackingDao;
    private final String autoUsernamePrefix = "auto-username@";
    private final String chatNotFoundErrorMessage = "Chat not found!";

    @Override
    @Transactional
    public Link add(long tgChatId, URI url) {
        log.debug("ADD IN LINK-SERVICE (JDBC): tgChatId: {}, url: {}", tgChatId, url);
        if (!chatDao.exists(
            new Chat(tgChatId, autoUsernamePrefix + tgChatId)
        )) {
            throw new TgChatNotExistException(chatNotFoundErrorMessage);
        }
        var link = new Link(url);
        if (!linkDao.exists(link)) {
            linkDao.add(link);
        }
        try {
            var linkId = linkDao.getId(link);
            trackingDao.add(
                new Tracking(
                    tgChatId, linkId
                )
            );
            return linkDao.getLinkById(linkId);
        } catch (InvalidDataAccessResourceUsageException ex) {
            throw new DuplicateLinkPostException(ex.getMessage());
        }
    }

    @Override
    @Transactional
    public Link delete(long tgChatId, URI url) {
        log.debug("DELETE IN LINK-SERVICE (JDBC): tgChatId: {}, url: {}", tgChatId, url);
        var link = new Link(url);
        Long linkId;
        try {
            linkId = linkDao.getId(link);
        } catch (InvalidDataAccessResourceUsageException ex) {
            throw new LinkNotFoundException(ex.getMessage());
        }
        if (!chatDao.exists(
            new Chat(tgChatId, autoUsernamePrefix + tgChatId)
        )) {
            throw new TgChatNotExistException(chatNotFoundErrorMessage);
        }
        try {
            trackingDao.delete(
                new Tracking(
                    tgChatId, linkId
                )
            );
        } catch (InvalidDataAccessResourceUsageException ex) {
            throw new LinkNotFoundException(ex.getMessage());
        }
        var deletedLink = linkDao.getLinkById(linkId);
        if (trackingDao.findAllByLinkId(linkId).isEmpty()) {
            linkDao.delete(link);
        }
        return deletedLink;
    }

    @Override
    public List<Link> listAll(long tgChatId) {
        log.debug("LIST-ALL IN LINK-SERVICE (JDBC): tgChatId: {}", tgChatId);
        if (!chatDao.exists(
            new Chat(tgChatId, autoUsernamePrefix + tgChatId)
        )) {
            throw new TgChatNotExistException(chatNotFoundErrorMessage);
        }
        return new ArrayList<>(
            trackingDao.findAllByChatId(tgChatId).stream().map(
                tracking -> linkDao.getLinkById(tracking.getLinkId())
            ).toList()
        );
    }

}
