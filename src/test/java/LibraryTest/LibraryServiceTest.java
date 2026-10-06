package LibraryTest;

import Library.*;
import org.junit.jupiter.api.Test;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class LibraryServiceTest {

    @Test
    void checkoutResource_whenIdIsNull_returnsFalse() throws Exception {
        // Arrange
        EmailProvider dummyEmail = mock(EmailProvider.class); // DUMMY
        ResourceRepository dummyRepo = mock(ResourceRepository.class); // DUMMY

        LibraryService service = new LibraryService(dummyEmail, dummyRepo);

        // Act
        boolean result = service.checkoutResource(null, "member@example.com");

        // Assert
        assertFalse(result);
        // Notice: dummyEmail and dummyRepo are never referenced or verified.
    }

    @Test
    void checkoutResource_whenResourceUnavailable_returnsFalse() throws Exception {
        // Arrange
        ResourceRepository repoStub = mock(ResourceRepository.class); // STUB
        EmailProvider dummyEmail = mock(EmailProvider.class); // DUMMY
        UUID testId = UUID.randomUUID();

        // Programming indirect inputs (canned responses)
        when(repoStub.isResourceAvailable(testId)).thenReturn(false);

        LibraryService service = new LibraryService(dummyEmail, repoStub);

        // Act
        boolean result = service.checkoutResource(testId, "member@example.com");

        // Assert
        assertFalse(result);
    }

    @Test
    void checkoutResource_whenSuccessful_returnsTrueAndSendsEmail() throws Exception {
        // Arrange
        ResourceRepository repoStub = mock(ResourceRepository.class); // STUB
        EmailProvider emailMock = mock(EmailProvider.class); // MOCK
        UUID testId = UUID.randomUUID();
        String email = "member@example.com";

        when(repoStub.isResourceAvailable(testId)).thenReturn(true);
        when(repoStub.updateStatus(testId, false)).thenReturn(true);
        when(emailMock.sendEmail(eq(email), anyString())).thenReturn(true);

        LibraryService service = new LibraryService(emailMock, repoStub);

        // Act
        boolean result = service.checkoutResource(testId, email);

        // Assert (State Verification)
        assertTrue(result);

        // Assert (Interaction Verification on the Mock)
        verify(emailMock, times(1)).sendEmail(eq(email), eq("Resource ID: " + testId + " checked out."));
        verify(repoStub, times(1)).updateStatus(testId, false);
    }

    @Test
    void checkoutResource_whenDbUpdateFails_throwsDatabaseFailureException() throws Exception {
        // Arrange
        ResourceRepository repoStub = mock(ResourceRepository.class); // STUB
        EmailProvider dummyEmail = mock(EmailProvider.class); // DUMMY
        UUID testId = UUID.randomUUID();

        when(repoStub.isResourceAvailable(testId)).thenReturn(true);
        when(repoStub.updateStatus(testId, false)).thenReturn(false);

        LibraryService service = new LibraryService(dummyEmail, repoStub);

        // Act & Assert
        assertThrows(DatabaseFailureException.class, () ->
                service.checkoutResource(testId, "member@example.com")
        );

        // Assert (Interaction Verification)
        verify(dummyEmail, never()).sendEmail(anyString(), anyString());
    }

    @Test
    void checkoutResource_whenEmailFails_throwsEmailFailureException() throws Exception {
        // Arrange
        ResourceRepository repoStub = mock(ResourceRepository.class); // STUB
        EmailProvider emailStub = mock(EmailProvider.class); // STUB
        UUID testId = UUID.randomUUID();
        String email = "member@example.com";

        when(repoStub.isResourceAvailable(testId)).thenReturn(true);
        when(repoStub.updateStatus(testId, false)).thenReturn(true);
        // Configure email stub to return false, triggering the exception in the service
        when(emailStub.sendEmail(eq(email), anyString())).thenReturn(false);

        LibraryService service = new LibraryService(emailStub, repoStub);

        // Act & Assert
        assertThrows(EmailFailureException.class, () ->
                service.checkoutResource(testId, email)
        );
    }
}