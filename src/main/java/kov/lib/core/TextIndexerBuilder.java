package kov.lib.core;

import kov.lib.api.TextIndexer;
import kov.lib.file.FileScanner;
import kov.lib.file.FileWatcher;
import kov.lib.index.IndexStorage;
import kov.lib.token.impl.RegexTokenizer;
import kov.lib.token.Tokenizer;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.WatchService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TextIndexerBuilder {
    private Tokenizer tokenizer = new RegexTokenizer();// по дэфолту использование Regex
    private int threadPoolSize = Runtime.getRuntime().availableProcessors(); // По числу ядер процессора

    public TextIndexerBuilder withTokenizer(RegexTokenizer tokenizer) {
        this.tokenizer = tokenizer;
        return this;
    }

    public TextIndexerBuilder withThreads(int threads) {
        this.threadPoolSize = threads;
        return this;
    }

    public TextIndexer build() throws IOException {
        // ядро памяти
        IndexStorage storage = new IndexStorage();

        // Создаем пул потоков
        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);

        FileScanner scanner = new FileScanner(tokenizer,executor, storage);

        WatchService watchService = FileSystems.getDefault().newWatchService();
        FileWatcher watcher = new FileWatcher(watchService, scanner, executor, storage);

        // Запускаем Watcher в отдельном фоновом (daemon) потоке
        Thread watcherThread = new Thread(watcher);
        watcherThread.setDaemon(true); // Daemon-поток умрет сам, когда завершится основная программа
        watcherThread.start();

        return new TextIndexerImpl(storage, tokenizer, watcher, scanner, executor);
    }
}
