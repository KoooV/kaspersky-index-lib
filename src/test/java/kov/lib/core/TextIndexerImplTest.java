package kov.lib.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class TextIndexerImplTest {

    private TextIndexerImpl indexer;
    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("test-indexer");
        indexer = (TextIndexerImpl) new TextIndexerBuilder().build();
    }

    @AfterEach
    void tearDown() throws IOException {
        indexer.shutdown();
        // Очистка временной директории
        Files.walk(tempDir)
                .map(Path::toFile)
                .forEach(File::delete);
    }

    @Test
    void testAddToIndexAndFind() throws IOException {
        // Создаем временный файл
        Path file1 = tempDir.resolve("file1.txt");
        try (FileWriter writer = new FileWriter(file1.toFile())) {
            writer.write("hello world");
        }

        // Индексируем файл
        indexer.addToIndex(file1.toString());

        // Небольшая задержка, чтобы индексатор успел обработать файл
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Ищем слова
        Set<String> helloResult = indexer.findContains("hello");
        Set<String> worldResult = indexer.findContains("world");
        Set<String> notFoundResult = indexer.findContains("notfound");

        // Проверяем результаты
        assertTrue(helloResult.contains(file1.toString()));
        assertTrue(worldResult.contains(file1.toString()));
        assertTrue(notFoundResult.isEmpty());
    }

    @Test
    void testRemoveFromIndex() throws IOException {
        Path file1 = tempDir.resolve("file1.txt");
        try (FileWriter writer = new FileWriter(file1.toFile())) {
            writer.write("test remove");
        }

        indexer.addToIndex(file1.toString());
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertTrue(indexer.findContains("remove").contains(file1.toString()));

        indexer.removeFromIndex(file1.toString());

        assertTrue(indexer.findContains("remove").isEmpty());
    }

    @Test
    void testClearIndex() throws IOException {
        Path file1 = tempDir.resolve("file1.txt");
        try (FileWriter writer = new FileWriter(file1.toFile())) {
            writer.write("test clear");
        }
        
        indexer.addToIndex(file1.toString());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertFalse(indexer.findContains("clear").isEmpty());

        indexer.clearIndex();

        assertTrue(indexer.findContains("clear").isEmpty());
    }
}
