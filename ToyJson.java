///usr/bin/env jbang "$0" "$@" ; exit $?


import static java.lang.System.*;

import java.io.File;

public class ToyJson {

    public static void main(String... args) {
        if (args.length == 0 || args[0].equals("")) {
            System.out.println("Provide valid input");
        } else {
            File jsonFile = null;
            try{
                jsonFile = new File(args[0]);
            } catch(Exception e) {
                e.printStackTrace();
            }

            if (jsonFile != null && jsonFile.exists()) {
                String fileName = jsonFile.getName();
                int index = fileName.lastIndexOf(".");
                
                if (index > 0) {
                    String extension = fileName.substring(index+1);
                    if (extension.equals("json")) {
                        System.out.println("json file");
                    } else {
                        System.out.println("Invalid file");
                    }
                }
            }
        }
    }
}
