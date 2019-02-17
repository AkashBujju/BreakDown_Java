import java.util.List;
import java.util.ArrayList;

enum StatementType {
	VAR_DECLARE_1,
	VAR_DECLARE_2,
	VAR_ASSIGN,
	ENUM,
	STRUCT,
	IF,
	ELSE,
	ELSE_IF,
	FUNC_DEF,
	WHILE,
	USE,
	ERROR,
	NOT_KNOWN
}

class Info {
	private List<StatementInfo> infos = new ArrayList<>();

	void add(StatementInfo info) {
		infos.add(info);
	}

	void process() {
		for(StatementInfo stat_info: infos) {
			// stat_info.process();
			stat_info.print();
		}
	}
}

abstract class StatementInfo {
	StatementType stat_type;
	String broken_string;

	StatementInfo(String broken_string, StatementType stat_type) {
		this.stat_type = stat_type;
		this.broken_string = broken_string;
	}

	abstract void process();
	abstract void print(); // @tmp
}

class ErrorInfo extends StatementInfo {
	String start_index;
	String end_index;
	String value;

	ErrorInfo(String broken_string) {
		super(broken_string, StatementType.ERROR);
		List<String> li = Util.split_using_at(broken_string);

		start_index = li.get(1);
		end_index = li.get(2);
		value = li.get(3);
	}

	void print() {
		System.out.println("Error");
		System.out.println("StartIndex: " + start_index);
		System.out.println("EndIndex: " + end_index);
		System.out.println("Value: " + value);

		System.out.println();
	}

	void process() {

	}
}

class VariableInfo extends StatementInfo {
	String name;
	String type;
	String value_exp;

	// StatementType has to be one of VAR_DECLARE_1, VAR_DECLARE_2, VAR_ASSIGN
	VariableInfo(String broken_string, StatementType type) {
		super(broken_string, type);
		List<String> li = Util.split_using_at(broken_string);

		this.name = li.get(1);
		this.type = li.get(2);
		this.value_exp = li.get(3);
	}

	void print() {
		System.out.println("Variable");
		System.out.println("Name: " + name);
		System.out.println("Type: " + type);
		System.out.println("Value_Exp: " + value_exp);

		System.out.println();
	}

	void process() {

	}
}

class FunctionInfo extends StatementInfo {
	String name;
	String return_type;
	String args;
	String value;	

	FunctionInfo(String broken_string) {
		super(broken_string, StatementType.FUNC_DEF);
		List<String> li = Util.split_using_at(broken_string);

		name = li.get(1);
		args = li.get(2);
		return_type = li.get(3);
		value = li.get(4);
	}

	void print() {
		System.out.println("Function");
		System.out.println("Name: " + name);
		System.out.println("Args: " + args);
		System.out.println("ReturnType: " + return_type);
		System.out.println("Value: " + value);

		System.out.println();
	}

	void process() {

	}
}
