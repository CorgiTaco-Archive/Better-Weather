package corgitaco.betterweather.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.toml.TomlWriter;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class AbstractCommentedConfigBuilder {

    private final CommentedConfig config;
    private final Path filePath;

    public AbstractCommentedConfigBuilder(Path filePath) {
        this.filePath = filePath;
        if (filePath.toFile().exists()) {
            this.config = CommentedFileConfig.builder(filePath).build();
        } else {
            this.config = CommentedConfig.inMemory();
        }
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


    public Map<?, ?> addMap(String comment, String key, Map<?, Number> defaultValue) {
        if (config.get(key) == null) {
            CommentedConfig temp = config.createSubConfig();
            defaultValue.forEach((a, b) -> {
                String yes = a.toString();
                if (temp.get(a.toString()) == null) {
                    temp.set(yes, b);
                }
            });
            config.set(key, temp);
        }

        CommentedConfig subConfig = config.get(key);
        String commentValue = config.getComment(key);
        if (commentValue == null) {
            config.setComment(key, comment);
        }

        return subConfig.valueMap();
    }


    public void build() {
        TomlWriter writer = new TomlWriter();
        writer.write(config, filePath.toFile(), WritingMode.REPLACE);
    }
}
