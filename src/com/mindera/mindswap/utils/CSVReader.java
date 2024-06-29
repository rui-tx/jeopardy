package com.mindera.mindswap.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


public class CSVReader {

    private CSVReader() {}

    public static <T> Map<String, ArrayList<T>> readItems(String csvFile, Function<String[], T> mapper, int keyIndex) {
        Map<String, ArrayList<T>> itemsByKey = new HashMap<>();
        String line;

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            while ((line = br.readLine()) != null) {
                String[] columns = line.split(",");
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
