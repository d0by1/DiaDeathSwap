package eu.diaworlds.deathswap.utils.config;

import com.google.common.collect.Sets;
import eu.diaworlds.deathswap.utils.Common;
import lombok.Getter;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;

@Getter
public class DConfig extends YamlConfiguration {

    private final JavaPlugin plugin;
    private final String fileName;
    private final File file;

    public DConfig(JavaPlugin plugin, String fileName) {
        this.plugin = plugin;
        this.fileName = fileName;
        this.file = new File(plugin.getDataFolder() + fileName);
        this.createData();
        this.loadConfig();
    }

    public DConfig(JavaPlugin plugin, File file) {
        this.plugin = plugin;
        this.file = file;
        this.fileName = file.getName();
        this.createData();
        this.loadConfig();
    }

    public void reload() {
        loadConfig();
    }

    public boolean loadConfig() {
        try {
            this.load(file);
            return true;
        } catch (IOException | InvalidConfigurationException e) {
            Common.log(Level.WARNING, "Failed to load configuration from file '%s'.", fileName);
            e.printStackTrace();
        }
        return false;
    }

    public void createData() {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            if (this.plugin.getResource(this.fileName) == null) {
                try {
                    this.file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                this.plugin.saveResource(this.fileName, false);
            }
        }
    }

    public boolean save() {
        try {
            this.save(file);
            return true;
        } catch (IOException e) {
            Common.log(Level.WARNING, "Failed to save configuration to file '%s'.", fileName);
            e.printStackTrace();
        }
        return false;
    }

    public void delete() {
        if (this.file.exists()) {
            this.file.delete();
        }
    }

    public Set<String> getKeys(String path) {
        return getKeys(path, false);
    }

    public Set<String> getKeys(String path, boolean deep) {
        if (!contains(path)) {
            return Sets.newHashSet();
        }
        return getConfigurationSection(path).getKeys(deep);
    }

    public Object getOrDefault(String path, Object defaultValue) {
        if (!contains(path)) {
            set(path, defaultValue);
            return defaultValue;
        }
        return get(path);
    }

}
