package RASQL;
import java.awt.Polygon;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;



public class DBAppTest {
	static DBApp dbApp;
	public static void main(String[] args) throws Exception {
//		String strTableName = "Integer";
		dbApp = new DBApp( );
		
		
//		dbApp.init();
//		
//		Hashtable htblColNameType = new Hashtable( );
//		htblColNameType.put("id", "java.lang.Integer");
//		htblColNameType.put("name", "java.lang.String");
//		htblColNameType.put("gpa", "java.lang.Double");
//		dbApp.createTable( strTableName, "id", htblColNameType );
//		viewTable(strTableName);
//				
//		insertTest(strTableName);
//		viewTable(strTableName);
		
//		delete(strTableName);
//		viewTable(strTableName);
		
//		testBoolean();

//		testDate();
		
//		testDouble();
		
//		testPolygon();
		
		testString();
//		dbApp.createBTreeIndex("Integer", "id");
		
//		viewTableBySelect("String");
		
//		testInteger(); // generated 20000 tuples
//		
//		viewTableBySelect("Integer");
		
//		Table table=(Table)dbApp.deserialize("Integer");
//		int cnt=0;
//		int cntpages=0;
//		for(int i=table.pages.size()-1;i>=0;i--) {
//			String s=table.pages.get(i);
//			
//			Page p=(Page)dbApp.deserialize(s);
//			
//			cnt+=p.v.size();
//			cntpages++;
//		}
//		System.out.println("count of tuples : "+cnt);
//		System.out.println("count of pages : " + cntpages);
		
		//Test Exceptions
		
//		String strTableName = "77Exceptions";
//		dbApp = new DBApp();
//		Hashtable htblColNameType = new Hashtable( );
//		htblColNameType.put("id", "java.awt.Polygon");
//		htblColNameType.put("name", "java.lang.String");
//		htblColNameType.put("gpa", "java.lang.Double");
//		dbApp.createTable( strTableName, "id", htblColNameType );

//		strTableName = "Polygon";
//		dbApp = new DBApp();
//		
//		Hashtable htblColNameValue = new Hashtable();
//		
//		int[]x1= {55,66,100},y1= {5,6,7};
//		
//		htblColNameValue.put("id", new Polygon(x1, y1, 3));
//		htblColNameValue.put("name", new String("zzz") );
//		htblColNameValue.put("gpa", new Double( 1.7 ) );
//		dbApp.insertIntoTable( strTableName , htblColNameValue);
//		
//		viewTable("String");
		
//		htblColNameValue = new Hashtable();
//		htblColNameValue.put("gpa", new Double(15));
//		dbApp.updateTable(strTableName,"Sawan" , htblColNameValue);
//		viewTable(strTableName);
//		
//		htblColNameValue = new Hashtable( );
//		htblColNameValue.put("gpa", new Double(0.7) );
//		dbApp.updateTable(strTableName,"1998-9-7" , htblColNameValue);
//		viewTable(strTableName);
		
//		htblColNameValue = new Hashtable( );
//		htblColNameValue.put("name", new Double( 0.001 ) );
//		dbApp.deleteFromTable(strTableName , htblColNameValue );
//		viewTable(strTableName);
	}
	static String[] name = {"Ahmed Noor","Sawan","Reda","Omar"};
	static void testPolygon() throws Exception{
		String strTableName = "Polygon";
//		dbApp = new DBApp();
//		Hashtable htblColNameType = new Hashtable( );
//		htblColNameType.put("id", "java.awt.Polygon");
//		htblColNameType.put("name", "java.lang.String");
//		htblColNameType.put("gpa", "java.lang.Double");
//		dbApp.createTable( strTableName, "id", htblColNameType );
		int[]x1= {1,2,3},y1= {7,5,6};
		
		int[]x2= {0,2,4},y2= {1,5,6};
//		dbApp.createBTreeIndex(strTableName, "name");
		Hashtable htblColNameValue = new Hashtable( );
//		htblColNameValue.put("id", new Polygon(x1, y1, 3));
//		htblColNameValue.put("name", new String("Ahmed Noor" ) );
//		htblColNameValue.put("gpa", new Double( 0.95 ) );
//		dbApp.insertIntoTable( strTableName , htblColNameValue );
//		
//		htblColNameValue = new Hashtable( );
//		htblColNameValue.put("id", new Polygon(x2, y2, 3));
//		htblColNameValue.put("name", new String("Ahmed Noor" ) );
//		htblColNameValue.put("gpa", new Double( 0.95 ) );
//		dbApp.insertIntoTable( strTableName , htblColNameValue );
//		dbApp.createRTreeIndex(strTableName, "id");

//		for(int i=0;i<15;i++) {
//			x1 = randomCoordinates(); y1 = randomCoordinates();
//			htblColNameValue.put("id", new Polygon(x1, y1, 3));
//			htblColNameValue.put("name", names[(int) (Math.random()*4)]);
//			htblColNameValue.put("gpa", new Double( Math.random() ) );
//			dbApp.insertIntoTable( strTableName , htblColNameValue );
//		}
//		viewTable(strTableName);
//		htblColNameValue = new Hashtable( );
//		htblColNameValue.put("gpa", new Double( 0.077 ));
//		htblColNameValue.put("name", "Eslam" );
//		dbApp.updateTable(strTableName,"(6,20),(8,5),(12,16)" , htblColNameValue);
//		htblColNameValue = new Hashtable( );
//		htblColNameValue.put("gpa", new Double( 0.077 ));
//		htblColNameValue.put("name", "Eslam" );
//		dbApp.updateTable(strTableName,"(15,1),(5,19),(4,5)" , htblColNameValue);

//		viewTable(strTableName);
//		htblColNameValue = new Hashtable( );
//		htblColNameValue.put("name", "Sawan");
//		dbApp.deleteFromTable(strTableName , htblColNameValue );
//		
//		htblColNameValue = new Hashtable( );
//		htblColNameValue.put("name", "Reda");
//		dbApp.deleteFromTable(strTableName , htblColNameValue );
//		viewTable(strTableName);
		
	}
	static int[] randomCoordinates() {
		int[] x = new int[3];
		for (int i = 0; i < 3; i++) {
			x[i] = (int) (Math.random()*20)+1;
		}
		return x;
	}
	static void testDate() throws Exception{
		String strTableName = "Date";
		dbApp = new DBApp();
		Hashtable htblColNameType = new Hashtable( );
		htblColNameType.put("id", "java.util.Date");
		htblColNameType.put("name", "java.lang.String");
		htblColNameType.put("gpa", "java.lang.Double");
		dbApp.createTable( strTableName, "id", htblColNameType );
		
		
		Hashtable htblColNameValue = new Hashtable( );
		htblColNameValue.put("id", new SimpleDateFormat("yyyy-MM-dd").parse("1998-9-7"));
		htblColNameValue.put("name", new String("Ahmed Noor" ) );
		htblColNameValue.put("gpa", new Double( 0.95 ) );
		dbApp.insertIntoTable( strTableName , htblColNameValue );
		
		htblColNameValue = new Hashtable( );
		htblColNameValue.put("id", new SimpleDateFormat("yyyy-MM-dd").parse("1998-9-7"));
		htblColNameValue.put("name", new String("Ahmed Noor" ) );
		htblColNameValue.put("gpa", new Double( 0.95 ) );
		dbApp.insertIntoTable( strTableName , htblColNameValue );
		
		htblColNameValue = new Hashtable( );
		htblColNameValue.put("id", new SimpleDateFormat("yyyy-MM-dd").parse("1998-9-7"));
		htblColNameValue.put("name", new String("Ahmed Noor" ) );
		htblColNameValue.put("gpa", new Double( 0.95 ) );
		dbApp.insertIntoTable( strTableName , htblColNameValue );
		
		htblColNameValue = new Hashtable( );
		htblColNameValue.put("id", new SimpleDateFormat("yyyy-MM-dd").parse("2020-9-7"));
		htblColNameValue.put("name", new String("Ahmed Noor" ) );
		htblColNameValue.put("gpa", new Double( 0.95 ) );
		dbApp.insertIntoTable( strTableName , htblColNameValue );
		viewTable(strTableName);
		
		htblColNameValue = new Hashtable( );
		htblColNameValue.put("gpa", new Double( 0.001) );
		dbApp.updateTable(strTableName,"1998-9-7" , htblColNameValue);
		viewTable(strTableName);

		
//		htblColNameValue = new Hashtable( );
//		htblColNameValue.put("name", new String("Ahmed Noor") );
//		dbApp.deleteFromTable(strTableName , htblColNameValue );
//		viewTable(strTableName);
		
	}
	static void testDouble() throws Exception{
		String strTableName = "Double";
		dbApp = new DBApp();
		Hashtable htblColNameType = new Hashtable( );
		htblColNameType.put("id", "java.lang.Double");
		htblColNameType.put("name", "java.lang.String");
		htblColNameType.put("gpa", "java.lang.Double");
		dbApp.createTable( strTableName, "id", htblColNameType );
		
		
		Hashtable htblColNameValue = new Hashtable( );
		htblColNameValue.put("id", new Double(1));
		htblColNameValue.put("name", new String("Ahmed Noor" ) );
		htblColNameValue.put("gpa", new Double( 0.95 ) );
		dbApp.insertIntoTable( strTableName , htblColNameValue );
		
		htblColNameValue = new Hashtable( );
		htblColNameValue.put("id", new Double(7));
		htblColNameValue.put("name", new String("Ahmed Noor" ) );
		htblColNameValue.put("gpa", new Double( 0.95 ) );
		dbApp.insertIntoTable( strTableName , htblColNameValue );
		
		htblColNameValue = new Hashtable( );
		htblColNameValue.put("id", new Double(0.1));
		htblColNameValue.put("name", new String("Ahmed Noor" ) );
		htblColNameValue.put("gpa", new Double( 0.95 ) );
		dbApp.insertIntoTable( strTableName , htblColNameValue );
		
		htblColNameValue = new Hashtable( );
		htblColNameValue.put("id", new Double(1.0007));
		htblColNameValue.put("name", new String("Ahmed Noor" ) );
		htblColNameValue.put("gpa", new Double( 0.95 ) );
		dbApp.insertIntoTable( strTableName , htblColNameValue );
		viewTable(strTableName);
		
		htblColNameValue = new Hashtable( );
		htblColNameValue.put("gpa", new Double( 0.001) );
		dbApp.updateTable(strTableName,"1.0007" , htblColNameValue);
		viewTable(strTableName);

		
//		htblColNameValue = new Hashtable( );
//		htblColNameValue.put("name", new String("Ahmed Noor") );
//		dbApp.deleteFromTable(strTableName , htblColNameValue );
//		viewTable(strTableName);
		
	}
	static String randomStringID() {
		char c = (char) (Math.random()*26+'A');
		char c2 = (char) (Math.random()*26+'A');		
		return c+""+c2;
	}
	static void testString() throws Exception{
		String strTableName = "String";
		dbApp = new DBApp();
		dbApp.init();
		
		Hashtable htblColNameType = new Hashtable( );
		htblColNameType.put("id", "java.lang.String");
		htblColNameType.put("name", "java.lang.String");
		htblColNameType.put("gpa", "java.lang.Double");
		dbApp.createTable( strTableName, "id", htblColNameType );
		
		dbApp.createBTreeIndex(strTableName, "gpa");
		Hashtable htblColNameValue = new Hashtable( );
		htblColNameValue.put("id", new String("abc"));
		htblColNameValue.put("name", new String("Ahmed Noor" ) );
		htblColNameValue.put("gpa", new Double( 0.95 ) );
		dbApp.insertIntoTable( strTableName , htblColNameValue );
		dbApp.createBTreeIndex(strTableName, "id");
		for(int i=0;i<20;i++) {
			htblColNameValue = new Hashtable( );
			htblColNameValue.put("id", new String(randomStringID()));
			htblColNameValue.put("name", new String(name[(int) (Math.random()*4)]) );
			htblColNameValue.put("gpa", 0.95 );
			dbApp.insertIntoTable( strTableName , htblColNameValue );			
		}
		
//		htblColNameValue = new Hashtable( );
//		htblColNameValue.put("id", new String("abc"));
//		htblColNameValue.put("name", new String("Ahmed Noor" ) );
//		htblColNameValue.put("gpa", new Double( 0.95 ) );
//		dbApp.insertIntoTable( strTableName , htblColNameValue );
//		
//		htblColNameValue = new Hashtable( );
//		htblColNameValue.put("id", new String("7111"));
//		htblColNameValue.put("name", new String("Ahmed Noor" ) );
//		htblColNameValue.put("gpa", new Double( 0.95 ) );
//		dbApp.insertIntoTable( strTableName , htblColNameValue );
		viewTable(strTableName);
		
//		htblColNameValue = new Hashtable( );
//		htblColNameValue.put("gpa", 0.07 );
//		dbApp.updateTable(strTableName,"EM" , htblColNameValue);
//		
//		
//		htblColNameValue = new Hashtable( );
//		htblColNameValue.put("gpa", 0.17 );
//		dbApp.updateTable(strTableName,"KP" , htblColNameValue);
//		viewTable(strTableName);

//		htblColNameValue = new Hashtable( );
//		htblColNameValue.put("id", new String("ZV") );
//		dbApp.deleteFromTable(strTableName , htblColNameValue );
		
//		htblColNameValue = new Hashtable( );
//		htblColNameValue.put("gpa", 0.95 );
//		dbApp.deleteFromTable(strTableName , htblColNameValue );
//		viewTable(strTableName);
		
	}
	static boolean[] bool = {true,false};
	static double[] gpa = {0.95, 0.84, 0.77, 0.83};
	static void testBoolean() throws Exception{
		String strTableName = "Boolean";
		dbApp = new DBApp();
//		dbApp.init();
//		Hashtable htblColNameType = new Hashtable( );
//		htblColNameType.put("id", "java.lang.Boolean");
//		htblColNameType.put("name", "java.lang.String");
//		htblColNameType.put("gpa", "java.lang.Double");
//		dbApp.createTable( strTableName, "id", htblColNameType );
//		
//		dbApp.createBTreeIndex(strTableName, "id");
		Hashtable htblColNameValue = new Hashtable( );
//		htblColNameValue.put("id", new Boolean(true));
//		htblColNameValue.put("name", new String("Ahmed Noor" ) );
//		htblColNameValue.put("gpa", new Double( 0.95 ) );
//		dbApp.insertIntoTable( strTableName , htblColNameValue );
//		
//		htblColNameValue = new Hashtable( );
//		htblColNameValue.put("id", new Boolean(false));
//		htblColNameValue.put("name", new String("Ahmed Noor" ) );
//		htblColNameValue.put("gpa", new Double( 0.95 ) );
//		dbApp.insertIntoTable( strTableName , htblColNameValue );
//		dbApp.createBTreeIndex(strTableName, "gpa");
//		for (int i = 0; i < 20; i++) {
//			htblColNameValue = new Hashtable( );
//			htblColNameValue.put("id", bool[(int) (Math.random()*2)]);
//			htblColNameValue.put("name", name[(int) (Math.random()*4)] );
//			htblColNameValue.put("gpa", 0.001);
//			htblColNameValue.put("gpa", gpa[(int) (Math.random()*4)]);
//			dbApp.insertIntoTable( strTableName , htblColNameValue );			
//		}
//		htblColNameValue = new Hashtable( );
//		htblColNameValue.put("id", new Boolean(true));
//		htblColNameValue.put("name", new String("Ahmed Noor" ) );
//		htblColNameValue.put("gpa", new Double( 0.95 ) );
//		dbApp.insertIntoTable( strTableName , htblColNameValue );
//		
//		htblColNameValue = new Hashtable( );
//		htblColNameValue.put("id", new Boolean(false));
//		htblColNameValue.put("name", new String("Ahmed Noor" ) );
//		htblColNameValue.put("gpa", new Double( 0.95 ) );
//		dbApp.insertIntoTable( strTableName , htblColNameValue );
//		viewTable(strTableName);
//		
//		viewTable(strTableName);
//		dbApp.createBTreeIndex(strTableName, "gpa");

//		htblColNameValue = new Hashtable( );
//		htblColNameValue.put("gpa", new Double(0.001) );
//		dbApp.updateTable(strTableName,"true" , htblColNameValue);
//		viewTable(strTableName);

		
		htblColNameValue = new Hashtable( );
		htblColNameValue.put("gpa", 0.001);
		dbApp.deleteFromTable(strTableName , htblColNameValue );
		viewTable(strTableName);
		
	}
	
	static void testInteger() throws Exception{
		String strTableName = "Integer";
		dbApp = new DBApp();
//		Hashtable htblColNameType = new Hashtable( );
//		htblColNameType.put("id", "java.lang.Integer");
//		htblColNameType.put("name", "java.lang.String");
//		htblColNameType.put("gpa", "java.lang.Double");
//		dbApp.createTable( strTableName, "id", htblColNameType );
		
//		String x="";
//		Integer o=-1;
		Hashtable htblColNameValue = new Hashtable( );
		for(int i=0;i<77;i++) {
			System.out.println(i);
			Integer randomInt=(int)(Math.random()*10000);
			Double randomDouble = Math.random()*10000;
			int length=1;
			String x="";
			
			for(int c=0;c<length;c++) {
				int letter=(int)(Math.random()*26);
				x=new String((char)('a'+letter)+"");
			}
			htblColNameValue.put("id", randomInt);
			htblColNameValue.put("name", x);
			htblColNameValue.put("gpa", randomDouble );
			dbApp.insertIntoTable( strTableName , htblColNameValue );
		}
		
		
//		htblColNameValue = new Hashtable();
//		htblColNameValue.put("gpa", new Double( 0.001) );
//		dbApp.updateTable(strTableName,o+"" , htblColNameValue);

//		htblColNameValue = new Hashtable();
//		htblColNameValue.put("name", new String(x) );
//		dbApp.deleteFromTable(strTableName , htblColNameValue );
		
		
	}
	
	static void viewTableBySelect(String strTableName) throws Exception{
		SQLTerm[] arrSQLTerms;
		arrSQLTerms = new SQLTerm[1];
		arrSQLTerms[0]=new SQLTerm("Integer", "id", "<=",9989);
//		arrSQLTerms[1]=new SQLTerm("String", "id", "!=","omarsawan");
		String[]strarrOperators = new String[0];
//		strarrOperators[0] = "AND";
		// select * from Student where name = “John Noor” or gpa = 1.5;
		Iterator resultSet = dbApp.selectFromTable(arrSQLTerms , strarrOperators);
		System.out.println("---------------------------");
		while(resultSet.hasNext()) {
			System.out.println(resultSet.next());
		}
		System.out.println("---------------------------");
	}
	
	static void viewTable(String strTableName) throws Exception{
		Table table = (Table) dbApp.deserialize(strTableName);
		Vector<String> pages = table.pages;
		System.out.println("---------------------------");
		for(String str:pages) {
			Page page = (Page) dbApp.deserialize(str);
			for(Vector<Object>tuple:page.v) {
				System.out.println(tuple);
			}
		}
		System.out.println("---------------------------");
	}
	
	static void viewTableInFile(String strTableName) throws Exception{
		Table table = (Table) dbApp.deserialize(strTableName);
		Vector<String> pages = table.pages;
		
		FileWriter fileWriter;
		PrintWriter pw=null;
		try {
			fileWriter = new FileWriter("data/testFile.txt");
			pw = new PrintWriter(fileWriter);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for(String str:pages) {
			Page page = (Page) dbApp.deserialize(str);
			for(Vector<Object>tuple:page.v) {
				pw.println(tuple);
			}
		}
		
		pw.println("--------------------------------");
		pw.flush();
	}
	static void updateTest(String strTableName) throws Exception{
		Hashtable htblColNameValue = new Hashtable( );
		htblColNameValue.put("gpa", new Double( 0.001 ) );
		dbApp.updateTable(strTableName, "23498", htblColNameValue);
		htblColNameValue.clear( );
		
	}
	static void insertTest(String strTableName) throws Exception {
		Hashtable htblColNameValue = new Hashtable( );
		htblColNameValue.put("id", new Integer( 2343432 ));
		htblColNameValue.put("name", new String("Ahmed Noor" ) );
		htblColNameValue.put("gpa", new Double( 0.95 ) );
		dbApp.insertIntoTable( strTableName , htblColNameValue );
		htblColNameValue.clear( );
		htblColNameValue.put("id", new Integer( 453455 ));
		htblColNameValue.put("name", new String("Ahmed Noor" ) );
		htblColNameValue.put("gpa", new Double( 0.95 ) );
		dbApp.insertIntoTable( strTableName , htblColNameValue );
		htblColNameValue.clear( );
		htblColNameValue.put("id", new Integer( 5674567 ));
		htblColNameValue.put("name", new String("Dalia Noor" ) );
		htblColNameValue.put("gpa", new Double( 1.25 ) );
		dbApp.insertIntoTable( strTableName , htblColNameValue );
		htblColNameValue.clear( );
		htblColNameValue.put("id", new Integer( 23498 ));
		htblColNameValue.put("name", new String("John Noor" ) );
		htblColNameValue.put("gpa", new Double( 1.5 ) );
		dbApp.insertIntoTable( strTableName , htblColNameValue );
		htblColNameValue.clear( );
		htblColNameValue.put("id", new Integer( 78452 ));
		htblColNameValue.put("name", new String("Zaky Noor" ) );
		htblColNameValue.put("gpa", new Double( 0.88 ) );
		dbApp.insertIntoTable( strTableName , htblColNameValue );
	}
	static void delete(String strTableName) throws Exception {
		Hashtable htblColNameValue = new Hashtable( );
		htblColNameValue.put("name", new String("Dalia Noor") );
		dbApp.deleteFromTable(strTableName , htblColNameValue );
		htblColNameValue.clear( );
		htblColNameValue.put("name", new String("Ahmed Noor") );
		dbApp.deleteFromTable(strTableName , htblColNameValue );
		htblColNameValue.clear( );

	}
	
}
