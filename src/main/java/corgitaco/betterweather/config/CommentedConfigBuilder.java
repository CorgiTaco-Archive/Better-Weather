package corgitaco.betterweather.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.electronwill.nightconfig.toml.TomlWriter;
import net.minecraft.util.IStringSerializable;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class CommentedConfigBuilder {

    private final CommentedConfig config;
    @Nullable
    private final Path filePath;

    public CommentedConfigBuilder(Path filePath) {
        this.filePath = filePath;
        if (!filePath.getParent().toFile().exists()) {
            try {
                Files.createDirectories(filePath.getParent());
            } catch (IOException e) {

            }
        }

        CommentedFileConfig builtConfig = CommentedFileConfig.builder(filePath).sync().autosave().writingMode(WritingMode.REPLACE).build();
        builtConfig.load();

        this.config = builtConfig;
    }

    public CommentedConfigBuilder(CommentedConfig config) {
        this.config = config;
        this.filePath = null;
    }


    public <T> List<T> addList(String comment, String key, List<T> defaultValue) {
        if (config.get(key) == null) {
            config.set(key, defaultValue);
        }

        if (config.getComment(key) == null) {
            config.setComment(key, comment);
        }
        return config.get(key);
    }

    public CommentedConfigBuilder addSubConfig(String comment, String key, CommentedConfigBuilder defaultValue) {
        if (config.get(key) == null) {
            config.set(key, organizeConfig(defaultValue.config));
        }

        CommentedConfig subConfig = config.get(key);
        String commentValue = config.getComment(key);
        if (commentValue == null) {
            config.setComment(key, comment);
        }

        return new CommentedConfigBuilder(subConfig);
    }


    public Map<?, ?> addMap(String comment, String key, Map<?, Number> defaultValue) {
        if (config.get(key) == null) {
            CommentedConfig subConfig = config.createSubConfig();
            defaultValue.forEach((a, b) -> {
                String subConfigKey = a.toString();
                if (subConfig.get(a.toString()) == null) {
                    subConfig.set(subConfigKey, b);
                }
            });
            config.set(key, subConfig);
        }

        CommentedConfig subConfig = config.get(key);
        String commentValue = config.getComment(key);
        if (commentValue == null) {
            config.setComment(key, comment);
        }

        return subConfig.valueMap();
    }

    public <T> T add(String comment, String key, T defaultValue) {
        if (config.get(key) == null) {
            config.set(key, defaultValue);
        }

        if (config.getComment(key) == null) {
            config.setComment(key, comment);
        }
        return config.get(key);
    }


    public <T extends Number & Comparable<T>> T addNumber(String comment, String key, T defaultValue, T min, T max) {
        if (config.get(key) == null) {
            config.set(key, defaultValue);
        }

        if (config.getComment(key) == null) {
            config.setComment(key, comment + String.format("\nRange: %s-%s", min, max));
        }
        T value = config.get(key);
        return value.compareTo(max) > 0 ? max : value.compareTo(min) < 0 ? min : value;
    }

    public <T extends Enum<T>> T addEnum(String comment, String key, T defaultValue) {
        if (config.get(key) == null) {
            config.set(key, defaultValue);
        }

        if (config.getComment(key) == null) {
            StringBuilder builder = new StringBuilder().append("Values: ").append(defaultValue instanceof IStringSerializable ? "\n" : "");
            for (T value : defaultValue.getDeclaringClass().getEnumConstants()) {
                if (defaultValue instanceof IStringSerializable) {
                    builder.append(((IStringSerializable) value).getSerializedName()).append("\n");
                } else {
                    builder.append(value.name()).append(", ");
                }
            }

            config.setComment(key, comment + "\n" + builder.toString());
        }


        String value = config.get(key).toString();
        return T.valueOf(defaultValue.getDeclaringClass(), value);
    }


    public <T> void updateValue(String key, T newValue) {
        this.config.set(key, newValue);
        build();
    }

    public <T> T getValue(String key) {
        return this.config.get(key);
    }

    public void build() {
        if (this.filePath == null) {
            throw new UnsupportedOperationException("This file cannot be parsed w/ a null file path.");
        }
        TomlWriter writer = new TomlWriter();
        writer.write(organizeConfig(config), filePath.toFile(), WritingMode.REPLACE);
    }

    public static CommentedConfig organizeConfig(CommentedConfig config) {
        CommentedConfig newConfig = CommentedConfig.of(Config.getDefaultMapCreator(false, true), TomlFormat.instance());

        List<Map.Entry<String, Object>> organizedCollection = config.valueMap().entrySet().stream().sorted(Comparator.comparing(Objects::toString)).collect(Collectors.toList());
        organizedCollection.forEach((stringObjectEntry -> {
            newConfig.add(stringObjectEntry.getKey(), stringObjectEntry.getValue());
        }));

        newConfig.commentMap().putAll(config.commentMap());
        return newConfig;
    }
}
