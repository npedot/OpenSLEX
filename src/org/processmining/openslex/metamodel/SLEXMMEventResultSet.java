package org.processmining.openslex.metamodel;

import java.sql.ResultSet;

public class SLEXMMEventResultSet extends SLEXMMAbstractResultSetObject {
	
	
	
	protected SLEXMMEventResultSet(SLEXMMStorageMetaModel storage, ResultSet rset) {
		super(storage, rset);
	}
	
	public SLEXMMEvent getNext() {
		SLEXMMEvent ev = null;
		try {
			if (this.rset != null && this.rset.next()) {
				
				int id = this.rset.getInt("id");
				int activityInstanceId = this.rset.getInt("activity_instance_id");
				int order = this.rset.getInt("ordering");
				String lifecycle = this.rset.getString("lifecycle");
				String resource = this.rset.getString("resource");
				long timestamp = this.rset.getLong("timestamp");
				ev = new SLEXMMEvent((SLEXMMStorageMetaModel) storage);
				ev.setId(id);
				ev.setActivityInstanceId(activityInstanceId);
				ev.setOrder(order);
				ev.setTimestamp(timestamp);
				ev.setLifecycle(lifecycle);
				ev.setResource(resource);
				//ev.retrieveAttributeValues();
				ev.setDirty(false);
				ev.setInserted(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (ev == null) {
				close();
			}
		}
		return ev;
	}
	
}
