package RASQL;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;


import BPTree.BPTree;
import recordsPointer.PageOfRefs;
import recordsPointer.Ref;
import RTree.RTree;

public class Page implements java.io.Serializable{
	
	String pageName;
	int clusteringKeyIndex;
	public Vector<Vector<Object>> v;
	public static int N=getMaxRowsCountinPage();
	
	public Page(String pageName,int clusteringKeyIndex) {
		this.clusteringKeyIndex = clusteringKeyIndex;
		this.pageName=pageName;
		v = new Vector<Vector<Object>>();
	}
	/**
	 * 
	 * @return Maximum number of records/objects that can be put the page
	 */

	public static int getMaxRowsCountinPage(){
		try 
		{
			FileReader reader = new FileReader("config/DBApp.properties");
			Properties properties = new Properties();
			properties.load(reader);
			return Integer.parseInt(properties.getProperty("MaximumRowsCountinPage"));			
		}
		catch(Exception e) 
		{
			e.printStackTrace();
		}
		return 200;
	}
	
	/**
	 * The method insert the given tuple in its correct position based on the clustering key
	 * @param tuple: to be inserted
	 * @return: if the page is full returns the largest tuple in the page to be inserted in the next page
	 */
	public Vector<Object> insertSorted(Vector<Object> tuple){
		Comparable mykeyValue = (Comparable) tuple.get(clusteringKeyIndex);
		int lw = 0,hi = v.size()-1,ans = v.size();
		while(lw<=hi) 
		{
			int mid  = (lw+hi)/2;
			Vector<Object> currentTuple = v.get(mid);
			Comparable currentKey = (Comparable) currentTuple.get(clusteringKeyIndex);
			int x = mykeyValue.compareTo(currentKey);
			if(x<0) 
			{
				ans = mid;
				hi = mid-1;
			}
			else 
			{
				lw = mid+1;
			}
		}
		v.add(ans, tuple);

		if(v.size()>N)
		{
			Vector<Object> last = v.remove(v.size()-1);
			return last;
		}
		return null;
	}
	
	
	
	/**
	 * The method updates the tuples in this page that match with the given clustering key
	 * @param strTableName:name of table to which the page belongs
	 * @param htblColNameValue:the new values for the column to be updated
	 * @param ClusteringKey:the given clustering key
	 * @param colNames:specifies the name of each column in table
	 * @param indexed:specifies whether the i-th column is indexed or not
	 * @param types:specifies the data type of each column
	 * @return: can be more tuples that match with the clustering key in the following page or not
	 * @throws DBAppException
	 */
	public boolean update(String strTableName,Hashtable<String, Object> htblColNameValue,
			Comparable ClusteringKey,Vector<String>colNames,
			Vector<Boolean>indexed,Vector<String>types) throws DBAppException {
		int low =0,hi = v.size()-1,ans=-1;
		while(low<=hi)
		{
			int mid = (low+hi)/2;
			Vector<Object> curr = v.get(mid);
			Comparable currKey = (Comparable) curr.get(clusteringKeyIndex);
			int x = currKey.compareTo(ClusteringKey);
			if(x < 0)
			{
				low = mid+1;
			}
			else
			{
				if(x == 0)
					ans = mid;
				hi = mid-1;
			}
		}
		

		if(ans == -1)//no tuple in this page with such clustering key
		{
			//so if last tuple key greater then cannot be more
			// if the last tuple key smaller then can be more
			Vector<Object> lastTuple = v.get(v.size()-1);
			Comparable lastTupleKey = (Comparable)lastTuple.get(clusteringKeyIndex);
			return lastTupleKey.compareTo(ClusteringKey) < 0;
		}
		
		
		boolean isPolygon = types.get(clusteringKeyIndex).equals("java.awt.Polygon");
		for(int i = ans;i<v.size();i++) 
		{
			Vector<Object> curr = v.get(i);
			Comparable currKey = (Comparable) curr.get(clusteringKeyIndex);
			int x = currKey.compareTo(ClusteringKey);
			if(x>0) return false;
			
			if(isPolygon)
			{
				// if clustering key is polygon
				// we must check that it is also .equals
				// as .compareTo returns 0 when the areas of the two compared polygons are equal
				// but also we want to check that the two polygons have the same vertices and this is done through .equals()
				if(!currKey.equals(ClusteringKey))
				{
					continue;
				}
			}
			

			//update the tuple and the corresponding indices
			for(int j=0;j<colNames.size();j++) 
			{
				if(htblColNameValue.containsKey(colNames.get(j)))
				{
					Object newVal = htblColNameValue.get(colNames.get(j));
					if(indexed.get(j))
					{
						if(types.get(j).equals("java.awt.Polygon"))
						{
							//RTree
							//Delete the old value
							RTree treeIndex = DBApp.getRTree(strTableName, colNames.get(j));
							Ref ref = new Ref(this.pageName);
							comparablePolygon key = (comparablePolygon)curr.get(j);
							String pointer = treeIndex.search(key);
							try
							{
								PageOfRefs pageOfRefs = (PageOfRefs)DBApp.deserialize(pointer);
								if(pageOfRefs.delete(ref)) 
								{
									// no more references in the over flow pages chain,
									//so the key should be deleted		
									treeIndex.delete(key);
								}
							}
							catch(FileNotFoundException e)
							{
								e.printStackTrace();
							}
							
							//insert the new value
							key = (comparablePolygon)newVal;
							treeIndex.insert(key, ref);
							DBApp.serialize(treeIndex, strTableName+"@"+colNames.get(j));
						}
						else
						{
							//Delete the old value
							BPTree treeIndex = DBApp.getBPTree(strTableName, colNames.get(j));
							Ref ref = new Ref(this.pageName);
							Comparable key = (Comparable)curr.get(j);
							String pointer = treeIndex.search(key);
							try
							{
								PageOfRefs pageOfRefs = (PageOfRefs)DBApp.deserialize(pointer);
								if(pageOfRefs.delete(ref)) 
								{
									// no more references in the over flow pages chain,
									//so the key should be deleted		
									treeIndex.delete(key);
								}
							}
							catch(FileNotFoundException e)
							{
								e.printStackTrace();
							}
							
							//insert the new value
							key = (Comparable)newVal;
							treeIndex.insert(key, ref);
							DBApp.serialize(treeIndex, strTableName+"@"+colNames.get(j));
						}
					}
					
						curr.set(j, newVal);
						curr.set(colNames.size(), LocalDateTime.now());
				}
			}
		}
		return true;
	}
	
//	public Vector<Object> removeTupleAt(int index) {
//		// not last
//		if(v.size() == N)
//		{
//			return v.remove(index);
//		}
//		return null;
//	}
	

	/**
	 * specify whether the page is full or not
	 */
	public boolean isFull() {
		return v.size() == N;
	}
	
	/**
	 * The methods takes a newly created BP/R Tree on a specific column and inserts the values corresponding to this co,umn in the tree 
	 * @param colPos: the index of the column
	 * @param tree:the given BP/R Tree tree
	 */
	public void putOldKeysInTree(int colPos,BPTree tree) {
		for(Vector<Object> currTuple: v)
		{
			Comparable key = (Comparable)currTuple.get(colPos);
			Ref ref = new Ref(this.pageName);
			tree.insert(key, ref);
		}
	}
	public void putOldKeysInTree(int colPos,RTree tree) {
		for(Vector<Object> currTuple: v)
		{
			comparablePolygon key = (comparablePolygon) currTuple.get(colPos);
			Ref ref = new Ref(this.pageName);
			tree.insert(key, ref);
		}
	}

}
