package edu.eflerrr.bot.repository;

import java.net.URL;
import java.util.List;

public interface BotRepository {
    boolean trackLink(Long chatId, URL url);

    boolean untrackLink(Long chatId, URL url);

    List<URL> listLink(Long chatId);

    boolean addUser(Long chatId);
}
