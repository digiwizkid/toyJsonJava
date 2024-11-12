# toyJsonJava
Toy JSON parser

JSON RFC - https://www.rfc-editor.org/rfc/rfc7159.html


**Json Grammar**

A JSON text is a sequence of tokens.  The set of tokens includes six
structural characters, strings, numbers, and three literal names.

These are the six structural characters:

      begin-array     = ws %x5B ws  ; [ left square bracket

      begin-object    = ws %x7B ws  ; { left curly bracket

      end-array       = ws %x5D ws  ; ] right square bracket

      end-object      = ws %x7D ws  ; } right curly bracket

      name-separator  = ws %x3A ws  ; : colon

      value-separator = ws %x2C ws  ; , comma

Insignificant whitespace is allowed before or after any of the six
structural characters.

      ws = *(
              %x20 /              ; Space
              %x09 /              ; Horizontal tab
              %x0A /              ; Line feed or New line
              %x0D )              ; Carriage return


**Values**

A JSON value MUST be an object, array, number, or string, or one of
the following three literal names:

      false null true

The literal names MUST be lowercase.  No other literal names are
allowed.

      value = false / null / true / object / array / number / string

      false = %x66.61.6c.73.65   ; false

      null  = %x6e.75.6c.6c      ; null

      true  = %x74.72.75.65      ; true

**Objects**
An object structure is represented as a pair of curly brackets
surrounding zero or more name/value pairs (or members).  A name is a
string.  A single colon comes after each name, separating the name
from the value.  A single comma separates a value from a following
name.  The names within an object SHOULD be unique.

      object = begin-object [ member *( value-separator member ) ]
               end-object

      member = string name-separator value


**Arrays**

An array structure is represented as square brackets surrounding zero
or more values (or elements).  Elements are separated by commas.

array = begin-array [ value *( value-separator value ) ] end-array

**Numbers**

The representation of numbers is similar to that used in most
programming languages.  A number is represented in base 10 using
decimal digits.  It contains an integer component that may be
prefixed with an optional minus sign, which may be followed by a
fraction part and/or an exponent part.  Leading zeros are not
allowed.

A fraction part is a decimal point followed by one or more digits.

Numeric values that cannot be represented in the grammar below (such
as Infinity and NaN) are not permitted.

      number = [ minus ] int [ frac ] [ exp ]

      decimal-point = %x2E       ; .

      digit1-9 = %x31-39         ; 1-9

      e = %x65 / %x45            ; e E

      exp = e [ minus / plus ] 1*DIGIT

      frac = decimal-point 1*DIGIT

      int = zero / ( digit1-9 *DIGIT )

      minus = %x2D               ; -

      plus = %x2B                ; +

      zero = %x30                ; 0


**Strings**

The representation of strings is similar to conventions used in the C
family of programming languages.  A string begins and ends with
quotation marks.  All Unicode characters may be placed within the
quotation marks, except for the characters that must be escaped:
quotation mark, reverse solidus, and the control characters (U+0000
through U+001F).

string = quotation-mark *char quotation-mark

      char = unescaped /
          escape (
              %x22 /          ; "    quotation mark  U+0022
              %x5C /          ; \    reverse solidus U+005C
              %x2F /          ; /    solidus         U+002F
              %x62 /          ; b    backspace       U+0008
              %x66 /          ; f    form feed       U+000C
              %x6E /          ; n    line feed       U+000A
              %x72 /          ; r    carriage return U+000D
              %x74 /          ; t    tab             U+0009
              %x75 4HEXDIG )  ; uXXXX                U+XXXX

      escape = %x5C              ; \

      quotation-mark = %x22      ; "

      unescaped = %x20-21 / %x23-5B / %x5D-10FFFF

**Parsers**

A JSON parser transforms a JSON text into another representation.  A
JSON parser MUST accept all texts that conform to the JSON grammar.
A JSON parser MAY accept non-JSON forms or extensions.

An implementation may set limits on the size of texts that it
accepts.  An implementation may set limits on the maximum depth of
nesting.  An implementation may set limits on the range and precision
of numbers.  An implementation may set limits on the length and
character contents of strings.


**Examples**


This is a JSON object:

      {
        "Image": {
            "Width":  800,
            "Height": 600,
            "Title":  "View from 15th Floor",
            "Thumbnail": {
                "Url":    "http://www.example.com/image/481989943",
                "Height": 125,
                "Width":  100
            },
            "Animated" : false,
            "IDs": [116, 943, 234, 38793]
          }
      }

Its Image member is an object whose Thumbnail member is an object and
whose IDs member is an array of numbers.

This is a JSON array containing two objects:
    [
        {
            "precision": "zip",
            "Latitude":  37.7668,
            "Longitude": -122.3959,
            "Address":   "",
            "City":      "SAN FRANCISCO",
            "State":     "CA",
            "Zip":       "94107",
            "Country":   "US"
        },
        {
            "precision": "zip",
            "Latitude":  37.371991,
            "Longitude": -122.026020,
            "Address":   "",
            "City":      "SUNNYVALE",
            "State":     "CA",
            "Zip":       "94085",
            "Country":   "US"
        }
    ]

Here are three small JSON texts containing only values:

"Hello world!"

42

true