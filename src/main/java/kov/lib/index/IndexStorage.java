package kov.lib.index;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class IndexStorage {

    // Key: file path, Value: уникальные слова из файла
    private final ConcurrentHashMap<String, Set<String>> forwardIndex = new ConcurrentHashMap<>();

    // Key: word, Value: set из file path в которых встречается word
    private final ConcurrentHashMap<String, Set<String>> invertedIndex = new ConcurrentHashMap<>();

    public void addOrUpdateIndex(String filePath, Set<String> words){

        removeSingleFile(filePath);// если файл был и его изменили

        if(words == null || words.isEmpty()) return;

        Set<String> wordsSet = ConcurrentHashMap.newKeySet();
        wordsSet.addAll(words);
        forwardIndex.put(filePath, wordsSet);

        for(String word : words){
            invertedIndex.computeIfAbsent(word, k -> ConcurrentHashMap.newKeySet()).add(filePath);
        }
    }

    public Set<String> getFilesContaining(String word){
        Set<String> result = invertedIndex.get(word);
        return result == null ? Collections.emptySet() : Collections.unmodifiableSet(result);
    }


    public void clearIndex(){
        forwardIndex.clear();
        invertedIndex.clear();
    }

    public void removeSingleFile(String filePath){
        Set<String> oldWords = forwardIndex.remove(filePath);

        if(oldWords != null){
            for(String word : oldWords){
                Set<String> filePaths = invertedIndex.get(word);
                if(filePaths != null){
                    filePaths.remove(filePath);

                    if(filePaths.isEmpty()){
                        invertedIndex.remove(word);
                    }
                }
            }
        }
    }

    public void removeByPath(String path){
        forwardIndex.keySet().stream()// для избежания ConcurrentModificationException
                .filter(existingPath -> existingPath.startsWith(path))
                .collect(Collectors.toList())
                .forEach(this::removeSingleFile);
        }
    }


