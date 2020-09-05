package recordsPointer;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;
import java.util.Vector;

import RASQL.DBApp;
import RASQL.DBAppException;
import RASQL.Page;

public class PageOfRefs implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private Vector<Ref> references;
	private String rawName;
	private int index;
	private String overFlowPagePointer;
	
	public static int N = Page.N;
	
	public PageOfRefs(String rawName,int index) {
		references = new Vector<Ref>();
		if(rawName.charAt(rawName.length()-1) == '@') {
			this.rawName = rawName;			
		}else {
			this.rawName=(rawName+"@");			
		}
		this.index = index;
		overFlowPagePointer = null;
	}


	/**
	 *  inserting a reference in this page or its overFlowPage in case of full page
	 * @param recordReference: the reference of the record to be inserted in this page 
	 */
	public void insert(Ref recordReference) {
		if(references.size() != N)//not full
		{
			references.add(recordReference);
			DBApp.serialize(this, rawName+index); // save it to disk as it has been edited
			return;
		}
		if(overFlowPagePointer == null) //there is no overFlowPage after this page yet
		{
			overFlowPagePointer = rawName+(index+1);// create new one and set pointer to it
			DBApp.serialize(this, rawName+index); // save it to disk as it has been edited
			PageOfRefs overFlowPage = new PageOfRefs(rawName, index+1);
			overFlowPage.insert(recordReference); // insert to it
			return;
		}
		// there exist an overFlowPage after this page
		try
		{
			PageOfRefs overFlowPage=(PageOfRefs)DBApp.deserialize(overFlowPagePointer);
			overFlowPage.insert(recordReference); 
		}
		catch(FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}
	/**
	 * 
	 * @param recordReference: to be deleted
	 * @return: true if there is no more references in this overFlowPages chain, false otherwise
	 * 				/i.e. the corresponding key should be deleted 
	 */
	public boolean delete(Ref recordReference) {
		int idx = search(recordReference);
		if(idx == -1) // not in current page
		{
			if(overFlowPagePointer != null)
			{
				try
				{
					PageOfRefs overFlowPage= (PageOfRefs)DBApp.deserialize(overFlowPagePointer);
					overFlowPage.delete(recordReference);
					if(overFlowPage.references.size() == 0 && overFlowPage.overFlowPagePointer == null)
					   // the next page becomes empty && it is the last page
					{
						this.overFlowPagePointer = null;
						DBApp.serialize(this, this.rawName+this.index);
					}
				}
				catch(FileNotFoundException e)
				{
					e.printStackTrace();
				}
			}
			return false;
		}
		// recoredRef exist in this page
		references.remove(idx);
		if(references.size() == 0)//page becomes empty so should be deleted
		{
			DBApp.deleteFile((rawName+index));
			String nextPagePointer = overFlowPagePointer;
			// shift the upcoming pages to the left
			while(nextPagePointer != null)
			{
				try
				{
					PageOfRefs currPage = (PageOfRefs)DBApp.deserialize(nextPagePointer);
					nextPagePointer = currPage.overFlowPagePointer;
					currPage.index--; // update index (shifted lift)
					if(currPage.overFlowPagePointer != null)	
						currPage.overFlowPagePointer = currPage.rawName+(currPage.index+1); //update pointer to next page
					else
						DBApp.deleteFile(rawName+(index+1));
					
					DBApp.serialize(currPage,currPage.rawName+currPage.index);
				}
				catch(FileNotFoundException e)
				{
					e.printStackTrace();
					break;
				}
				
			}
			if(this.index == 0 && this.overFlowPagePointer == null) // no more records in the chain
			// it was the first page and also the last page and now it is empty
			{
				return true;
			}
			return false;
		}
		DBApp.serialize(this, rawName+index);
		return false;
	}
	private int search(Ref recordReference) {
		for(int i=0;i<references.size();i++)
		{
			Ref currRef = references.get(i);
			if(currRef.getPage().equals(recordReference.getPage()))
			{
				return i;
			}
		}
		return -1;
	}
	public void update(String strTableName,Hashtable<String,Object> htblColNameValue,
						Comparable clusteringKey,Vector<String>colNames,Vector<Boolean>indexed
						,Vector<String>types,TreeSet<String> pagesDone) throws DBAppException {
		for(Ref r:references) 
		{
			String currPageName = r.getPage();
			if(pagesDone.contains(currPageName))
				continue;
			pagesDone.add(currPageName);
			Page curPage=null;
			try 
			{
				curPage=(Page)DBApp.deserialize(currPageName);
			}
			catch (FileNotFoundException e1) 
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			curPage.update(strTableName,htblColNameValue,clusteringKey,colNames,indexed,types);
			
			DBApp.serialize(curPage,currPageName);
		}
		
		if(overFlowPagePointer!=null) 
		{
			try 
			{
				PageOfRefs page=(PageOfRefs)DBApp.deserialize(overFlowPagePointer);
				page.update(strTableName,htblColNameValue,clusteringKey,colNames,indexed,types,pagesDone);
				
			} 
			catch (FileNotFoundException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	public Iterator<Vector<Object>> allTuples(TreeSet<String>pagesDone,Comparable val,int indexOfKey) {
		
		LinkedList<Vector<Object>>list=new LinkedList<>();
		
		for(Ref r:references) 
		{
			String currPageName = r.getPage();
			if(pagesDone.contains(currPageName))
				continue;
			pagesDone.add(currPageName);
			Page curPage=null;
			try 
			{
				curPage=(Page)DBApp.deserialize(currPageName);
			}
			catch (FileNotFoundException e1) 
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			for(Vector<Object>tuple:curPage.v) 
			{
				Comparable tupleKey=(Comparable)tuple.get(indexOfKey);
				if(tupleKey.compareTo(val)==0) 
				{
					list.add(tuple);
				}
			}
		}
		
		if(overFlowPagePointer!=null) 
		{
			try 
			{
				PageOfRefs page=(PageOfRefs)DBApp.deserialize(overFlowPagePointer);
				
				Iterator<Vector<Object>> nxt=page.allTuples(pagesDone, val, indexOfKey);
				while(nxt.hasNext()) 
				{
					list.add(nxt.next());
				}
				
				
			} 
			catch (FileNotFoundException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return list.iterator();
	}
	public Iterator<Vector<Object>> allTuplesExact(TreeSet<String>pagesDone,Comparable val,int indexOfKey) {
		
		LinkedList<Vector<Object>>list=new LinkedList<>();
		
		for(Ref r:references) 
		{
			String currPageName = r.getPage();
			if(pagesDone.contains(currPageName))
				continue;
			pagesDone.add(currPageName);
			Page curPage=null;
			try 
			{
				curPage=(Page)DBApp.deserialize(currPageName);
			}
			catch (FileNotFoundException e1) 
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			for(Vector<Object>tuple:curPage.v) 
			{
				Comparable tupleKey=(Comparable)tuple.get(indexOfKey);
				if(tupleKey.equals(val)) 
				{
					list.add(tuple);
				}
			}
		}
		
		if(overFlowPagePointer!=null) 
		{
			try 
			{
				PageOfRefs page=(PageOfRefs)DBApp.deserialize(overFlowPagePointer);
				
				Iterator<Vector<Object>> nxt=page.allTuples(pagesDone, val, indexOfKey);
				while(nxt.hasNext()) 
				{
					list.add(nxt.next());
				}
				
				
			} 
			catch (FileNotFoundException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return list.iterator();
	}
}
	

