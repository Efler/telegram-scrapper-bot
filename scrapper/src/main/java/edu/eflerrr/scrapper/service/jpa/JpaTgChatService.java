package edu.eflerrr.scrapper.service.jpa;

import edu.eflerrr.scrapper.domain.jpa.entity.Chat;
import edu.eflerrr.scrapper.domain.jpa.repository.ChatRepository;
import edu.eflerrr.scrapper.domain.jpa.repository.LinkRepository;
import edu.eflerrr.scrapper.exception.DuplicateRegistrationException;
import edu.eflerrr.scrapper.exception.TgChatNotExistException;
import edu.eflerrr.scrapper.service.TgChatService;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
public class JpaTgChatService implements TgChatService {

    private final ChatRepository chatRepository;
    private final LinkRepository linkRepository;
    private final String autoUsernamePrefix = "auto-username@";

    @Override
    @Transactional
    public void register(long tgChatId) {
        log.debug("REGISTER IN TG-CHAT-SERVICE (JPA): tgChatId: {}", tgChatId);

        if (chatRepository.existsById(tgChatId)) {
            throw new DuplicateRegistrationException(
                String.format("Chat with ID %d already exists", tgChatId)
            );
        }

        Chat chat = new Chat();
        chat.setId(tgChatId);
        chat.setUsername(autoUsernamePrefix + tgChatId);
        chat.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        chatRepository.save(chat);
    }

    @Override
    @Transactional
    public void unregister(long tgChatId) {
        log.debug("UNREGISTER IN TG-CHAT-SERVICE (JPA): tgChatId: {}", tgChatId);

        Chat chat = chatRepository.findById(tgChatId)
            .orElseThrow(() -> new TgChatNotExistException("Chat not found!"));

        for (var link : chat.getLinks()) {
            if (link.getChats().size() == 1 && link.getChats().contains(chat)) {
                linkRepository.delete(link);
            }
        }

        chat.getLinks().clear();
        chatRepository.delete(chat);
    }

}
