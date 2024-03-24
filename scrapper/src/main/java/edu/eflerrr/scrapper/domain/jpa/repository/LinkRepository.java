package edu.eflerrr.scrapper.domain.jpa.repository;

import edu.eflerrr.scrapper.domain.jpa.entity.Link;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LinkRepository extends JpaRepository<Link, Long> {

    Optional<Link> findLinkByUrl(URI url);

    Set<Link> findLinksByCheckedAtBefore(OffsetDateTime threshold);

}
