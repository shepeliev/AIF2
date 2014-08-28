package com.aif.language.token;

import com.aif.language.common.ISplitter;

import java.util.Arrays;
import java.util.List;

public class TokenSplitter implements ISplitter<String>{

    private final ITokenSeparatorExtractor tokenSeparatorExtractor;

    public TokenSplitter(final ITokenSeparatorExtractor tokenSeparatorExtractor) {
        this.tokenSeparatorExtractor = tokenSeparatorExtractor;
    }

    public TokenSplitter() {
        this(ITokenSeparatorExtractor.Type.PREDEFINED.getInstance());
    }

    @Override
    public List<String> split(final String txt) {
        final List<Character> separators = tokenSeparatorExtractor.getSeparators(txt);
        final String regExp = TokenSplitter.prepareRegex(separators);
        return Arrays.asList(txt.split(regExp));
    }

    private static String prepareRegex(final List<Character> separators) {
        final StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("[");
        separators.stream().forEach(separator -> stringBuffer.append(separator));
        stringBuffer.append("]+");
        return stringBuffer.toString();
    }

}
