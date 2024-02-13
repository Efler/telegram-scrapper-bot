package edu.eflerrr.bot.repository.impl;

import edu.eflerrr.bot.repository.BotRepository;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryBotRepository implements BotRepository {
    private final Map<Long, List<URL>> memory;
    private final String userNotFoundMessage = "User not found!";
    private final String nullArgsMessage = "Chat ID or URL is null!";

    @Autowired
    public InMemoryBotRepository(Map<Long, List<URL>> memory) {
        this.memory = memory;
    }

    @Override
    public boolean trackLink(Long chatId, URL url) {
        if (chatId == null || url == null) {
            throw new IllegalArgumentException(nullArgsMessage);
        }
        if (!memory.containsKey(chatId)) {
            throw new IllegalArgumentException(userNotFoundMessage);
        }
        var trackingUrls = new ArrayList<>(memory.get(chatId));
        if (trackingUrls.contains(url)) {
            return false;
        }
        trackingUrls.add(url);
        trackingUrls.sort(Comparator.comparing(URL::toString));
        memory.put(chatId, trackingUrls);
        return true;
    }

    @Override
    public boolean untrackLink(Long chatId, URL url) {
        if (chatId == null || url == null) {
            throw new IllegalArgumentException(nullArgsMessage);
        }
        if (!memory.containsKey(chatId)) {
            throw new IllegalArgumentException(userNotFoundMessage);
        }
        var trackingUrls = new ArrayList<>(memory.get(chatId));
        if (!trackingUrls.contains(url)) {
            return false;
        }
        trackingUrls.remove(url);
        memory.put(chatId, trackingUrls);
        return true;
    }

    @Override
    public List<URL> listLink(Long chatId) {
        if (chatId == null) {
            throw new IllegalArgumentException(nullArgsMessage);
        }
        if (!memory.containsKey(chatId)) {
            throw new IllegalArgumentException(userNotFoundMessage);
        }
        return memory.get(chatId);
    }

    @Override
    public boolean addUser(Long chatId) {
        if (chatId == null) {
            throw new IllegalArgumentException(nullArgsMessage);
        }
        if (memory.containsKey(chatId)) {
            return false;
        }
        memory.put(chatId, new ArrayList<>());
        return true;
    }
}
