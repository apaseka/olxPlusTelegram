package olx.telegram.parser;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdvertisementDashboardRepository extends CrudRepository<AdvertisementDashboard, Long> {
}
