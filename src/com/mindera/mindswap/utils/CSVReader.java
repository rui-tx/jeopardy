package com.mindera.mindswap.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Reads a CSV file and returns a map of items by key.
 */
public class CSVReader {

    private CSVReader() {
    }

    /**
     * Reads a CSV file and returns a map of items by key.
     *
     * @param csvFile  the CSV file to read
     * @param mapper   a function that maps a string array to an item
     * @param keyIndex the index of the key in the string array
     * @param <T>      the type of the item
     * @return a map of items by key
     */
    public static <T> Map<String, ArrayList<T>> readItems(String csvFile, Function<String[], T> mapper, int keyIndex) {
        Map<String, ArrayList<T>> itemsByKey = new HashMap<>();
        String line;

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            while ((line = br.readLine()) != null) {
                String[] columns = line.split("/");
                if (columns.length > keyIndex) {
                    String key = columns[keyIndex];
                    T item = mapper.apply(columns);

                    itemsByKey.computeIfAbsent(key, k -> new ArrayList<>()).add(item);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return itemsByKey;
    }
}
