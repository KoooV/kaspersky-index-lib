package kov.lib.token.impl;

import kov.lib.token.Tokenizer;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.emptySet;

public class RegexTokenizer implements Tokenizer {
    @Override
    public Set<String> tokenize(String text) {
        if(text == null || text.isEmpty()) return emptySet();

        String lowerCaseText = text.toLowerCase();

        return Arrays.stream(lowerCaseText.split("[^a-zA-Z0-9]+"))
                .collect(Collectors.toSet());
    }
}
