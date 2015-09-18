package org.processmining.openslex.metamodel;

import java.sql.ResultSet;
import java.sql.SQLException;

abstract class SLEXMMAbstractResultSetObject {
	
	protected ResultSet rset = null;
	protected SLEXMMStorage storage = null;
	
	protected SLEXMMAbstractResultSetObject(SLEXMMStorage storage, ResultSet rset) {
		this.storage = storage;
		this.rset = rset;
	}
	
	public void close() {
		if (rset != null) {
			try {
				rset.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}
}