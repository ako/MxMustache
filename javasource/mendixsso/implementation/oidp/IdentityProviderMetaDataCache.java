package mendixsso.implementation.oidp;

import mendixsso.implementation.utils.OpenIDUtils;
import mendixsso.proxies.constants.Constants;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import com.nimbusds.openid.connect.sdk.validators.IDTokenValidator;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

public class IdentityProviderMetaDataCache {

    private static IdentityProviderMetaDataCache instance;

    private IdentityProviderMetaData identityProviderMeta;


    public synchronized static IdentityProviderMetaDataCache getInstance() {
        if (instance == null) {
            instance = new IdentityProviderMetaDataCache();
        }
        return instance;
    }

    private static IdentityProviderMetaData loadConfiguration() throws Exception {

        try {
            final String discoveryUrl = OpenIDUtils.ensureEndsWithSlash(Constants.getOpenIdConnectProvider()) + Constants.getOpenIdConnectDiscoveryPath();

            final URI providerConfigurationURL = new URI(discoveryUrl);

            final String providerInfo;
            try (final InputStream stream = providerConfigurationURL.toURL().openStream()) {
                providerInfo = OpenIDUtils.convertInputStreamToString(stream);
            }

            final OIDCProviderMetadata providerMetadata = OIDCProviderMetadata.parse(providerInfo);

            final String decryptedClientSecret = Constants.getEnvironmentPassword();

            final ClientID clientId = new ClientID(Constants.getEnvironmentUUID());

            // set up the validator
            final IDTokenValidator idTokenValidator = new IDTokenValidator(
                    providerMetadata.getIssuer(),
                    clientId,
                    JWSAlgorithm.RS256,
                    providerMetadata.getJWKSetURI().toURL());

            idTokenValidator.setMaxClockSkew(Constants.getIdTokenValidatorMaxClockSkew().intValue());

            return new IdentityProviderMetaData(clientId, decryptedClientSecret, providerMetadata,
                    idTokenValidator, new ResponseType(ResponseType.Value.CODE));

        } catch (MalformedURLException | URISyntaxException e) {
            return ExceptionUtils.rethrow(e);
        }

    }

    public IdentityProviderMetaData getIdentityProviderMetaData() throws Exception {
        if (identityProviderMeta == null) {
            synchronized (this) {
                identityProviderMeta = loadConfiguration();
            }
        }
        return identityProviderMeta;
    }

}
