package org.keycloak.social.discord;

import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.models.IdentityProviderModel;

public class DiscordIdentityProviderConfig extends OAuth2IdentityProviderConfig {

    public DiscordIdentityProviderConfig(IdentityProviderModel model) {
        super(model);
    }

    public DiscordIdentityProviderConfig() {
    }

    public String getBotPermissions() {
        return getConfig().get("botPermissions");
    }

}
