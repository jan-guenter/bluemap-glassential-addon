/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.glassential.adapter.bluemap523;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.resource.fusion.model.FusionDirection;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Immutable programs parsed from the exact, first-wins active model closure. */
public final class FusionProgramCatalog {

    static final int EXPECTED_MODEL_CLOSURE = 100;
    static final int EXPECTED_FUSION_PROGRAMS = 93;
    static final int EXPECTED_ONE_WAY_PROGRAMS = 2;
    private static final int MAX_AST_EDGE_DEPTH = 4;
    private static final int MAX_AST_NODES = 32;
    private static final FusionPredicate NEVER = new FusionPredicate.Never();
    private static final FusionPredicate SAME_STATE = new FusionPredicate.SameState();

    private final Map<Key, Program> programs;
    private final Map<Key, Key> oneWayBaseModels;

    private FusionProgramCatalog(Map<Key, Program> programs, Map<Key, Key> oneWayBaseModels) {
        this.programs = Map.copyOf(programs);
        this.oneWayBaseModels = Map.copyOf(oneWayBaseModels);
    }

    static FusionProgramCatalog parse(
            Map<String, byte[]> rawModels,
            Set<Key> fusionModelKeys,
            Set<Key> oneWayModelKeys
    ) {
        if (rawModels.size() != EXPECTED_MODEL_CLOSURE
                || fusionModelKeys.size() != EXPECTED_FUSION_PROGRAMS
                || oneWayModelKeys.size() != EXPECTED_ONE_WAY_PROGRAMS) {
            throw new IllegalArgumentException("exact model roster changed");
        }

        Map<Key, RawModel> models = new LinkedHashMap<>();
        for (Map.Entry<String, byte[]> entry : rawModels.entrySet()) {
            Key key = modelKey(entry.getKey());
            JsonObject object = parseObject(entry.getValue());
            if (object == null || models.put(key, parseRawModel(key, object)) != null) {
                throw new IllegalArgumentException("missing or duplicate exact model");
            }
        }
        Set<Key> selected = new HashSet<>(fusionModelKeys);
        selected.addAll(oneWayModelKeys);
        if (selected.size() != EXPECTED_FUSION_PROGRAMS + EXPECTED_ONE_WAY_PROGRAMS
                || !models.keySet().containsAll(selected)) {
            throw new IllegalArgumentException("selected model is absent from closure");
        }

        Map<Key, Program> programs = new LinkedHashMap<>();
        for (Key key : fusionModelKeys) {
            RawModel raw = models.get(key);
            Map<String, String> textures = resolveTextures(
                    key, models, new LinkedHashSet<>()
            );
            Map<String, FusionPredicate> predicates = raw.explicitFusion()
                    ? parseConnections(key, raw.object())
                    : parseRegularPane(key, raw);
            programs.put(key, new Program(key, textures, predicates));
        }

        Map<Key, Key> oneWay = new LinkedHashMap<>();
        for (Key key : oneWayModelKeys) {
            RawModel raw = models.get(key);
            Key base = parseOneWay(key, raw.object());
            if (!fusionModelKeys.contains(base) || oneWay.put(key, base) != null) {
                throw new IllegalArgumentException("one-way base model leaves exact Fusion route");
            }
        }
        if (programs.size() != EXPECTED_FUSION_PROGRAMS
                || oneWay.size() != EXPECTED_ONE_WAY_PROGRAMS) {
            throw new IllegalArgumentException("compiled program roster changed");
        }
        return new FusionProgramCatalog(programs, oneWay);
    }

    Program get(Key model) {
        return programs.get(model);
    }

    Key oneWayBase(Key model) {
        return oneWayBaseModels.get(model);
    }

    int size() {
        return programs.size();
    }

    int oneWaySize() {
        return oneWayBaseModels.size();
    }

    Map<Key, Program> programs() {
        return programs;
    }

    Map<Key, Key> oneWayBaseModels() {
        return oneWayBaseModels;
    }

    private static RawModel parseRawModel(Key key, JsonObject object) {
        Key parent = object.has("parent")
                ? Key.parse(requiredString(object.get("parent"))) : null;
        Map<String, String> textures = new LinkedHashMap<>();
        JsonObject textureObject = objectValue(object.get("textures"));
        if (object.has("textures") && textureObject == null) {
            throw new IllegalArgumentException("malformed model textures: " + key);
        }
        if (textureObject != null) {
            for (Map.Entry<String, JsonElement> entry : textureObject.entrySet()) {
                textures.put(entry.getKey(), requiredString(entry.getValue()));
            }
        }
        String loader = string(object.get("loader"));
        boolean fusion = "fusion:model".equals(loader);
        return new RawModel(key, parent, Map.copyOf(textures), object, fusion);
    }

    private static Map<String, String> resolveTextures(
            Key key,
            Map<Key, RawModel> models,
            Set<Key> visiting
    ) {
        RawModel own = models.get(key);
        if (own == null || !visiting.add(key)) {
            throw new IllegalArgumentException("missing or cyclic exact model parent");
        }
        LinkedHashMap<String, String> textures = new LinkedHashMap<>();
        if (own.parent() != null && models.containsKey(own.parent())) {
            textures.putAll(resolveTextures(own.parent(), models, visiting));
        }
        textures.putAll(own.textures());
        visiting.remove(key);
        return Map.copyOf(textures);
    }

    private static Map<String, FusionPredicate> parseRegularPane(Key key, RawModel raw) {
        JsonObject object = raw.object();
        if (!object.keySet().equals(Set.of("parent", "render_type", "textures"))
                || raw.parent() == null
                || !raw.parent().getFormatted().startsWith(
                        "glassential:block/pane/template/template_glass_pane_"
                )
                || !"translucent".equals(requiredString(object.get("render_type")))) {
            throw new IllegalArgumentException("unsupported regular-pane model: " + key);
        }
        return Map.of("default", SAME_STATE);
    }

    private static Map<String, FusionPredicate> parseConnections(
            Key key,
            JsonObject object
    ) {
        Set<String> permitted = Set.of(
                "type", "loader", "parent", "connections", "textures", "render_type"
        );
        String type = string(object.get("type"));
        if (!permitted.containsAll(object.keySet())
                || !("connecting".equals(type) || "fusion:connecting".equals(type))
                || !"fusion:model".equals(string(object.get("loader")))
                || !object.has("connections")) {
            throw new IllegalArgumentException("unsupported Fusion model schema: " + key);
        }
        JsonElement rawConnections = object.get("connections");
        Map<String, FusionPredicate> result = new LinkedHashMap<>();
        if (rawConnections.isJsonArray()) {
            result.put("default", parsePredicateList(rawConnections.getAsJsonArray()));
            return Map.copyOf(result);
        }
        if (!rawConnections.isJsonObject()) {
            throw new IllegalArgumentException("malformed Fusion connections: " + key);
        }
        JsonObject materials = rawConnections.getAsJsonObject();
        if (materials.size() == 0 || materials.size() > 16) {
            throw new IllegalArgumentException("malformed Fusion material map");
        }
        Map<String, String> aliases = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : materials.entrySet()) {
            JsonElement value = entry.getValue();
            if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
                String alias = requiredString(value);
                if (!alias.startsWith("#") || alias.length() == 1) {
                    throw new IllegalArgumentException("malformed Fusion material alias");
                }
                aliases.put(entry.getKey(), alias.substring(1));
            } else if (value.isJsonArray()) {
                result.put(entry.getKey(), parsePredicateList(value.getAsJsonArray()));
            } else if (value.isJsonObject()) {
                int[] nodes = {0};
                result.put(entry.getKey(), parsePredicate(value, 0, nodes));
            } else {
                throw new IllegalArgumentException("malformed Fusion material predicate");
            }
        }
        for (Map.Entry<String, String> alias : aliases.entrySet()) {
            FusionPredicate target = resolveAlias(alias.getValue(), result, aliases, new HashSet<>());
            result.put(alias.getKey(), target);
        }
        return Map.copyOf(result);
    }

    private static FusionPredicate resolveAlias(
            String name,
            Map<String, FusionPredicate> predicates,
            Map<String, String> aliases,
            Set<String> visiting
    ) {
        FusionPredicate predicate = predicates.get(name);
        if (predicate != null) {
            return predicate;
        }
        String alias = aliases.get(name);
        if (alias == null || !visiting.add(name)) {
            throw new IllegalArgumentException("missing or cyclic Fusion material alias");
        }
        FusionPredicate result = resolveAlias(alias, predicates, aliases, visiting);
        visiting.remove(name);
        return result;
    }

    private static Key parseOneWay(Key key, JsonObject object) {
        if (!object.keySet().equals(Set.of("loader", "glass_model"))
                || !"glassential:one_way_loader".equals(string(object.get("loader")))) {
            throw new IllegalArgumentException("unsupported one-way model schema: " + key);
        }
        return Key.parse(requiredString(object.get("glass_model")));
    }

    private static FusionPredicate parsePredicateList(JsonArray array) {
        if (array.size() == 0 || array.size() > 16) {
            throw new IllegalArgumentException("malformed Fusion predicate list");
        }
        int[] nodes = {0};
        List<FusionPredicate> predicates = new ArrayList<>();
        for (JsonElement value : array) {
            predicates.add(parsePredicate(value, 0, nodes));
        }
        return predicates.size() == 1 ? predicates.get(0) : new FusionPredicate.Any(predicates);
    }

    private static FusionPredicate parsePredicate(JsonElement value, int depth, int[] nodes) {
        if (depth > MAX_AST_EDGE_DEPTH
                || ++nodes[0] > MAX_AST_NODES
                || !value.isJsonObject()) {
            throw new IllegalArgumentException("Fusion predicate exceeds bounds");
        }
        JsonObject object = value.getAsJsonObject();
        return switch (string(object.get("type"))) {
            case "or" -> {
                requireKeys(object, Set.of("type", "predicates"));
                yield new FusionPredicate.Any(parsePredicates(object, depth, nodes));
            }
            case "and" -> {
                requireKeys(object, Set.of("type", "predicates"));
                yield new FusionPredicate.All(parsePredicates(object, depth, nodes));
            }
            case "is_direction" -> {
                requireKeys(object, Set.of("type", "directions"));
                yield new FusionPredicate.DirectionIn(parseDirections(object));
            }
            case "match_state" -> {
                requireKeys(object, Set.of("type", "block", "properties"));
                yield new FusionPredicate.MatchState(
                        parseGlassentialBlock(object), parseProperties(object)
                );
            }
            case "is_same_block" -> {
                requireKeys(object, Set.of("type"));
                yield new FusionPredicate.SameBlock();
            }
            case "is_same_state" -> {
                requireKeys(object, Set.of("type"));
                yield SAME_STATE;
            }
            default -> throw new IllegalArgumentException("unsupported Fusion predicate");
        };
    }

    private static List<FusionPredicate> parsePredicates(
            JsonObject object,
            int depth,
            int[] nodes
    ) {
        JsonArray array = arrayValue(object.get("predicates"));
        if (array == null || array.size() == 0 || array.size() > 16) {
            throw new IllegalArgumentException("malformed Fusion predicate list");
        }
        List<FusionPredicate> predicates = new ArrayList<>();
        for (JsonElement element : array) {
            predicates.add(parsePredicate(element, depth + 1, nodes));
        }
        return predicates;
    }

    private static Set<FusionDirection> parseDirections(JsonObject object) {
        JsonArray array = arrayValue(object.get("directions"));
        if (array == null || array.size() == 0 || array.size() > 8) {
            throw new IllegalArgumentException("malformed direction predicate");
        }
        Set<FusionDirection> directions = new HashSet<>();
        for (JsonElement element : array) {
            if (!directions.add(FusionDirection.parse(requiredString(element)))) {
                throw new IllegalArgumentException("duplicate Fusion direction");
            }
        }
        return Set.copyOf(directions);
    }

    private static Map<String, Set<String>> parseProperties(JsonObject object) {
        JsonObject properties = objectValue(object.get("properties"));
        if (properties == null || properties.size() == 0 || properties.size() > 8) {
            throw new IllegalArgumentException("malformed state predicate properties");
        }
        Map<String, Set<String>> result = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : properties.entrySet()) {
            result.put(entry.getKey(), Set.of(requiredString(entry.getValue())));
        }
        return Map.copyOf(result);
    }

    private static String parseGlassentialBlock(JsonObject object) {
        String block = requiredString(object.get("block"));
        if (!block.startsWith("glassential:")) {
            throw new IllegalArgumentException("predicate leaves exact native namespace");
        }
        return block;
    }

    private static JsonObject parseObject(byte[] raw) {
        try {
            JsonElement value = JsonParser.parseReader(new StringReader(
                    new String(raw, StandardCharsets.UTF_8)
            ));
            return value.isJsonObject() ? value.getAsJsonObject() : null;
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private static void requireKeys(JsonObject object, Set<String> keys) {
        if (!object.keySet().equals(keys)) {
            throw new IllegalArgumentException("unsupported Fusion predicate fields");
        }
    }

    private static JsonObject objectValue(JsonElement value) {
        return value != null && value.isJsonObject() ? value.getAsJsonObject() : null;
    }

    private static JsonArray arrayValue(JsonElement value) {
        return value != null && value.isJsonArray() ? value.getAsJsonArray() : null;
    }

    private static String requiredString(JsonElement value) {
        String result = string(value);
        if (result.isEmpty()) {
            throw new IllegalArgumentException("required model string is absent");
        }
        return result;
    }

    private static String string(JsonElement value) {
        return value != null && value.isJsonPrimitive()
                && value.getAsJsonPrimitive().isString() ? value.getAsString() : "";
    }

    private static Key modelKey(String path) {
        String prefix = "assets/";
        String middle = "/models/";
        if (!path.startsWith(prefix) || !path.endsWith(".json")) {
            throw new IllegalArgumentException("malformed model path");
        }
        int split = path.indexOf(middle, prefix.length());
        if (split < 0) {
            throw new IllegalArgumentException("malformed model path");
        }
        String namespace = path.substring(prefix.length(), split);
        if (!(namespace.equals("glassential") || namespace.equals("minecraft"))) {
            throw new IllegalArgumentException("model path leaves exact namespaces");
        }
        return Key.parse(namespace + ":" + path.substring(
                split + middle.length(), path.length() - 5
        ));
    }

    record Program(
            Key model,
            Map<String, String> textures,
            Map<String, FusionPredicate> predicates
    ) {
        FusionPredicate predicate(String materialKey) {
            FusionPredicate exact = predicates.get(materialKey);
            return exact != null ? exact : predicates.getOrDefault("default", NEVER);
        }
    }

    private record RawModel(
            Key model,
            Key parent,
            Map<String, String> textures,
            JsonObject object,
            boolean explicitFusion
    ) {
    }
}
