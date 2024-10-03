package org.keycloak.social.discord;

import org.keycloak.broker.oidc.OIDCIdentityProviderConfig;
import org.keycloak.models.IdentityProviderModel;

public class DiscordIdentityProviderConfig extends OIDCIdentityProviderConfig {

    public DiscordIdentityProviderConfig(IdentityProviderModel model) {
        super(model);
    }

    public DiscordIdentityProviderConfig() {
    }
}
