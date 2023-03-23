package io.github.lukebemish.modularmetals

import blue.endless.jankson.Jankson
import com.electronwill.nightconfig.toml.TomlFormat
import com.electronwill.nightconfig.toml.TomlParser
import com.electronwill.nightconfig.toml.TomlWriter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Constants {
    private Constants() {}

    public static final String MOD_ID = "modularmetals"
    public static final String MOD_NAME = "Modular Metals"
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME)
    public static final Jankson JANKSON = Jankson.builder().build()
    public static final TomlParser TOML_PARSER = TomlFormat.instance().createParser()
    public static final TomlWriter TOML_WRITER = TomlFormat.instance().createWriter()
    public static final Gson GSON = new GsonBuilder().setLenient().setPrettyPrinting().create()
}
