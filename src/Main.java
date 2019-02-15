public class Main {

    public static void main(String[] args) {
        String filename = "C:\\Users\\Akash\\Documents\\Programming\\Java\\BreakDown_InputStream\\asset\\demo.veg";
        StringBuffer data;
        MyFile my_file;

        try {
            my_file = new MyFile(filename);
            data = my_file.get_data();

            String eaten_string = Util.eat_spaces(data.toString());
            data = new StringBuffer(eaten_string);

            System.out.println("data: " + data);
            System.out.println("Size: " + data.length());
            System.out.println();

            // Process character by character ....
            BreakDown b = new BreakDown(data.toString());
            StringBuffer sb = b.get_broken_string();

            //

            System.out.println("sb: " + sb);
        }
        catch (Exception e) {
            System.out.println("Exception @ class Main: ");
            System.out.println("What: " + e);
        }

    }
}
