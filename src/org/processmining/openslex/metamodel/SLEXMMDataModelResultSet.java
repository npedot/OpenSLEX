/*
 * 
 */
package org.processmining.openslex.metamodel;

import java.sql.ResultSet;

// TODO: Auto-generated Javadoc
/**
 * The Class SLEXMMDataModelResultSet.
 *
 * @author <a href="mailto:e.gonzalez@tue.nl">Eduardo Gonzalez Lopez de Murillas</a>
 * @see <a href="https://www.win.tue.nl/~egonzale/projects/openslex/" target="_blank">OpenSLEX</a>
 */
public class SLEXMMDataModelResultSet extends AbstractRSetElement<SLEXMMDataModel> {
	
	/**
	 * Instantiates a new SLEXMM data model result set.
	 *
	 * @param storage the storage
	 * @param rset the rset
	 */
	public SLEXMMDataModelResultSet(SLEXMMStorageMetaModel storage, ResultSet rset) {
		super(storage, rset, SLEXMMDataModel.class);
	}
	
	/**
	 * Gets the next.
	 *
	 * @return the next
	 */
	public SLEXMMDataModel getNext() {
		SLEXMMDataModel dm = null;
		try {
			if (this.rset != null && this.rset.next()) {
				
				String name = this.rset.getString("name");
				int id = this.rset.getInt("id");
				dm = storage.getFromCache(SLEXMMDataModel.class, id);
				if (dm == null) {
					dm = new SLEXMMDataModel(storage);
					dm.setName(name);
					dm.setId(id);
					dm.setDirty(false);
					dm.setInserted(true);
					storage.putInCache(dm);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (dm == null) {
				close();
			}
		}
		return dm;
	}
	
}
