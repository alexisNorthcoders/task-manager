package com.projects.taskmanager.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "task")
public class TaskProperties {
    private int titleMaxLength = 120;
    private int descriptionMaxLength = 1000;

    public int getTitleMaxLength() {
        return titleMaxLength;
    }

    public void setTitleMaxLength(int titleMaxLength) {
        this.titleMaxLength = titleMaxLength;
    }

    public int getDescriptionMaxLength() {
        return descriptionMaxLength;
    }

    public void setDescriptionMaxLength(int descriptionMaxLength) {
        this.descriptionMaxLength = descriptionMaxLength;
    }
}


