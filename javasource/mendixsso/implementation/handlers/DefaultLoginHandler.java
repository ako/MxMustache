package mendixsso.implementation.handlers;

import mendixsso.implementation.SessionInitializer;
import mendixsso.implementation.error.ConflictingUserException;
import mendixsso.implementation.utils.ErrorUtils;
import mendixsso.proxies.UserProfile;
import com.mendix.m2ee.api.IMxRuntimeRequest;
import com.mendix.m2ee.api.IMxRuntimeResponse;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IUser;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;


public class DefaultLoginHandler implements ILoginHandler {

    public void onCompleteLogin(IContext context, UserProfile userProfile, OIDCTokenResponse oidcTokenResponse, String continuation, IMxRuntimeRequest req, IMxRuntimeResponse resp) {
        IUser user;
        try {
            user = SessionInitializer.findOrCreateUser(userProfile);
        } catch (ConflictingUserException e) {
            ErrorUtils.serveError(req, resp, ErrorUtils.ResponseType.UNAUTHORIZED, e.getMessage(), false, e);
            return;
        } catch (Throwable e) {
            ErrorUtils.serveError(req, resp, ErrorUtils.ResponseType.INTERNAL_SERVER_ERROR, "We failed to register your account in this app. Please try again later or contact the administrator of this app.", false, e);
            return;
        }

        if (user == null) {
            ErrorUtils.serveError(req, resp, ErrorUtils.ResponseType.UNAUTHORIZED, "Your account has not been authorized to use this application. ", false, null);
        } else if (user.getUserRoleNames().size() == 0) {
            ErrorUtils.serveError(req, resp, ErrorUtils.ResponseType.UNAUTHORIZED, "Your account has not been authorized to use this application. No permissions for this app have been assigned to your account. ", false, null);
        } else {
            try {
                SessionInitializer.createSessionForUser(context, resp, req, user, oidcTokenResponse);
                SessionInitializer.redirectToIndex(req, resp, continuation);
            } catch (Exception e) {
                ErrorUtils.serveError(req, resp, ErrorUtils.ResponseType.INTERNAL_SERVER_ERROR, "Failed to initialize session", false, e);
            }
        }
    }

    @Override
    public void onAlreadyHasSession(IContext context, IUser user, String continuation, IMxRuntimeRequest req, IMxRuntimeResponse resp) {
        if (user == null) {
            ErrorUtils.serveError(req, resp, ErrorUtils.ResponseType.UNAUTHORIZED, "Your account has not been authorized to use this application. ", false, null);
        } else if (user.getUserRoleNames().size() == 0) {
            ErrorUtils.serveError(req, resp, ErrorUtils.ResponseType.UNAUTHORIZED, "Your account has not been authorized to use this application. No permissions for this app have been assigned to your account. ", false, null);
        } else {
            try {
                SessionInitializer.notifyAlreadyHasSession(user);
                SessionInitializer.createSessionForUser(context, resp, req, user, null);
                SessionInitializer.redirectToIndex(req, resp, continuation);
            } catch (Exception e) {
                ErrorUtils.serveError(req, resp, ErrorUtils.ResponseType.INTERNAL_SERVER_ERROR, "Failed to initialize session", false, e);
            }
        }
    }

    @SuppressWarnings("unused")
    public void onCompleteAnonymousLogin(String continuation, IMxRuntimeRequest req, IMxRuntimeResponse resp)
            throws IllegalStateException {
        try {
            /* Setting up guest sessions is not the responsibility of this module, but otherwise:
             if (Core.getConfiguration().getEnableGuestLogin()) {
             ISession session = Core.initializeGuestSession();
             SessionInitializer.writeSessionCookies(resp, session);
             }
             */
            SessionInitializer.redirectToIndex(req, resp, continuation);
        } catch (Exception e) {
            ErrorUtils.serveError(req, resp, ErrorUtils.ResponseType.INTERNAL_SERVER_ERROR, "Failed to initialize session", false, e);
        }
    }

}
