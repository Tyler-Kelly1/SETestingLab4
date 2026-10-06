package Library;



import java.util.UUID;

public interface ResourceRepository {
    // Check whether resource is available.
    boolean isResourceAvailable(UUID resource);

    // Update Status of resource to check out or return.
    boolean updateStatus(UUID resource, boolean available) throws DatabaseFailureException;
}
