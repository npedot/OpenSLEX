/*
 * 
 */
package org.processmining.openslex.metamodel;

import java.sql.ResultSet;

// TODO: Auto-generated Javadoc
/**
 * The Class SLEXMMCaseAttributeResultSet.
 *
 * @author <a href="mailto:e.gonzalez@tue.nl">Eduardo Gonzalez Lopez de Murillas</a>
 * @see <a href="https://www.win.tue.nl/~egonzale/projects/openslex/" target="_blank">OpenSLEX</a>
 */
public class SLEXMMCaseAttributeResultSet extends AbstractRSetElement<SLEXMMCaseAttribute> {
	
	/**
	 * Instantiates a new SLEXMM case attribute result set.
	 *
	 * @param storage the storage
	 * @param rset the rset
	 */
	public SLEXMMCaseAttributeResultSet(SLEXMMStorageMetaModel storage, ResultSet rset) {
		super(storage, rset, SLEXMMCaseAttribute.class);
	}
	
	/**
	 * Gets the next.
	 *
	 * @return the next
	 */
	public SLEXMMCaseAttribute getNext() {
		SLEXMMCaseAttribute ai = null;
		try {
			if (this.rset != null && this.rset.next()) {
				
				int id = this.rset.getInt("id");
				String name = this.rset.getString("name");
				ai = storage.getFromCache(SLEXMMCaseAttribute.class, id);
				if (ai == null) {
					ai = new SLEXMMCaseAttribute((SLEXMMStorageMetaModel) storage);
					ai.setId(id);
					ai.setName(name);
					ai.setDirty(false);
					ai.setInserted(true);
					storage.putInCache(ai);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (ai == null) {
				close();
			}
		}
		return ai;
	}
	
}
