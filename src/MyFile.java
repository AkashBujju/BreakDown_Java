import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class LineInfo {
    int line_number = -1;
    int start_index = -1;
    int end_index = -1;
}

class MyFile {
    private String filename;
    private List<LineInfo> line_info = new ArrayList<>();

    MyFile(String filename) throws FileNotFoundException {
        this.filename = filename;

        // Storing the beginning and ending index of each line, ignoring the new_line character
        try (Scanner s = new Scanner(new BufferedReader(new FileReader(filename)))) {
            int current_begin_index = 0;
            int line_number = 0;

            while (s.hasNextLine()) {
                line_number += 1;
                String current_line = s.nextLine();
                current_line = Util.eat_only_spaces(current_line);

                int end_index = current_line.length() + current_begin_index - 1;

                LineInfo tmp_line_info = new LineInfo();
                tmp_line_info.line_number = line_number;
                tmp_line_info.start_index = current_begin_index;
                tmp_line_info.end_index = end_index;

                line_info.add(tmp_line_info);

                current_begin_index = end_index + 1;
            }
        }
    }

    int get_line_number(int index) {
        for(LineInfo l: line_info) {
            if(index >= l.start_index && index <= l.end_index)
                return l.line_number;
        }

        return -1;
    }

    StringBuffer get_data() throws FileNotFoundException {
        StringBuffer data = new StringBuffer();

        try (Scanner s = new Scanner(new BufferedReader(new FileReader(filename)))) {
            while (s.hasNextLine()) {
                data.append(s.nextLine());
            }
        }

        return data;
    }
}
