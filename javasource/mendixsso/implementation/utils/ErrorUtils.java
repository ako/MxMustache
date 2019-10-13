package mendixsso.implementation.utils;

import com.mendix.core.Core;
import com.mendix.logging.ILogNode;
import com.mendix.m2ee.api.IMxRuntimeRequest;
import com.mendix.m2ee.api.IMxRuntimeResponse;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

import static mendixsso.proxies.constants.Constants.*;

public class ErrorUtils {

    private static final ILogNode LOG = Core.getLogger("ErrorUtils");

    public static void serveError(IMxRuntimeRequest request, IMxRuntimeResponse response, ResponseType responseType, String message, Boolean messageIncludesHtml, Throwable e) {
        serveError(response, new TemplateVariables() {{
                    putString("{{TITLE}}", StringUtils.isBlank(responseType.title) ? "Oops!" : responseType.title);

                    if (messageIncludesHtml) {
                        putHtml("{{MESSAGE}}", message);
                    } else {
                        putString("{{MESSAGE}}", message);
                    }


                    putString("{{SHOW_TRY_AGAIN}}", "");
                    putString("{{TRY_AGAIN_URL}}", OpenIDUtils.getApplicationUrl(request));
                    putString("{{TRY_AGAIN_TEXT}}", getTryAgainText());

                    putString("{{SHOW_CONTACT_SUPPORT}}", "");
                    putString("{{SUPPORT_EMAIL}}", getSupportEmail());
                    putString("{{SUPPORT_EMAIL_SUBJECT}}", getSupportEmailSubject());
                }}
        );

        if (e != null)
            LOG.error("Error occured: " + responseType.title + ":\n" + message + ": " + e.getMessage(), e);
        else
            LOG.error("Error occured: " + responseType.title + ":\n" + message);
    }

    private static void serveError(IMxRuntimeResponse response, TemplateVariables templateVars) {
        try {
            response.setContentType("text/html");
            response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
            response.setStatus(HttpURLConnection.HTTP_OK);

            final String templateFilePath = Core.getConfiguration().getResourcesPath()
                    + File.separator + "templates"
                    + File.separator + "sso_error.html";

            final String renderedTemplate = TemplateRenderer.render(templateFilePath, templateVars);

            try (OutputStream out = response.getOutputStream()) {
                out.write(renderedTemplate.getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            LOG.error("Error while serving error page", e);
        }
    }

    public enum ResponseType {
        INTERNAL_SERVER_ERROR("Internal Server Error", IMxRuntimeResponse.INTERNAL_SERVER_ERROR),
        UNAUTHORIZED("Unauthorized", IMxRuntimeResponse.UNAUTHORIZED);

        final String title;
        final int status;

        ResponseType(String title, int httpStatus) {
            this.title = title;
            this.status = httpStatus;
        }
    }

}
