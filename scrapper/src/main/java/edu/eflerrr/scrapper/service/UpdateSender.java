package edu.eflerrr.scrapper.service;

import java.net.URI;
import java.util.List;

public interface UpdateSender {

    void sendUpdate(Long id, URI url, String description, List<Long> tgChatIds);

}
