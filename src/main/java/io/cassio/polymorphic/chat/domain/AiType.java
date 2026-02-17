package io.cassio.polymorphic.chat.domain;

import java.util.Locale;

public enum AiType {
    COHERE("cohere");

    private final String aiName;

    AiType(String aiName) {
        this.aiName = aiName;
    }

    public String aiName() {
        return aiName;
    }

    public static AiType from(String value) {
        if (value == null) throw new IllegalArgumentException("aiType is null");
        String v = value.trim().toLowerCase(Locale.ROOT);
        for (AiType t : values()) {
            if (t.aiName.equals(v) || t.name().toLowerCase(Locale.ROOT).equals(v)) return t;
        }
        throw new IllegalArgumentException("Unsupported aiType: " + value);
    }
}
