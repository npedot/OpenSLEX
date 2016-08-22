/*
 * 
 */
package org.processmining.openslex.metamodel;

import java.util.HashMap;

// TODO: Auto-generated Javadoc
/**
 * The Class SLEXMMCase.
 *
 * @author <a href="mailto:e.gonzalez@tue.nl">Eduardo Gonzalez Lopez de Murillas</a>
 * @see <a href="https://www.win.tue.nl/~egonzale/projects/openslex/" target="_blank">OpenSLEX</a>
 */
public class SLEXMMCase extends SLEXMMAbstractDatabaseObject {

	/** The name. */
	private String name = null;
	
	/** The attribute values. */
	private HashMap<SLEXMMCaseAttribute, SLEXMMCaseAttributeValue> attributeValues = null;
	
	/**
	 * Instantiates a new SLEXMM case.
	 *
	 * @param storage the storage
	 */
	protected SLEXMMCase(SLEXMMStorageMetaModel storage) {
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
	 * @param ai the ai
	 * @return true, if successful
	 */
	public boolean add(SLEXMMActivityInstance ai) {
		return getStorage().addActivityInstanceToCase(this,ai);
	}
	
	/**
	 * Adds the.
	 *
	 * @param activityInstanceId the activity instance id
	 * @return true, if successful
	 */
	public boolean add(int activityInstanceId) {
		return getStorage().addActivityInstanceToCase(this.getId(),activityInstanceId);
	}
	
	/* (non-Javadoc)
	 * @see org.processmining.openslex.metamodel.SLEXMMAbstractDatabaseObject#insert(org.processmining.openslex.metamodel.SLEXMMAbstractDatabaseObject)
	 */
	@Override
	boolean insert(SLEXMMAbstractDatabaseObject t) {
		return getStorage().insert((SLEXMMCase) t);
	}

	/* (non-Javadoc)
	 * @see org.processmining.openslex.metamodel.SLEXMMAbstractDatabaseObject#update(org.processmining.openslex.metamodel.SLEXMMAbstractDatabaseObject)
	 */
	@Override
	boolean update(SLEXMMAbstractDatabaseObject t) {
		return getStorage().update((SLEXMMCase) t);
	}

//	public SLEXMMActivityInstanceResultSet getActivityInstanceResultSet() {
//		return getStorage().getActivityInstancesOfCase(this);
//	}
	
	/**
	 * Retrieve attribute values.
	 */
	protected void retrieveAttributeValues() {
		attributeValues = getStorage().getAttributeValuesForCase(this);
	}
	
	protected void setAttributeValues(HashMap<SLEXMMCaseAttribute, SLEXMMCaseAttributeValue> attributeValues) {
		this.attributeValues = attributeValues;
	}
	
	/**
	 * Gets the attribute values.
	 *
	 * @return the attribute values
	 */
	public HashMap<SLEXMMCaseAttribute, SLEXMMCaseAttributeValue> getAttributeValues() {
		if (attributeValues == null) {
			retrieveAttributeValues();
		}
		return attributeValues;
	}
	
	/**
	 * Gets the event result set.
	 *
	 * @return the event result set
	 */
	public SLEXMMEventResultSet getEventResultSet() {
		return getStorage().getEventsOfCase(this);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return ("case#"+getId()).hashCode();
	}
}
