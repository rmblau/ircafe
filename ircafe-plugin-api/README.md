# IRCafe Plugin API

`ircafe-plugin-api` is the public SPI jar for IRCafe plugins. Plugin jars should compile against
this subproject and should only import IRCafe types from packages ending in `.spi`.

The runtime discovers plugin implementations with Java `ServiceLoader`. A plugin jar is a normal jar
with:

- public provider classes with public no-arg constructors.
- service-provider configuration files under `META-INF/services`.
- IRCafe plugin manifest attributes.

Runtime services, Spring beans, Swing UI objects, mutable config stores, HTTP clients, executors, and
resource lifecycles are still app-owned unless a specific SPI type exposes a narrow plugin-facing
request or context.

## Runtime Contexts

Some extension points need request-scoped runtime values while still keeping lifecycle and app
configuration owned by IRCafe. These APIs use small plugin-facing context objects instead of Spring
services or root config models.

`MessageTranslationBackendProvider` receives `MessageTranslationBackendContext` when the app invokes
a translation backend. The context contains only:

- the configured translation endpoint.
- the configured API key or token.
- the normalized request timeout in milliseconds.

Older stateless translation providers can still implement `translate(MessageTranslationRequest)`.
New translation providers that need runtime values should implement
`translate(MessageTranslationRequest, MessageTranslationBackendContext)`.

`LinkPreviewResolver` receives `LinkPreviewHttp` when the app invokes a link preview resolver. The
HTTP facade is an app-owned runtime seam: IRCafe applies proxy configuration, shared embed headers,
and request timeouts inside the concrete implementation. Resolvers should use this facade instead of
creating their own app-coupled HTTP clients. If an installed plugin resolver throws at runtime,
IRCafe records a plugin diagnostics problem for that plugin/resolver and continues trying later
resolvers.

`BouncerNetworkMappingStrategy` receives `BouncerNetworkMappingContext` when IRCafe maps a
discovered bouncer network into portable ephemeral-server data. The context contains only the
runtime generic-login-template policy and whether login hints should be preferred. Server registry
updates, ephemeral server ownership, auto-connect, disconnect cleanup, and UI refresh behavior stay
app-owned. Older stateless mapping strategies can still implement
`resolveNetwork(BouncerServerProfile, BouncerDiscoveredNetwork)`.

## Compatibility

| Item | Current value |
| --- | --- |
| Java release | `25` |
| Plugin API version | `1` |
| Default plugin directory leaf | `plugins` |
| Public IRCafe package policy | Use `.spi` packages only |

The plugin API version is an integer compatibility line, not the IRCafe app version. Plugin jars
must declare `Ircafe-Plugin-Api-Version: 1` until a later compatibility line is introduced.

## Manifest

Declared plugin jars must include these manifest attributes:

```text
Ircafe-Plugin-Id: example-plugin
Ircafe-Plugin-Version: 1.0.0
Ircafe-Plugin-Api-Version: 1
```

`Implementation-Version` may be used instead of `Ircafe-Plugin-Version` when the build tool already
sets it:

```text
Ircafe-Plugin-Id: example-plugin
Implementation-Version: 1.0.0
Ircafe-Plugin-Api-Version: 1
```

Plugin authoring tools can use the public helpers in
`cafe.woden.ircclient.plugin.spi.IrcafePluginManifest`:

```java
Map<String, String> attributes =
    IrcafePluginManifest.compatibleManifestAttributes("example-plugin", "1.0.0");
```

or:

```java
Map<String, String> attributes =
    IrcafePluginManifest.compatibleImplementationVersionManifestAttributes(
        "example-plugin", "1.0.0");
```

## Service Descriptors

For each SPI contract a plugin implements, add a file named after the service type under
`META-INF/services`. The file contains one provider class name per line.

Example for a slash-command parser:

```text
META-INF/services/cafe.woden.ircclient.app.commands.spi.SlashCommandParseStrategy
```

with contents:

```text
example.commands.ExampleAnnounceParseStrategy
```

Plugin authoring tools can use
`cafe.woden.ircclient.plugin.spi.IrcafePluginServiceDescriptors` to build descriptor paths and
contents:

```java
String path =
    IrcafePluginServiceDescriptors.serviceDescriptorPath(SlashCommandParseStrategy.class);
String content =
    IrcafePluginServiceDescriptors.serviceDescriptorContent(
        "example.commands.ExampleAnnounceParseStrategy");
```

## Command Plugin Walkthrough

Command plugins compile against `:ircafe-plugin-api` only. Do not depend on
`:ircafe-feature-commands` or root application classes; those modules contain IRCafe's internal
parser/runtime implementation rather than external authoring contracts.

### Parse and present a slash command

Use `SlashCommandParseStrategy` for a stateless command that maps user input to one of the portable
`SlashCommandParseResult` forms. `tryParse` must return `null` for lines the plugin does not own so
other plugin and app parsers can continue. Once the strategy recognizes its command, malformed
arguments should return `SlashCommandParseResult.unknown(line)` rather than falling through.

Pair the parser with `SlashCommandPresentationContributor` when the command should appear in
autocomplete or help. `SlashCommandDescriptor` adds and normalizes the leading slash. Topic-help
map keys use the command name without the slash. Register each provider separately:

```text
META-INF/services/cafe.woden.ircclient.app.commands.spi.SlashCommandParseStrategy
META-INF/services/cafe.woden.ircclient.app.commands.spi.SlashCommandPresentationContributor
```

### Parse and execute a backend-named command

Use `BackendNamedCommandHandler` plus `BackendNamedCommandExecutor` when a command needs app-mediated
execution. The handler owns aliases, parsing, autocomplete, and help metadata. Its
`supportedCommandNames()` values omit the leading slash, and aliases should return one canonical
`BackendNamedCommandParseResult.command()`.

The executor lists canonical names in `handledCommandNames()` and performs side effects only through
`BackendNamedCommandExecutionContext`. Return `true` when the request was consumed, including when
the executor reports a validation or connection error; return `false` only when the request does not
belong to that executor. Register both providers:

```text
META-INF/services/cafe.woden.ircclient.app.commands.spi.BackendNamedCommandHandler
META-INF/services/cafe.woden.ircclient.app.commands.spi.BackendNamedCommandExecutor
```

All command providers must be public, stateless, and expose public no-argument constructors. IRCafe
owns connection state, target selection, status/error presentation, and raw-line transmission
through the supplied SPI contexts.

## Translation Plugin Walkthrough

Translation plugins compile against `:ircafe-plugin-api` only. Do not depend on
`:ircafe-feature-translation` or root application classes; those modules contain IRCafe's internal
provider catalogs, execution policy, settings adapters, HTTP clients, and UI integration. IRCafe
owns provider loading, configured-backend selection, request scheduling, concurrency limits,
timeouts, automatic-language preflight, result suppression, and rendering.

### Contribute a translation backend

Implement `MessageTranslationBackendProvider` and return a stable, non-blank `backendId()`. IRCafe
trims backend ids and matches them case-insensitively. Choose a unique id: two different providers
with the same normalized backend id make the backend registry invalid rather than silently replacing
one another.

Stateless providers may implement `translate(MessageTranslationRequest)`. Providers that need
runtime endpoint, secret, or timeout values should implement the context-aware
`translate(MessageTranslationRequest, MessageTranslationBackendContext)` overload. The request and
context are immutable, request-scoped values. Do not retain them, app config, executors, or UI
objects after the call returns. Return a non-null `CompletionStage`; complete it exceptionally when
the backend fails. A null stage, null result, or blank translated text is treated as an unusable
backend result, and IRCafe applies its own timeout and rendering policy around the stage.

```text
META-INF/services/cafe.woden.ircclient.app.translation.spi.MessageTranslationBackendProvider
```

### Contribute manual target languages

Implement `MessageTranslationLanguageProvider` to add target-language choices. IRCafe trims and
lowercases language codes, converts underscores to hyphens, ignores blank codes and null entries,
and uses the normalized code as the label when the supplied label is blank. Providers are evaluated
in order and the first language for a normalized code wins. Keep codes stable and use user-facing
labels.

```text
META-INF/services/cafe.woden.ircclient.app.translation.spi.MessageTranslationLanguageProvider
```

A plugin may publish either provider independently or both from one jar. Providers must be public,
stateless, and expose public no-argument constructors. Translation plugins own only their backend
call and portable metadata; they must not mutate IRCafe settings, select chat targets, schedule UI
updates, or render transcript content.

## Theme Plugin Walkthrough

Theme plugins compile against `:ircafe-plugin-api` only. Do not depend on root application classes,
Swing implementation classes, FlatLaf implementation types, or internal theme catalogs. IRCafe owns
provider loading, picker/catalog merging, selected-theme persistence, Look & Feel installation,
window refresh, and fallback behavior.

### Contribute a picker option and FlatLaf preset

Implement `ThemeContributionProvider` to publish picker metadata, FlatLaf preset defaults, or both.
`ThemeOption.id()` values must be stable and non-blank. IRCafe compares ids case-insensitively, and
built-in or earlier contributions win when ids collide. External picker entries should normally use
`ThemePack.PLUGIN`; `ThemeTone` controls light/dark filtering, and `featured` controls whether the
option appears in the curated featured list.

A custom FlatLaf option should have a matching `ThemePresetContribution` id. The preset id is trimmed
and matched case-insensitively. Its `dark` flag selects the dark or light FlatLaf base, and
`extraDefaults` is passed to FlatLaf as immutable UI defaults. An option without a matching preset
only contributes picker metadata; it does not grant access to IRCafe's internal Look & Feel
installer. Blank ids and null entries are ignored, and built-in presets cannot be replaced by an
external contribution.

Register the provider with:

```text
META-INF/services/cafe.woden.ircclient.ui.settings.theme.spi.ThemeContributionProvider
```

Providers must be public, stateless, and expose public no-argument constructors. Return plugin-API
values only; do not retain Swing components, mutate `UIManager`, persist settings, or refresh windows
from the provider.

## Bouncer Plugin Walkthrough

Bouncer plugins also compile against `:ircafe-plugin-api` only. Do not depend on
`:ircafe-feature-bouncer`, the built-in bouncer provider jar, or root application classes. IRCafe
owns protocol event coordination, server and ephemeral-server registries, runtime config,
auto-connect scheduling, disconnect cleanup, logging, and UI refresh behavior.

### Map a discovered network

Implement `BouncerNetworkMappingStrategy` when IRCafe already has a discovery path producing
`BouncerDiscoveredNetwork` values for the backend and needs plugin-defined mapping into portable
server data. Return a stable `backendId()`; IRCafe trims backend ids and matches them
case-insensitively. Choose a unique id because the first resolved strategy for a normalized backend
id wins.

New providers should normally implement the context-aware
`resolveNetwork(BouncerServerProfile, BouncerDiscoveredNetwork, BouncerNetworkMappingContext)`
overload. The context exposes only app-approved runtime policy. Return `ResolvedBouncerNetwork`, and
optionally override `buildEphemeralServer(...)` to adjust portable auto-join data. Do not mutate app
registries, persist settings, or open connections from the provider.

Register the strategy with:

```text
META-INF/services/cafe.woden.ircclient.bouncer.spi.BouncerNetworkMappingStrategy
```

### Consume bouncer discovery events

Implement `BouncerBackendDiscoveryHandler` to consume normalized discovery events for one backend.
Its `backendId()` follows the same trimmed, case-insensitive matching rule. IRCafe calls
`onNetworkDiscovered(...)` for matching events and `onOriginDisconnected(...)` when the originating
connection is being cleaned up. Providers may implement either bouncer SPI independently; IRCafe
matches providers by normalized backend id, not by provider class.

Register the handler with:

```text
META-INF/services/cafe.woden.ircclient.bouncer.spi.BouncerBackendDiscoveryHandler
```

Both provider types must be public, stateless, and expose public no-argument constructors. Keep all
returned and retained values plugin-API-only; root config models, Spring beans, Swing objects, and
feature implementation classes are not external authoring contracts.

## Notification Sound Plugin Walkthrough

Notification sound plugins compile against `:ircafe-plugin-api` only. Do not depend on
`:ircafe-feature-notify` or root application classes. IRCafe owns sound-file copying, runtime-config
paths, settings, executor scheduling, playback freshness/rate limiting, Java Sound fallback,
diagnostics/logging, and Swing file chooser behavior.

### Contribute custom sound file extensions

Implement `CustomSoundFileExtensionProvider` to allow additional file types in custom-sound import
and chooser flows. Return extensions without a leading dot when possible. IRCafe trims values,
removes leading dots, compares extensions case-insensitively, de-duplicates them, and ignores blank
or non-alphanumeric entries. Extension providers only declare accepted file-name suffixes; they do
not decode, copy, or play files.

Register the provider with:

```text
META-INF/services/cafe.woden.ircclient.notify.spi.CustomSoundFileExtensionProvider
```

### Handle custom sound playback

Implement `CustomSoundPlaybackProvider` when imported files need a decoder outside Java Sound. IRCafe
passes an existing resolved `Path` to each provider in order. Return `true` only after taking
ownership of that playback request. Return `false` for unsupported or deliberately unhandled files
so later providers and the built-in Java Sound fallback can run. If a provider throws, IRCafe
isolates the failure, records it for debug diagnostics, and continues the provider chain/fallback.
Providers must not move, delete, or retain ownership of the supplied file.

Register the provider with:

```text
META-INF/services/cafe.woden.ircclient.notify.spi.CustomSoundPlaybackProvider
```

A plugin may publish either provider independently or both from one jar. Both provider types must be
public, stateless, and expose public no-argument constructors.

## Embed and Link Preview Plugin Walkthrough

Embed plugins compile against `:ircafe-plugin-api` only. Do not depend on
`:ircafe-feature-embed` or root application classes; those modules contain IRCafe's internal
provider catalogs, HTTP adapters, rendering pipeline, cache state, and Swing integration.

Use the smallest SPI that matches the extension:

- `LinkPreviewResolver` for a complete preview resolver with custom matching and result mapping.
- `OEmbedLinkPreviewProvider` when a site can use IRCafe's generic oEmbed resolver.
- `ImageUrlExtensionProvider` for additional direct-image URL extensions.
- `EmbedHttpHeaderProvider` for host-specific headers shared by image and preview fetches.
- `NewsPublisherProfileProvider` for publisher-specific article selectors and metadata keys.

### Resolve a complete link preview

A `LinkPreviewResolver` receives the normalized target `URI`, the original URL string, and the
app-owned `LinkPreviewHttp` facade. Use that facade for network requests so IRCafe retains proxy,
shared-header, timeout, and diagnostics ownership. Return `null` for URLs the resolver does not own
or cannot resolve. Resolver exceptions are isolated, recorded in plugin diagnostics, and later resolvers continue.

Resolvers run in ascending `sortOrder()`. The default order is
`BuiltInLinkPreviewResolverOrders.PLUGIN_DEFAULT`; use a custom order only when the resolver must
run before or after a known generic fallback. Provider classes are deduplicated with built-in or
earlier providers winning.

```text
META-INF/services/cafe.woden.ircclient.ui.chat.embed.spi.LinkPreviewResolver
```

### Contribute generic oEmbed support

Use `OEmbedLinkPreviewProvider` when the plugin only needs URL matching, endpoint construction, and
fallback labels. `matches` must return `false` for unrelated URLs. `endpointFor` may return `null`
when no request should be made. IRCafe performs the request, maps the normalized oEmbed fields, and
continues the outer resolver chain when the provider cannot produce a preview.

The provider id should be stable and lowercase. Provider order is significant: the first provider
whose `matches` method returns `true` owns that oEmbed attempt.

```text
META-INF/services/cafe.woden.ircclient.ui.chat.embed.spi.OEmbedLinkPreviewProvider
```

### Contribute direct-image extensions and shared headers

`ImageUrlExtensionProvider.imageFileExtensions()` may return values with or without a leading dot.
IRCafe trims them, lowercases them, adds a missing dot, rejects path-like values, and deduplicates
them in provider order. The resulting extensions are used consistently for direct-image detection,
link-preview exclusion, and temporary-file naming.

`EmbedHttpHeaderProvider.embedHttpHeaders(URI)` should return an empty map for unrelated hosts.
Blank names and values are ignored. Providers are applied in order and later valid values replace
earlier values with the same header name. Provider exceptions are isolated from the fetch.

```text
META-INF/services/cafe.woden.ircclient.ui.chat.embed.spi.ImageUrlExtensionProvider
META-INF/services/cafe.woden.ircclient.ui.chat.embed.spi.EmbedHttpHeaderProvider
```

### Contribute news publisher extraction profiles

`NewsPublisherProfileProvider` contributes immutable `NewsPublisherProfile` values. Profile keys are
trimmed and lowercased. Host suffixes are lowercased, leading `www.` is removed, and selector arrays
are trimmed and deduplicated. Profiles are evaluated in provider order, so use specific host
suffixes and stable unique keys. A provider exception or null profile is ignored while catalog
construction continues.

```text
META-INF/services/cafe.woden.ircclient.ui.chat.embed.spi.NewsPublisherProfileProvider
```

IRCafe retains installed-plugin loading, HTTP transport, proxy and timeout policy, caching,
in-flight request tracking, diagnostics, image decoding, Swing rendering, and document mutation.
Embed providers must expose public no-argument constructors and must not retain ownership of the
`LinkPreviewHttp` facade or app-provided request values after a call returns.

## Installation

Place plugin jars in the runtime plugin directory:

- next to the runtime config file, under the `plugins` directory.
- usually `${XDG_CONFIG_HOME}/ircafe/plugins` when `XDG_CONFIG_HOME` is set.
- otherwise `~/.config/ircafe/plugins`.

Jars with an `Ircafe-Plugin-Id` manifest attribute are declared plugin jars. Other jars in the same
directory are treated as helper dependency jars and are added to plugin classpaths, but they are not
reported as installed plugins.

Plugin jars are scanned in sorted jar-file order. If two declared plugin jars use the same
`Ircafe-Plugin-Id`, the first jar remains installed and the duplicate jar is skipped with a plugin
diagnostics problem.

## Release and Support Notes

Use these notes when validating a release, troubleshooting a plugin install, or preparing support
artifacts for a plugin-related issue.

### Plugin install paths

External plugin jars belong in the configured runtime plugin directory. By default this is the
`plugins` directory beside the runtime config file: `${XDG_CONFIG_HOME}/ircafe/plugins` when
`XDG_CONFIG_HOME` is set, otherwise `~/.config/ircafe/plugins`. Declared plugin jars must carry
`Ircafe-Plugin-Id`, a plugin version, and `Ircafe-Plugin-Api-Version`; helper jars may sit beside
plugin jars without IRCafe manifest attributes and are treated as classpath dependencies only.

### Compatibility checks

Before cutting or validating a plugin-capable release, run:

```bash
GRADLE_USER_HOME=.gradle-local ./gradlew verifyPluginReleaseGraph bootJar
```

`verifyPluginReleaseGraph` groups the plugin API jar policy, built-in provider packaging checks,
bootJar plugin packaging checks, the external plugin smoke test, and architecture boundary checks. For
focused investigation, run `verifyPluginApiJarPolicy`, `verifyBuiltInProviderPackaging`,
`verifyBootJarPluginPackaging`, or `externalPluginSmokeTest` individually. Plugin jars should
continue to compile against the single `:ircafe-plugin-api` authoring jar and declare plugin API
version `1` until the compatibility line changes.

### Built-in provider jars and feature modules

`ircafe-builtins-*` subprojects are IRCafe-shipped provider jars. They are packaged on the app
runtime classpath, publish generated `META-INF/services` descriptors, and should load through
ServiceLoader just like external providers, but users do not install them in the runtime plugin
directory.

`ircafe-feature-*` subprojects are implementation modules, not plugin jars. They may own
root-independent feature runtime logic and tests, but they should not publish ServiceLoader
descriptors or become external plugin authoring APIs. Swing rendering, installed-plugin loading,
runtime config/preferences, schedulers, cache ownership, and concrete HTTP/proxy adapters remain
root-app concerns unless a specific SPI context or feature port says otherwise.

### Support artifacts

When collecting details for plugin support, include:

- the resolved plugin directory path and a sorted list of jars in it.
- the plugin jar manifest attributes, especially `Ircafe-Plugin-Id`, plugin version, and
  `Ircafe-Plugin-Api-Version`.
- the Plugins diagnostics panel entries or exported runtime diagnostics rows.
- any plugin problem level, summary, details, jar path, provider class name, and Java exception
  type reported by IRCafe.
- the release verification commands that were run, especially `verifyPluginReleaseGraph`,
  `externalPluginSmokeTest`, and `bootJar`.

## Diagnostics

Plugin discovery and provider-load failures are reported through the Plugins diagnostics surface,
runtime diagnostics exports, and application logs. Problems include the plugin jar path, the Java
exception type when available, and the loader message.

Common causes:

- missing manifest.
- missing `Ircafe-Plugin-Id`.
- missing both `Ircafe-Plugin-Version` and `Implementation-Version`.
- missing or non-numeric `Ircafe-Plugin-Api-Version`.
- unsupported plugin API version.
- duplicate plugin id.
- provider class is not public.
- provider class does not expose a public no-arg constructor.
- missing helper dependency jar beside the plugin jar.

## Plugin API v1 compatibility policy

`Ircafe-Plugin-Api-Version: 1` identifies the current public plugin contract. IRCafe records that
contract in `ircafe-plugin-api/api-baseline/v1.txt` as a deterministic binary signature covering
public and protected classes, constructors, methods, fields, record components, generic shapes,
sealed-type permits, constants, and runtime-visible annotation types.

`verifyPluginApiV1Baseline` runs as part of `verifyPluginApiJarPolicy`,
`ircv3MigrationCheck`, and `verifyPluginReleaseGraph`. It rejects unreviewed additions, removals, or
signature changes. This is deliberately stricter than a minimal binary-compatibility check so every
public contract change receives API review.

After an intentional compatible change, run `./gradlew generatePluginApiV1Baseline`, review the
baseline diff, and commit it with the API change. Do not regenerate the file merely to make a failed
check pass. Removing or incompatibly changing an API v1 contract requires a deprecation and
migration plan or a new plugin API version with its own versioned baseline. Existing v1 baselines
remain tracked so already-published plugin contracts cannot silently disappear.

## Current Extension Families

The current guide fixtures compile real plugin jars against `:ircafe-plugin-api` and exercise these
extension families:

| Feature area | SPI contracts |
| --- | --- |
| Translation | `MessageTranslationBackendProvider`, `MessageTranslationLanguageProvider` |
| IRCv3 metadata | `Ircv3ExtensionProvider` |
| IRCv3 message-tag parsing | `Ircv3MessageTagParserProvider` |
| IRCv3 outbound message mutations | `Ircv3MessageMutationProvider` |
| IRCv3 outbound protocol commands | `Ircv3OutboundCommandProvider` |
| IRCv3 inbound tagged-message signals | `Ircv3InboundTagSignalProvider` |
| IRCv3 inbound parsed-command signals | `Ircv3InboundCommandSignalProvider` |
| Themes | `ThemeContributionProvider` |
| Bouncer support | `BouncerBackendDiscoveryHandler`, `BouncerNetworkMappingStrategy` |
| Backend metadata | `BackendExtension`, `MatrixOutboundUploadMsgTypeProvider` |
| Slash command parsing/presentation | `SlashCommandParseStrategy`, `SlashCommandPresentationContributor` |
| Backend-named commands | `BackendNamedCommandHandler`, `BackendNamedCommandExecutor` |
| Outbound help | `OutboundHelpContributor` |
| Link and embed metadata | `LinkPreviewResolver`, `OEmbedLinkPreviewProvider`, `ImageUrlExtensionProvider`, `EmbedHttpHeaderProvider`, `NewsPublisherProfileProvider` |
| Message input | `MatrixUploadMsgTypeProvider`, `MessageInputSpellcheckDictionaryProvider`, `MessageInputWordSuggestionProvider` |
| UI launchers | `ExternalBrowserSchemeProvider`, `ExternalBrowserCommandProvider` |
| Notification sounds | `CustomSoundFileExtensionProvider`, `CustomSoundPlaybackProvider` |


`Ircv3MessageTagParserProvider` supplies the transport-neutral parser used before other IRCv3
runtime providers inspect message tags. Requests contain only a transport-supplied tag map and raw
IRC line. The highest `messageTagParserPriority()` wins, and equal-priority conflicts are rejected.
Providers return normalized tag maps; IRCafe validates tag counts, key syntax, and value sizes before
the tags reach event adapters. The focused message-ID runtime provider publishes the `MESSAGE_ID`
inbound-tag operation so stable, draft, client-only, and backend-specific message-ID aliases are
resolved consistently before event construction, standard-reply correlation, or duplicate suppression.
Connection objects, credentials, PircBotX/Quassel objects, persistence, and UI state are never exposed.

`Ircv3MessageMutationProvider` supplies runtime rendering for reply, reaction, edit, and redaction
operations. Providers declare the operations they own. A higher `priority()` replaces a lower-priority
provider for the same operation, while equal-priority conflicts are rejected and reported through
plugin diagnostics. Runtime requests contain only the target, message id, and operation payload so
providers remain independent of Spring, Swing, and transport-client classes.

`Ircv3OutboundCommandProvider` supplies runtime rendering for typing, read markers, CHATHISTORY,
multiline transport commands, ZNC playback requests, labeled-response tag injection, and MONITOR
list/status/clear/add/remove commands. Providers return zero or more raw IRC lines and declare the
operations they own. The same priority and conflict rules apply. Request factories expose only the
protocol inputs required by each operation. CHATHISTORY providers may normalize blank default
selectors and clamp limits in their rendered command; IRCafe validates the returned grammar and
reuses the resulting structured plan for previews, IRC transport, and native-backend history
requests. IRCafe still owns capability readiness, target sanitation, connection state, raw-line
transmission, UI behavior, and retries.

`Ircv3InboundTagSignalProvider` supplies runtime interpretation for channel-context, reply,
reaction, redaction, typing, read-marker, message-edit, account, echo-message private-target hints,
BATCH-reference, history-bootstrap suppression, labeled-response, server-time, and message-id
interpretation. Echo-message requests carry the application-owned self-nick aliases used to
identify self-authored traffic without exposing connection objects. History-bootstrap requests
carry only the target, payload, and an application-owned self-authored flag. Server-time requests may also carry the application-owned observation epoch
used to derive bounded passive-lag samples. Providers
declare `inboundTagOperations()` and return transport-neutral signal values. A higher
`inboundTagPriority()` replaces a lower-priority provider for the same operation; equal-priority
conflicts are rejected and reported through plugin diagnostics. IRCafe still owns server identity,
conversation/event adaptation, negotiated-capability gating, per-connection account-state
deduplication, transcript mutation, logging, and UI routing.

`Ircv3InboundCommandSignalProvider` supplies runtime interpretation for parsed IRC commands whose
semantics are not limited to message tags. The current operations cover away/account/extended-join
presence observations, raw away-notify fallbacks and self-away `305`/`306` confirmations,
SETNAME/CHGHOST identity changes, FAIL/WARN/NOTE standard replies, MONITOR
status/list numerics, USERHOST, WHOIS and WHOWAS identity numerics, WHO and WHOX observations,
BATCH lifecycle controls, ZNC capability and RPL_MYINFO/004 detection, direct MARKREAD
observations, direct REDACT observations, generic CAP ACK/DEL/NEW/LS/NAK change and fallback
planning, and focused RPL_ISUPPORT interpretation for generic
token updates, WHOX availability, MONITOR limits, CLIENTTAGDENY client-tag policy, secure-only STS
capability learning observations, and SASL
capability-list/ACK/NAK, AUTHENTICATE or result-line, and stable failure observations. STS requests
carry the application-owned connection host, secure-transport flag, and observation epoch without
exposing connection or persistence objects. CAP-negotiation requests may carry only negotiated
message-tags/BATCH/history booleans and pending capability names so providers can plan safe fallback
requests without receiving connection or transport objects. SASL requests carry only server-supplied capability
tokens or protocol lines; usernames, passwords, client responses, and stateful authentication
exchange objects are never exposed to runtime providers. `MULTILINE_CAPABILITY_STATE` requests
carry only final/draft offered and negotiated limits, so providers can reinterpret CAP limit
transitions without receiving connection objects.
Providers declare `inboundCommandOperations()` and return portable observation values. Higher
`inboundCommandPriority()` replaces a lower-priority provider per operation, while equal-priority
conflicts are rejected and reported through plugin diagnostics. IRCafe retains PircBotX parsing
hooks, server identity, event construction, hostmask validation, BATCH buffering, playback-capture
lifecycle, logging, connection state, STS cache/persistence and transport upgrades, SASL credentials
and stateful response generation, and UI routing.

Keep plugin examples small and stateless. If a plugin needs app state, network policy, lifecycle
ownership, or UI integration that is not already represented by an SPI request/context type, that
extension point needs a deliberate API change before it should become an external plugin contract.
