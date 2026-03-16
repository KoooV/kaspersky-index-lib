package kov.lib.file;

import kov.lib.index.IndexStorage;
import kov.lib.token.Tokenizer;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;

public class FileScanner {
    private final Tokenizer tokenizer;
    private final ExecutorService executorService;
    private final IndexStorage storage;


    public FileScanner(Tokenizer tokenizer, ExecutorService executorService, IndexStorage storage) {
        this.tokenizer = tokenizer;
        this.executorService = executorService;
        this.storage = storage;
    }

    public void scanAndIndex(Path startPath){
        if(!Files.exists(startPath)){
            System.out.println("Путь не существует" + startPath);
            return;
        }

        if(Files.isRegularFile(startPath)){
            executorService.submit(() -> readSingleFile(startPath));
        }

        try(Stream<Path> paths = Files.walk(startPath)){
            paths.filter(Files::isRegularFile)
                    .forEach(file ->{
                        executorService.submit(() -> readSingleFile(file));
                    });

        }catch (IOException e){
            System.out.println("Ошибка при обходе пути" + startPath + e.getMessage());
        }
    }

    public void readSingleFile(Path file){
        Set<String> uniqueWords = ConcurrentHashMap.newKeySet();

        try(Stream<String> lines = Files.lines(file, StandardCharsets.UTF_8)){
            lines.forEach(
                    line -> {
                        Set<String> wordsFromFile = tokenizer.tokenize(line);
                        uniqueWords.addAll(wordsFromFile);
                    });
            storage.addOrUpdateIndex(file.toString(), uniqueWords);

        }catch(IOException e){
            System.out.println("Не удалось прочитать файл" + file + e.getMessage());
        }
    }
}
