package test;

import java.util.Hashtable;
import java.util.Map;


class AA{
    int a;
    String s;
    public AA(){a=0;}
    public AA(int n){
        a = n;
    }
}
public class DataStructureTest {
    public static void main(String[] args)
    {
        Hashtable<String, AA> hashtable = new Hashtable();

        hashtable.put("0995",new AA(4));
        hashtable.put("0991",new AA(3));
        hashtable.put("0992",new AA(2));


        if(hashtable.get("099599").s != null){
            System.out.println(hashtable.get("099599"));
        }
//        for(AA stuff : hashtable.values())
//        {
//            System.out.println(stuff.a);
//        }
//        System.out.println("-------------------");

//        Object obj = hashtable.remove("0995");
//        hashtable.put("1111", (AA) obj);



//        for(Map.Entry<String, AA> stuff : hashtable.entrySet())
//        {
//            System.out.println(stuff.getKey());
//        }
    }
}
