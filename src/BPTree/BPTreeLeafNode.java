package BPTree;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;
import java.util.Vector;

import RASQL.DBApp;
import recordsPointer.PageOfRefs;
import recordsPointer.Ref;

public class BPTreeLeafNode<T extends Comparable<T>> extends BPTreeNode<T> implements Serializable{

	/**
	 * 
	**/
	private static final long serialVersionUID = 1L;
	private String[] pointers;
	private BPTreeLeafNode<T> next;
	
	private String strTableName;
	private String strColName;
	
	@SuppressWarnings("unchecked")
	public BPTreeLeafNode(int n,String strTableName,String strColName) 
	{
		super(n);
		this.strTableName = strTableName;
		this.strColName = strColName;
		keys = new Comparable[n];
		pointers = new String[n];

	}
	
	/**
	 * @return the next leaf node
	 */
	public BPTreeLeafNode<T> getNext()
	{
		return this.next;
	}
	
	/**
	 * sets the next leaf node
	 * @param node the next leaf node
	 */
	public void setNext(BPTreeLeafNode<T> node)
	{
		this.next = node;
	}
	
	/**
	 * @param index the index to find its record
	 * @return the reference of the queried index
	 */
	public String getPointer(int index) 
	{
		return pointers[index];
	}
	
	/**
	 * sets the record at the given index with the passed reference
	 * @param index the index to set the value at
	 * @param recordReference the reference to the record
	 */
	public void setPointer(int index, String pointer) 
	{
		pointers[index] = pointer;
	}

	/**
	 * @return the reference of the first record
	 */
	public String getFirstPointer()
	{
		return pointers[0];
	}

	/**
	 * @return the reference of the last record
	 */
	public String getLastPointer()
	{
		return pointers[numberOfKeys-1];
	}
	
	/**
	 * finds the minimum number of keys the current node must hold
	 */
	public int minKeys()
	{
		if(this.isRoot())
			return 1;
		return (order + 1) / 2;
	}
	
	/**
	 * insert the specified key associated with a given record refernce in the B+ tree
	 */
	public PushUp<T> insert(T key, Ref recordReference, BPTreeInnerNode<T> parent, int ptr)
	{
		// if key already exist in that node return its index otherwise -1
		int idx = keyIndex(key);
		if(idx != -1)
		{
			String pageName = pointers[idx];
			try
			{
				PageOfRefs refPage =(PageOfRefs)DBApp.deserialize(pageName);
				refPage.insert(recordReference);
			}
			catch(FileNotFoundException e)
			{
				e.printStackTrace();
			}
			return null;
		}
		if(this.isFull())
		{
			BPTreeNode<T> newNode = this.split(key, recordReference);
			Comparable<T> newKey = newNode.getFirstKey();
			return new PushUp<T>(newNode, newKey);
		}
		else
		{
			int index = 0;
			while (index < numberOfKeys && getKey(index).compareTo(key) < 0)
				++index;
			String newPointer = this.strTableName+"@"+this.strColName+"@"+key.toString();
			PageOfRefs refPage = new PageOfRefs(newPointer,0);
			refPage.insert(recordReference);
			this.insertAt(index, key, newPointer+"@0");
			return null;
		}
	}
	private int keyIndex(T key) {
		for(int i=0;i<numberOfKeys;i++)
			if(keys[i].compareTo(key)==0)
				return i;
		return -1;
	}
	/**
	 * inserts the passed key associated with its record reference in the specified index
	 * @param index the index at which the key will be inserted
	 * @param key the key to be inserted
	 * @param recordReference the pointer to the record associated with the key
	 */
	private void insertAt(int index, Comparable<T> key, String pointer) 
	{
		for (int i = numberOfKeys - 1; i >= index; --i) 
		{
			this.setKey(i + 1, getKey(i));
			this.setPointer(i + 1, getPointer(i));
		}

		this.setKey(index, key);
		this.setPointer(index, pointer);
		++numberOfKeys;
	}
	
	/**
	 * splits the current node
	 * @param key the new key that caused the split
	 * @param recordReference the reference of the new key
	 * @return the new node that results from the split
	 */
	public BPTreeNode<T> split(T key, Ref recordReference) 
	{
		int keyPos = this.findIndex(key); // the position in the node in which the key should be inserted
		int midIndex = numberOfKeys / 2;
		if((numberOfKeys & 1) == 1 && keyPos > midIndex)	//split nodes evenly
			++midIndex;		

		
		int totalKeys = numberOfKeys + 1;
		//move keys to a new node
		BPTreeLeafNode<T> newNode = new BPTreeLeafNode<T>(order,this.strTableName,this.strColName);
		for (int i = midIndex; i < totalKeys - 1; ++i) 
		{
			newNode.insertAt(i - midIndex, this.getKey(i), this.getPointer(i));
			numberOfKeys--;
		}
		
		//insert the new key
		String newPointer = this.strTableName+"@"+this.strColName+"@"+key.toString();
		PageOfRefs refPage = new PageOfRefs(newPointer,0);
		refPage.insert(recordReference);
		if(keyPos < totalKeys / 2)
			this.insertAt(keyPos, key, newPointer+"@0");
		else
			newNode.insertAt(keyPos - midIndex, key, newPointer+"@0");
		
		//set next pointers
		newNode.setNext(this.getNext());
		this.setNext(newNode);
		
		return newNode;
	}
	
	/**
	 * finds the index at which the passed key must be located 
	 * @param key the key to be checked for its location
	 * @return the expected index of the key
	 */
	public int findIndex(T key) 
	{
		for (int i = 0; i < numberOfKeys; ++i) 
		{
			int cmp = getKey(i).compareTo(key);
			if (cmp > 0) 
				return i;
		}
		return numberOfKeys;
	}

	/**
	 * returns the pointer to the page of references with the passed key and null if does not exist
	 */
	@Override
	public String search(T key) 
	{
		for(int i = 0; i < numberOfKeys; ++i)
			if(this.getKey(i).compareTo(key) == 0)
				return this.getPointer(i);
		return null;
	}
	
	/**
	 * delete the passed key from the B+ tree
	 */
	public boolean delete(T key, BPTreeInnerNode<T> parent, int ptr) 
	{
		for(int i = 0; i < numberOfKeys; ++i)
			if(keys[i].compareTo(key) == 0)
			{
				this.deleteAt(i);
				if(i == 0 && ptr > 0)
				{
					//update key at parent
					parent.setKey(ptr - 1, this.getFirstKey());
				}
				//check that node has enough keys
				if(!this.isRoot() && numberOfKeys < this.minKeys())
				{
					//1.try to borrow
					if(borrow(parent, ptr))
						return true;
					//2.merge
					merge(parent, ptr);
				}
				return true;
			}
		return false;
	}
	
	/**
	 * delete a specific recordReference only that is in refPage associated with the passed key from the B+ tree
	 */
	public void deleteRef(T key, BPTreeInnerNode<T> parent, int ptr, Ref recordReference) 
	{
		for(int i = 0; i < numberOfKeys; ++i)
			if(keys[i].compareTo(key) == 0)
			{
				if(deleteRefAt(i,recordReference)) // the associated pointer becomes empty after deletion
				{
					// remove the key from the tree
					delete(key, parent, ptr);
				}
				return;
			}
	}
	private boolean deleteRefAt(int i,Ref recordReference) {
		try
		{
			PageOfRefs refPage = (PageOfRefs)DBApp.deserialize(pointers[i]);
			return refPage.delete(recordReference);
			
		}
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
			return false;
		}
	}
	/**
	 * delete a key at the specified index of the node
	 * @param index the index of the key to be deleted
	 */
	public void deleteAt(int index)
	{
		for(int i = index; i < numberOfKeys - 1; ++i)
		{
			keys[i] = keys[i+1];
			pointers[i] = pointers[i+1];
		}
		numberOfKeys--;
	}
	/**
	 * tries to borrow a key from the left or right sibling
	 * @param parent the parent of the current node
	 * @param ptr the index of the parent pointer that points to this node 
	 * @return true if borrow is done successfully and false otherwise
	 */
	public boolean borrow(BPTreeInnerNode<T> parent, int ptr)
	{
		//check left sibling
		if(ptr > 0)
		{
			BPTreeLeafNode<T> leftSibling = (BPTreeLeafNode<T>) parent.getChild(ptr-1);
			if(leftSibling.numberOfKeys > leftSibling.minKeys())
			{
				this.insertAt(0, leftSibling.getLastKey(), leftSibling.getLastPointer());		
				leftSibling.deleteAt(leftSibling.numberOfKeys - 1);
				parent.setKey(ptr - 1, keys[0]);
				return true;
			}
		}
		
		//check right sibling
		if(ptr < parent.numberOfKeys)
		{
			BPTreeLeafNode<T> rightSibling = (BPTreeLeafNode<T>) parent.getChild(ptr+1);
			if(rightSibling.numberOfKeys > rightSibling.minKeys())
			{
				this.insertAt(numberOfKeys, rightSibling.getFirstKey(), rightSibling.getFirstPointer());
				rightSibling.deleteAt(0);
				parent.setKey(ptr, rightSibling.getFirstKey());
				return true;
			}
		}
		return false;
	}
	
	/**
	 * merges the current node with its left or right sibling
	 * @param parent the parent of the current node
	 * @param ptr the index of the parent pointer that points to this node 
	 */
	public void merge(BPTreeInnerNode<T> parent, int ptr)
	{
		if(ptr > 0)
		{
			//merge with left
			BPTreeLeafNode<T> leftSibling = (BPTreeLeafNode<T>) parent.getChild(ptr-1);
			leftSibling.merge(this);
			parent.deleteAt(ptr-1);			
		}
		else
		{
			//merge with right
			BPTreeLeafNode<T> rightSibling = (BPTreeLeafNode<T>) parent.getChild(ptr+1);
			this.merge(rightSibling);
			parent.deleteAt(ptr);
		}
	}
	
	/**
	 * merge the current node with the specified node. The foreign node will be deleted
	 * @param foreignNode the node to be merged with the current node
	 */
	public void merge(BPTreeLeafNode<T> foreignNode)
	{
		for(int i = 0; i < foreignNode.numberOfKeys; ++i)
			this.insertAt(numberOfKeys, foreignNode.getKey(i), foreignNode.getPointer(i));
		
		this.setNext(foreignNode.getNext());
	}
	
	public BPTreeLeafNode<T> getSmallestLeafNode() {
		return this;
	}
	
	public Iterator<Vector<Object>> allTuplesSmallerThan(T key,int indexOfKey) {
		
		LinkedList<Vector<Object>>list=new LinkedList<>();
		
		boolean end=false;
		for(int i=0;i<numberOfKeys;i++) 
		{
			if(getKey(i).compareTo(key)>=0) 
			{
				end=true;
				break;
			}
			
			String name=getPointer(i);
			try 
			{
				PageOfRefs page=(PageOfRefs)DBApp.deserialize(name);
				Iterator<Vector<Object>> nxt = page.allTuples(new TreeSet<String>(),getKey(i),indexOfKey);
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

		BPTreeLeafNode<T> next = getNext();
		
		if(!end && next!=null) 
		{
			Iterator<Vector<Object>> nxt=next.allTuplesSmallerThan(key,indexOfKey);
			while(nxt.hasNext()) 
			{
				list.add(nxt.next());
			}
		}
		
		return list.iterator();
	}
	
	public Iterator<Vector<Object>> allTuplesSmallerThanOrEqual(T key,int indexOfKey) {
		
		LinkedList<Vector<Object>>list=new LinkedList<>();
		
		boolean end=false;
		for(int i=0;i<numberOfKeys;i++) 
		{
			if(getKey(i).compareTo(key)>0) 
			{
				end=true;
				break;
			}
			
			String name=getPointer(i);
			try 
			{
				PageOfRefs page=(PageOfRefs)DBApp.deserialize(name);
				Iterator<Vector<Object>> nxt= page.allTuples(new TreeSet<String>(),getKey(i),indexOfKey);
				while(nxt.hasNext())
				{
					list.add(nxt.next());
				}
			} catch (FileNotFoundException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}

		BPTreeLeafNode<T> next = getNext();
		if(!end && next!=null) 
		{
			Iterator<Vector<Object>> nxt=next.allTuplesSmallerThanOrEqual(key,indexOfKey);
			while(nxt.hasNext()) 
			{
				list.add(nxt.next());
			}
		}
		
		return list.iterator();
	}
	
	public BPTreeLeafNode<T> searchLeafNode(T key) 
	{
		return this;
		
	}
	
	public Iterator<Vector<Object>> allTuplesgreaterThanOrEqual(T key,int indexOfKey) {
		LinkedList<Vector<Object>>list=new LinkedList<>();
		for(int i=0;i<numberOfKeys;i++) 
		{
			if(getKey(i).compareTo(key)<0) 
			{
				continue;
			}
			
			String name=getPointer(i);
			try 
			{
				PageOfRefs page=(PageOfRefs)DBApp.deserialize(name);
				Iterator<Vector<Object>> nxt = page.allTuples(new TreeSet<String>(),getKey(i),indexOfKey);
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

		BPTreeLeafNode<T> next = getNext();
		if(next!=null) 
		{
			Iterator<Vector<Object>> nxt=next.allTuplesgreaterThanOrEqual(key, indexOfKey);
			while(nxt.hasNext()) 
			{
				list.add((Vector<Object>)nxt.next());
			}
		}
		
		return list.iterator();
	}
	public Iterator<Vector<Object>> allTuplesgreaterThan(T key,int indexOfKey) {

		LinkedList<Vector<Object>>list=new LinkedList<>();
		
		for(int i=0;i<numberOfKeys;i++) 
		{
			if(getKey(i).compareTo(key)<=0) 
			{
				continue;
			}
			
			String name=getPointer(i);
			try {
				PageOfRefs page=(PageOfRefs)DBApp.deserialize(name);
				Iterator<Vector<Object>> nxt = page.allTuples(new TreeSet<String>(),getKey(i),indexOfKey);
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

		BPTreeLeafNode<T> next = getNext();
		if(next!=null) 
		{
			Iterator<Vector<Object>> nxt=next.allTuplesgreaterThan(key, indexOfKey);
			while(nxt.hasNext()) 
			{
				list.add(nxt.next());
			}
		}
		
		return list.iterator();
	}
	
}