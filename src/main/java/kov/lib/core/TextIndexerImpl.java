package kov.lib.core;

import kov.lib.api.TextIndexer;
import kov.lib.file.FileScanner;
import kov.lib.file.FileWatcher;
import kov.lib.index.IndexStorage;
import kov.lib.token.Tokenizer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class TextIndexerImpl implements TextIndexer {

    private final IndexStorage indexStorage;
    private final Tokenizer tokenizer;
    private final FileWatcher fileWatcher;
    private final FileScanner fileScanner;
    private final ExecutorService executorService;

    public TextIndexerImpl(IndexStorage indexStorage, Tokenizer tokenizer, FileWatcher fileWatcher, FileScanner fileScanner, ExecutorService executorService) {
        this.indexStorage = indexStorage;
        this.tokenizer = tokenizer;
        this.fileWatcher = fileWatcher;
        this.fileScanner = fileScanner;
        this.executorService = executorService;
    }


    @Override
    public void addToIndex(String filePath) {
        Path path = Paths.get(filePath);

        if(!Files.exists(path)){
            System.err.println("Файла не существует" + path);
            return;
        }

        try{
            if(Files.isDirectory(path)) fileWatcher.registerAll(path);
            fileScanner.scanAndIndex(path);

        }catch (IOException e){
            System.err.println("Ошибка при добавлении пути" + path + e.getMessage());
        }
    }

    @Override
    public Set<String> findContains(String word) {
    Set<String> searchedTokens = tokenizer.tokenize(word);

    if(searchedTokens == null || searchedTokens.isEmpty()){
        return Collections.emptySet();
    }

    String cleanWord = searchedTokens.iterator().next();
        return indexStorage.getFilesContaining(cleanWord);
    }

    @Override
    public void removeFromIndex(String path) {
        indexStorage.removeByPath(path);
    }

    @Override
    public void clearIndex() {
        indexStorage.clearIndex();
    }

    @Override
    public void shutdown() {
        executorService.shutdownNow();
        // Прерываем поток FileWatcher, чтобы он вышел из бесконечного цикла
        Thread.currentThread().interrupt();
    }
}
