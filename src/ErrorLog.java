import java.util.ArrayList;
import java.util.List;

public class ErrorLog {
    List<String> log;

    ErrorLog() {
        log = new ArrayList<>();
    }

    void push_error(String error_msg, int line_number) {
        String final_error = "Error at line " + line_number + ": ";
        final_error += error_msg;

        log.add(final_error);
    }

    void show_all_error() {
        if(log.size() == 0) {
            System.out.println("No Errors");
        }
        else {
            System.out.println("Errors are: ");
            for(String s: log) {
                System.out.println(s);
            }
        }
    }
}
