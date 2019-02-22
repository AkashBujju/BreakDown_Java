import java.util.List;
import java.util.ArrayList;

public class SymbolTable {
	List<VariableInfo> var_infos;	

	SymbolTable() {
		var_infos = new ArrayList<>();
	}

	void add(String name, String type, String raw_value, String scope) {
		VariableInfo var_info = new VariableInfo();
		var_info.name = name;
		var_info.type = type;
		var_info.raw_value = raw_value;
		var_info.scope = scope;

		var_infos.add(var_info);
	}

	// @Incomplete .....
}
