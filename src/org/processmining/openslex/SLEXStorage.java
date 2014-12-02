package org.processmining.openslex;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.processmining.openslex.utils.ScriptRunner;

public class SLEXStorage {
	
	private static final String STORAGE_COLLECTION = "data"+File.separator+"collections.db";
	private static final String STORAGE_DATAMODEL = "data"+File.separator+"datamodels.db";
	
	private static final String COLLECTION_ALIAS = "collectionsdb";
	private static final String DATAMODEL_ALIAS = "datamodelsdb";
	
	private InputStream COLLECTION_SCHEMA_IN =  SLEXStorage.class.getResourceAsStream("/org/processmining/openslex/resources/collections.sql");
	private InputStream DATAMODEL_SCHEMA_IN = SLEXStorage.class.getResourceAsStream("/org/processmining/openslex/resources/datamodels.sql");
	
	public static final String COMMON_CLASS_NAME = "COMMON";
	private static final String JOURNAL_MODE = "OFF";
	private static final String PRAGMA_SYNCHRONOUS_MODE = "OFF";
	
	private boolean collection_attached = false;
	private boolean datamodel_attached = false;
	
	private boolean autoCommitOnCreation = true;
	
	private Connection connection = null;
	
	private static SLEXStorage _instance = null;
	
	public static SLEXStorage getInstance() throws ClassNotFoundException {
		if (_instance == null) {
			_instance = new SLEXStorage();
		}
		
		return _instance;
	}
	
	private SLEXStorage() throws ClassNotFoundException {
		init();
		openCollectionStorage(null);
		openDataModelStorage(null);
	}
	
	public void setAutoCommit(boolean autoCommit) {
		try {
			this.connection.setAutoCommit(autoCommit);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void commit() {
		try {
			this.connection.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String getCurrentWorkingDir() {
		return System.getProperty("user.dir");
	}
	
	public boolean openCollectionStorage(String filename) {
		if (collection_attached) {
			collection_attached = !detachDatabase(COLLECTION_ALIAS);
		}
		if (!collection_attached) {
			String fname = filename;
			if (fname == null) {
				fname = STORAGE_COLLECTION;
			}
			if (attachDatabaseFile(fname,COLLECTION_ALIAS,COLLECTION_SCHEMA_IN)) {
				collection_attached = true;
			}
			return collection_attached;
		} else {
			return false;
		}
	}
	
	public boolean openDataModelStorage(String filename) {
		if (datamodel_attached) {
			datamodel_attached = !detachDatabase(DATAMODEL_ALIAS);
		}
		if (!datamodel_attached) {
			String fname = filename;
			if (fname == null) {
				fname = STORAGE_DATAMODEL;
			}
			if (attachDatabaseFile(fname,DATAMODEL_ALIAS,DATAMODEL_SCHEMA_IN)) {
				datamodel_attached = true;
			}
			return datamodel_attached;
		} else {
			return false;
		}
	}
	
	private boolean checkSchema(String filename, String alias, InputStream schemaIn) {
		boolean result = false;
		Connection connAux = null;
		try {
			connAux = DriverManager.getConnection("jdbc:sqlite:"+filename);
			ScriptRunner scriptRunner = new ScriptRunner(connAux, false, false);
			scriptRunner.runScript(new InputStreamReader(schemaIn));
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		} finally {
			if (connAux != null) {
				try {
					connAux.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		
		return result;
	}
	
	private void closeStatement(Statement statement) {
		try {
			if (statement != null) {
				statement.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void closeResultSet(ResultSet rset) {
		try {
			if (rset != null) {
				rset.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private boolean attachDatabaseFile(String filename, String alias, InputStream schemaIn) {
		Statement statement = null;
		boolean result = false;
		try {
			File f = new File(filename);
			if (!f.exists()) {
				f.getParentFile().mkdirs();
				f.createNewFile();
			}
			
			if (checkSchema(filename,alias,schemaIn)) {
				statement = connection.createStatement();
				statement.setQueryTimeout(30);
				statement.execute("ATTACH DATABASE 'file:"+filename+"' AS "+alias);
				result = true;
			} else {
				result = false;
			}
			
		} catch (Exception e) {
			result = false;
		} finally {
			closeStatement(statement);
			setJournalMode(JOURNAL_MODE);
			setSynchronousMode(PRAGMA_SYNCHRONOUS_MODE);
		}
		return result;
	}
	
	private boolean detachDatabase(String alias) {
		Statement statement = null;
		boolean result = false;
		try {
			statement = connection.createStatement();
			statement.setQueryTimeout(30);
			statement.execute("DETACH DATABASE "+alias);
			result = true;
		} catch (Exception e) {
			result = false;
		} finally {
			closeStatement(statement);
		}
		
		return result;
	}
	
	private boolean setJournalMode(String mode) {
		if (mode != null) {
			Statement statement = null;
			boolean result = false;
			try {
				statement = connection.createStatement();
				statement.setQueryTimeout(30);
				statement.execute("pragma journal_mode = "+mode+";");
				result = true;
			} catch (Exception e) {
				result = false;
			} finally {
				closeStatement(statement);
			}
			
			return result;
		}
		return false;
	}
	
	private boolean setSynchronousMode(String mode) {
		if (mode != null) {
			Statement statement = null;
			boolean result = false;
			try {
				statement = connection.createStatement();
				statement.setQueryTimeout(30);
				statement.execute("pragma synchronous = "+mode+";");
				result = true;
			} catch (Exception e) {
				result = false;
			} finally {
				closeStatement(statement);
			}
			
			return result;
		}
		return false;
	}
	
	private String queryPragma(String pragma) {
		String result = "";
		if (pragma != null) {
			Statement statement = null;
			try {
				statement = connection.createStatement();
				statement.setQueryTimeout(30);
				ResultSet r = statement.executeQuery("pragma "+pragma);
				if (r.next()) {
					result = r.getString(1);
				}
			} catch (Exception e) {
				result = e.getLocalizedMessage();
			} finally {
				closeStatement(statement);
			}
		}
		return result;
	}
	
	private void init() throws ClassNotFoundException {
		
		Class.forName("org.sqlite.JDBC");
		
		try {
			connection = DriverManager.getConnection("jdbc:sqlite::memory:");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void disconnect() {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void setAutoCommitOnCreation(boolean flag) {
		this.autoCommitOnCreation = flag;
	}
	
	public boolean isAutoCommitOnCreationEnabled() {
		return this.autoCommitOnCreation;
	}
	
	public SLEXEventCollection createEventCollection(String name) {
		SLEXEventCollection ec = new SLEXEventCollection(this, name);
		if (isAutoCommitOnCreationEnabled()) {
			ec.commit();
		}
		return ec;
	}
	
	public SLEXEvent createEvent(int collectionId) {
		SLEXEvent ev = new SLEXEvent(this);
		ev.setCollectionId(collectionId);
		if (isAutoCommitOnCreationEnabled()) {
			ev.commit();
		}
		return ev;
	}
	
	public SLEXClass findClass(String name) {
		SLEXClass cl = null;
		Statement statement = null;
		try {
			statement = connection.createStatement();
			statement.setQueryTimeout(30);
			ResultSet rset = statement.executeQuery("SELECT id,name,common FROM "+COLLECTION_ALIAS+".class WHERE name = '"+name+"'");
			
			if (rset.next()) {
				boolean common = rset.getBoolean("common");
				int id = rset.getInt("id");
				cl = new SLEXClass(this, name, common);
				cl.setId(id);
				cl.setDirty(false);
				cl.setInserted(false);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			cl = null;
		} finally {
			closeStatement(statement);
		}
		
		return cl;
	}
	
	public SLEXAttribute findAttribute(String className, String name) {
		SLEXAttribute at = null;
		Statement statement = null;
		try {
			statement = connection.createStatement();
			statement.setQueryTimeout(30);
			ResultSet rset = statement.executeQuery("SELECT AT.id,AT.name,AT.classID FROM "
														+COLLECTION_ALIAS+".attribute AS AT, "
														+COLLECTION_ALIAS+".class as CL WHERE "
															+" AT.name = '"+name+"' AND "
															+" AT.classID = CL.id AND "
															+" CL.name = '"+className+"'");
			
			if (rset.next()) {
				int classId = rset.getInt("classID");
				int id = rset.getInt("id");
				at = new SLEXAttribute(this);
				at.setId(id);
				at.setClassId(classId);
				at.setName(name);
				at.setDirty(false);
				at.setInserted(false);
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
			at = null;
		} finally {
			closeStatement(statement);
		}
		
		return at;
	}
	
	public SLEXAttribute findOrCreateAttribute(String className, String name, boolean common) {
		
		SLEXAttribute at = findAttribute(className, name);
		
		if (at == null) {
			at = createAttribute(className, name, common);
		}
		
		return at;
	}
	
	public SLEXAttribute createAttribute(String className, String name, boolean common) {
		SLEXAttribute at = new SLEXAttribute(this);
		at.setName(name);
		SLEXClass clazz = findClass(className);
		
		if (clazz == null) {
			clazz = createClass(className,common);
		}
		
		at.setClassId(clazz.getId());
		if (isAutoCommitOnCreationEnabled()) {
			at.commit();
		}
		return at;
	}
	
	public SLEXAttributeValue createAttributeValue(int attributeId, int eventId, String value) {
		SLEXAttributeValue av = new SLEXAttributeValue(this,attributeId,eventId);
		av.setValue(value);
		if (isAutoCommitOnCreationEnabled()) {
			av.commit();
		}
		return av;
	}
	
	public SLEXClass createClass(String name, boolean common) {
		SLEXClass cl = new SLEXClass(this,name,common);
		if (isAutoCommitOnCreationEnabled()) {
			cl.commit();
		}
		return cl;
	}
	
	public SLEXDMDataModel createDMDataModel(String name) {
		SLEXDMDataModel dm = new SLEXDMDataModel(this);
		dm.setName(name);
		if (isAutoCommitOnCreationEnabled()) {
			dm.commit();
		}
		return dm;
	}
	
	public SLEXDMClass createDMClass(int data_modelId, String name, boolean common) {
		SLEXDMClass cl = new SLEXDMClass(this, name, common, data_modelId);
		if (isAutoCommitOnCreationEnabled()) {
			cl.commit();
		}
		return cl;
	}
	
	public SLEXDMAttribute createDMAttribute(int classId, String name, boolean common) {
		SLEXDMAttribute at = new SLEXDMAttribute(this);
		at.setName(name);
		at.setClassId(classId);
		if (isAutoCommitOnCreationEnabled()) {
			at.commit();
		}
		return at;
	}
	
	public SLEXDMKey createDMKey(int classId, String name, int type, int refers_to_key) {
		SLEXDMKey k = new SLEXDMKey(this);
		k.setName(name);
		k.setClassId(classId);
		k.setType(type);
		if (type == SLEXDMKey.FOREIGN_KEY) {
			k.setRefersToKey(refers_to_key);
		}
		if (isAutoCommitOnCreationEnabled()) {
			k.commit();
		}
		return k;
	}
	
	public SLEXDMKeyAttribute createDMKeyAttribute(int keyId, int attributeId, int refersToAttributeId, int position) {
		SLEXDMKeyAttribute kat = new SLEXDMKeyAttribute(this, keyId, attributeId);
		kat.setRefersToId(refersToAttributeId);
		kat.setPosition(position);
		if (isAutoCommitOnCreationEnabled()) {
			kat.commit();
		}
		return kat;
	}
	
	private int getLastInsertedRowId(Statement stmnt) {
		int id = -1;
		ResultSet res = null;
		try {
			res = stmnt.getGeneratedKeys();
			if (res.next()) {
				id = res.getInt("last_insert_rowid()");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeResultSet(res);
		}
		return id;
	}
	
	protected boolean insert(SLEXEventCollection ec) {
		Statement statement = null;
		boolean result = false;
		try {
			statement = connection.createStatement();
			statement.setQueryTimeout(30);
			statement.execute("INSERT INTO "+COLLECTION_ALIAS+".collection (name) VALUES ('"+ec.getName()+"')");
			ec.setId(getLastInsertedRowId(statement));
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		} finally {
			closeStatement(statement);
		}
		
		return result;
	}
	
	protected boolean update(SLEXEventCollection ec) {
		Statement statement = null;
		boolean result = false;
		try {
			statement = connection.createStatement();
			statement.setQueryTimeout(30);
			statement.execute("UPDATE "+COLLECTION_ALIAS+".collection SET name =  '"+ec.getName()+"' WHERE id = '"+ec.getId()+"'");
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		} finally {
			closeStatement(statement);
		}
		
		return result;
	}
	
	protected boolean insert(SLEXEvent ev) {
		Statement statement = null;
		boolean result = false;
		try {
			statement = connection.createStatement();
			statement.setQueryTimeout(30);
			statement.execute("INSERT INTO "+COLLECTION_ALIAS+".event (collectionID) VALUES ('"+ev.getCollectionId()+"')");
			ev.setId(getLastInsertedRowId(statement));
			// TODO Insert all the attributes and attribute-values in the event, if any
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		} finally {
			closeStatement(statement);
		}
		
		return result;
	}
	
	protected boolean update(SLEXEvent ev) {
		Statement statement = null;
		boolean result = false;
		try {
			statement = connection.createStatement();
			statement.setQueryTimeout(30);
			statement.execute("UPDATE "+COLLECTION_ALIAS+".event SET collectionID = '"+ev.getCollectionId()+"' WHERE id = '"+ev.getId()+"'");
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		} finally {
			closeStatement(statement);
		}
		
		return result;
	}
	
	protected boolean insert(SLEXAttribute at) {
		Statement statement = null;
		boolean result = false;
		try {
			statement = connection.createStatement();
			statement.setQueryTimeout(30);
			statement.execute("INSERT INTO "+COLLECTION_ALIAS+".attribute (name,classID) VALUES ('"+at.getName()+"','"+at.getClassId()+"')");
			at.setId(getLastInsertedRowId(statement));
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		} finally {
			closeStatement(statement);
		}
		
		return result;
	}
	
	protected boolean update(SLEXAttribute at) {
		Statement statement = null;
		boolean result = false;
		try {
			statement = connection.createStatement();
			statement.setQueryTimeout(30);
			statement.execute("UPDATE "+COLLECTION_ALIAS+".attribute SET classID = '"+at.getClassId()+"',name = '"+at.getName()+"' WHERE id = '"+at.getId()+"'");
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		} finally {
			closeStatement(statement);
		}
		
		return result;
	}
	
	protected boolean insert(SLEXDMAttribute at) {
		Statement statement = null;
		boolean result = false;
		try {
			statement = connection.createStatement();
			statement.setQueryTimeout(30);
			statement.execute("INSERT INTO "+DATAMODEL_ALIAS+".attribute (name,classID) VALUES ('"+at.getName()+"','"+at.getClassId()+"')");
			at.setId(getLastInsertedRowId(statement));
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		} finally {
			closeStatement(statement);
		}
		
		return result;
	}
	
	protected boolean update(SLEXDMAttribute at) {
		Statement statement = null;
		boolean result = false;
		try {
			statement = connection.createStatement();
			statement.setQueryTimeout(30);
			statement.execute("UPDATE "+DATAMODEL_ALIAS+".attribute SET classID = '"+at.getClassId()+"',name = '"+at.getName()+"' WHERE id = '"+at.getId()+"'");
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		} finally {
			closeStatement(statement);
		}
		
		return result;
	}
	
	protected boolean insert(SLEXDMKey k) {
		Statement statement = null;
		boolean result = false;
		try {
			statement = connection.createStatement();
			statement.setQueryTimeout(30);
			statement.execute("INSERT INTO "+DATAMODEL_ALIAS+".key (name,classID,type,refers_to_key) VALUES ('"+k.getName()+"','"+k.getClassId()+"','"+k.getType()+"','"+k.getRefersToKey()+"')");
			k.setId(getLastInsertedRowId(statement));
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		} finally {
			closeStatement(statement);
		}
		
		return result;
	}
	
	protected boolean update(SLEXDMKey k) {
		Statement statement = null;
		boolean result = false;
		try {
			statement = connection.createStatement();
			statement.setQueryTimeout(30);
			statement.execute("UPDATE "+DATAMODEL_ALIAS+".key SET classID = '"+k.getClassId()+"',name = '"+k.getName()+"',type = '"+k.getType()+"',refers_to_key = '"+k.getRefersToKey()+"' WHERE id = '"+k.getId()+"'");
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		} finally {
			closeStatement(statement);
		}
		
		return result;
	}
	
	protected boolean insert(SLEXDMKeyAttribute ka) {
		Statement statement = null;
		boolean result = false;
		try {
			statement = connection.createStatement();
			statement.setQueryTimeout(30);
			String refersTo = null;
			if (ka.getRefersToId() == SLEXDMKeyAttribute.REFERS_TO_NULL) {
				refersTo = "NULL";
			} else {
				refersTo = ka.getRefersToId()+"";
			}
			statement.execute("INSERT INTO "+DATAMODEL_ALIAS+".key_attribute (keyID,attributeID,refersTo,position) VALUES ('"+ka.getKeyId()+"','"+ka.getAttributeId()+"','"+refersTo+"','"+ka.getPosition()+"')");
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		} finally {
			closeStatement(statement);
		}
		
		return result;
	}
	
	protected boolean update(SLEXDMKeyAttribute ka) {
		Statement statement = null;
		boolean result = false;
		try {
			statement = connection.createStatement();
			statement.setQueryTimeout(30);
			String refersTo = null;
			if (ka.getRefersToId() == SLEXDMKeyAttribute.REFERS_TO_NULL) {
				refersTo = "NULL";
			} else {
				refersTo = ka.getRefersToId()+"";
			}
			statement.execute("UPDATE "+DATAMODEL_ALIAS+".key_attribute SET refersTo = '"+refersTo+"', position = '"+ka.getPosition()+"' WHERE keyID = '"+ka.getKeyId()+"' AND attributeID = '"+ka.getAttributeId()+"'");
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		} finally {
			closeStatement(statement);
		}
		
		return result;
	}
	
	protected boolean insert(SLEXAttributeValue av) {
		PreparedStatement statement = null;
		boolean result = false;
		try {
			statement = connection.prepareStatement("INSERT INTO "+COLLECTION_ALIAS+".attribute_value (eventID,attributeID,value) VALUES (?,?,?)");
			statement.setQueryTimeout(30);
			statement.setInt(1, av.getEventId());
			statement.setInt(2, av.getAttributeId());
			statement.setString(3, av.getValue());
			statement.execute();
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		} finally {
			closeStatement(statement);
		}
		
		return result;
	}
	
	protected boolean update(SLEXAttributeValue av) {
		PreparedStatement statement = null;
		boolean result = false;
		try {
			statement = connection.prepareStatement("UPDATE "+COLLECTION_ALIAS+".attribute_value SET value = ? WHERE eventID = ? AND attributeID = ?");
			statement.setQueryTimeout(30);
			statement.setString(1, av.getValue());
			statement.setInt(2, av.getEventId());
			statement.setInt(3, av.getAttributeId());
			statement.execute();
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		} finally {
			closeStatement(statement);
		}
		
		return result;
	}
	
	protected boolean insert(SLEXClass cl) {
		Statement statement = null;
		boolean result = false;
		try {
			statement = connection.createStatement();
			statement.setQueryTimeout(30);
			statement.execute("INSERT INTO "+COLLECTION_ALIAS+".class (name,common) VALUES ('"+cl.getName()+"','"+cl.isCommon()+"')");
			cl.setId(getLastInsertedRowId(statement));
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		} finally {
			closeStatement(statement);
		}
		
		return result;
	}
	
	protected boolean update(SLEXClass cl) {
		Statement statement = null;
		boolean result = false;
		try {
			statement = connection.createStatement();
			statement.setQueryTimeout(30);
			statement.execute("UPDATE "+COLLECTION_ALIAS+".class SET name = '"+cl.getName()+"', common = '"+cl.isCommon()+"' WHERE id = '"+cl.getId()+"'");
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		} finally {
			closeStatement(statement);
		}
		
		return result;
	}
	
	protected boolean insert(SLEXDMClass cl) {
		Statement statement = null;
		boolean result = false;
		try {
			statement = connection.createStatement();
			statement.setQueryTimeout(30);
			statement.execute("INSERT INTO "+DATAMODEL_ALIAS+".class (name,common,data_modelID) VALUES ('"+cl.getName()+"','"+cl.isCommon()+"','"+cl.getDataModelId()+"')");
			cl.setId(getLastInsertedRowId(statement));
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		} finally {
			closeStatement(statement);
		}
		
		return result;
	}
	
	protected boolean update(SLEXDMClass cl) {
		Statement statement = null;
		boolean result = false;
		try {
			statement = connection.createStatement();
			statement.setQueryTimeout(30);
			statement.execute("UPDATE "+DATAMODEL_ALIAS+".class SET name = '"+cl.getName()+"', common = '"+cl.isCommon()+"', data_modelID = '"+cl.getDataModelId()+"' WHERE id = '"+cl.getId()+"'");
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		} finally {
			closeStatement(statement);
		}
		
		return result;
	}
	
	protected boolean insert(SLEXPerspective p) {
		Statement statement = null;
		boolean result = false;
		try {
			statement = connection.createStatement();
			statement.setQueryTimeout(30);
			statement.execute("INSERT INTO "+COLLECTION_ALIAS+".perspective (name,collectionID) VALUES ('"+p.getName()+"','"+p.getCollectionId()+"')");
			p.setId(getLastInsertedRowId(statement));
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		} finally {
			closeStatement(statement);
		}
		
		return result;
	}
	
	protected boolean update(SLEXPerspective p) {
		Statement statement = null;
		boolean result = false;
		try {
			statement = connection.createStatement();
			statement.setQueryTimeout(30);
			statement.execute("UPDATE "+COLLECTION_ALIAS+".perspective SET name = '"+p.getName()+"', collectionID = '"+p.getCollectionId()+"' WHERE id = '"+p.getId()+"'");
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		} finally {
			closeStatement(statement);
		}
		
		return result;
	}
	
	protected boolean insert(SLEXDMDataModel dm) {
		Statement statement = null;
		boolean result = false;
		try {
			statement = connection.createStatement();
			statement.setQueryTimeout(30);
			statement.execute("INSERT INTO "+DATAMODEL_ALIAS+".data_model (name) VALUES ('"+dm.getName()+"')");
			dm.setId(getLastInsertedRowId(statement));
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		} finally {
			closeStatement(statement);
		}
		
		return result;
	}
	
	protected boolean update(SLEXDMDataModel dm) {
		Statement statement = null;
		boolean result = false;
		try {
			statement = connection.createStatement();
			statement.setQueryTimeout(30);
			statement.execute("UPDATE "+DATAMODEL_ALIAS+".data_model SET name = '"+dm.getName()+"' WHERE id = '"+dm.getId()+"'");
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		} finally {
			closeStatement(statement);
		}
		
		return result;
	}
	
	protected boolean insert(SLEXTrace t) {
		Statement statement = null;
		boolean result = false;
		try {
			statement = connection.createStatement();
			statement.setQueryTimeout(30);
			statement.execute("INSERT INTO "+COLLECTION_ALIAS+".trace (caseID,perspectiveID) VALUES ('"+t.getCaseId()+"','"+t.getPerspectiveId()+"')");
			t.setId(getLastInsertedRowId(statement));
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		} finally {
			closeStatement(statement);
		}
		
		return result;
	}
	
	protected boolean update(SLEXTrace t) {
		Statement statement = null;
		boolean result = false;
		try {
			statement = connection.createStatement();
			statement.setQueryTimeout(30);
			statement.execute("UPDATE "+COLLECTION_ALIAS+".trace SET caseID = '"+t.getCaseId()+"', perspectiveID = '"+t.getPerspectiveId()+"' WHERE id = '"+t.getId()+"'");
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		} finally {
			closeStatement(statement);
		}
		
		return result;
	}
	
	public SLEXEventCollectionResultSet getEventCollections() {
		SLEXEventCollectionResultSet ecrset = null;
		Statement statement = null;
		try {
			statement = connection.createStatement();
			ResultSet rset = statement.executeQuery("SELECT * FROM "+COLLECTION_ALIAS+".collection");
			ecrset = new SLEXEventCollectionResultSet(this, rset);
		} catch (Exception e) {
			e.printStackTrace();
			closeStatement(statement);
		}
		
		return ecrset; 
	}

	public SLEXPerspectiveResultSet getPerspectives() {
		SLEXPerspectiveResultSet ecrset = null;
		Statement statement = null;
		try {
			statement = connection.createStatement();
			ResultSet rset = statement.executeQuery("SELECT * FROM "+COLLECTION_ALIAS+".perspective");
			ecrset = new SLEXPerspectiveResultSet(this, rset);
		} catch (Exception e) {
			e.printStackTrace();
			closeStatement(statement);
		}
		
		return ecrset; 
	}
	
	public SLEXPerspectiveResultSet getPerspectivesOfCollection(SLEXEventCollection ec) {
		SLEXPerspectiveResultSet ecrset = null;
		Statement statement = null;
		try {
			statement = connection.createStatement();
			ResultSet rset = statement.executeQuery("SELECT * FROM "+COLLECTION_ALIAS+".perspective WHERE collectionID = "+ec.getId());
			ecrset = new SLEXPerspectiveResultSet(this, rset);
		} catch (Exception e) {
			e.printStackTrace();
			closeStatement(statement);
		}
		
		return ecrset;
	}
	
	protected SLEXEventResultSet getEventsOfCollection(
			SLEXEventCollection ec) {
		SLEXEventResultSet erset = null;
		Statement statement = null;
		try {
			statement = connection.createStatement();
			ResultSet rset = statement.executeQuery("SELECT * FROM "+COLLECTION_ALIAS+".event WHERE collectionID = "+ec.getId());
			erset = new SLEXEventResultSet(this, rset);
		} catch (Exception e) {
			e.printStackTrace();
			closeStatement(statement);
		}
		
		return erset; 
	}
	
	protected SLEXEventResultSet getEventsOfCollectionOrderedBy(
			SLEXEventCollection ec,
			List<SLEXAttribute> orderAttributes) {
		SLEXEventResultSet erset = null;
		Statement statement = null;
		try {
			statement = connection.createStatement();
			String wherequery = " WHERE E.collectionID = "+ec.getId();
			String orderquery = " ORDER BY ";
			String query = 			
					" SELECT * FROM "+COLLECTION_ALIAS+".event AS E ";
			for (int i=0;i<orderAttributes.size();i++) {
				query += " ,"+COLLECTION_ALIAS+".attribute_value AS ATV"+i+" ";
				wherequery += " AND ATV"+i+".eventID = E.id "+
						 " AND ATV"+i+".attributeID = "+orderAttributes.get(i).getId()+" ";
				orderquery += " CAST(ATV"+i+".value AS INTEGER) ";
				if (i < orderAttributes.size()-1) {
					orderquery += ", ";
				}
			}
			query += wherequery + orderquery;
			ResultSet rset = statement.executeQuery(query);
			erset = new SLEXEventResultSet(this, rset);
		} catch (Exception e) {
			e.printStackTrace();
			closeStatement(statement);
		}
		
		return erset;
	}

	protected Hashtable<SLEXAttribute, SLEXAttributeValue> getAttributeValuesForEvent(
			SLEXEvent ev) {
		Hashtable<SLEXAttribute, SLEXAttributeValue> attributeValues = null;
		Statement statement = null;
		try {
			statement = connection.createStatement();
			statement.setQueryTimeout(30);
			ResultSet rset = statement.executeQuery("SELECT AT.classID, AT.id, AT.name, ATV.value FROM "+
														COLLECTION_ALIAS+".attribute AS AT, "+
														COLLECTION_ALIAS+".attribute_value AS ATV, "+
														COLLECTION_ALIAS+".event AS EV "+
														"WHERE EV.id = "+ev.getId()+" AND ATV.eventID = EV.id AND "
																+ " ATV.attributeID = AT.id ");
			attributeValues = new Hashtable<>();
			
			while (rset.next()) {
				SLEXAttribute at = new SLEXAttribute(this);
				at.setClassId(rset.getInt(1));
				at.setId(rset.getInt(2));
				at.setName(rset.getString(3));
				at.setDirty(false);
				at.setInserted(true);
				SLEXAttributeValue atv = new SLEXAttributeValue(this, at.getId(), ev.getId());
				atv.setValue(rset.getString(4));
				atv.setDirty(false);
				atv.setInserted(true);
				attributeValues.put(at, atv);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			attributeValues = null;
		} finally {
			closeStatement(statement);
		}
		return attributeValues;
	}
	

	
	public SLEXDMClassResultSet getClassesForDataModel(SLEXDMDataModel dm) {
		SLEXDMClassResultSet crset = null;
		Statement statement = null;
		try {
			statement = connection.createStatement();
			ResultSet rset = statement.executeQuery("SELECT * FROM "+DATAMODEL_ALIAS+".class WHERE data_modelID = "+dm.getId());
			crset = new SLEXDMClassResultSet(this, rset);
		} catch (Exception e) {
			e.printStackTrace();
			closeStatement(statement);
		}
		
		return crset;
	}
	
	public SLEXDMDataModelResultSet getDataModels() {
		SLEXDMDataModelResultSet dmrset = null;
		Statement statement = null;
		try {
			statement = connection.createStatement();
			ResultSet rset = statement.executeQuery("SELECT * FROM "+DATAMODEL_ALIAS+".data_model");
			dmrset = new SLEXDMDataModelResultSet(this, rset);
		} catch (Exception e) {
			e.printStackTrace();
			closeStatement(statement);
		}
		
		return dmrset;
	}
	


	public List<SLEXDMAttribute> getAttributesForDMClass(SLEXDMClass cl) {
		List<SLEXDMAttribute> atList = new Vector<>();
		Statement statement = null;
		
		try {
			statement = connection.createStatement();
			ResultSet rset = statement.executeQuery("SELECT * FROM "+DATAMODEL_ALIAS+".attribute WHERE classID = "+cl.getId());
			while (rset.next()) {
				int id = rset.getInt("id");
				String name = rset.getString("name");
				int classId = rset.getInt("classID");
				SLEXDMAttribute at = new SLEXDMAttribute(this);
				at.setId(id);
				at.setName(name);
				at.setClassId(classId);
				at.setDirty(false);
				at.setInserted(true);
				atList.add(at);
			}
		} catch (Exception e) {
			e.printStackTrace();
			atList = null;
		} finally {
			closeStatement(statement);
		}
		return atList;
	}
	
	public List<SLEXDMKey> getKeysForDMClass(SLEXDMClass cl) {
		List<SLEXDMKey> kList = new Vector<>();
		Statement statement = null;
		
		try {
			statement = connection.createStatement();
			ResultSet rset = statement.executeQuery("SELECT * FROM "+DATAMODEL_ALIAS+".key WHERE classID = "+cl.getId());
			while (rset.next()) {
				int id = rset.getInt("id");
				String name = rset.getString("name");
				int classId = rset.getInt("classID");
				int type = rset.getInt("type");
				int refers_to_key = rset.getInt("refers_to_key");
				SLEXDMKey k = new SLEXDMKey(this);
				k.setId(id);
				k.setName(name);
				k.setClassId(classId);
				k.setType(type);
				k.setRefersToKey(refers_to_key);
				k.setDirty(false);
				k.setInserted(true);
				kList.add(k);
			}
		} catch (Exception e) {
			e.printStackTrace();
			kList = null;
		} finally {
			closeStatement(statement);
		}
		return kList;
	}
	
	public List<SLEXDMKeyAttribute> getKeyAttributesForDMKey(SLEXDMKey k) {
		List<SLEXDMKeyAttribute> katList = new Vector<>();
		Statement statement = null;
		
		try {
			statement = connection.createStatement();
			ResultSet rset = statement.executeQuery("SELECT * FROM "+DATAMODEL_ALIAS+".key_attribute WHERE keyID = "+k.getId());
			while (rset.next()) {
				int keyId = rset.getInt("keyID");
				int attributeId = rset.getInt("attributeID");
				int refersTo = rset.getInt("refersTo");
				int position = rset.getInt("position");
				SLEXDMKeyAttribute kat = new SLEXDMKeyAttribute(this,keyId,attributeId);
				kat.setRefersToId(refersTo);
				kat.setPosition(position);
				kat.setDirty(false);
				kat.setInserted(true);
				katList.add(kat);
			}
		} catch (Exception e) {
			e.printStackTrace();
			katList = null;
		} finally {
			closeStatement(statement);
		}
		return katList;
	}
	
	protected SLEXEventResultSet getEventsOfTrace(SLEXTrace t) {
		SLEXEventResultSet erset = null;
		Statement statement = null;
		try {
			statement = connection.createStatement();
			ResultSet rset = statement.executeQuery("SELECT * FROM "+COLLECTION_ALIAS+".event AS E, "+COLLECTION_ALIAS+".trace_has_event AS TE WHERE E.id = TE.eventID AND TE.traceID='"+t.getId()+"' ORDER BY TE.ordering");
			erset = new SLEXEventResultSet(this, rset);
		} catch (Exception e) {
			e.printStackTrace();
			closeStatement(statement);
		}
		
		return erset; 
	}

	protected int getNumberEventsOfTrace(SLEXTrace t) {
		// TEST
		int events = 0;
		Statement statement = null;
		try {
			statement = connection.createStatement();
			ResultSet rset = statement.executeQuery("SELECT count(*) FROM "+COLLECTION_ALIAS+".event AS E, "+COLLECTION_ALIAS+".trace_has_event AS TE WHERE E.id = TE.eventID AND TE.traceID='"+t.getId()+"'");
			if (rset.next()) {
				events = rset.getInt(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			closeStatement(statement);
		}
		return events;
	}
	
	public SLEXTrace createTrace(int perspectiveId, String caseId) {
		SLEXTrace t = new SLEXTrace(this);
		t.setPerspectiveId(perspectiveId);
		t.setCaseId(caseId);
		if (isAutoCommitOnCreationEnabled()) {
			t.commit();
		}
		return t;
	}

	public SLEXPerspective createPerspective(SLEXEventCollection evCol,
			String name) {
		SLEXPerspective p = new SLEXPerspective(this);
		p.setCollectionId(evCol.getId());
		p.setName(name);
		if (isAutoCommitOnCreationEnabled()) {
			p.commit();
		}
		return p;
	}

	public SLEXTrace cloneTrace(SLEXTrace t) {
		SLEXTrace ct = this.createTrace(t.getPerspectiveId(), t.getCaseId());		
		
		Statement statement = null;
		try {
			statement = connection.createStatement();
			//statement.setQueryTimeout(30);
			statement.execute("INSERT INTO "+COLLECTION_ALIAS+".trace_has_event (traceID,eventID,ordering) "+
							" SELECT '"+ct.getId()+"', eventID, ordering FROM "+COLLECTION_ALIAS+".trace_has_event "+
							" WHERE traceID = '"+t.getId()+"' ");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeStatement(statement);
		}
		
		return ct;
	}

	protected boolean addEventToTrace(SLEXTrace t, SLEXEvent e) {
		Statement statement = null;
		boolean result = false;
		try {
			statement = connection.createStatement();
			statement.setQueryTimeout(30);
			statement.execute("INSERT INTO "+COLLECTION_ALIAS+".trace_has_event (traceID,eventID,ordering) VALUES ('"+t.getId()+"','"+e.getId()+"',(SELECT IFNULL(MAX(ordering), 0) + 1 FROM "+COLLECTION_ALIAS+".trace_has_event))");
			result = true;
		} catch (Exception ex) {
			ex.printStackTrace();
			result = false;
		} finally {
			closeStatement(statement);
		}
		
		return result;
	}

	private boolean removeEventsFromTrace(SLEXTrace t) {
		Statement statement = null;
		boolean result = false;
		try {
			statement = connection.createStatement();
			statement.setQueryTimeout(30);
			statement.execute("DELETE FROM "+COLLECTION_ALIAS+".trace_has_event WHERE traceID = '"+t.getId()+"'");
			result = true;
		} catch (Exception ex) {
			ex.printStackTrace();
			result = false;
		} finally {
			closeStatement(statement);
		}
		
		return result;
	}
	
	protected boolean removeTraceFromPerspective(SLEXPerspective p,
			SLEXTrace t) {
		Statement statement = null;
		boolean result = false;
		try {
			result = removeEventsFromTrace(t);
			statement = connection.createStatement();
			statement.setQueryTimeout(30);
			statement.execute("DELETE FROM "+COLLECTION_ALIAS+".trace WHERE id = '"+t.getId()+"'");
		} catch (Exception ex) {
			ex.printStackTrace();
			result = false;
		} finally {
			closeStatement(statement);
		}
		
		return result;
	}

	protected SLEXTraceResultSet getTracesOfPerspective(
			SLEXPerspective p) {
		SLEXTraceResultSet trset = null;
		Statement statement = null;
		try {
			statement = connection.createStatement();
			ResultSet rset = statement.executeQuery("SELECT * FROM "+COLLECTION_ALIAS+".trace WHERE perspectiveID = "+p.getId());
			trset = new SLEXTraceResultSet(this, rset);
		} catch (Exception e) {
			e.printStackTrace();
			closeStatement(statement);
		}
		
		return trset; 
	}
	
}
