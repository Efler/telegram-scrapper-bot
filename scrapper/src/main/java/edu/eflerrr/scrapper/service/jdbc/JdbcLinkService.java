package edu.eflerrr.scrapper.service.jdbc;

import edu.eflerrr.scrapper.domain.dao.ChatDao;
import edu.eflerrr.scrapper.domain.dao.LinkDao;
import edu.eflerrr.scrapper.domain.dao.TrackingDao;
import edu.eflerrr.scrapper.domain.dto.Chat;
import edu.eflerrr.scrapper.domain.dto.Link;
import edu.eflerrr.scrapper.domain.dto.Tracking;
import edu.eflerrr.scrapper.exception.DuplicateLinkPostException;
import edu.eflerrr.scrapper.exception.LinkNotFoundException;
import edu.eflerrr.scrapper.exception.TgChatNotExistException;
import edu.eflerrr.scrapper.service.LinkService;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(value = "app.service.implementation", havingValue = "jdbc")
@RequiredArgsConstructor
public class JdbcLinkService implements LinkService {

    private final ChatDao chatDao;
    private final LinkDao linkDao;
    private final TrackingDao trackingDao;
    private final String autoUsernamePrefix = "auto-username@";
    private final String chatNotFoundErrorMessage = "Chat not found!";

    @Override
    public Link add(long tgChatId, URI url) {
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
    public Link delete(long tgCHatId, URI url) {
        var link = new Link(url);
        Long linkId;
        try {
            linkId = linkDao.getId(link);
        } catch (InvalidDataAccessResourceUsageException ex) {
            throw new LinkNotFoundException(ex.getMessage());
        }
        try {
            trackingDao.delete(
                new Tracking(
                    tgCHatId, linkId
                )
            );
        } catch (InvalidDataAccessResourceUsageException ex) {
            throw new TgChatNotExistException(ex.getMessage());
        }
        var deletedLink = linkDao.getLinkById(linkId);
        if (trackingDao.findAllByLinkId(linkId).isEmpty()) {
            linkDao.delete(link);
        }
        return deletedLink;
    }

    @Override
    public List<Link> listAll(long tgChatId) {
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
