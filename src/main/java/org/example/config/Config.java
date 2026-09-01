package org.example.config;

import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class Config {
    public String BOT_USERNAME = "@PoolsManagerBot";
    public String BOT_TOKEN = "8895760203:AAGMlEf3iHnuvia6I42NhWYhbw9YlrGLzjw";

    public String CHAT_GPT_TOKEN = "0aDT7jrSwd0nKak4HTSer5qZVDCkezYmU0E1M8dJkCX7WPk6ACQCxMpKuMHbokx7";

    public List<String> ACTIVATION_WORDS = List.of("Hi", "/start", "היי");
    public int MIN_USERS_TO_START_SURVEY = 3;
}
