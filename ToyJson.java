/// usr/bin/env jbang "$0" "$@" ; exit $?


import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.System.out;
import static java.nio.file.Files.lines;

public class ToyJson {

    public static void main(String... args) {
        final ToyJsonHelper helper = new ToyJsonHelper();

        // check for valid input
        if (args.length == 0 || args[0].equals("")) {
            out.println("Provide valid input");
        } else {
            if (helper.isValidJsonFile(args[0])) {
                out.println("Json file");

                try {
                    helper.readTextFile(helper.prepareFile(args[0]));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
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

        private void readTextFile(File file) throws IOException {

            // supplier to reuse stream
            Supplier<Stream<String>> lineSupplier = () -> {
                try {
                    return lines(Paths.get(file.getPath()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            };

            // check for array or object
            boolean isArray = lineSupplier.get().filter(line -> !line.trim().isEmpty())
                    .findFirst().get().charAt(0) == '[';

            boolean isObj = lineSupplier.get().filter(line -> !line.trim().isEmpty())
                    .findFirst().get().charAt(0) == '{';

                /*if (isArray) {
                    HashMap<String, String> parsedObjMap = (HashMap<String, String>) parseObject(lineSupplier);
                    JObject jObject = new JObject(parsedObjMap);
                    jObject.display();
                } else {*/
            HashMap<String, Object> parsedObjMap = (HashMap<String, Object>) parseObject(lineSupplier);
            JObject jObject = new JObject(parsedObjMap);
            jObject.display();
//                }
        }

        private Map<String, Object> parseObject(Supplier<Stream<String>> supplier) {
            // single object : test.json
            return supplier.get().filter(line -> !line.trim().isEmpty()) // Filter out empty lines
                    .filter(line -> !line.trim().equals("{")) // ignore {, }, [, ]
                    .filter(line -> !line.trim().equals("}")) // ignore {, }, [, ]
                    .map(line -> line.split(":", 2)) // Split each pair by colon
                    .peek(line -> out.println(line.length + " - " + line[0] + " - " + line[1]))
                    .collect(
                            Collectors.toMap(
                                    line -> line[0].trim(),
                                    line -> line[1].trim(),
                                    (oldValue, newValue) -> oldValue // Merge function: keep the old value
                            ));
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