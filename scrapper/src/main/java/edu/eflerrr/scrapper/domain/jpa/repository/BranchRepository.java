package edu.eflerrr.scrapper.domain.jpa.repository;

import edu.eflerrr.scrapper.domain.jpa.entity.Branch;
import edu.eflerrr.scrapper.domain.jpa.entity.Link;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {

    void deleteBranchesByLinkAndRepositoryOwnerAndRepositoryNameAndBranchName(
        Link link, String owner, String username, String branchName
    );

}
