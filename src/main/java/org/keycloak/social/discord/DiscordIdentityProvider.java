package org.keycloak.social.discord;


import com.fasterxml.jackson.databind.JsonNode;
import jakarta.ws.rs.core.UriBuilder;
import org.keycloak.broker.oidc.AbstractOAuth2IdentityProvider;
import org.keycloak.broker.oidc.mappers.AbstractJsonUserAttributeMapper;
import org.keycloak.broker.provider.AuthenticationRequest;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.broker.social.SocialIdentityProvider;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.KeycloakSession;

public class DiscordIdentityProvider extends AbstractOAuth2IdentityProvider<DiscordIdentityProviderConfig>
        implements SocialIdentityProvider<DiscordIdentityProviderConfig> {

    public static final String AUTH_URL = "https://discord.com/oauth2/authorize";
    public static final String TOKEN_URL = "https://discord.com/api/oauth2/token";
    public static final String PROFILE_URL = "https://discord.com/api/users/@me";
    public static final String DEFAULT_SCOPES = "identify email";

    public DiscordIdentityProvider(KeycloakSession session, DiscordIdentityProviderConfig config) {
        super(session, config);

        config.setAuthorizationUrl(AUTH_URL);
        config.setTokenUrl(TOKEN_URL);
        config.setUserInfoUrl(PROFILE_URL);

    }

    @Override
    protected BrokeredIdentityContext extractIdentityFromProfile(EventBuilder event, JsonNode userInfo) {
        BrokeredIdentityContext user = new BrokeredIdentityContext(getJsonProperty(userInfo, "id"), getConfig());

        final var preferredUsername = getJsonProperty(userInfo, "username");
        // the user_name is not unique, so we need to append the discriminator to make it unique
        final var uniqueUserName = preferredUsername + "_" + getJsonProperty(userInfo, "discriminator");
        final var email = getJsonProperty(userInfo, "email");

        user.setEmail(email);
        user.setUsername(uniqueUserName);
        user.setIdp(this);

        AbstractJsonUserAttributeMapper.storeUserProfileForMapper(user, userInfo, getConfig().getAlias());
        return user;
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
    protected UriBuilder createAuthorizationUrl(AuthenticationRequest request) {
        final var preparedUri = super.createAuthorizationUrl(request);

        if (getConfig().getBotPermissions() != null) {
            preparedUri.queryParam("permissions", getConfig().getBotPermissions());
        }

        return preparedUri;
    }

    @Override
    protected boolean supportsExternalExchange() {
        return true;
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
