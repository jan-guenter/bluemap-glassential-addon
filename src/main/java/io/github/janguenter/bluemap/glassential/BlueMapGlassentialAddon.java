/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.glassential;

import io.github.janguenter.bluemap.addon.adapter.api.bluemap523.BlueMapRuntimeCompatibility;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/** BlueMap add-on entrypoint installed before resource-pack construction. */
public final class BlueMapGlassentialAddon implements Runnable {

    public BlueMapGlassentialAddon() {
    }

    @Override
    public void run() {
        try {
            if (!BlueMapRuntimeCompatibility.matchesCurrent()) {
                inactive("unsupported BlueMap internal ABI", null);
                return;
            }
            Class<?> adapter = Class.forName(
                    "io.github.janguenter.bluemap.glassential.adapter.bluemap523.BlueMap523Adapter",
                    true,
                    BlueMapGlassentialAddon.class.getClassLoader()
            );
            Method install = adapter.getMethod("install");
            install.invoke(null);
        } catch (InvocationTargetException exception) {
            inactive("exact adapter initialization failed", exception.getCause());
        } catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
            inactive("exact adapter is unavailable", exception);
        }
    }

    private static void inactive(String reason, Throwable cause) {
        String detail = cause == null ? "" : " (" + cause.getClass().getSimpleName() + ")";
        System.err.println("BlueMap Glassential add-on is inactive: " + reason + detail + ".");
    }
}
