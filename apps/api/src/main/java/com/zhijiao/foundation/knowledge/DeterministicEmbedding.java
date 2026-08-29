package com.zhijiao.foundation.knowledge;

import java.util.ArrayList;
import java.util.List;

/** Replaceable local embedding fixture; production providers can implement the same boundary. */
public final class DeterministicEmbedding {
    private final int dimensions;

    public DeterministicEmbedding(int dimensions) {
        if (dimensions < 4) {
            throw new IllegalArgumentException("Embedding dimensions must be at least 4");
        }
        this.dimensions = dimensions;
    }

    public List<Double> embed(String text) {
        double[] vector = new double[dimensions];
        String normalized = text == null ? "" : text.trim().toLowerCase();
        List<String> tokens = tokens(normalized);
        if (tokens.isEmpty()) {
            return zeros();
        }
        for (String token : tokens) {
            int hash = token.hashCode();
            int index = Math.floorMod(hash, dimensions);
            vector[index] += (hash & 1) == 0 ? 1.0 : -1.0;
        }
        double norm = 0.0;
        for (double value : vector) {
            norm += value * value;
        }
        norm = Math.sqrt(norm);
        List<Double> result = new ArrayList<>(dimensions);
        for (double value : vector) {
            result.add(norm == 0.0 ? 0.0 : value / norm);
        }
        return List.copyOf(result);
    }

    private List<String> tokens(String text) {
        List<String> result = new ArrayList<>();
        StringBuilder ascii = new StringBuilder();
        for (int index = 0; index < text.length();) {
            int codePoint = text.codePointAt(index);
            index += Character.charCount(codePoint);
            if (Character.isLetterOrDigit(codePoint) && codePoint < 128) {
                ascii.appendCodePoint(codePoint);
                continue;
            }
            if (!ascii.isEmpty()) {
                result.add(ascii.toString());
                ascii.setLength(0);
            }
            if (Character.isLetterOrDigit(codePoint)) {
                result.add(new String(Character.toChars(codePoint)));
            }
        }
        if (!ascii.isEmpty()) {
            result.add(ascii.toString());
        }
        return result;
    }

    private List<Double> zeros() {
        return java.util.Collections.nCopies(dimensions, 0.0);
    }
}
