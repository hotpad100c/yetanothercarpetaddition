/*
 * This file is part of the Yet Another Carpet Addition project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2025  Ryan100c and contributors
 *
 * Yet Another Carpet Addition is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Yet Another Carpet Addition is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Yet Another Carpet Addition.  If not, see <https://www.gnu.org/licenses/>.
 */

package mypals.ml.features.hopperCounterDataCollector;

import java.io.*;
import java.nio.file.Files;
import java.time.Instant;
import java.util.*;

public class CounterLogger {
    private final String filename;
    private final List<String> counterNames;


    public CounterLogger(String filename, List<String> counterNames) throws IOException {
        this.filename = filename;
        this.counterNames = counterNames;

        File file = new File(filename);
        File parentDir = file.getParentFile();
        if (!parentDir.exists()) {
            Files.createDirectories(parentDir.toPath());
        }
    }

    public void logCounters(Map<String, String> counters) throws IOException {
        boolean fileExists = new File(filename).exists();

        try (FileWriter fw = new FileWriter(filename, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {

            if (!fileExists) {
                String header = "timestamp|=|" + String.join("|=|", counterNames);
                out.println(header);
            }

            String timestamp = Instant.now().toString();
            List<String> values = new ArrayList<>();
            values.add(timestamp);
            for (String name : counterNames) {
                values.add(counters.getOrDefault(name, "ERROR|:|").toString());
            }

            out.println(String.join("|=|", values));
        }
    }


    public Map<String, Map<String, String>> readCounters() throws IOException {
        Map<String, Map<String, String>> records = new LinkedHashMap<>();
        File file = new File(filename);

        if (!file.exists()) {
            return records;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String headerLine = br.readLine();
            if (headerLine == null) {
                return records;
            }
            String[] headers = headerLine.split("\\|=\\|");
            List<String> headerList = Arrays.asList(headers);

            if (!headerList.get(0).equals("timestamp")) {
                throw new IOException("Invalid CSV format: missing timestamp column");
            }

            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split("\\|=\\|");
                if (values.length < 1) {
                    continue;
                }
                String timestamp = values[0];

                Map<String, String> counters = new HashMap<>();
                for (int i = 1; i < headers.length && i < values.length; i++) {
                    counters.put(headers[i], values[i]);
                }

                records.put(timestamp, counters);
            }
        }
        return records;
    }


    public void clearCounters() throws IOException {
        try (FileWriter fw = new FileWriter(filename);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            String header = "timestamp|=|" + String.join("|=|", counterNames);
            out.println(header);
        }
    }
}