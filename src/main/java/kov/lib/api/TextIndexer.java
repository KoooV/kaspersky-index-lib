package kov.lib.api;

import java.util.Set;

public interface TextIndexer {
    void addToIndex(String filePath);

    Set<String> findContains(String word);

    void removeFromIndex(String path);

    void clearIndex();

    void shutdown();
}
