package test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class StringStuff {

    public static void main(String[] args)
    {
        String s1 = "/caht-from-to-hi how are you";
        String[] arr = s1.split("-");
        for(String s : arr)
        {
            System.out.println(s);
        }

    }
}
