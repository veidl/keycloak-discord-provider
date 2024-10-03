package org.keycloak.social.discord;


import com.fasterxml.jackson.databind.JsonNode;
import jakarta.ws.rs.core.Response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.keycloak.OAuthErrorException;
import org.keycloak.broker.oidc.OIDCIdentityProvider;
import org.keycloak.broker.oidc.OIDCIdentityProviderConfig;
import org.keycloak.broker.oidc.mappers.AbstractJsonUserAttributeMapper;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.broker.social.SocialIdentityProvider;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.ErrorResponseException;

public class DiscordIdentityProvider extends OIDCIdentityProvider implements SocialIdentityProvider<OIDCIdentityProviderConfig> {

    public static final String AUTH_URL = "https://discord.com/oauth2/authorize";
    public static final String TOKEN_URL = "https://discord.com/api/oauth2/token";
    public static final String PROFILE_URL = "https://discord.com/api/users/@me";
    private static final String ISSUER = "https://discord.com";
    private static final String DEFAULT_SCOPES = "identify email";
    private static final Log log = LogFactory.getLog(DiscordIdentityProvider.class);

    public DiscordIdentityProvider(KeycloakSession session, OIDCIdentityProviderConfig config) {
        super(session, config);

        config.setAuthorizationUrl(AUTH_URL);
        config.setTokenUrl(TOKEN_URL);
        config.setUserInfoUrl(PROFILE_URL);
    }

    @Override
    protected BrokeredIdentityContext extractIdentityFromProfile(EventBuilder event, JsonNode userInfo) {
        final var id = getJsonProperty(userInfo, "id");
        log.info("id: " + id);
        if (id == null) {
            event.detail(Details.REASON, "id claim is null from user info json");
            event.error(Errors.INVALID_TOKEN);
            throw new ErrorResponseException(OAuthErrorException.INVALID_TOKEN, "invalid token", Response.Status.BAD_REQUEST);
        }

        BrokeredIdentityContext identity = new BrokeredIdentityContext(id, getConfig());

        final var preferredUsername = getJsonProperty(userInfo, "username");
        // the user_name is not unique, so we need to append the discriminator to make it unique
        final var uniqueUserName = preferredUsername + "#" + getJsonProperty(userInfo, "discriminator");
        String email = getJsonProperty(userInfo, "email");

        log.info("preferredUsername: " + preferredUsername);
        log.info("uniqueUserName: " + uniqueUserName);
        log.info("email: " + email);

        identity.setId(id);
        identity.setEmail(email);
        identity.setBrokerUserId(getConfig().getAlias() + "." + id);
        identity.setUsername(uniqueUserName);
        identity.setIdp(this);

        AbstractJsonUserAttributeMapper.storeUserProfileForMapper(identity, userInfo, getConfig().getAlias());

        log.info("identity: " + identity);

        return identity;
    }

    @Override
    protected BrokeredIdentityContext doGetFederatedIdentity(String accessToken) {
        try {
            final var profile = SimpleHttp.doGet(PROFILE_URL, session)
                    .header("Authorization", "Bearer " + accessToken)
                    .asJson();

            return extractIdentityFromProfile(null, profile);
        } catch (Exception e) {
            throw new IdentityBrokerException("Could not obtain user profile from discord.", e);
        }
    }

    @Override
    protected String getDefaultScopes() {
        return DEFAULT_SCOPES;
    }

    @Override
    protected String getProfileEndpointForValidation(EventBuilder event) {
        return PROFILE_URL;
    }
}
