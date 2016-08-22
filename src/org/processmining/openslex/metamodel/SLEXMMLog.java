/*
 * 
 */
package org.processmining.openslex.metamodel;

import java.util.HashMap;

// TODO: Auto-generated Javadoc
/**
 * The Class SLEXMMLog.
 *
 * @author <a href="mailto:e.gonzalez@tue.nl">Eduardo Gonzalez Lopez de Murillas</a>
 * @see <a href="https://www.win.tue.nl/~egonzale/projects/openslex/" target="_blank">OpenSLEX</a>
 */
public class SLEXMMLog extends SLEXMMAbstractDatabaseObject {

	/** The name. */
	private String name = null;
	
	/** The process id. */
	private int processId = -1;
	
	/** The attribute values. */
	private HashMap<SLEXMMLogAttribute, SLEXMMLogAttributeValue> attributeValues = null;
	
	/**
	 * Instantiates a new SLEXMM log.
	 *
	 * @param storage the storage
	 */
	protected SLEXMMLog(SLEXMMStorageMetaModel storage) {
		super(storage);
	}

	/**
	 * Gets the storage.
	 *
	 * @return the storage
	 */
	public SLEXMMStorageMetaModel getStorage() {
		return (SLEXMMStorageMetaModel) super.storage;
	}
	
	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
		setDirty(true);
	}
	
	/**
	 * Adds the.
	 *
	 * @param cs the cs
	 * @return true, if successful
	 */
	public boolean add(SLEXMMCase cs) {
		return getStorage().addCaseToLog(this.getId(),cs.getId());
	}
	
	/**
	 * Adds the.
	 *
	 * @param caseId the case id
	 * @return true, if successful
	 */
	public boolean add(int caseId) {
		return getStorage().addCaseToLog(this.getId(),caseId);
	}
	
	/**
	 * Gets the process id.
	 *
	 * @return the process id
	 */
	public int getProcessId() {
		return this.processId;
	}
	
	/**
	 * Sets the process id.
	 *
	 * @param processId the new process id
	 */
	protected void setProcessId(int processId) {
		this.processId = processId;
		setDirty(true);
	}
	
	/* (non-Javadoc)
	 * @see org.processmining.openslex.metamodel.SLEXMMAbstractDatabaseObject#insert(org.processmining.openslex.metamodel.SLEXMMAbstractDatabaseObject)
	 */
	@Override
	boolean insert(SLEXMMAbstractDatabaseObject t) {
		return getStorage().insert((SLEXMMLog) t);
	}

	/* (non-Javadoc)
	 * @see org.processmining.openslex.metamodel.SLEXMMAbstractDatabaseObject#update(org.processmining.openslex.metamodel.SLEXMMAbstractDatabaseObject)
	 */
	@Override
	boolean update(SLEXMMAbstractDatabaseObject t) {
		return getStorage().update((SLEXMMLog) t);
	}
	
	/**
	 * Retrieve attribute values.
	 */
	protected void retrieveAttributeValues() {
		attributeValues = getStorage().getAttributeValuesForLog(this);
	}
	
	
	protected void setAttributeValues(HashMap<SLEXMMLogAttribute, SLEXMMLogAttributeValue> attributeValues) {
		this.attributeValues = attributeValues;
	}
	
	/**
	 * Gets the attribute values.
	 *
	 * @return the attribute values
	 */
	public HashMap<SLEXMMLogAttribute, SLEXMMLogAttributeValue> getAttributeValues() {
		if (attributeValues == null) {
			retrieveAttributeValues();
		}
		return attributeValues;
	}
	
	/**
	 * Gets the case result set.
	 *
	 * @return the case result set
	 */
	public SLEXMMCaseResultSet getCaseResultSet() {
		return getStorage().getCasesForLog(this.getId());
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return ("log#"+getId()).hashCode();
	}
}
