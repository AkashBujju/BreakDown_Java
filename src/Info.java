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
		for(StatementInfo stat_info: infos)
			stat_info.process();
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
}

class ErrorInfo extends StatementInfo {
	String start_index;
	String end_index;
	String value;

	ErrorInfo(String broken_string) {
		super(broken_string, StatementType.ERROR);

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
	}

	void process() {

	}
}

class FunctionInfo extends StatementInfo {
	String function_name;
	String return_type;
	String args;
	String value;	

	FunctionInfo(String broken_string) {
		super(broken_string, StatementType.FUNC_DEF);
		List<String> li = Util.split_using_at(broken_string);

		function_name = li.get(1);
		args = li.get(2);
		return_type = li.get(3);
		value = li.get(4);
	}

	void process() {

	}
}
