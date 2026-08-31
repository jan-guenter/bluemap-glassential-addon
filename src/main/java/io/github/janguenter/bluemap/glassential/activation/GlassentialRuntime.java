/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.glassential.activation;

import io.github.janguenter.bluemap.glassential.adapter.bluemap523.FusionProgramCatalog;

/** Process-scoped state for the single exact Glassential/Fusion route. */
public final class GlassentialRuntime {

    public static final String ROUTE_ID = "glassential-fusion-3.4.5-1.3.12";
    public static final GlassentialRuntime INSTANCE = new GlassentialRuntime();

    private final RouteActivation route = new RouteActivation(ROUTE_ID);
    private volatile FusionProgramCatalog catalog;

    private GlassentialRuntime() {
    }

    public RouteActivation route() {
        return route;
    }

    public FusionProgramCatalog catalog() {
        return catalog;
    }

    public synchronized void activate(FusionProgramCatalog installedCatalog) {
        catalog = java.util.Objects.requireNonNull(installedCatalog, "installedCatalog");
        route.activate();
    }

    public synchronized void inactive(String detail) {
        catalog = null;
        route.inactive(detail);
    }

    public void disable(String detail) {
        catalog = null;
        route.fail(detail);
    }
}
