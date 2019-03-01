import java.util.List;
import java.util.ArrayList;

public class ErrorLog {
	List<String> log;

	ErrorLog() {
		log = new ArrayList<>();
	}

	void push(String error, String str, int line_number) {
		String e = "------> " + str + "\n" + "Error: " + error + " @@ line " + line_number + ".";
		log.add(e);
	}

	void show() {
		int error_number = 0;
		System.out.println();
		System.out.println(log.size() + " ERRORS found ....");
		System.out.println("-----------------------------");
		for(String s: log) {
			error_number += 1;
			System.out.println(error_number + ": " + s);
			System.out.println();
		}
		System.out.println("-----------------------------");
		System.out.println();
	}
}
