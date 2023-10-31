package replpp.rmi;

import java.io.FileNotFoundException;
import java.io.PrintStream;

public class RmiOutputStream extends PrintStream {

    public static void main(String[] args) throws Exception {
        RmiOutputStream out = new RmiOutputStream("D:\\test.txt", "");
        System.out.println("helo");
    }

    public RmiOutputStream(String fileName, String str) throws FileNotFoundException {
        super(fileName);
        this.str = str;
    }

    private String str;

    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }

    @Override
    public void print(String s) {
//        System.out.println(":" + s);
        if (s.contains("val")) {
            this.str = this.str + s + "\n";
        }
    }

    @Override
    public void println(String x) {
//        System.out.println("ln:" + x);
        if (x.contains("val")) {
            this.str = this.str + x + "\n";
        }

    }
}


