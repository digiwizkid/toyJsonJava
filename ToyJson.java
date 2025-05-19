/// usr/bin/env jbang "$0" "$@" ; exit $?

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
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
            boolean isArray =
                    lineSupplier
                            .get()
                            .filter(line -> !line.trim().isEmpty())
                            .findFirst()
                            .get()
                            .charAt(0) ==
                            '[';

            // parse nested obj
            parseNestedObj(file);

            if (isArray) {
                HashMap<String, Object> parsedObjMap = (HashMap<
                        String,
                        Object
                        >) parseArray(lineSupplier);
                //                JObject jObject = new JObject(parsedObjMap);
                //                jObject.display();
                //
                //                JArray jArray = new JArray();lineSupplier
                //                jArray.addObj(jObject);
                //                jArray.display();
            } else {
                //                HashMap<String, Object> parsedObjMap = (HashMap<String, Object>) parseObject(lineSupplier);
                //                parseMapForObject(parsedObjMap);
                //                JObject jObject = new JObject(parsedObjMap);
                //                jObject.display();
            }
        }

        private void parseMapForObject(HashMap<String, Object> inputMap) {
            out.println("map content\n");
            inputMap.forEach((key, value) -> {
                out.println(key + " : " + value);
            });
        }

        private void parseNestedObj(File file) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                StringBuilder sb = new StringBuilder();
                String nextLine;

                while ((nextLine = br.readLine()) != null) {
                    sb.append(nextLine);
                }

                br.close();
                out.println("res:\n" + sb);

                // {  "Image": {    "Width":  800,    "Height": 600,    "Title":  "View from 15th Floor",    "Thumbnail": {      "Url":    "http://www.example.com/image/481989943",      "Height": 125,      "Width":  100    },    "Animated" : false,    "IDs": [116, 943, 234, 38793]  }}

                // beautify
                String resultString;
                resultString = Arrays.stream(sb
                        .chars()
                        .mapToObj(c -> {
                            char character = (char) c;

                            if (isOpeningPunctuation(character))
                                return character + "\n";
                            else if (isClosingPunctuation(character))
                                return "\n" + character;
                            else
                                return character + "";

                        })
                        .map(str->str.split(":"))
                        .toArray())
                                .map(str->str.toString().trim())
                        .collect(Collectors.joining());

                out.println("res1:\n" + resultString);

            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // Helper method to check for punctuation
        private boolean isOpeningPunctuation(char c) {
            return c == '{' || c == ',' || c == '[';
        }

        private boolean isClosingPunctuation(char c) {
            return c == '}' || c == ']';
        }


        private Map<String, Object> parseObject(
                Supplier<Stream<String>> supplier
        ) {
            // single linear object : test.json
            return supplier
                    .get()
                    .filter(line -> !line.trim().isEmpty()) // Filter out empty lines
                    .filter(line -> !line.trim().equals("{")) // ignore {
                    .filter(line -> !line.trim().equals("}")) // ignore }
                    .filter(line -> !line.trim().equals("[")) // ignore [
                    .filter(line -> !line.trim().equals("]")) // ignore ]
                    .map(line -> line.split(":", 2)) // Split each pair by colon
                    .collect(
                            Collectors.toMap(
                                    line -> line[0].trim(),
                                    line -> line[1].trim(),
                                    (oldValue, newValue) -> oldValue // Merge function: keep the old value
                            )
                    );
        }

        private Map<String, Object> parseArray(
                Supplier<Stream<String>> supplier
        ) {
            /*[
                {
                    "name": "test1"
                },
                {
                    "name": "test2"
                }
            ]*/

            Map<String, Object> result = supplier
                    .get()
                    .filter(line -> !line.trim().isEmpty()) // Filter out empty lines
                    .filter(line -> !line.trim().equals("[")) // ignore [
                    .filter(line -> !line.trim().equals("{")) // ignore [
                    .filter(line -> !line.trim().equals("}")) // ignore [
                    .filter(line -> !line.trim().equals("},")) // ignore [
                    .filter(line -> !line.trim().equals("]")) // ignore ]
                    .map(line -> line.split(":")) // Split each obj by ,
                    .filter(line -> !line.equals(","))
                    .collect(
                            Collectors.toMap(
                                    line -> line[0],
                                    line -> line[1],
                                    (oldValue, newValue) -> oldValue
                            )
                    );

            return new HashMap<>();
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
            out.println("Displaying object content: \n");
            elementMap.forEach((key, value) -> {
                out.println(key + ": " + value);
            });
        }
    }

    static class JArray {

        ArrayList<JObject> objList;

        public void addObj(JObject jObject) {
            if (objList == null) {
                objList = new ArrayList<>();
            }
            objList.add(jObject);
        }

        public ArrayList getObjList() {
            return objList;
        }

        public void setObjList(ArrayList objList) {
            this.objList = objList;
        }

        public void display() {
            out.println("Displaying array content: \n");
        }
    }
}
