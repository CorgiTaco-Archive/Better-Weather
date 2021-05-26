/*
The MIT License (MIT)
Copyright (c) 2020 Joseph Bettendorff aka "Commoble"
Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:
The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
// DataFixerUpper is Copyright (c) Microsoft Corporation. All rights reserved. Licensed under the MIT license.
*/
package corgitaco.betterweather.util;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.NullObject;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.RecordBuilder;

import java.time.temporal.Temporal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TomlCommentedConfigOps implements DynamicOps<Object> {
    public static final TomlCommentedConfigOps INSTANCE = new TomlCommentedConfigOps(new HashMap<>(), false);
    private final Map<String, String> keyCommentMap;
    private final boolean isAlphabeticallySorted;

    public TomlCommentedConfigOps(Map<String, String> keyCommentMap, boolean isAlphabeticallySorted) {
        this.keyCommentMap = keyCommentMap;
        this.isAlphabeticallySorted = isAlphabeticallySorted;
    }

    @Override
    public Object empty() {
        return NullObject.NULL_OBJECT;
    }

    @Override
    public <U> U convertTo(DynamicOps<U> outOps, Object input) {
        if (input instanceof Config) {
            return this.convertMap(outOps, input);
        }
        if (input instanceof Collection) {
            return this.convertList(outOps, input);
        }
        if (input == null || input instanceof NullObject) {
            return outOps.empty();
        }
        if (input instanceof Enum) {
            return outOps.createString(((Enum<?>) input).name());
        }
        if (input instanceof Temporal) {
            return outOps.createString(input.toString());
        }
        if (input instanceof String) {
            return outOps.createString((String) input);
        }
        if (input instanceof Boolean) {
            return outOps.createBoolean((Boolean) input);
        }
        if (input instanceof Number) {
            return outOps.createNumeric((Number) input);
        }
        throw new UnsupportedOperationException("TomlConfigOps was unable to convert toml value: " + input);
    }

    @Override
    public DataResult<Number> getNumberValue(Object input) {
        return input instanceof Number
                ? DataResult.success((Number) input)
                : DataResult.error("Not a number: " + input);
    }

    @Override
    public boolean compressMaps() {
        return false;
    }

    @Override
    public Object createNumeric(Number i) {
        return i;
    }

    @Override
    public DataResult<String> getStringValue(Object input) {
        if (input instanceof CommentedConfig || input instanceof Collection) {
            return DataResult.error("Not a string: " + input);
        } else {
            return DataResult.success(String.valueOf(input));
        }
    }

    @Override
    public Object createString(String value) {
        return value;
    }

    @Override
    public Object createBoolean(boolean value) {
        return value;
    }

    @Override
    public DataResult<Object> mergeToList(Object list, Object value) {
        if (!(list instanceof Collection) && list != this.empty()) {
            return DataResult.error("mergeToList called with not a list: " + list, list);
        }
        final Collection<Object> result = new ArrayList<>();
        if (list != this.empty()) {
            Collection<? extends Object> listAsCollection = (Collection<? extends Object>) list;
            result.addAll(listAsCollection);
        }
        result.add(value);
        return DataResult.success(result);
    }

    @Override
    public DataResult<Object> mergeToMap(Object map, Object key, Object value) {
        if (!(map instanceof CommentedConfig) && map != this.empty()) {
            return DataResult.error("mergeToMap called with not a map: " + map, map);
        }
        DataResult<String> stringResult = this.getStringValue(key);
        Optional<DataResult.PartialResult<String>> badResult = stringResult.error();
        if (badResult.isPresent()) {
            return DataResult.error("key is not a string: " + key, map);
        }

        return stringResult.flatMap(s -> {

            final CommentedConfig output = CommentedConfig.inMemory();
            if (map != this.empty()) {
                CommentedConfig oldConfig = organizeConfig((CommentedConfig) map);
                output.addAll(oldConfig);
                output.commentMap().putAll(oldConfig.commentMap());
            }
            output.add(s, value);
            if (TomlCommentedConfigOps.this.getKeyCommentMap().containsKey(s)) {
                if (!output.containsComment(s)) {
                    output.setComment(s, TomlCommentedConfigOps.this.getKeyCommentMap().get(s));
                }
            }
            return DataResult.success(organizeConfig(output));
        });
    }

    public CommentedConfig organizeConfig(CommentedConfig config) {
        if (!isAlphabeticallySorted) {
            return config;
        }

        CommentedConfig newConfig = CommentedConfig.of(Config.getDefaultMapCreator(false, true), TomlFormat.instance());

        List<Map.Entry<String, Object>> organizedCollection = config.valueMap().entrySet().stream().sorted(Comparator.comparing(Objects::toString)).collect(Collectors.toList());
        organizedCollection.forEach((stringObjectEntry -> {
            newConfig.add(stringObjectEntry.getKey(), stringObjectEntry.getValue());
        }));

        newConfig.commentMap().putAll(config.commentMap());
        return newConfig;
    }

    @Override
    public DataResult<Stream<Pair<Object, Object>>> getMapValues(Object input) {
        if (!(input instanceof CommentedConfig)) {
            return DataResult.error("Not a Config: " + input);
        }
        final CommentedConfig config = (CommentedConfig) input;
        return DataResult.success(config.entrySet().stream().sorted(Comparator.comparing(Objects::toString)).map(entry -> Pair.of(entry.getKey(), entry.getValue())));
    }

    @Override
    public Object createMap(Stream<Pair<Object, Object>> map) {
        final CommentedConfig result = CommentedConfig.inMemory();
        map.sorted(Comparator.comparing(Objects::toString)).forEach(p -> {
            String key = this.getStringValue(p.getFirst()).getOrThrow(false, s -> {
            });

            result.add(key, p.getSecond());
            if (TomlCommentedConfigOps.this.getKeyCommentMap().containsKey(key)) {
                if (!result.containsComment(key)) {
                    result.setComment(key, TomlCommentedConfigOps.this.getKeyCommentMap().get(key));
                }
            }
        });
        return organizeConfig(result);
    }

    @Override
    public DataResult<Stream<Object>> getStream(Object input) {
        if (input instanceof Collection) {
            Collection<Object> collection = (Collection<Object>) input;
            return DataResult.success(collection.stream().sorted(Comparator.comparing(Object::toString)));
        }
        return DataResult.error("Not a collection: " + input);
    }

    @Override
    public Object createList(Stream<Object> input) {
        return input.sorted(Comparator.comparing(Object::toString)).collect(Collectors.toList());
    }

    @Override
    public Object remove(Object input, String key) {
        if (input instanceof CommentedConfig) {
            final CommentedConfig result = CommentedConfig.inMemory();
            final Config oldConfig = (Config) input;
            oldConfig.entrySet().stream().sorted(Comparator.comparing(Objects::toString))
                    .filter(entry -> !Objects.equals(entry.getKey(), key))
                    .forEach(entry -> {
                        result.add(entry.getKey(), entry.getValue());
                        if (TomlCommentedConfigOps.this.getKeyCommentMap().containsKey(key)) {
                            if (!result.containsComment(key)) {
                                result.setComment(key, TomlCommentedConfigOps.this.getKeyCommentMap().get(key));
                            }
                        }
                    });
            return organizeConfig(result);
        }
        return organizeConfig((CommentedConfig) input);
    }


    @Override
    public String toString() {
        return "TOML";
    }

    @Override
    public RecordBuilder<Object> mapBuilder() {
        return DynamicOps.super.mapBuilder();
    }

    public Map<String, String> getKeyCommentMap() {
        return keyCommentMap;
    }

    class TomlRecordBuilder extends RecordBuilder.AbstractStringBuilder<Object, CommentedConfig> {

        protected TomlRecordBuilder() {
            super(TomlCommentedConfigOps.this);
        }

        @Override
        protected CommentedConfig initBuilder() {
            return CommentedConfig.inMemory();
        }

        @Override
        protected CommentedConfig append(String key, Object value, CommentedConfig builder) {
            builder.add(key, value);
            return builder;
        }

        @Override
        protected DataResult<Object> build(CommentedConfig builder, Object prefix) {
            if (prefix == null || prefix instanceof NullObject) {
                return DataResult.success(builder);
            }
            if (prefix instanceof CommentedConfig) {
                final CommentedConfig result = CommentedConfig.inMemory();
                final CommentedConfig oldConfig = (CommentedConfig) prefix;
                for (Config.Entry entry : oldConfig.entrySet()) {
                    result.add(entry.getKey(), entry.getValue());
                    if (TomlCommentedConfigOps.this.getKeyCommentMap().containsKey(entry.getKey())) {
                        if (result.containsComment(entry.getKey())) {
                            result.setComment(entry.getKey(), TomlCommentedConfigOps.this.getKeyCommentMap().get(entry.getKey()));
                        }
                    }
                }
                for (Config.Entry entry : builder.entrySet()) {
                    result.add(entry.getKey(), entry.getValue());
                    if (TomlCommentedConfigOps.this.getKeyCommentMap().containsKey(entry.getKey())) {
                        if (result.containsComment(entry.getKey())) {
                            result.setComment(entry.getKey(), TomlCommentedConfigOps.this.getKeyCommentMap().get(entry.getKey()));
                        }
                    }
                }
                return DataResult.success(result);
            }
            return DataResult.error("mergeToMap called with not a Config: " + prefix, prefix);
        }
    }
}