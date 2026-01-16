package config;

public class EnvConfig {
    public static final String BASE_URL = System.getProperty("base.url", "https://lmsb2b.do.dibimbing.id");
    public static final String IS_DEBUG = System.getProperty("debug", "false");
}
