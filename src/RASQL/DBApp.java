package RASQL;

import java.awt.Polygon;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.Iterator;
import java.util.LinkedList;



import BPTree.BPTree;
import BPTree.BPTreeLeafNode;
import recordsPointer.*;
import RTree.RTree;
import RTree.RTreeLeafNode;



public class DBApp{
	
	private static BufferedWriter bw; // For appending in the meta-data file
	private static BufferedReader br;
	private static PrintWriter out; // For overwriting in the meta-data file
	
	public static String types[] = { "java.lang.Integer", "java.lang.String",
			"java.lang.Double", "java.lang.Boolean", "java.util.Date" ,"java.awt.Polygon"};
	
	
	/**
	 * contains the code which needed to be executed upon the start up of the program
	 */
	public void init() {
		try 
		{
			File file = new File("data/metadata.csv");
			bw=new BufferedWriter(new FileWriter(file,true)); //for appending
			out = new PrintWriter(new FileWriter(file)); // for overwriting
			bw.write("Table Name,Column Name,Column Type,ClusteringKey,Indexed");
			bw.newLine();
			bw.flush();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			System.out.println("Cannot do init()");
		}
	}
	
	
	/**
	 * The following three methods are used to check the validity of the Table name when user creates a table 
	 */	
	private static boolean isLetter(char c) {
		return (c>='a'&&c<='z') || (c>='A'&&c<='Z');
	}
	private static boolean isDigit(char c) {
		return c>='0' && c<='9';
	}
	private static boolean allowed(char c) {
		return isLetter(c) || isDigit(c) || c=='_';
	}
	
	/**
	 * creating a new table in the DataBase
	 * @param strTableName: the name of the table to be created
	 * @param strClusteringKeyColumn: the column name of the clustering key 
	 * @param htblColNameType: Mapping a data type to each column
	 * @throws DBAppException 
	 */
	public void createTable(String strTableName,
			String strClusteringKeyColumn,
			Hashtable<String,String> htblColNameType )
		    throws DBAppException{
			
			if(strTableName.length()==0)
			{
				throw new DBAppException("Trying to create a table without a name");
			}
			
			char firstChar = strTableName.charAt(0);
			if(!isLetter(firstChar))
			{
				throw new DBAppException("Table name must start with a letter");
			}
			
			// the allowed characters in the table name are only
			// letters (must start with a letter) , numbers , underscore '_'
			for(int i=0;i<strTableName.length();i++)
			{
				char c = strTableName.charAt(i);
				if(!allowed(c))
					throw new DBAppException("The character "+c+" is not allowed in the Table name");
			}
			
			
			boolean alreadyExist = false;
			
			try
			{
				FileInputStream fileInput = new FileInputStream("data/"+strTableName+".class");
				alreadyExist = true;
				fileInput.close();
			}
			catch(FileNotFoundException e)
			{
				alreadyExist = false;
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		
			if(alreadyExist) //then throw DBAppExcep // Table name already exist
			{
				throw new DBAppException("Table name already exists");
			}
			
			if(!htblColNameType.containsKey(strClusteringKeyColumn))
			{
				throw new DBAppException("The specified clustering key is not a column");
			}
			
			//check correct types format
			for(Entry<String, String>e:htblColNameType.entrySet()) 
			{
				String curr = e.getValue();
				boolean contained = false;
				for(String candidateType:types)
				{
					if(candidateType.equals(curr))
					{
						contained = true;
						break;
					}
				}
				if(!contained)
				{
					throw new DBAppException("Column types have incorrect formats: "+
							curr);
				}
			}

			
			try 
			{
				File file = new File("data/metadata.csv");
				bw=new BufferedWriter(new FileWriter(file,true)); //for appending
				for(Entry<String, String>e:htblColNameType.entrySet()) 
				{
					bw.write(strTableName+','+e.getKey()+','+e.getValue()+
							','+(strClusteringKeyColumn.equals(e.getKey()))+','+false);
					bw.newLine();
				}
				Table table = new Table(strTableName);
				serialize(table,strTableName);
				bw.flush();
			} 
			catch (FileNotFoundException e) 
			{
				e.printStackTrace();
				System.out.println("metadata DOES NOT EXIST");
			}
			catch(IOException e)
			{
				e.printStackTrace();
				System.out.println("IO problem with metadata");
			}
	}
	
	
	/**
	 * To get the object value from a string (Parsing a string to object)
	 * @param keytype: the type that should the 'strClusteringKey' parsed to
	 * @param strClusteringKey: the String that should be parsed
	 * @return :the value of the parsed String
	 * @throws Exception: whenever the String cannot to be parsed to the specified type
	 */
	public static Comparable parse(String keytype,String strClusteringKey)	throws Exception{
		if(keytype.equals(types[0]))  // Integer
		{
			return Integer.parseInt(strClusteringKey);
		}
		else if(keytype.equals(types[1])) // String
		{
			return strClusteringKey;
		}
		else if(keytype.equals(types[2]))  // Double
		{
			return Double.parseDouble(strClusteringKey);
		}
		else if(keytype.equals(types[3])) // Boolean
		{
			if(strClusteringKey.equals("True") || strClusteringKey.equals("true"))return true;
			
			if(strClusteringKey.equals("False") || strClusteringKey.equals("false"))return false;
			
			throw new Exception();
		}
		else if(keytype.equals(types[4])) // Date
		{
			return parseDate(strClusteringKey);
		}
		else  // Polygon
		{
			// example of the string to be parsed ->  "(10,20),(30,30),(40,40),(50,60)"
			return parsePolygon(strClusteringKey);
		}
	}
	/**
	 *  parses the string input to date  
	 */
	private static Date parseDate(String s) throws Exception{
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Date date= format.parse(s);
		return date;
	}
	/**
	 * parses the string input to comparablePolygon (a subclass of java polygon class but implements comparable)
	 */
	private static comparablePolygon parsePolygon(String s) throws Exception{
		int cnt=0; // # of vertices
		char[]arr=s.toCharArray();
		for(char x:arr) 
		{
			if(x=='(')cnt++;
		}
		int[]x=new int[cnt],y=new int[cnt];
		int idx=0;
		StringTokenizer st = new StringTokenizer(s," (),"); // more general as the input s may contain spaces at unknown positions
		while(st.hasMoreTokens())
		{
			x[idx]=Integer.parseInt(st.nextToken());
			y[idx]=Integer.parseInt(st.nextToken());
			idx++;
		}
		return new comparablePolygon(x, y, cnt);

//_________________________________________________________________________
//		int cnt=0; // # of vertices
//		char[]arr=s.toCharArray();
//		for(char x:arr) 
//		{
//			if(x=='(')cnt++;
//		}
//		int[]x=new int[cnt],y=new int[cnt];
//		int i=0;
//		int idx=0;
//		while(i<arr.length) {
//			i++;//skip left bracket
//			StringBuilder xPoint=new StringBuilder(),yPoint=new StringBuilder();
//			while(arr[i]!=',') 
//			{
//				xPoint.append(arr[i++]);
//			}
//			i++;//skip ,
//			while(arr[i]!=')') 
//			{
//				yPoint.append(arr[i++]);
//			}
//			i+=2;//skip right bracket and ,
//			
//			x[idx]=Integer.parseInt(xPoint.toString());
//			y[idx++]=Integer.parseInt(yPoint.toString());
//		}
//		return new comparablePolygon(x, y, cnt);
	}

	/**
	 * The method logic is to take a tuple as input and insert it in the specified table
	 * @param strTableName: table name of table that tuple will be inserted in
	 * @param htblColNameValue: description for the tuple : (a value mapped to each column)
	 * @throws DBAppException
	 */
	
	public void insertIntoTable(String strTableName,
			 Hashtable<String,Object> htblColNameValue)
			 throws DBAppException {	
		
		Vector<Object>v=new Vector<Object>();
		Vector<Boolean>indexed = new Vector<Boolean>();
		Vector<String>colNames = new Vector<String>();
		Vector<String>types=new Vector<String>();
		int keyIndex=-1; 
		try
		{
			br=new BufferedReader(new FileReader("data/metadata.csv"));
		}
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		}
		try
		{
			while(br.ready()) 
			{
				String[]row = br.readLine().split(",");
				if(row[0].equals(strTableName)) 
				{
					if(row[3].equals("true") || row[3].equals("True")) // clustering key column
					{
						keyIndex = v.size();
					}
					if(row[4].equals("true") || row[4].equals("True"))
					{
						indexed.add(true);
					}
					else
					{
						indexed.add(false);
					}
					colNames.add(row[1]);
					String type=row[2];
					types.add(type);
					if(!htblColNameValue.containsKey(row[1]))
					{
						throw new DBAppException("You must insert values for all columns: "+row[1]+" Does not exist");
					}
					Object value=htblColNameValue.get(row[1]);
					if(value==null) 
					{
						throw new DBAppException("Cannot insert null value to the column "+row[1]);
					}
						
					String valueType = value.getClass().getName();
					if((valueType).equals(type)) 
					{
						if(type.equals("java.awt.Polygon")) 
						{
							Polygon p=(Polygon)value;
							comparablePolygon compP=new comparablePolygon(p.xpoints, p.ypoints, p.npoints);
							v.add(compP);
						}
						else
							v.add(value);
					}
					else 
					{
						throw new DBAppException("incompatibile types: "+
								valueType+" cannot be converted to "+
								type);
					}
					
				}
			}
		}
		catch(IOException e) 
		{
			e.printStackTrace();
		}
		LocalDateTime now = LocalDateTime.now();
		v.add(now);
		
		try
		{
			//insert into table using binary search 
			Table t = (Table)deserialize(strTableName);
			t.insert(v, keyIndex,colNames,indexed,types);
			/*
			 * The updating of references for the shifted tuples due to the insert operation
			 * and inserting the reference of the inserted tuple in the existing indices 
			 * are done inside the t.insert method
			 */
			serialize(t, strTableName);
		}
		catch(FileNotFoundException e)
		{
			throw new DBAppException("Table name is incorrect");
		}
	}
	
	// updateTable notes:
	// htblColNameValue holds the key and new value 
	// htblColNameValue will not include clustering key as column name
	// htblColNameValue entries are ANDED together 
	/**
	 * update the tuples with the given clustering key to the new values given in the hash table
	 * @param strTableName
	 * @param strClusteringKey
	 * @param htblColNameValue
	 * @throws DBAppException
	 */
	public void updateTable(String strTableName,String strClusteringKey,
			Hashtable<String,Object> htblColNameValue)  throws DBAppException{
		int keyIndex=-1;
		Vector<Boolean> indexed=new Vector<Boolean>();
		Vector<String>colNames=new Vector<String>();
		Vector<String>types=new Vector<String>();
		try
		{
			br=new BufferedReader(new FileReader("data/metadata.csv"));
		}
		catch(FileNotFoundException e)
		{
			e.printStackTrace();
		}
		try 
		{
			while(br.ready()) 
			{
				String[]row = br.readLine().split(",");
				if(row[0].equals(strTableName))
				{
					if(row[4].equals("true") || row[4].equals("True"))
					{
						indexed.add(true);
					}
					else
					{
						indexed.add(false);
					}
					if(row[3].equals("true") || row[3].equals("True"))//the clustering key
					{
						keyIndex = colNames.size();
					}
					types.add(row[2]);
					colNames.add(row[1]);
				}
			
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
		
		for(int i=0;i<colNames.size();i++) 
		{
			String currCol = colNames.get(i);
			if(htblColNameValue.containsKey(currCol))
			{
				Object newVal = htblColNameValue.get(currCol);
				String type = types.get(i);
				String newValType = newVal.getClass().getName();
				if(!newValType.equals(type))
				{
					throw new DBAppException("incompatibile types: "+
							newValType+" cannot be converted to "+
							type);
				}
			}
		}
		
		

		if(!indexed.get(keyIndex)) 
		{//no index
		 //the clustering key column does not have an index
			try
			{
				Table table = (Table)deserialize(strTableName);
				table.update(htblColNameValue, strClusteringKey, keyIndex,colNames,indexed,types);
				serialize(table, strTableName);
			}
			catch(FileNotFoundException e)
			{
				throw new DBAppException("Table name is incorrect");
			}
		}
		else 
		{
			String keyType = types.get(keyIndex);
			if(keyType.equals("java.awt.Polygon")) 
			{// 2- R tree Index created
				String keyColName = colNames.get(keyIndex);
				RTree tree=getRTree(strTableName, keyColName);
				
				comparablePolygon key=null;
				try 
				{
					key = (comparablePolygon)parse(keyType, strClusteringKey);
				} 
				catch (Exception e) 
				{
					throw new DBAppException("incompatibile types for the clustering key");
				}
				
				String overflowPage=tree.search(key);
				if(overflowPage!=null) 
				{
					try 
					{
						PageOfRefs page=(PageOfRefs)deserialize(overflowPage);
						TreeSet<String>pagesDone = new TreeSet<String>();
						page.update(strTableName,htblColNameValue,key,colNames,indexed
								,types,pagesDone);
					}
					catch (FileNotFoundException e) 
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			else 
			{// 3- B+tree Index created on clustering key
				String keyColName = colNames.get(keyIndex);
				BPTree tree=getBPTree(strTableName, keyColName);
				
				Comparable key=null;
				try 
				{
					key = parse(keyType, strClusteringKey);
				} 
				catch (Exception e) 
				{
					throw new DBAppException("incompatibile types for the clustering key");
				}
				
				String overflowPage=tree.search(key);
				if(overflowPage!=null) 
				{
					try 
					{
						PageOfRefs page=(PageOfRefs)deserialize(overflowPage);
						TreeSet<String>pagesDone = new TreeSet<String>();
						page.update(strTableName,htblColNameValue,key,colNames,indexed
								,types,pagesDone);
					}
					catch (FileNotFoundException e) 
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		
	}	
	/**
	 * this method is to get the tree index associated with a specific table and column
	 */
	public static BPTree getBPTree(String strTableName,String colName){
		String file=strTableName+"@"+colName;
		try 
		{
			return (BPTree)deserialize(file);
		} 
		catch (FileNotFoundException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	

	/**
	 * this method is to get the tree index associated with a specific table and column
	 */
	public static RTree getRTree(String strTableName,String colName){
		String file=strTableName+"@"+colName;
		try 
		{
			return (RTree)deserialize(file);
		} 
		catch (FileNotFoundException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * To delete tuples from a specific tables that have a specific values for their columns
	 * @param strTableName
	 * @param htblColNameValue
	 * @throws DBAppException
	 */
	// deleteFromTable notes:
	// htblColNameValue holds the key and value. This will be used in search
	// to identify which rows/tuples to delete.
	// htblColNameValue entries are ANDED together
	public void deleteFromTable(String strTableName,
			Hashtable<String,Object> htblColNameValue)throws DBAppException{
		
		Vector<Object>vals=new Vector<Object>();
		Vector<String> colNames = new Vector<String>();
		Vector<Boolean>indexed = new Vector<Boolean>();
		Vector<String> types = new Vector<String>();
		try
		{
			br=new BufferedReader(new FileReader("data/metadata.csv"));
		}
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		}
	try
	{
		while(br.ready()) 
		{
			String[]row = br.readLine().split(",");
			if(row[0].equals(strTableName))
			{
				if(row[4].equals("True") || row[4].equals("true"))
				{
					indexed.add(true);
				}
				else
				{
					indexed.add(false);
				}
				colNames.add(row[1]);
				types.add(row[2]);
				if(!htblColNameValue.containsKey(row[1])) 
				{
					vals.add(null);
					continue;
				}
				Object currVal=htblColNameValue.get(row[1]);
				if(!currVal.getClass().getName().equals(row[2])) 
				{
					throw new DBAppException(
							"incorrect datatype ::"+
							currVal.getClass().getName()+":: for a column of type ::"+row[2]+"::");
				}
				
				if(row[2].equals("java.awt.Polygon")) 
				{
					Polygon p=(Polygon)currVal;
					Object o=new comparablePolygon(p.xpoints, p.ypoints, p.npoints);
					vals.add(o);
				}
				else 
				{
					vals.add(currVal);
				}
			}
		}
	}
	catch(IOException e)
	{
		e.printStackTrace();
	}
	try
	{
		Table table = (Table)deserialize(strTableName);
		for(int k=table.pages.size()-1;k>=0;k--)
		{
			String curr = table.pages.get(k);
			Page currentPage = (Page)deserialize(curr);
			for(int m=currentPage.v.size()-1;m>=0;m--)
			{
				Vector<Object> tuple = currentPage.v.get(m);
				boolean match = true;
				for(int i=0;i<vals.size();i++)
				{
					
					Object val = vals.get(i);
					if(val==null)continue;
					
					
						
					if(!val.equals(tuple.get(i)))
					{
						match = false;
						break;
					}
					
				}
				if(!match)
				{
					continue;
				}
				for(int i=0;i<indexed.size();i++)
				{
					// the i-th column
					if(!indexed.get(i))
						continue;
					if(types.get(i).equals("java.awt.Polygon"))
					{
						// R tree index manipulate
						RTree treeIndex = getRTree(strTableName, colNames.get(i)); // the line that may cause the exception
						comparablePolygon key = (comparablePolygon)tuple.get(i);
						String pointer = treeIndex.search(key);
						try
						{
							PageOfRefs pageOfRefs = (PageOfRefs)deserialize(pointer);
							String pageName = curr; //table.pages.get(k);
							Ref ref = new Ref(pageName);
							if(pageOfRefs.delete(ref)) 
							{
								// no more references in the over flow pages chain,
								//so the key should be deleted		
								treeIndex.delete(key);
								serialize(treeIndex, strTableName+"@"+colNames.get(i));
							}
						}
						catch(FileNotFoundException e)
						{
							e.printStackTrace();
						}
					}
					else 
					{
						//B+tree
						BPTree treeIndex = getBPTree(strTableName, colNames.get(i)); // the line that may cause the exception
						Comparable key = (Comparable)tuple.get(i);
						String pointer = treeIndex.search(key);
						try
						{
							PageOfRefs pageOfRefs = (PageOfRefs)deserialize(pointer);
							String pageName = curr; //table.pages.get(k);
							Ref ref = new Ref(pageName);
							if(pageOfRefs.delete(ref)) 
							{
								// no more references in the over flow pages chain,
								//so the key should be deleted		
								treeIndex.delete(key);
								serialize(treeIndex, strTableName+"@"+colNames.get(i));
							}
						}
						catch(FileNotFoundException e)
						{
							e.printStackTrace();
						}
						
					}
					
					
					
				}
				table.removeTuple(k, m);
			}
		}
		serialize(table, strTableName);
	}
	catch(FileNotFoundException e)
	{
		throw new DBAppException("Table name is incorrect");
	}
}	
	
	public void createRTreeIndex(String strTableName,String strColName) throws DBAppException{
		try
		{
			FileInputStream fileInput = new FileInputStream(strTableName+"@"+strColName);
			throw new DBAppException("The column: "+strColName+" in the table: "+
									strTableName+" already indexed");
		}
		catch(FileNotFoundException e)
		{
			Table table = null;
			try
			{
				table = (Table) deserialize(strTableName);
			}
			catch(FileNotFoundException e1)
			{
				e1.printStackTrace();
				throw new DBAppException("Table name is incorrect");
			}
			
			try
			{
				br=new BufferedReader(new FileReader("data/metadata.csv"));
			}
			catch (FileNotFoundException e1) 
			{
				e.printStackTrace();
			}
			
			String type = null;
			StringBuilder sb=new StringBuilder();
			int colPos = -1;
			int counter = 0;
			try
			{
				while(br.ready()) 
				{
					String currLine = br.readLine();
					String[] row = currLine.split(",");
					if(row[0].equals(strTableName) && row[1].equals(strColName))
					{
						colPos = counter;
						type = row[2];
						sb.append(row[0]+','+row[1]+','+row[2]+','+row[3]+','+true+"\n");
					}
					else
					{
						sb.append(currLine+"\n");
					}
					if(row[0].equals(strTableName))
					{
						counter++;
					}
				}
				
			}
			catch(IOException e1)
			{
				e1.printStackTrace();
			}
			if(type == null) 
			{
				throw new DBAppException("Column name is incorrect");	
			}
			if(!type.equals("java.awt.Polygon")) {
				throw new DBAppException("Cannot create R-Tree index on column of type: "+type);
			}

			RTree tree = new RTree(strTableName, strColName);
			serialize(tree , strTableName+"@"+strColName);
			table.putOldKeysInTree(colPos,tree);
			serialize(tree , strTableName+"@"+strColName);
			
			try 
			{
				out=new PrintWriter("data/metadata.csv");
			} 
			catch (FileNotFoundException e2) 
			{
				e2.printStackTrace();
			}
			out.print(sb.toString());
			out.flush();
		}		
	}
	public void createBTreeIndex(String strTableName,
								String strColName) throws DBAppException {
		
		// The index will be stored in the hard disk in a file named (table Name +"@"+ col name)
		try
		{
			FileInputStream fileInput = new FileInputStream("data/"+strTableName+"@"+strColName+".class");
			throw new DBAppException("The column: "+strColName+" in the table: "+
									strTableName+" already indexed");
		}
		catch(FileNotFoundException e)
		{
			Table table = null;
			try
			{
				table = (Table) deserialize(strTableName);
			}
			catch(FileNotFoundException e1)
			{
				e1.printStackTrace();
				throw new DBAppException("Table name is incorrect");
			}
			
			try
			{
				br=new BufferedReader(new FileReader("data/metadata.csv"));
			}
			catch (FileNotFoundException e1) 
			{
				e.printStackTrace();
			}
			
			String type = null;
			StringBuilder sb=new StringBuilder();
			int colPos = -1;
			int counter = 0;
			try
			{
				while(br.ready()) 
				{
					String currLine = br.readLine();
					String[] row = currLine.split(",");
					if(row[0].equals(strTableName) && row[1].equals(strColName))
					{
						colPos = counter;
						type = row[2];
						sb.append(row[0]+','+row[1]+','+row[2]+','+row[3]+','+true+"\n");
					}
					else
					{
						sb.append(currLine+"\n");
					}
					if(row[0].equals(strTableName))
					{
						counter++;
					}
				}
				
			}
			catch(IOException e1)
			{
				e1.printStackTrace();
			}
			if(type == null) 
			{
				throw new DBAppException("Column name is incorrect");	
			}
			

			BPTree tree = initBPTreeOfType(type,strTableName,strColName);
			serialize(tree , strTableName+"@"+strColName);
			table.putOldKeysInTree(colPos,tree);
			serialize(tree , strTableName+"@"+strColName);
			
			try 
			{
				out=new PrintWriter("data/metadata.csv");
			} 
			catch (FileNotFoundException e2) 
			{
				e2.printStackTrace();
			}
			out.print(sb);
			out.flush();
		}
	}
	private static BPTree initBPTreeOfType(String type,String strTableName,String strColName)
			throws DBAppException{
		switch(type)
		{
		case "java.lang.Integer":return new BPTree<Integer>(strTableName,strColName);
		case "java.lang.String":return new BPTree<String>(strTableName,strColName);
		case "java.lang.Double":return new BPTree<Double>(strTableName,strColName);
		case "java.lang.Boolean":return new BPTree<Boolean>(strTableName,strColName);
		case "java.util.Date":return new BPTree<Date>(strTableName,strColName);
		default:throw new DBAppException("Cannot create B+Tree index on column of type: "+type);
		}
		
	}
	
	public static void writeProperties() throws Exception {
		Properties properties = new Properties();
		properties.setProperty("MaximumRowsCountinPage", "200");
		properties.setProperty("NodeSize", "15");
		properties.store(new FileWriter("config/DBApp.properties"),"DataBase Engine Properties");
	}
	
	
	
	public Iterator selectFromTable(SQLTerm[] arrSQLTerms,
			String[] strarrOperators)
			throws DBAppException{
		if(arrSQLTerms.length==0)throw new DBAppException("There are no sql terms to search for");
		if(strarrOperators.length!=arrSQLTerms.length-1)throw new DBAppException("Number of operators should be equal to number of SQL Terms minus 1");
		
		Iterator<Vector<Object>> Result=new LinkedList<Vector<Object>>().iterator();
		boolean[]indexed=new boolean[arrSQLTerms.length];
		boolean[]clustered=new boolean[arrSQLTerms.length];
		int[]indexOfColumnInTable=new int[arrSQLTerms.length];
		int[]typeOfColumns=new int[arrSQLTerms.length];
		int i=0;
		for(SQLTerm term: arrSQLTerms) 
		{
			int[] arr=check(term._strTableName,term._strColumnName,term._objValue);
			//arr has 4 elements the 1st is the index of the column in table,the 2nd is whether this column is indexed,
			//the 3rd is the type of index  ,  thr 4th is whether this column is clustered
			//0 -> no index , 1 -> B+ tree , 2 -> R tree
			if(arr[0]==-1) 
			{
				throw new DBAppException("Table "+term._strTableName+" doesn't have a column named "+term._strColumnName);
			}
			indexOfColumnInTable[i]=arr[0];
			indexed[i]=(arr[1]==1);
			typeOfColumns[i]=arr[2];
			clustered[i++]=(arr[3]==1);
		}
		
		for(int j=0;j<arrSQLTerms.length;j++) 
		{
			int indexType=typeOfColumns[j];
			Iterator<Vector<Object>> cur=null;
			// four cases :
			
			if(indexType==0 || arrSQLTerms[j]._strOperator.equals("!=")) 
			{
				if(!arrSQLTerms[j]._strOperator.equals("!=") && clustered[j]) {
					//1- the column is the clustering key and the operator is not '!=' and no index on the column
					cur=binarySearch(arrSQLTerms[j], indexOfColumnInTable[j]);
				}
				else {
					// 2- No Index created or the operator is '!='
					cur=searchLinearly(arrSQLTerms[j], indexOfColumnInTable[j]);					
				}
			}
			else 
			{
				if(indexType==1) 
				{// 3- B+tree Index created
					BPTree tree=getBPTree(arrSQLTerms[j]._strTableName, arrSQLTerms[j]._strColumnName);
					cur=searchBPTree(tree,(Comparable)arrSQLTerms[j]._objValue,arrSQLTerms[j]._strOperator,indexOfColumnInTable[j]);
				}
				else 
				{// 4- R tree Index created
					RTree tree=getRTree(arrSQLTerms[j]._strTableName, arrSQLTerms[j]._strColumnName);
					Polygon p=(Polygon)arrSQLTerms[j]._objValue;
					comparablePolygon compP=new comparablePolygon(p.xpoints, p.ypoints, p.npoints);
					cur=searchRTree(tree, compP, arrSQLTerms[j]._strOperator, indexOfColumnInTable[j]);
				}
			}
			
			if(j==0) 
			{
				Result=cur;
			}
			else 
			{
				Result=intersect(Result, cur, strarrOperators[j-1]);
			}
		}
		return Result;
		
	}
	static Iterator<Vector<Object>> searchBPTree(BPTree tree,Comparable val,String operator,int indexOfKey) throws DBAppException {
		
		Iterator<Vector<Object>> res=new LinkedList<Vector<Object>>().iterator();
		
		if(operator.equals("=")) 
		{
			String overFlowPageName=tree.search(val);
			if(overFlowPageName==null)return res;
			try 
			{
				PageOfRefs overFlowPage=(PageOfRefs)deserialize(overFlowPageName);
				res=overFlowPage.allTuplesExact(new TreeSet<String>(),val,indexOfKey);
			} catch (FileNotFoundException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return res;
		}
		
//		if(operator.equals("!=")) {
//			BPTreeLeafNode cur=tree.getSmallestLeafNode();
//			res=cur.allTuplesExcept(val,indexOfKey);
//			return res;
//		}
		
		if(operator.equals("<")) 
		{
			BPTreeLeafNode cur=tree.getSmallestLeafNode();
			res=cur.allTuplesSmallerThan(val,indexOfKey);
			return res;
		}
		if(operator.equals("<="))
		{
			BPTreeLeafNode cur=tree.getSmallestLeafNode();
			res=cur.allTuplesSmallerThanOrEqual(val,indexOfKey);
			return res;
		}
		if(operator.equals(">"))
		{
			BPTreeLeafNode cur=tree.searchKey(val);
			res=cur.allTuplesgreaterThan(val,indexOfKey);
			return res;
		}
		if(operator.equals(">=")) 
		{
			BPTreeLeafNode cur=tree.searchKey(val);
			res=cur.allTuplesgreaterThanOrEqual(val,indexOfKey);
			return res;
		}
		
		
		throw new DBAppException("Invalid operator");
	}
	
	static Iterator<Vector<Object>> searchRTree(RTree tree,comparablePolygon val,String operator,int indexOfKey) throws DBAppException {
		
		Iterator<Vector<Object>> res=new LinkedList<Vector<Object>>().iterator();
		
		if(operator.equals("=")) 
		{
			String overFlowPageName=tree.search(val);
			if(overFlowPageName==null)return res;
			try 
			{
				PageOfRefs overFlowPage=(PageOfRefs)deserialize(overFlowPageName);
				res=overFlowPage.allTuplesExact(new TreeSet<String>(),val,indexOfKey);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return res;
		}
		
//		if(operator.equals("!=")) {
//			RTreeLeafNode cur=tree.getSmallestLeafNode();
//			res=cur.allTuplesExcept(val,indexOfKey);
//			return res;
//		}
		
		if(operator.equals("<")) 
		{
			RTreeLeafNode cur=tree.getSmallestLeafNode();
			res=cur.allTuplesSmallerThan(val,indexOfKey);
			return res;
		}
		if(operator.equals("<=")) 
		{
			RTreeLeafNode cur=tree.getSmallestLeafNode();
			res=cur.allTuplesSmallerThanOrEqual(val,indexOfKey);
			return res;
		}
		if(operator.equals(">")) 
		{
			RTreeLeafNode cur=tree.searchKey(val);
			res=cur.allTuplesgreaterThan(val,indexOfKey);
			return res;
		}
		if(operator.equals(">=")) 
		{
			RTreeLeafNode cur=tree.searchKey(val);
			res=cur.allTuplesgreaterThanOrEqual(val,indexOfKey);
			return res;
		}
		
		
		throw new DBAppException("Invalid operator");
	}
	
	
	private static Iterator<Vector<Object>> intersect(Iterator<Vector<Object>> i1,Iterator<Vector<Object>> i2,String o) throws DBAppException {
		if(o.equals("AND")) 
		{
			return and(i1, i2);
		}
		
		if(o.equals("OR")) 
		{
			return or(i1, i2);
		}
		
		if(o.equals("XOR")) 
		{
			return xor(i1, i2);
		}
		throw new DBAppException("Invalid array operator");
	}
	
	private static Iterator<Vector<Object>> and(Iterator<Vector<Object>> i1,Iterator<Vector<Object>> i2) {//all tuples in i1 and i2 both
		
		LinkedList<Vector<Object>>res=new LinkedList<Vector<Object>>();
		
		while (i1.hasNext())
		{
	        Vector<Object> t1 =(Vector<Object>) i1.next();
	    
	        boolean contain=false;
	        LinkedList<Vector<Object>> temp=new LinkedList<>();
	        while(i2.hasNext()) 
	        {
	        	Vector<Object> t2=(Vector<Object>)i2.next();
	        	temp.add(t2);
	        	if(equal(t1,t2)) 
	        	{
	        		contain=true;
	        	}
	        }
	        if(contain) 
	        {
	        	res.add(t1);
	        }
	        i2 = temp.iterator();
	    }
		return res.iterator();
	}
	
	private static Iterator<Vector<Object>> or(Iterator<Vector<Object>> i1,Iterator<Vector<Object>> i2) {//all tuples in i1 or i2
		LinkedList<Vector<Object>>res=new LinkedList<>();
		
		LinkedList<Vector<Object>> temp = new LinkedList<Vector<Object>>();
		while (i2.hasNext())
		{
	        Vector<Object> t2 =(Vector<Object>) i2.next();
	        temp.add(t2);
	        res.add(t2);
		}
		i2 = temp.iterator();
		while (i1.hasNext())
		{
	        Vector<Object> t1 =(Vector<Object>) i1.next();
	        boolean contain=false;
	        temp = new LinkedList<Vector<Object>>();
	        while(i2.hasNext()) 
	        {
	        	Vector<Object> t2=(Vector<Object>)i2.next();
	        	temp.add(t2);
	        	if(equal(t1,t2)) 
	        	{
	        		contain=true;
	        	}
	        }
	        if(!contain) 
	        {
	        	res.add(t1);
	        }
	        i2 = temp.iterator();
	    }
		return res.iterator();
	}
	
	private static Iterator<Vector<Object>> xor(Iterator<Vector<Object>> i1,Iterator<Vector<Object>> i2) {//all elements in i1 and not i2 , or in i2 and not i1
		LinkedList<Vector<Object>>res=new LinkedList<>();
		
		LinkedList<Vector<Object>> tmp1 = new LinkedList<>();
		LinkedList<Vector<Object>> tmp2;
		while (i1.hasNext())
		{
	        Vector<Object> t1 =(Vector<Object>) i1.next();
	        tmp1.add(t1);
	        boolean contain=false;
	        tmp2 = new LinkedList<>();
	        while(i2.hasNext()) 
	        {
	        	Vector<Object> t2=(Vector<Object>)i2.next();
	        	tmp2.add(t2);
	        	if(equal(t1,t2)) 
	        	{
	        		contain=true;
	        	}
	        }
	        if(!contain) 
	        {
	        	res.add(t1);
	        }
	        i2 = tmp2.iterator();
	    }
		i1 = tmp1.iterator();
		tmp2 = new LinkedList<>();
		while (i2.hasNext())
		{
	        Vector<Object> t2 =(Vector<Object>) i2.next();
	        tmp2.add(t2);
	        boolean contain=false;
	        tmp1 = new LinkedList<>();
	        while(i1.hasNext()) 
	        {
	        	Vector<Object> t1=(Vector<Object>)i1.next();
	        	tmp1.add(t1);
	        	if(equal(t1,t2)) 
	        	{
	        		contain=true;
	        	}
	        }
	        if(!contain) 
	        {
	        	res.add(t2);
	        }
	        i1 = tmp1.iterator();
	    }
		
		return res.iterator();
	}
	
	static boolean equal(Vector<Object> v1,Vector<Object> v2) {
		for(int i=0;i<v1.size();i++) 
		{
			Comparable c1=(Comparable)v1.get(i);
			Comparable c2=(Comparable)v2.get(i);
			if(!c1.equals(c2))return false;
		}
		return true;
	}
	static Iterator<Vector<Object>> binarySearch(SQLTerm term,int indexOfColumn) throws DBAppException{
		Table curTable=null;
		try 
		{
			curTable=(Table)deserialize(term._strTableName);
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		}
		
		Object obj=term._objValue;
		Comparable key;
		if(obj.getClass().getName().equals("java.awt.Polygon")) 
		{
			Polygon p=(Polygon)obj;
			comparablePolygon compP=new comparablePolygon(p.xpoints, p.ypoints, p.npoints);
			key=compP;
		}
		else 
		{
			key=(Comparable)obj;
		}
		return compareBinarySearch(key, term._strOperator, curTable,indexOfColumn);
	}
	static Iterator<Vector<Object>> compareBinarySearch(Comparable key,String operator,Table table,int indexOfColumn) throws DBAppException {
		if(operator.equals(">")) {
			return searchgreater(key, indexOfColumn, table);
		}
		if(operator.equals(">=")) {
			return searchgreaterOrEqual(key, indexOfColumn, table);
		}
		if(operator.equals("<")) {
			return searchSmaller(key, indexOfColumn, table);
		}
		if(operator.equals("<=")) {
			return searchSmallerOrEqual(key, indexOfColumn, table);
		}
		if(operator.equals("=")) {
			return searchEqual(key, indexOfColumn, table);
		}
		throw new DBAppException("Invalid operator");
	}
	
	private static Iterator<Vector<Object>> searchEqual(Comparable key,int indexOfColumn,Table curTable) throws DBAppException {
		LinkedList<Vector<Object>>list=new LinkedList<>();
		int indexOfStartPage=curTable.search(key, indexOfColumn);
		
		int indexOfStartTuple=0;
		if(curTable.pages.size()==0)return list.iterator();
		int lo=0,hi=curTable.pages.size();
		
		Page curPage=null;
		try 
		{
			curPage=(Page)deserialize(curTable.pages.get(indexOfStartPage));
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		}
		
		while(lo<=hi) {
			int mid=(lo+hi)>>1;
			Comparable cur=(Comparable)curPage.v.get(mid).get(indexOfColumn);
			if(cur.compareTo(key)>=0) {
				indexOfStartTuple=mid;
				hi=mid-1;
			}
			else {
				lo=mid+1;
			}
		}
		
		for(int pageIdx=indexOfStartPage;pageIdx<curTable.pages.size();pageIdx++) {
			String p=curTable.pages.get(pageIdx);
			
			if(pageIdx!=indexOfStartPage) {
				curPage=null;
				try 
				{
					curPage=(Page)deserialize(p);
				} 
				catch (FileNotFoundException e) 
				{
					e.printStackTrace();
				}
				indexOfStartTuple=0;
			}
			
			
			
			boolean end=false;
			for(int idx=indexOfStartTuple;idx<curPage.v.size();idx++) 
			{
				Vector<Object> tuple=curPage.v.get(idx);
				Comparable curKey=(Comparable)tuple.get(indexOfColumn);
				int compare=curKey.compareTo(key);
				if(compare<0) 
				{
					continue;
				}
				if(compare>0) {
					end=true;
					break;
				}
				list.add(tuple);
				
			}
			if(end)break;
		}
		
		return list.iterator();
	}
	
	private static Iterator<Vector<Object>> searchSmallerOrEqual(Comparable key,int indexOfColumn,Table curTable) throws DBAppException {
		LinkedList<Vector<Object>>list=new LinkedList<>();
		int indexOfStartPage=0;
		
		for(int pageIdx=indexOfStartPage;pageIdx<curTable.pages.size();pageIdx++) {
			String p=curTable.pages.get(pageIdx);
			Page curPage=null;
			try 
			{
				curPage=(Page)deserialize(p);
			} 
			catch (FileNotFoundException e) 
			{
				e.printStackTrace();
			}
			boolean end=false;
			for(Vector<Object> tuple:curPage.v) 
			{
				Comparable curKey=(Comparable)tuple.get(indexOfColumn);
				int compare=curKey.compareTo(key);
				
				if(compare>0) {
					end=true;
					break;
				}
				list.add(tuple);
				
			}
			if(end)break;
		}
		
		return list.iterator();
	}
	
	private static Iterator<Vector<Object>> searchSmaller(Comparable key,int indexOfColumn,Table curTable) throws DBAppException {
		LinkedList<Vector<Object>>list=new LinkedList<>();
		int indexOfStartPage=0;
		
		for(int pageIdx=indexOfStartPage;pageIdx<curTable.pages.size();pageIdx++) {
			String p=curTable.pages.get(pageIdx);
			Page curPage=null;
			try 
			{
				curPage=(Page)deserialize(p);
			} 
			catch (FileNotFoundException e) 
			{
				e.printStackTrace();
			}
			boolean end=false;
			for(Vector<Object> tuple:curPage.v) 
			{
				Comparable curKey=(Comparable)tuple.get(indexOfColumn);
				int compare=curKey.compareTo(key);
				
				if(compare>=0) {
					end=true;
					break;
				}
				list.add(tuple);
				
			}
			if(end)break;
		}
		
		return list.iterator();
	}
	
	private static Iterator<Vector<Object>> searchgreaterOrEqual(Comparable key,int indexOfColumn,Table curTable) throws DBAppException {
		LinkedList<Vector<Object>>list=new LinkedList<>();
		int indexOfStartPage=curTable.search(key, indexOfColumn);
		
		int indexOfStartTuple=0;
		if(curTable.pages.size()==0)return list.iterator();
		
		int lo=0,hi=curTable.pages.size();
		
		Page curPage=null;
		try 
		{
			curPage=(Page)deserialize(curTable.pages.get(indexOfStartPage));
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		}
		
		while(lo<=hi) {
			int mid=(lo+hi)>>1;
			Comparable cur=(Comparable)curPage.v.get(mid).get(indexOfColumn);
			if(cur.compareTo(key)>=0) {
				indexOfStartTuple=mid;
				hi=mid-1;
			}
			else {
				lo=mid+1;
			}
		}
		
		for(int pageIdx=indexOfStartPage;pageIdx<curTable.pages.size();pageIdx++) {
			String p=curTable.pages.get(pageIdx);
			
			if(pageIdx!=indexOfStartPage) {
				curPage=null;
				try 
				{
					curPage=(Page)deserialize(p);
				} 
				catch (FileNotFoundException e) 
				{
					e.printStackTrace();
				}
				indexOfStartTuple=0;
			}
			
			for(int idx=indexOfStartTuple;idx<curPage.v.size();idx++) 
			{
				Vector<Object> tuple=curPage.v.get(idx);
				Comparable curKey=(Comparable)tuple.get(indexOfColumn);
				int compare=curKey.compareTo(key);
				if(compare<0) {
					continue;
				}
				list.add(tuple);
				
			}
		}
		
		return list.iterator();
	}
	
	private static Iterator<Vector<Object>> searchgreater(Comparable key,int indexOfColumn,Table curTable) throws DBAppException {
		LinkedList<Vector<Object>>list=new LinkedList<>();
		int indexOfStartPage=curTable.search(key, indexOfColumn);
		
		int indexOfStartTuple=0;
		if(curTable.pages.size()==0)return list.iterator();
		int lo=0,hi=curTable.pages.size();
		
		Page curPage=null;
		try 
		{
			curPage=(Page)deserialize(curTable.pages.get(indexOfStartPage));
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		}
		
		while(lo<=hi) {
			int mid=(lo+hi)>>1;
			Comparable cur=(Comparable)curPage.v.get(mid).get(indexOfColumn);
			if(cur.compareTo(key)>=0) {
				indexOfStartTuple=mid;
				hi=mid-1;
			}
			else {
				lo=mid+1;
			}
		}
		
		for(int pageIdx=indexOfStartPage;pageIdx<curTable.pages.size();pageIdx++) {
			String p=curTable.pages.get(pageIdx);
			
			if(pageIdx!=indexOfStartPage) {
				curPage=null;
				try 
				{
					curPage=(Page)deserialize(p);
				} 
				catch (FileNotFoundException e) 
				{
					e.printStackTrace();
				}
				indexOfStartTuple=0;
			}
			
			
			
			boolean end=false;
			for(int idx=indexOfStartTuple;idx<curPage.v.size();idx++) 
			{
				Vector<Object> tuple=curPage.v.get(idx);
				Comparable curKey=(Comparable)tuple.get(indexOfColumn);
				int compare=curKey.compareTo(key);
				
				if(compare<=0) {
					continue;
				}
				list.add(tuple);
				
			}
		}
		
		return list.iterator();
	}
	
	private static Iterator<Vector<Object>> searchLinearly(SQLTerm term,int indexOfColumn) throws DBAppException {
		LinkedList<Vector<Object>>list=new LinkedList<>();
		Table curTable=null;
		try 
		{
			curTable=(Table)deserialize(term._strTableName);
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		}
		
		Object obj=term._objValue;
		Comparable key;
		if(obj.getClass().getName().equals("java.awt.Polygon")) 
		{
			Polygon p=(Polygon)obj;
			comparablePolygon compP=new comparablePolygon(p.xpoints, p.ypoints, p.npoints);
			key=compP;
		}
		else 
		{
			key=(Comparable)obj;
		}
		
		for(String p:curTable.pages) 
		{
			Page curPage=null;
			try 
			{
				curPage=(Page)deserialize(p);
			} 
			catch (FileNotFoundException e) 
			{
				e.printStackTrace();
			}
			for(Vector<Object> tuple:curPage.v) 
			{
				if(compare((Comparable)tuple.get(indexOfColumn),key,term._strOperator)) 
				{
					list.add(tuple);
				}
			}
			
		}
		return list.iterator();
	}
	private static boolean compare(Comparable op1,Comparable op2,String operator) throws DBAppException {
		Comparable value1=op1,value2=op2;
		
		if(operator.equals(">")) {
			return value1.compareTo(value2)>0;
		}
		if(operator.equals(">=")) {
			return value1.compareTo(value2)>=0;
		}
		if(operator.equals("<")) {
			return value1.compareTo(value2)<0;
		}
		if(operator.equals("<=")) {
			return value1.compareTo(value2)<=0;
		}
		if(operator.equals("!=")) {
			return !value1.equals(value2);
		}
		if(operator.equals("=")) {
			
			return value1.equals(value2);
		}
		throw new DBAppException("Invalid operator");
	}
	
	/**
	 * 
	 * @param tableName
	 * @param columnName
	 * @param value
	 * @return
	 * @throws DBAppException
	 */
	static int[] check(String tableName,String columnName,Object value) throws DBAppException {
		int idx=0;
		
		try
		{
			br=new BufferedReader(new FileReader("data/metadata.csv"));
		}
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		}
		try
		{
			while(br.ready()) 
			{
				String[]row = br.readLine().split(",");
				if(row[0].equals(tableName)) 
				{
					if(row[1].equals(columnName)) 
					{
						if(!value.getClass().getName().equals(row[2])) 
						{
							throw new DBAppException(
									"incorrect datatype in column: "+columnName+" in table: "+tableName);
						}
						int indexed=(row[4].equals("false") || row[4].equals("False"))?0:1;
						int type=0;
						if(indexed==1) {
							if(row[2].equals("java.awt.Polygon")) 
							{
								type=2;
							}
							else 
							{
								type=1;
							}
						}
						int clustered=(row[3].equals("false") || row[3].equals("False"))?0:1;
						return new int[] {idx,indexed,type,clustered};
					}
					idx++;
				}
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		return new int[] {-1,-1,-1,-1};
	}
	
	
	
	
	// Serialization and De-serialization for Hard Disk access
	/**
	 * 
	 * @param ob: the object to be serialized (encoded) and saved in a file on the hard disk
	 * @param filename: the name of the file carrying the serialized object
	 */
	
	public static void serialize(Object ob,String filename){
		try
		{
			FileOutputStream fileOutput = new FileOutputStream("data/"+filename+".class");
			ObjectOutputStream out = new ObjectOutputStream(fileOutput);
			out.writeObject(ob);
			out.close();
			fileOutput.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param filename: the name of the file carrying the object to be retrieved
	 * @return: the object after de-serializing (decoded) it
	 * @throws FileNotFoundExceptio: if the file name Does not exist 
	 */
	
	public static Object deserialize(String filename)throws FileNotFoundException {
														// (one / or // ?) 
		FileInputStream fileInput = new FileInputStream("data/"+filename+".class");
		try
		{
			ObjectInputStream in = new ObjectInputStream(fileInput);
			Object ob = in.readObject();
			in.close();
			fileInput.close();
			return ob;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public static void deleteFile(String filename) {
		File file = new File("data/"+filename+".class");
		file.delete();
	}
	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		writeProperties();
	}
}
