package RASQL;


import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Vector;


import BPTree.BPTree;
import recordsPointer.PageOfRefs;
import recordsPointer.Ref;
import RTree.RTree;


public class Table implements Serializable{
	
	private static final long serialVersionUID = 1L;
	Vector<String>pages;
	String name;
	int pageNumber;
	
	public Table(String strTableName){
		name=strTableName;
		pages=new Vector<String>();
	}
	
	public int search(Comparable tupleKey,int keyIndex) {
		int lo =0,hi = pages.size()-1,ans=0;
		while(lo<=hi)
		{
			int mid = (lo+hi)>>1;
			String curr = pages.get(mid);
			try
			{
				Page currentPage = (Page)DBApp.deserialize(curr);
				Vector<Object> startTuple = currentPage.v.get(0);
				Comparable startTupleKey = (Comparable) startTuple.get(keyIndex);
				if(startTupleKey.compareTo(tupleKey) < 0)
				{
					ans = mid;
					lo = mid+1;
				}
				else
				{
					hi = mid-1;
				}
			}
			catch (FileNotFoundException e) 
			{
				e.printStackTrace();
			}
		}
		return ans;
	}
	
	/**
	 * The method insert a given tuple in the table
	 * @param tuple: to be inserted
	 * @param keyIndex: determines the position of the clustering key column in the table
	 * @param colNames: the names of the table columns
	 * @param indexed: specifies whether there is an index on each column or not
	 * @param types: specifies the data types for each column
	 */
	public void insert(Vector<Object> tuple,int keyIndex,
						Vector<String>colNames,Vector<Boolean>indexed,Vector<String>types) {
		
		Vector<Object> v = tuple;
		//search binary
		Comparable tupleKey = (Comparable) tuple.get(keyIndex);
		int low =0,hi = pages.size()-1,ans=0;
		while(low<=hi)
		{
			int mid = (low+hi)>>1;
			String curr = pages.get(mid);
			try
			{
				Page currentPage = (Page)DBApp.deserialize(curr);
				Vector<Object> startTuple = currentPage.v.get(0);
				Comparable startTupleKey = (Comparable) startTuple.get(keyIndex);
				if(startTupleKey.compareTo(tupleKey) <= 0)
				{
					ans = mid;
					low = mid+1;
				}
				else
				{
					hi = mid-1;
				}
			}
			catch (FileNotFoundException e) 
			{
				e.printStackTrace();
			}
		}
		
		try
		{

			if(ans<pages.size()) 
			{
				Page candidatePage = (Page)DBApp.deserialize(pages.get(ans));
				Vector<Object> lastTuple = candidatePage.v.get(candidatePage.v.size()-1);
				Comparable lastTupleKey = (Comparable)lastTuple.get(keyIndex);
				if(candidatePage.isFull() && lastTupleKey.compareTo(tupleKey) <= 0)
				{
					ans++;
				}
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		int idx=ans;
		//insert
		while(idx < pages.size())
		{
			String curr = pages.get(idx);
			String old = idx == ans? null:pages.get(idx-1);
			
			// curr is now the page name of v
			// old is the old page name of v
			for(int i=0;i<indexed.size();i++)
			{
				if(indexed.get(i))
				{
					if(types.get(i).equals("java.awt.Polygon")) 
					{
						RTree tree = DBApp.getRTree(name, colNames.get(i));
						comparablePolygon key = (comparablePolygon) v.get(i);
						Ref newRecordReference = new Ref(curr);
						tree.insert(key, newRecordReference);
						if(old!=null)
						{
							Ref oldRecordReference = new Ref(old);
							String pointer = tree.search(key);
							try
							{
								PageOfRefs overFlowPage = (PageOfRefs)DBApp.deserialize(pointer);
								overFlowPage.delete(oldRecordReference);
							}
							catch(FileNotFoundException e)
							{
								e.printStackTrace();
							}
						}
						DBApp.serialize(tree, name+"@"+colNames.get(i));						
					}
					else 
					{
						BPTree tree = DBApp.getBPTree(name, colNames.get(i));
						Comparable key = (Comparable)v.get(i);
						Ref newRecordReference = new Ref(curr);
						tree.insert(key, newRecordReference);
						if(old!=null)
						{
							Ref oldRecordReference = new Ref(old);
							String pointer = tree.search(key);
							try
							{
								PageOfRefs overFlowPage = (PageOfRefs)DBApp.deserialize(pointer);
								overFlowPage.delete(oldRecordReference);
							}
							catch(FileNotFoundException e)
							{
								e.printStackTrace();
							}
						}
						DBApp.serialize(tree, name+"@"+colNames.get(i));						
					}
				}
			}

			try
			{
				Page currentPage = (Page)DBApp.deserialize(curr);
				v = currentPage.insertSorted(v);
				DBApp.serialize(currentPage,curr);
				if(v==null)
					return;
				idx++;
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
		}
		if(v==null)
			return;
		
		String pageName = pageNumber+""+name;
		pageNumber++;
		Page newPage = new Page(pageName,keyIndex);
		pages.add(pageName);
		try 
		{
			newPage.insertSorted(v);
			DBApp.serialize(newPage,pageName);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		String curr = pages.get(idx);
		String old = idx == ans? null:pages.get(idx-1);
		
		// curr is now the page name of v
		// old is the old page name of v
		for(int i=0;i<indexed.size();i++)
		{
			if(indexed.get(i))
			{
				if(types.get(i).equals("java.awt.Polygon")) 
				{
					RTree tree = DBApp.getRTree(name, colNames.get(i));
					comparablePolygon key = (comparablePolygon) v.get(i);
					Ref newRecordReference = new Ref(curr);
					tree.insert(key, newRecordReference);
					if(old!=null)
					{
						Ref oldRecordReference = new Ref(old);
						String pointer = tree.search(key);
						try
						{
							PageOfRefs overFlowPage = (PageOfRefs)DBApp.deserialize(pointer);
							overFlowPage.delete(oldRecordReference);
						}
						catch(FileNotFoundException e)
						{
							e.printStackTrace();
						}
					}
					DBApp.serialize(tree, name+"@"+colNames.get(i));						
				}
				else 
				{
					BPTree tree = DBApp.getBPTree(name, colNames.get(i));
					Comparable key = (Comparable)v.get(i);
					Ref newRecordReference = new Ref(curr);
					tree.insert(key, newRecordReference);
					if(old!=null)
					{
						Ref oldRecordReference = new Ref(old);
						String pointer = tree.search(key);
						try
						{
							PageOfRefs overFlowPage = (PageOfRefs)DBApp.deserialize(pointer);
							overFlowPage.delete(oldRecordReference);
						}
						catch(FileNotFoundException e)
						{
							e.printStackTrace();
						}
					}
					DBApp.serialize(tree, name+"@"+colNames.get(i));						
				}
			}
		}		
	}

	/**
	 * The method updates all the tuples with the given clustering key by the new values given in the hash table
	 * @param htblColNameValue
	 * @param strClusteringKey
	 * @param keyIndex
	 * @param colNames
	 * @param indexed
	 * @param types
	 * @throws DBAppException
	 */

	public void update(Hashtable<String, Object> htblColNameValue,String strClusteringKey,int keyIndex
						,Vector<String> colNames,Vector<Boolean>indexed,Vector<String> types) throws DBAppException{
		
	
		//search
		String keyType = types.get(keyIndex);
		Comparable tupleKey;
		try 
		{
			tupleKey = DBApp.parse(keyType, strClusteringKey);
		} 
		catch (Exception e1) 
		{
			throw new DBAppException("incompatible types for clustering key");
		}
		int low=0,hi=pages.size()-1,ans=0;
		//update using binary search for the start page
		while(low<=hi)
		{
			int mid=(low+hi)/2;
			String curr = pages.get(mid);
			Page currentPage =null;
			
			try 
			{
				currentPage = (Page)DBApp.deserialize(curr);
			} 
			catch (FileNotFoundException e) 
			{
				e.printStackTrace();
				return;
			}
			Vector<Object> startTuple = currentPage.v.get(0);
			Comparable startTupleKey = (Comparable)startTuple.get(keyIndex);
			if(startTupleKey.compareTo(tupleKey) < 0)
			{
				ans = mid;
				low=mid+1;
			}
			else
			{
				hi=mid-1;
			}
			
		}
		


		
		int idx=ans;
		while(idx < pages.size())
		{
			String curr = pages.get(idx);
			
				Page currentPage =null;
				
				try 
				{
					currentPage = (Page)DBApp.deserialize(curr);
				} 
				catch (FileNotFoundException e) 
				{
					e.printStackTrace();
					return;
				}
				
				boolean canBeMore=currentPage.update(name, htblColNameValue, tupleKey,colNames,indexed,types);
				DBApp.serialize(currentPage,curr);
				if(!canBeMore)
					return;
				idx++;
			
		}
		
	}
	
	/**
	 * The methods delete the tuple in the given page 
	 * @param pageIdx:the index of the page containing the tuple
	 * @param tupleIdx: the index of the tuple inside the page
	 */
	public void removeTuple(int pageIdx,int tupleIdx){
		try
		{
			Page curr = (Page)DBApp.deserialize(pages.get(pageIdx));
			curr.v.remove(tupleIdx);
			if(curr.v.size() == 0) 
			{
				DBApp.deleteFile(pages.get(pageIdx));
				pages.remove(pageIdx);
			}
			else 
			{
				DBApp.serialize(curr, pages.get(pageIdx));	
			}		
		}
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		}		
	}
	
	
	/**
	 * 
	 * @param colPos
	 * @param tree
	 */
	public void putOldKeysInTree(int colPos,BPTree tree) {
		for(String currPageName:pages)
		{
			Page currPage = null;
			try
			{
				currPage = (Page)DBApp.deserialize(currPageName);
			}
			catch(FileNotFoundException e)
			{
				e.printStackTrace();
			}
			currPage.putOldKeysInTree(colPos,tree);
		}
	}
	public void putOldKeysInTree(int colPos,RTree tree) {
		for(String currPageName:pages)
		{
			Page currPage = null;
			try
			{
				currPage = (Page)DBApp.deserialize(currPageName);
			}
			catch(FileNotFoundException e)
			{
				e.printStackTrace();
			}
			currPage.putOldKeysInTree(colPos,tree);
		}
	}

}
