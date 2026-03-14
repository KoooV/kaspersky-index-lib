package kov.lib.file;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import static java.nio.file.StandardWatchEventKinds.*;


public class FileWatcher implements Runnable {

    private final WatchService watchService;
    private final Map<WatchKey, Path> keys;

    public FileWatcher(WatchService watchService, Map<WatchKey, Path> keys) {
        this.watchService = watchService;
        this.keys = new HashMap<>();
    }

    @Override
    public void run() {

    }
    // подпись всех папок на события OS (рекурсивно)
    public void registerAll(Path start) throws IOException{
        Files.walkFileTree(start, new SimpleFileVisitor<>(){
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                registerDirectory(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    // подпись конкретной папки
    public void registerDirectory(Path dir) throws IOException{
        WatchKey key = dir.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        keys.put(key, dir);
    }

}
