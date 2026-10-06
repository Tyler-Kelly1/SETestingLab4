package Library;

import java.util.UUID;

public class LibraryService {
    private final EmailProvider emailProvider;
    private final ResourceRepository resourceRepository;

    public LibraryService(EmailProvider emailProvider, ResourceRepository resourceRepository) {
        this.emailProvider = emailProvider;
        this.resourceRepository = resourceRepository;
    }

    public boolean checkoutResource(UUID resourceId, String memberEmail) throws DatabaseFailureException, EmailFailureException {
        // ID validation is simpler because UUID cannot be "empty" like a String
        if (resourceId == null) return false;

        if (!resourceRepository.isResourceAvailable(resourceId)) {
            return false;
        }

        boolean statusUpdated = resourceRepository.updateStatus(resourceId, false);
        if(!statusUpdated) {
            throw new DatabaseFailureException("Could not check out item.");
        }

        boolean emailSent = emailProvider.sendEmail(memberEmail, "Resource ID: " + resourceId + " checked out.");
        if (!emailSent) {
            throw new EmailFailureException("Could not send email.");
        }
        return  true;

    }
}