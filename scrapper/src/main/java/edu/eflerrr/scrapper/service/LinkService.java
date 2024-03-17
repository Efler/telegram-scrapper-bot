package edu.eflerrr.scrapper.service;

import edu.eflerrr.scrapper.domain.jdbc.dto.Link;
import java.net.URI;
import java.util.List;

public interface LinkService {

    Link add(long tgChatId, URI url);

    Link delete(long tgChatId, URI url);

    List<Link> listAll(long tgChatId);

}
