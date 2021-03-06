/*
 * 
 */
package org.processmining.openslex.metamodel;

import java.sql.ResultSet;

// TODO: Auto-generated Javadoc
/**
 * The Class SLEXMMClassResultSet.
 *
 * @author <a href="mailto:e.gonzalez@tue.nl">Eduardo Gonzalez Lopez de Murillas</a>
 * @see <a href="https://www.win.tue.nl/~egonzale/projects/openslex/" target="_blank">OpenSLEX</a>
 */
public class SLEXMMClassResultSet extends AbstractRSetElement<SLEXMMClass> {
	
	/**
	 * Instantiates a new SLEXMM class result set.
	 *
	 * @param storage the storage
	 * @param rset the rset
	 */
	public SLEXMMClassResultSet(SLEXMMStorageMetaModel storage, ResultSet rset) {
		super(storage, rset, SLEXMMClass.class);
	}
	
	/**
	 * Gets the next.
	 *
	 * @return the next
	 */
	public SLEXMMClass getNext() {
		SLEXMMClass cl = null;
		try {
			if (this.rset != null && this.rset.next()) {
				
				int id = this.rset.getInt("id");
				int dataModelId = this.rset.getInt("datamodel_id");
				String name = this.rset.getString("name");
				cl = storage.getFromCache(SLEXMMClass.class, id);
				if (cl == null) {
					cl = new SLEXMMClass((SLEXMMStorageMetaModel)storage,name,dataModelId);
					cl.setId(id);
					//cl.retrieveAttributesAndKeys();
					cl.setDirty(false);
					cl.setInserted(true);
					storage.putInCache(cl);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cl == null) {
				close();
			}
		}
		return cl;
	}
	
}
