package kov.lib;

import kov.lib.api.TextIndexer;
import kov.lib.core.TextIndexerBuilder;

import java.io.IOException;
import java.util.Scanner;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        try {
            TextIndexer indexer = new TextIndexerBuilder()
                    .withThreads(4)
                    .build();

            System.out.println("--- Текстовый поисковый движок запущен ---");
            System.out.println("Доступные команды:");
            System.out.println("  add <путь>     - добавить файл или папку в индекс");
            System.out.println("  search <слово> - найти файлы по слову");
            System.out.println("  exit           - выйти из программы");

            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.print("\nВведите команду > ");
                String input = scanner.nextLine().trim();

                if (input.isEmpty()) continue;

                if (input.equalsIgnoreCase("exit")) {
                    System.out.println("Завершение работы...");
                    // корректная остановка потоков
                    indexer.shutdown();
                    break;
                }

                String[] parts = input.split("\\s+", 2);
                String command = parts[0].toLowerCase();
                String argument = parts.length > 1 ? parts[1] : "";

                switch (command) {
                    case "add":
                        if (argument.isEmpty()) {
                            System.out.println("Ошибка: укажите путь. Пример: add C:/my_folder");
                        } else {
                            System.out.println("Добавляем в индекс (в фоне): " + argument);
                            indexer.addToIndex(argument);
                        }
                        break;

                    case "search":
                        if (argument.isEmpty()) {
                            System.out.println("Ошибка: укажите слово для поиска.");
                        } else {
                            Set<String> results = indexer.findContains(argument);
                            if (results.isEmpty()) {
                                System.out.println("Слово '" + argument + "' не найдено ни в одном файле.");
                            } else {
                                System.out.println("Найдено в файлах:");
                                results.forEach(path -> System.out.println(" - " + path));
                            }
                        }
                        break;

                    default:
                        System.out.println("Неизвестная команда. Используйте add, search или exit.");
                }
            }

        } catch (IOException e) {
            System.err.println("Критическая ошибка при запуске индексатора: " + e.getMessage());
        }

    }
}