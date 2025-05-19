import java.util.*;

/**
 * A basic JSON parser implemented in Java
 * Supports parsing of JSON objects, arrays, strings, numbers, booleans, and null
 */
public class JSONParser {
    private String jsonStr;
    private int pos = 0;

    public JSONParser(String jsonStr) {
        this.jsonStr = jsonStr;
    }

    /**
     * Parse the JSON string and return the corresponding Java object
     */
    public Object parse() {
        skipWhitespace();
        Object result = parseValue();
        skipWhitespace();

        // Check if there is any unconsumed input
        if (pos < jsonStr.length()) {
            throw new RuntimeException("Unexpected character at position " + pos);
        }

        return result;
    }

    /**
     * Parse a JSON value
     */
    private Object parseValue() {
        skipWhitespace();

        char c = peek();

        if (c == '{') {
            return parseObject();
        } else if (c == '[') {
            return parseArray();
        } else if (c == '"') {
            return parseString();
        } else if (c == 't' || c == 'f') {
            return parseBoolean();
        } else if (c == 'n') {
            return parseNull();
        } else if (Character.isDigit(c) || c == '-') {
            return parseNumber();
        } else {
            throw new RuntimeException("Unexpected character '" + c + "' at position " + pos);
        }
    }

    /**
     * Parse a JSON object
     */
    private Map<String, Object> parseObject() {
        Map<String, Object> map = new HashMap<>();

        // Consume the opening brace
        consume('{');
        skipWhitespace();

        // Check for empty object
        if (peek() == '}') {
            consume('}');
            return map;
        }

        // Parse key-value pairs
        while (true) {
            skipWhitespace();

            // Parse the key (must be a string)
            if (peek() != '"') {
                throw new RuntimeException("Expected string key, found '" + peek() + "' at position " + pos);
            }

            String key = parseString();
            skipWhitespace();

            // Consume the colon
            consume(':');
            skipWhitespace();

            // Parse the value
            Object value = parseValue();
            map.put(key, value);

            skipWhitespace();

            // Check for end of object or next key-value pair
            if (peek() == '}') {
                consume('}');
                return map;
            }

            // Consume the comma
            consume(',');
        }
    }

    /**
     * Parse a JSON array
     */
    private List<Object> parseArray() {
        List<Object> list = new ArrayList<>();

        // Consume the opening bracket
        consume('[');
        skipWhitespace();

        // Check for empty array
        if (peek() == ']') {
            consume(']');
            return list;
        }

        // Parse values
        while (true) {
            Object value = parseValue();
            list.add(value);

            skipWhitespace();

            // Check for end of array or next value
            if (peek() == ']') {
                consume(']');
                return list;
            }

            // Consume the comma
            consume(',');
            skipWhitespace();
        }
    }

    /**
     * Parse a JSON string
     */
    private String parseString() {
        StringBuilder sb = new StringBuilder();

        // Consume the opening quote
        consume('"');

        while (pos < jsonStr.length() && peek() != '"') {
            char c = peek();

            if (c == '\\') {
                // Handle escape sequences
                consume('\\');

                if (pos >= jsonStr.length()) {
                    throw new RuntimeException("Unexpected end of input in string");
                }

                c = consume();

                switch (c) {
                    case '"':
                    case '\\':
                    case '/':
                        sb.append(c);
                        break;
                    case 'b':
                        sb.append('\b');
                        break;
                    case 'f':
                        sb.append('\f');
                        break;
                    case 'n':
                        sb.append('\n');
                        break;
                    case 'r':
                        sb.append('\r');
                        break;
                    case 't':
                        sb.append('\t');
                        break;
                    case 'u':
                        // Parse 4-digit hex Unicode value
                        if (pos + 4 > jsonStr.length()) {
                            throw new RuntimeException("Unexpected end of input in Unicode escape");
                        }

                        String hex = jsonStr.substring(pos, pos + 4);
                        try {
                            int unicode = Integer.parseInt(hex, 16);
                            sb.append((char) unicode);
                            pos += 4;
                        } catch (NumberFormatException e) {
                            throw new RuntimeException("Invalid Unicode escape sequence");
                        }
                        break;
                    default:
                        throw new RuntimeException("Invalid escape sequence: \\" + c);
                }
            } else {
                sb.append(consume());
            }
        }

        // Consume the closing quote
        consume('"');

        return sb.toString();
    }

    /**
     * Parse a JSON number
     */
    private Number parseNumber() {
        int start = pos;

        // Handle negative sign
        if (peek() == '-') {
            consume();
        }

        // Handle integer part
        if (peek() == '0') {
            consume();
        } else if (Character.isDigit(peek())) {
            consume();
            while (pos < jsonStr.length() && Character.isDigit(peek())) {
                consume();
            }
        } else {
            throw new RuntimeException("Invalid number at position " + pos);
        }

        boolean isFloat = false;

        // Handle decimal part
        if (pos < jsonStr.length() && peek() == '.') {
            isFloat = true;
            consume();

            if (!Character.isDigit(peek())) {
                throw new RuntimeException("Expected digit after decimal point");
            }

            while (pos < jsonStr.length() && Character.isDigit(peek())) {
                consume();
            }
        }

        // Handle exponent part
        if (pos < jsonStr.length() && (peek() == 'e' || peek() == 'E')) {
            isFloat = true;
            consume();

            if (peek() == '+' || peek() == '-') {
                consume();
            }

            if (!Character.isDigit(peek())) {
                throw new RuntimeException("Expected digit in exponent");
            }

            while (pos < jsonStr.length() && Character.isDigit(peek())) {
                consume();
            }
        }

        String numberStr = jsonStr.substring(start, pos);

        try {
            if (isFloat) {
                return Double.parseDouble(numberStr);
            } else {
                long longValue = Long.parseLong(numberStr);
                if (longValue >= Integer.MIN_VALUE && longValue <= Integer.MAX_VALUE) {
                    return (int) longValue;
                }
                return longValue;
            }
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid number format: " + numberStr);
        }
    }

    /**
     * Parse a JSON boolean
     */
    private Boolean parseBoolean() {
        if (pos + 4 <= jsonStr.length() && jsonStr.substring(pos, pos + 4).equals("true")) {
            pos += 4;
            return true;
        } else if (pos + 5 <= jsonStr.length() && jsonStr.substring(pos, pos + 5).equals("false")) {
            pos += 5;
            return false;
        } else {
            throw new RuntimeException("Expected 'true' or 'false' at position " + pos);
        }
    }

    /**
     * Parse a JSON null
     */
    private Object parseNull() {
        if (pos + 4 <= jsonStr.length() && jsonStr.substring(pos, pos + 4).equals("null")) {
            pos += 4;
            return null;
        } else {
            throw new RuntimeException("Expected 'null' at position " + pos);
        }
    }

    /**
     * Skip whitespace characters
     */
    private void skipWhitespace() {
        while (pos < jsonStr.length() && Character.isWhitespace(jsonStr.charAt(pos))) {
            pos++;
        }
    }

    /**
     * Peek at the current character without consuming it
     */
    private char peek() {
        if (pos >= jsonStr.length()) {
            throw new RuntimeException("Unexpected end of input");
        }
        return jsonStr.charAt(pos);
    }

    /**
     * Consume the current character
     */
    private char consume() {
        if (pos >= jsonStr.length()) {
            throw new RuntimeException("Unexpected end of input");
        }
        return jsonStr.charAt(pos++);
    }

    /**
     * Consume a specific character
     */
    private void consume(char expected) {
        if (pos >= jsonStr.length()) {
            throw new RuntimeException("Unexpected end of input, expected '" + expected + "'");
        }

        char actual = jsonStr.charAt(pos);
        if (actual != expected) {
            throw new RuntimeException("Expected '" + expected + "', found '" + actual + "' at position " + pos);
        }

        pos++;
    }

    /**
     * Example usage
     */
    public static void main(String[] args) {
        String json = "{\n" +
                "  \"name\": \"John Doe\",\n" +
                "  \"age\": 30,\n" +
                "  \"isStudent\": false,\n" +
                "  \"grades\": [95, 87.5, 92],\n" +
                "  \"address\": {\n" +
                "    \"street\": \"123 Main St\",\n" +
                "    \"city\": \"Anytown\",\n" +
                "    \"zipCode\": \"12345\"\n" +
                "  },\n" +
                "  \"phoneNumbers\": [\n" +
                "    {\n" +
                "      \"type\": \"home\",\n" +
                "      \"number\": \"555-1234\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"type\": \"work\",\n" +
                "      \"number\": \"555-5678\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"nullValue\": null\n" +
                "}";

        try {
            JSONParser parser = new JSONParser(json);
            Object result = parser.parse();
            System.out.println("Parsing successful!");
            System.out.println(result);
        } catch (Exception e) {
            System.err.println("Error parsing JSON: " + e.getMessage());
        }
    }
}