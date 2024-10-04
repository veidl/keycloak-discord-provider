# Keycloak Discord Provider

Keycloak Social Login extension for Discord.

## Install

Download `keycloak-discord-provider-<version>.jar`
from [Releases page](https://github.com/veidl/keycloak-discord-provider/releases).
Then deploy it into `$KEYCLOAK_HOME/providers` directory.

This project provides a docker-compose file to run Keycloak with the Discord provider.
It automatically uses the generated jar file in the `providers` directory.

## Setup

### Discord

Access to [Discord Developer Portal](https://discord.com/developers/applications) and create your application.
You can get Client ID and Client Secret from the created application.

### Keycloak

1. Add `Discord` Identity Provider in the realm which you want to configure.
2. In the `Discord` identity provider page, set `Client Id` and `Client Secret`. Default scopes are `identify email`.
3. (Optional) Set Bot Permissions to if you plan to use a BOT token via discord.

> **Note:** If you plan to use a BOT token, you have to add the `bot` scope to the `Scopes` field.

## Source Build

Clone this repository and run `mvn clean package`. <br>
You can see `keycloak-discord-provider-<version>.jar` under `target` directory.

## Licence

[Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0)

## Author

- [Veidl](https://github.com/veidl)

