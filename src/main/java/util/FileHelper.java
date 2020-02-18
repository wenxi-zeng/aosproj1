package util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileHelper {

    public static List<String> read(String filename) throws IOException {
        File file = new File(filename);
        List<String> lines = new ArrayList<>();

        if (file.exists()) {
            BufferedReader fileReader = new BufferedReader(new FileReader(filename));

            try {
                String line = fileReader.readLine();

                while (line != null) {
                    lines.add(line);
                    line = fileReader.readLine();
                }
            } finally {
                fileReader.close();
            }
        }

        return lines;
    }

    public static void append(String filename, String line) throws IOException {
        FileWriter fileWriter = new FileWriter(filename, true);
        fileWriter.write(line);
        fileWriter.close();
    }

    public static void append(String dir, String filename, String line) throws IOException {
        append(dir.isEmpty() ? filename : dir + File.separator + filename, line);
    }

    public static boolean exist(String filename) {
        File file = new File(filename);
        return file.exists();
    }
}
