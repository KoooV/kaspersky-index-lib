package kov.lib.file;

import kov.lib.index.IndexStorage;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import static java.nio.file.StandardWatchEventKinds.*;

public class FileWatcher implements Runnable {

    private final WatchService watchService;
    private final Map<WatchKey, Path> keys = new HashMap<>();
    private final FileScanner fileScanner;
    private final ExecutorService executorService;
    private final IndexStorage indexStorage;

    public FileWatcher(WatchService watchService, FileScanner fileScanner, ExecutorService executorService, IndexStorage indexStorage) {
        this.watchService = watchService;
        this.fileScanner = fileScanner;
        this.executorService = executorService;
        this.indexStorage = indexStorage;
    }

    @Override
    public void run() {
        try{
            while(!Thread.currentThread().isInterrupted()){

                WatchKey watchKey = watchService.take();// блокируем поток пока OS не пришлет event
                Path dir = keys.get(watchKey);

                if(dir == null){
                    continue;
                }

                for(WatchEvent<?> event : watchKey.pollEvents()){
                    WatchEvent.Kind<?> kind = event.kind();

                    Path name = (Path) event.context();// относительное имя (file.txt)
                    Path child = dir.resolve(name);// путь к папке + имя

                    if(kind == OVERFLOW){
                        System.out.println("Переполнение событий, невозможно отследить:" + dir);
                        fileScanner.scanAndIndex(dir);
                        continue;
                    }

                    if(kind == ENTRY_CREATE){
                        if(Files.isDirectory(child)){
                            registerAll(child);
                            fileScanner.scanAndIndex(child);
                        }
                        // если это файл не читаем его здесь тк он может стать доступным до заполнения данными
                    }

                    else if(kind == ENTRY_MODIFY){
                        if(Files.exists(child) && !Files.isDirectory(child)){
                            executorService.submit(() -> fileScanner.readSingleFile(child));// вызываем асинхронно чтобы не пропустить event в потоке FileWatcher
                        }
                    }

                    else if(kind == ENTRY_DELETE){
                        indexStorage.removeByPath(child.toString());
                    }
                }

                boolean valid = watchKey.reset();// возвращает false если папка удалена
                if(!valid){
                    keys.remove(watchKey);
                    if(keys.isEmpty()) break;
                }
            }
        }catch(IOException e){
            System.err.println("Ошибка при отслеживании файлов: " + e.getMessage());
        } catch (InterruptedException e) {
            System.out.println("FileWatcher остановлен");
            Thread.currentThread().interrupt();
        }
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
