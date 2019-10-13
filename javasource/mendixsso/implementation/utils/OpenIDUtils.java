package mendixsso.implementation.utils;

import com.mendix.core.Core;
import com.mendix.logging.ILogNode;
import com.mendix.m2ee.api.IMxRuntimeRequest;
import com.mendix.systemwideinterfaces.core.ISession;
import mendixsso.proxies.constants.Constants;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.Random;
import java.util.Scanner;

import static mendixsso.implementation.handlers.OpenIDHandler.CALLBACK;
import static mendixsso.implementation.handlers.OpenIDHandler.OPENID_CLIENTSERVLET_LOCATION;

public class OpenIDUtils {

    private static final String ALPHA_CAPS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String ALPHA = "abcdefghijklmnopqrstuvwxyz";
    private static final String NUM = "0123456789";
    private static final String SPL_CHARS = "!@#$%^&*_=+-/";
    private static final ILogNode LOG = Core.getLogger(Constants.getLogNode());
    private static final String HTTP = "http";
    private static final String HTTPS = "https";

    public static String getApplicationUrl(IMxRuntimeRequest req) {
        final String serverName = req.getHttpServletRequest().getServerName();
        if (serverName == null) {
            LOG.warn("Something went wrong while determining the server name from the request, defaulting to the application root URL.");
            return getDefaultAppRootUrl();
        }

        try {
            // Because the Mendix Cloud load balancers terminate SSL connections, it is not possible to determine
            // the original request scheme (whether it is http or https). Therefore we assume https for all connections
            // except localhost (to enable local development).
            final String scheme = "localhost".equals(serverName.toLowerCase()) ? HTTP : HTTPS;
            final int serverPort = req.getHttpServletRequest().getServerPort();
            // Ports 80 and 443 should be avoided, as they are the default, therefore we pass in -1
            final URI appUri = new URI(scheme, null, serverName, serverPort == 80 || serverPort == 443 ? -1 : serverPort,
                    "/", null, null);
            return appUri.toString();
        } catch (URISyntaxException e) {
            LOG.warn("Something went wrong while constructing the application URL, defaulting to the application root URL.", e);
            return getDefaultAppRootUrl();
        }
    }

    private static String getDefaultAppRootUrl() {
        return ensureEndsWithSlash(Core.getConfiguration().getApplicationRootUrl());
    }

    public static String getRedirectUri(IMxRuntimeRequest req) {
        return getApplicationUrl(req) + OPENID_CLIENTSERVLET_LOCATION + CALLBACK;
    }

    private static String base64Encode(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static String getFingerPrint(IMxRuntimeRequest req) {
        String agent = req.getHeader("User-Agent");
        if (agent != null)
            return base64Encode(agent.getBytes());

        return "";
    }

    public static String getFingerPrint(ISession session) {
        String agent = session.getUserAgent();
        if (agent != null)
            return base64Encode(agent.getBytes());

        return "";

    }

    public static String ensureEndsWithSlash(String text) {
        return text.endsWith("/") ? text : text + "/";
    }

    public static String randomStrongPassword(int minLen, int maxLen, int noOfCAPSAlpha,
                                              int noOfDigits, int noOfSplChars) {
        if (minLen > maxLen)
            throw new IllegalArgumentException("Min. Length > Max. Length!");
        if ((noOfCAPSAlpha + noOfDigits + noOfSplChars) > minLen)
            throw new IllegalArgumentException
                    ("Min. Length should be at least sum of (CAPS, DIGITS, SPL CHARS) Length!");
        Random rnd = new Random();
        int len = rnd.nextInt(maxLen - minLen + 1) + minLen;
        char[] pswd = new char[len];
        int index;
        for (int i = 0; i < noOfCAPSAlpha; i++) {
            index = getNextIndex(rnd, len, pswd);
            pswd[index] = ALPHA_CAPS.charAt(rnd.nextInt(ALPHA_CAPS.length()));
        }
        for (int i = 0; i < noOfDigits; i++) {
            index = getNextIndex(rnd, len, pswd);
            pswd[index] = NUM.charAt(rnd.nextInt(NUM.length()));
        }
        for (int i = 0; i < noOfSplChars; i++) {
            index = getNextIndex(rnd, len, pswd);
            pswd[index] = SPL_CHARS.charAt(rnd.nextInt(SPL_CHARS.length()));
        }
        for (int i = 0; i < len; i++) {
            if (pswd[i] == 0) {
                pswd[i] = ALPHA.charAt(rnd.nextInt(ALPHA.length()));
            }
        }
        return String.valueOf(pswd);
    }

    private static int getNextIndex(Random rnd, int len, char[] pswd) {
        int index;
        //noinspection StatementWithEmptyBody
        while (pswd[index = rnd.nextInt(len)] != 0) ;
        return index;
    }

    public static String convertInputStreamToString(InputStream is) {
        final Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
