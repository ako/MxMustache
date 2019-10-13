package mendixsso.implementation.error;

public class ConflictingUserException extends RuntimeException {

    @Override
    public String getMessage() {
        return "Found local user with the same unique login name as your SSO account. Please contact the administrator of this app.";
    }
}
