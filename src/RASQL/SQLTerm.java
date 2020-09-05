package RASQL;

public class SQLTerm {
	String _strTableName;
	String _strColumnName;
	String _strOperator;
	Object _objValue;
	public SQLTerm(String _strTableName,String _strColumnName,String _strOperator,Object _objValue) {
		this._strTableName=_strTableName;
		this._strColumnName=_strColumnName;
		this._strOperator=_strOperator;
		this._objValue=_objValue;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
