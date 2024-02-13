import edu.ucsb.cs156.example.entities.UCSBOrganization;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UCSBOrganizationRepository extends CrudRepository<UCSBOrganization, String>{
    // public UCSBOrganization findByOrgCode(String orgCode);
    // public UCSBOrganization findByOrgTranslationShort(String orgTranslationShort);
    // public UCSBOrganization findByOrgTranslation(String orgTranslation);
    // public UCSBOrganization findByInactive(boolean inactive);
} 
