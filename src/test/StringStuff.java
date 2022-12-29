package test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class StringStuff {

    public static void main(String[] args)
    {
        String s1 = "mohamd,hi";

        //if(s1.startsWith("/chat")) System.out.println("hi");


        System.out.println(s1.split(",")[1]);

    }
}
