/// usr/bin/env jbang "$0" "$@" ; exit $?


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.System.out;

public class ToyJson {

    public static void main(String... args) {
        final ToyJsonHelper helper = new ToyJsonHelper();

        // check for valid input
        if (args.length == 0 || args[0].equals("")) {
            out.println("Provide valid input");
        } else {
            if (helper.isValidJsonFile(args[0])) {
                out.println("Json file");
                helper.readTextFile(helper.prepareFile(args[0]));
            } else {
                out.println("Not a JSON");
            }
        }
    }

    static class ToyJsonHelper {
        // input file validation based on the file extension
        private boolean isValidJsonFile(String fileNameStr) {
            boolean result = false;
            File jsonFile = prepareFile(fileNameStr);

            if (jsonFile != null && jsonFile.exists()) {
                String fileName = jsonFile.getName();
                int index = fileName.lastIndexOf(".");

                if (index > 0) {
                    String extension = fileName.substring(index + 1);
                    if (extension.equals("json")) {
                        result = true;
                    }
                }
            }
            return result;
        }

        private File prepareFile(String fileNameStr) {
            File tempFile = null;

            try {
                tempFile = new File(fileNameStr);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return tempFile;
        }

        private void readTextFile(File file) {
            Map<String, String> objMap = new HashMap<>();

            try (Stream<String> stream = Files.lines(Paths.get(file.getPath()))) {
                objMap = stream.filter(line -> !line.trim().isEmpty()) // Filter out empty lines
                        .filter(line -> line.length() > 2) // ignore { and }
                        .map(line -> line.split(":")) // Split each pair by colon
                        .collect(
                                Collectors.toMap(
                                        line -> line[0].trim(),
                                        line -> line[1].substring(0, line[1].length() - 1).trim(),
                                        (oldValue, newValue) -> oldValue // Merge function: keep the old value
                                ));

                JObject jObject = new JObject((HashMap) objMap);
                jObject.display();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static class JObject<T> {
        HashMap<String, T> elementMap;

        public JObject(HashMap<String, T> elementMap) {
            this.elementMap = elementMap;
        }

        public HashMap<String, T> getElementMap() {
            return elementMap;
        }

        public void setElementMap(HashMap<String, T> elementMap) {
            this.elementMap = elementMap;
        }

        public void display() {
            elementMap.forEach((key, value) -> {
                out.println(key + ": " + value);
            });
        }
    }
}