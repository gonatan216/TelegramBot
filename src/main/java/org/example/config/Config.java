package com.avishai.config;

import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class Config {
    public String BOT_USERNAME = System.getenv("BOT_USERNAME");
    public String BOT_TOKEN = System.getenv("BOT_TOKEN");

    public String CHAT_GPT_TOKEN = System.getenv("CHAT_GPT_TOKEN");

    public List<String> ACTIVATION_WORDS = List.of("Hi", "/start", "היי");
    public int MIN_USERS_TO_START_SURVEY = 0;
}
