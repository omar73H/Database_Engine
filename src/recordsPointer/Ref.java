package recordsPointer;

import java.io.Serializable;

public class Ref implements Serializable{
	
	/**
	 * This class represents a pointer to the record. It is used at the leaves of the B+ tree 
	 */
	private static final long serialVersionUID = 1L;
	private String pageName;
	
	public Ref(String pageName)
	{
		this.pageName = pageName;
	}
	
	/**
	 * @return the page at which the record is saved on the hard disk
	 */
	public String getPage()
	{
		return pageName;
	}
	
}