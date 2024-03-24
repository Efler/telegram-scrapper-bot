package edu.eflerrr.scrapper.service.jpa;

import edu.eflerrr.scrapper.domain.jpa.entity.Chat;
import edu.eflerrr.scrapper.domain.jpa.entity.Link;
import edu.eflerrr.scrapper.domain.jpa.repository.ChatRepository;
import edu.eflerrr.scrapper.domain.jpa.repository.LinkRepository;
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
import org.springframework.transaction.annotation.Transactional;
import static edu.eflerrr.scrapper.configuration.TimeConstants.MIN_DATE_TIME;

@RequiredArgsConstructor
@Slf4j
public class JpaLinkService implements LinkService {

    private final ChatRepository chatRepository;
    private final LinkRepository linkRepository;
    private final String chatNotFoundErrorMessage = "Chat not found!";

    @Override
    @Transactional
    public edu.eflerrr.scrapper.domain.jdbc.dto.Link add(long tgChatId, URI url) {
        log.debug("ADD IN LINK-SERVICE (JPA): tgChatId: {}, url: {}", tgChatId, url);

        Chat chat = chatRepository.findById(tgChatId)
            .orElseThrow(() -> new TgChatNotExistException(chatNotFoundErrorMessage));

        var link = linkRepository.findLinkByUrl(url)
            .orElseGet(() -> {
                var newLink = new Link();
                newLink.setUrl(url);
                newLink.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
                newLink.setCheckedAt(MIN_DATE_TIME);
                newLink.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
                return linkRepository.saveAndFlush(newLink);
            });

        if (!chat.getLinks().contains(link)) {
            chat.addLink(link);
        } else {
            throw new DuplicateLinkPostException("Tracking already exists!");
        }

        return new edu.eflerrr.scrapper.domain.jdbc.dto.Link(
            link.getId(), link.getUrl(),
            link.getCreatedAt(), link.getCheckedAt(),
            link.getUpdatedAt()
        );
    }

    @Override
    @Transactional
    public edu.eflerrr.scrapper.domain.jdbc.dto.Link delete(long tgChatId, URI url) {
        log.debug("DELETE IN LINK-SERVICE (JPA): tgChatId: {}, url: {}", tgChatId, url);

        Chat chat = chatRepository.findById(tgChatId)
            .orElseThrow(() -> new TgChatNotExistException(chatNotFoundErrorMessage));

        Link link = linkRepository.findLinkByUrl(url)
            .orElseThrow(() -> new LinkNotFoundException("Link not found!"));

        if (!chat.getLinks().contains(link)) {
            throw new LinkNotFoundException("Tracking not found!");
        }

        chat.deleteLink(link);

        if (link.getChats().isEmpty()) {
            linkRepository.delete(link);
        }

        return new edu.eflerrr.scrapper.domain.jdbc.dto.Link(
            link.getId(), link.getUrl(),
            link.getCreatedAt(), link.getCheckedAt(),
            link.getUpdatedAt()
        );
    }

    @Override
    @Transactional
    public List<edu.eflerrr.scrapper.domain.jdbc.dto.Link> listAll(long tgChatId) {
        log.debug("LIST-ALL IN LINK-SERVICE (JPA): tgChatId: {}", tgChatId);
        Chat chat = chatRepository.findById(tgChatId)
            .orElseThrow(() -> new TgChatNotExistException(chatNotFoundErrorMessage));

        return chat.getLinks().stream()
            .map(link -> new edu.eflerrr.scrapper.domain.jdbc.dto.Link(
                link.getId(), link.getUrl(),
                link.getCreatedAt(), link.getCheckedAt(),
                link.getUpdatedAt()
            ))
            .toList();
    }

}
