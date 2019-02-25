package com.hivemq.extensions.config;

import com.google.gson.Gson;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.annotations.Nullable;

public class ClusterNodeEntryUtils {

    @Nullable
    public static ClusterNodeEntry parseClusterNodeEntry(@NotNull final String entryContent) {
        if (entryContent == null) {
            throw new NullPointerException("EntryContent must not be null!");
        }
        if (entryContent.isBlank()) {
            throw new IllegalArgumentException("EntryContent must not be empty!");
        }

        Gson g = new Gson();
        return g.fromJson(entryContent, ClusterNodeEntry.class);
    }

}
