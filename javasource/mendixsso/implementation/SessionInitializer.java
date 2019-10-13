package mendixsso.implementation;

import com.mendix.core.Core;
import com.mendix.core.CoreException;
import com.mendix.logging.ILogNode;
import com.mendix.m2ee.api.IMxRuntimeRequest;
import com.mendix.m2ee.api.IMxRuntimeResponse;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixObject;
import com.mendix.systemwideinterfaces.core.ISession;
import com.mendix.systemwideinterfaces.core.IUser;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import mendixsso.implementation.error.ConflictingUserException;
import mendixsso.implementation.handlers.OpenIDHandler;
import mendixsso.implementation.utils.MendixUtils;
import mendixsso.implementation.utils.OpenIDUtils;
import mendixsso.proxies.*;
import mendixsso.proxies.microflows.Microflows;
import system.proxies.User;

import java.io.IOException;
import java.util.*;

import static mendixsso.proxies.constants.Constants.getLogNode;
import static mendixsso.proxies.constants.Constants.getTokenExpiryInSeconds;
import static mendixsso.proxies.microflows.Microflows.encrypt;

public class SessionInitializer {

    public static final String XASID_COOKIE = "XASID";
    private static final String ORIGIN_COOKIE = "originURI";
    private static final ILogNode LOG = Core.getLogger(getLogNode());
    private static final String XAS_SESSION_ID = Core.getConfiguration().getSessionIdCookieName();
    private static final String USER_ENTITY = UserMapper.getInstance().getUserEntityName();
    private static final String SYSTEM_USER_ENTITY = "System.User";
    private static final String USER_ENTITY_NAME = "Name";
    private static final String DEFAULT_MENDIX_USERNAME_ATTRIBUTE = "Name";


    /**
     * Given a username, starts a new session for the user and redirects back to index.html.
     * If no matching account is found for the user, a new account will be created automatically.
     *
     * @param resp
     * @param req
     * @param user
     * @return
     * @throws CoreException
     * @throws IOException
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    static public void createSessionForUser(IContext context, IMxRuntimeResponse resp,
                                            IMxRuntimeRequest req, IUser user, OIDCTokenResponse oidcTokenResponse) throws Exception {

        LOG.info("User " + user.getName() + " authenticated. Starting session..");

        final String sessionId = req.getCookie(XAS_SESSION_ID);

        final ISession session = Core.initializeSession(user, sessionId);

        // Used to enable Single Sign Off request (from remote sso *server*); must only sign off user in a particular User Agent / Browser
        final String ua = req.getHeader("User-Agent");
        session.setUserAgent(ua);

        if (oidcTokenResponse != null && oidcTokenResponse.getTokens() != null) {

            final Map<String, Object> customParams = oidcTokenResponse.getCustomParameters();

            if (oidcTokenResponse.getTokens().getAccessToken() != null) {
                final AccessToken accessToken = oidcTokenResponse.getTokens().getAccessToken();
                final int accessTokenExpiry = (int) accessToken.getLifetime();

                createToken(context, session, user, TokenType.ACCESS_TOKEN,
                        accessToken.getValue(),
                        accessTokenExpiry);
            }

            if (oidcTokenResponse.getTokens().getRefreshToken() != null) {
                final Long refreshTokenExpiry = (Long) customParams.getOrDefault("refresh_token_expires_in", getTokenExpiryInSeconds());

                createToken(context, session, user, TokenType.REFRESH_TOKEN,
                        oidcTokenResponse.getTokens().getRefreshToken().getValue(),
                        refreshTokenExpiry.intValue());
            }

            if (oidcTokenResponse.getOIDCTokens().getIDToken() != null) {
                final Long idTokenExpiry = (Long) customParams.getOrDefault("id_token_expires_in", getTokenExpiryInSeconds());

                createToken(context, session, user, TokenType.ID_TOKEN,
                        oidcTokenResponse.getOIDCTokens().getIDToken().getParsedString(),
                        idTokenExpiry.intValue());
            }

        } else {

            // the only way to get here is via DefaultLoginHandler.onAlreadyHasSession(),
            // so we can safely assume that the current session already has tokens

            // so we retrieve all tokens from the old session, ...
            final List<Token> tokens = retrieveTokensForSession(context, sessionId);

            // and migrate them to the newly created one
            for (Token token : tokens) {
                token.setSessionId(session.getId().toString());
                token.commit();
            }

        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Created session, fingerprint: " + OpenIDUtils.getFingerPrint(session));
        }

        writeSessionCookies(resp, session);

    }

    private static void writeSessionCookies(IMxRuntimeResponse resp,
                                            ISession session) {
        resp.addCookie(XAS_SESSION_ID, session.getId().toString(), "/", "", -1, true);
        resp.addCookie(XASID_COOKIE, "0." + Core.getXASId(), "/", "", -1, true);
        resp.addCookie(ORIGIN_COOKIE, "/" + OpenIDHandler.OPENID_CLIENTSERVLET_LOCATION + OpenIDHandler.LOGIN, "/", "", -1, false);
    }

    public static void redirectToIndex(IMxRuntimeRequest req, IMxRuntimeResponse resp, String continuation) {
        resp.setStatus(IMxRuntimeResponse.SEE_OTHER);

        //no continuation provided, use index
        if (continuation == null)
            resp.addHeader("location", OpenIDHandler.INDEX_PAGE);
        else {
            if (continuation.trim().startsWith("javascript:")) {
                throw new IllegalArgumentException("Javascript injection detected!");
            } else if (!continuation.startsWith("http://") && !continuation.startsWith("https://")) {
                resp.addHeader("location", OpenIDUtils.getApplicationUrl(req) + continuation);
            } else {
                resp.addHeader("location", continuation);
            }
        }
    }

    /**
     * Finds a user account matching the given username. If not found the new account callback triggered.
     *
     * @param userProfile
     * @return Newly created user or null.
     * @throws Throwable
     * @throws CoreException
     */
    public static IUser findOrCreateUser(UserProfile userProfile) throws Throwable {
        IContext c = Core.createSystemContext();
        c.startTransaction();
        String openID = userProfile.getOpenId();
        try {

            IUser user = findUser(c, openID, false);
            //Existing user
            if (user != null) {
                Response response = null;
                try {
                    response = Microflows.invokeOnNonFirstLoginSSOUser(c, userProfile);
                } catch (Exception e) { //for internal server errors
                    LOG.warn("Failed to update user roles for '" + openID + "', permissions for this user might be outdated", e);
                }
                if (!Objects.requireNonNull(response).getSuccess()) { // for other errors
                    if (ResponseError.CONFLICTING_USER_ERROR.toString().equals(response.getError()))
                        throw new ConflictingUserException();
                    else
                        throw new RuntimeException(response.getErrorDescription());
                }
            }

            //New user
            else {
                preventUserCollision(c, openID);

                String baseMsg = "User '" + openID + "' does not exist in database. Triggering OnFirstLogin action... ";
                LOG.info(baseMsg);

                //Expect user input here.
                // Create new user:
                Microflows.invokeOnFirstLoginSSOUser(c, userProfile);

                IUser newUser = findUser(c, openID, false);
                if (newUser != null) {
                    LOG.info(baseMsg + "Account created.");
                    user = newUser;
                } else {
                    LOG.info(baseMsg + "No user was created. Rejecting the login request.");
                }
            }

            c.endTransaction();
            return user;
        } catch (Throwable e) {
            LOG.warn("Find or create user for openID '" + openID + "' caught exception. Triggering rollback.");
            c.rollbackTransAction();
            throw e;
        }
    }

    public static void notifyAlreadyHasSession(IUser user) {
        IContext c = Core.createSystemContext();
        c.startTransaction();
        String openID = user.getName();
        try {

            Microflows.updateUserRoles(c, User.initialize(c, user.getMendixObject()));

            c.endTransaction();
        } catch (Throwable e) {
            LOG.warn("Find or create user for openID '" + openID + "' caught exception. Triggering rollback.");
            c.rollbackTransAction();
            throw e;
        }
    }

    private static void preventUserCollision(IContext c, String openID) throws CoreException {
        //For successful SSO login there should be no system user with the same openID
        IUser systemUser = findUser(c, openID, true);
        if (systemUser != null) {
            throw new ConflictingUserException();
        }
    }

    private static IUser findUser(IContext c, String openID, boolean systemUserRequested) throws CoreException {
        String userEntity = USER_ENTITY;
        if (systemUserRequested) {
            userEntity = SYSTEM_USER_ENTITY;
        }

        List<IMendixObject> userList = Core.retrieveXPathQuery(c, String.format("//%s[%s='%s']", userEntity, USER_ENTITY_NAME, openID));

        if (userList.size() > 0) {
            IMendixObject userObject = userList.get(0);
            String username = userObject.getValue(c, DEFAULT_MENDIX_USERNAME_ATTRIBUTE);
            if (LOG.isTraceEnabled())
                LOG.trace("Getting System.User using username: '" + username + "'");

            return Core.getUser(c, username);
        } else {
            return null;
        }
    }

    private static void createToken(IContext context, ISession session, IUser user, TokenType tokenType, String value, int expiresIn) throws CoreException {
        final Token newToken = new Token(context);
        newToken.setTokenType(tokenType);
        newToken.setValue(encrypt(context, value));
        newToken.setExpiresIn(expiresIn);
        newToken.setExpiresAt(new Date(System.currentTimeMillis() + expiresIn * 1000L));
        newToken.setSessionId(session.getId().toString());
        newToken.setToken_User(User.initialize(context, user.getMendixObject()));
        newToken.commit();
    }

    private static List<Token> retrieveTokensForSession(IContext context, String sessionId) {
        return MendixUtils.retrieveFromDatabase(context, Token.class,
                "//%s[%s = $sessionId]",
                new HashMap<String, Object>() {{
                    put("sessionId", sessionId);
                }},
                Token.entityName,
                Token.MemberNames.SessionId.toString()
        );
    }

}
