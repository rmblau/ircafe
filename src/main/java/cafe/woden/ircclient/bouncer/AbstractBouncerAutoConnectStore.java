package cafe.woden.ircclient.bouncer;

import cafe.woden.ircclient.config.api.BouncerDiscoveryConfigPort;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.processors.BehaviorProcessor;
import java.util.Map;
import java.util.Objects;
import org.jmolecules.architecture.layered.ApplicationLayer;

/** Shared state/persistence behavior for bouncer auto-connect stores. */
@ApplicationLayer
public abstract class AbstractBouncerAutoConnectStore implements BouncerAutoConnectStore {

  private final BouncerDiscoveryConfigPort runtimeConfig;

  private final BouncerAutoConnectRulesState rulesState = new BouncerAutoConnectRulesState();

  private final BehaviorProcessor<Map<String, Map<String, Boolean>>> updates =
      BehaviorProcessor.create();

  protected AbstractBouncerAutoConnectStore(BouncerDiscoveryConfigPort runtimeConfig) {
    this.runtimeConfig = Objects.requireNonNull(runtimeConfig, "runtimeConfig");
  }

  protected final synchronized void initialize(Map<String, Map<String, Boolean>> seed) {
    rulesState.replace(seed, this::normalizeNetworkKey);
    emit();
  }

  public Flowable<Map<String, Map<String, Boolean>>> updates() {
    return updates.onBackpressureLatest();
  }

  public synchronized Map<String, Map<String, Boolean>> snapshot() {
    return rulesState.snapshot();
  }

  public synchronized Map<String, Boolean> networksForBouncer(String bouncerServerId) {
    return rulesState.networksForBouncer(bouncerServerId);
  }

  @Override
  public synchronized boolean isEnabled(String bouncerServerId, String networkName) {
    return rulesState.isEnabled(bouncerServerId, networkName, this::normalizeNetworkKey);
  }

  @Override
  public synchronized void setEnabled(String bouncerServerId, String networkName, boolean enable) {
    rulesState
        .setEnabled(bouncerServerId, networkName, enable, this::normalizeNetworkKey)
        .ifPresent(
            rule -> {
              persistAutoConnectRule(
                  rule.bouncerServerId(), rule.networkKey(), rule.enabled());
              emit();
            });
  }

  public synchronized boolean isAutoConnectEnabled(
      String bouncerServerId, String networkName) {
    return isEnabled(bouncerServerId, networkName);
  }

  public synchronized void setAutoConnectEnabled(
      String bouncerServerId, String networkName, boolean enable) {
    setEnabled(bouncerServerId, networkName, enable);
  }

  protected BouncerDiscoveryConfigPort runtimeConfig() {
    return runtimeConfig;
  }

  protected abstract String normalizeNetworkKey(String networkName);

  protected abstract void persistAutoConnectRule(
      String bouncerServerId, String networkKey, boolean enable);

  private void emit() {
    updates.onNext(snapshot());
  }
}
