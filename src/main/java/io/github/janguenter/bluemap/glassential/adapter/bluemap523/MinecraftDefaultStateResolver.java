/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.glassential.adapter.bluemap523;

import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.world.BlockState;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Reflection-only boundary to Minecraft 1.21.1's registered block defaults. */
final class MinecraftDefaultStateResolver {

    private static final String BUILT_IN_REGISTRIES =
            "net.minecraft.core.registries.BuiltInRegistries";
    private static final String REGISTRY = "net.minecraft.core.Registry";
    private static final String RESOURCE_LOCATION = "net.minecraft.resources.ResourceLocation";
    private static final String BLOCK = "net.minecraft.world.level.block.Block";
    private static final String STATE_HOLDER =
            "net.minecraft.world.level.block.state.StateHolder";
    private static final String PROPERTY =
            "net.minecraft.world.level.block.state.properties.Property";

    private static final Key GRASS_BLOCK = Key.minecraft("grass_block");
    private static final Key OAK_LEAVES = Key.minecraft("oak_leaves");
    private static final Key IRON_BLOCK = Key.minecraft("iron_block");

    private final Object blockRegistry;
    private final Method parseResourceLocation;
    private final Method containsKey;
    private final Method getBlock;
    private final Method getBlockKey;
    private final Method defaultBlockState;
    private final Method getValues;
    private final Method getPropertyName;
    private final Method getPropertyValueName;
    private final Map<Key, Optional<BlockState>> cache = new ConcurrentHashMap<>();

    private MinecraftDefaultStateResolver() throws ReflectiveOperationException {
        Class<?> resourceLocationType = Class.forName(RESOURCE_LOCATION);
        Class<?> registryType = Class.forName(REGISTRY);
        Class<?> blockType = Class.forName(BLOCK);
        Class<?> stateHolderType = Class.forName(STATE_HOLDER);
        Class<?> propertyType = Class.forName(PROPERTY);

        Class<?> builtInRegistriesType = Class.forName(BUILT_IN_REGISTRIES);
        blockRegistry = builtInRegistriesType.getField("BLOCK").get(null);
        parseResourceLocation = resourceLocationType.getMethod("parse", String.class);
        containsKey = registryType.getMethod("containsKey", resourceLocationType);
        getBlock = registryType.getMethod("get", resourceLocationType);
        getBlockKey = registryType.getMethod("getKey", Object.class);
        defaultBlockState = blockType.getMethod("defaultBlockState");
        getValues = stateHolderType.getMethod("getValues");
        getPropertyName = propertyType.getMethod("getName");
        getPropertyValueName = propertyType.getMethod("getName", Comparable.class);
    }

    static MinecraftDefaultStateResolver createVerified() {
        try {
            MinecraftDefaultStateResolver resolver = new MinecraftDefaultStateResolver();
            return knownDefaultsMatch(
                    resolver.resolveRegistered(GRASS_BLOCK),
                    resolver.resolveRegistered(OAK_LEAVES),
                    resolver.resolveRegistered(IRON_BLOCK)
            ) ? resolver : null;
        } catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
            return null;
        }
    }

    /** Returns null only when the key is not registered; reflective failures throw. */
    BlockState resolveRegistered(Key key) {
        return cache.computeIfAbsent(key, this::resolveUncached).orElse(null);
    }

    private Optional<BlockState> resolveUncached(Key key) {
        try {
            Object location = invoke(parseResourceLocation, null, key.getFormatted());
            if (!Boolean.TRUE.equals(invoke(containsKey, blockRegistry, location))) {
                return Optional.empty();
            }
            Object block = invoke(getBlock, blockRegistry, location);
            Object registeredKey = invoke(getBlockKey, blockRegistry, block);
            if (block == null || registeredKey == null
                    || !key.getFormatted().equals(registeredKey.toString())) {
                throw new IllegalStateException("registered block lookup changed identity");
            }
            Object defaultState = invoke(defaultBlockState, block);
            Object rawValues = invoke(getValues, defaultState);
            if (!(rawValues instanceof Map<?, ?> values)) {
                throw new IllegalStateException("default block state values changed type");
            }
            Map<String, String> properties = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : values.entrySet()) {
                Object property = entry.getKey();
                Object value = entry.getValue();
                if (!(value instanceof Comparable<?> comparable)) {
                    throw new IllegalStateException("default block property is not comparable");
                }
                Object name = invoke(getPropertyName, property);
                Object serialized = invoke(getPropertyValueName, property, comparable);
                if (!(name instanceof String propertyName)
                        || !(serialized instanceof String propertyValue)
                        || properties.put(propertyName, propertyValue) != null) {
                    throw new IllegalStateException("default block property encoding changed");
                }
            }
            return Optional.of(new BlockState(key, Map.copyOf(properties)));
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Minecraft default-state reflection failed", exception);
        }
    }

    static boolean knownDefaultsMatch(
            BlockState grass,
            BlockState leaves,
            BlockState iron
    ) {
        return exactState(grass, GRASS_BLOCK, Map.of("snowy", "false"))
                && exactState(leaves, OAK_LEAVES, Map.of(
                        "distance", "7",
                        "persistent", "false",
                        "waterlogged", "false"
                ))
                && exactState(iron, IRON_BLOCK, Map.of());
    }

    private static boolean exactState(
            BlockState state,
            Key expectedId,
            Map<String, String> expectedProperties
    ) {
        return state != null && expectedId.equals(state.getId())
                && expectedProperties.equals(state.getProperties());
    }

    private static Object invoke(Method method, Object target, Object... arguments)
            throws ReflectiveOperationException {
        try {
            return method.invoke(target, arguments);
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof Error error) {
                throw error;
            }
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new ReflectiveOperationException("reflected Minecraft call failed", cause);
        }
    }
}
