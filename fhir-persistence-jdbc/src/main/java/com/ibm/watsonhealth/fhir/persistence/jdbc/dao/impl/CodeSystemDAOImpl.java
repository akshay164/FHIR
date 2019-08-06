/**
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.watsonhealth.fhir.persistence.jdbc.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.watsonhealth.fhir.persistence.exception.FHIRPersistenceException;
import com.ibm.watsonhealth.fhir.persistence.jdbc.dao.api.CodeSystemDAO;
import com.ibm.watsonhealth.fhir.persistence.jdbc.exception.FHIRPersistenceDataAccessException;

/**
 * Refactor of the normalized DAO implementation which focuses on the
 * database interaction for parameter_names. Caching etc is handled
 * elsewhere...we're just doing JDBC stuff here.
 * 
 * This DAO uses a connection provided to its constructor. It's therefore
 * assumed to be a short-lived object, created on-the-fly
 */
public class CodeSystemDAOImpl implements CodeSystemDAO {
    private static final Logger log = Logger.getLogger(ParameterDAONormalizedImpl.class.getName());
    private static final String CLASSNAME = ParameterDAONormalizedImpl.class.getName(); 
    
    public static final String DEFAULT_TOKEN_SYSTEM = "default-token-system";
        
    private static final String SQL_CALL_ADD_CODE_SYSTEM_ID = "CALL %s.add_code_system(?, ?)";

    private static final String SQL_SELECT_ALL_CODE_SYSTEMS = "SELECT CODE_SYSTEM_ID, CODE_SYSTEM_NAME FROM CODE_SYSTEMS";
    
    private static final String SQL_SELECT_CODE_SYSTEM_ID = "SELECT CODE_SYSTEM_ID FROM CODE_SYSTEMS WHERE CODE_SYSTEM_NAME = ?";

    // The JDBC connection used by this DAO instance
    private final Connection connection;
    
    /**
     * Constructs a DAO instance suitable for acquiring connections from a JDBC Datasource object.
     */
    public CodeSystemDAOImpl(Connection c) {
        this.connection = c;
    }
    
    @Override
    public Map<String, Integer> readAllCodeSystems() throws FHIRPersistenceDataAccessException {
        final String METHODNAME = "readAllCodeSystems";
        log.entering(CLASSNAME, METHODNAME);
                
        ResultSet resultSet = null;
        String systemName;
        int systemId;
        Map<String, Integer> systemMap = new HashMap<>();
        String errMsg = "Failure retrieving all code systems.";
        long dbCallStartTime;
        double dbCallDuration;
                
        dbCallStartTime = System.nanoTime();
        try (PreparedStatement stmt = connection.prepareStatement(SQL_SELECT_ALL_CODE_SYSTEMS)) {
            resultSet = stmt.executeQuery();
            dbCallDuration = (System.nanoTime()-dbCallStartTime)/1e6;
            if (log.isLoggable(Level.FINE)) {
                log.fine("DB read all code systems complete. executionTime=" + dbCallDuration + "ms");
            }
            while (resultSet.next()) {
                systemId = resultSet.getInt(1);
                systemName = resultSet.getString(2);
                systemMap.put(systemName, systemId);
            }
        }
        catch (Throwable e) {
            throw new FHIRPersistenceDataAccessException(errMsg,e);
        }
        finally {
            log.exiting(CLASSNAME, METHODNAME);
        }
                
        return systemMap;
    }
    
    /**
     * Calls a stored procedure to read the system contained in the passed Parameter in the Code_Systems table.
     * If it's not in the DB, it will be stored and a unique id will be returned.
     * @param parameter
     * @return Integer - The generated id of the stored system.
     * @throws FHIRPersistenceDBConnectException 
     * @throws FHIRPersistenceDataAccessException 
     * @throws FHIRPersistenceException
     */
    @Override
    public Integer readOrAddCodeSystem(String systemName) throws FHIRPersistenceDataAccessException   {
        final String METHODNAME = "readOrAddCodeSystem";
        log.entering(CLASSNAME, METHODNAME);
        
        Integer systemId = null;
        String currentSchema;
        String stmtString;
        String errMsg = "Failure storing code system id: name=" + systemName;
        long dbCallStartTime;
        double dbCallDuration;
                
        try {
            currentSchema = connection.getSchema().trim();
            stmtString = String.format(SQL_CALL_ADD_CODE_SYSTEM_ID, currentSchema);
            try (CallableStatement stmt = connection.prepareCall(stmtString)) {
                stmt.setString(1, systemName);
                stmt.registerOutParameter(2, Types.INTEGER);
                dbCallStartTime = System.nanoTime();
                stmt.execute();
                dbCallDuration = (System.nanoTime()-dbCallStartTime)/1e6;
                if (log.isLoggable(Level.FINE)) {
                        log.fine("DB read code system id complete. executionTime=" + dbCallDuration + "ms");
                }
                systemId = stmt.getInt(2);
            }
        }
        catch (Throwable e) {
            throw new FHIRPersistenceDataAccessException(errMsg,e);
        } 
        finally {
            log.exiting(CLASSNAME, METHODNAME);
        }
        return systemId;
    }

    /* (non-Javadoc)
     * @see com.ibm.watsonhealth.fhir.persistence.jdbc.dao.api.CodeSystemDAO#readCodeSystemId(java.lang.String)
     */
    @Override
    public Integer readCodeSystemId(String codeSystem) throws FHIRPersistenceDataAccessException {
        final String METHODNAME = "readCodeSystemId";
        log.entering(CLASSNAME, METHODNAME);
                
        Integer result;
        ResultSet resultSet = null;
        String errMsg = "Failure retrieving code system. name=" + codeSystem;
        long dbCallStartTime;
        double dbCallDuration;
                
        try (PreparedStatement stmt = connection.prepareStatement(SQL_SELECT_CODE_SYSTEM_ID)) {
            stmt.setString(1, codeSystem);
            dbCallStartTime = System.nanoTime();
            resultSet = stmt.executeQuery();
            dbCallDuration = (System.nanoTime()-dbCallStartTime)/1e6;
            if (log.isLoggable(Level.FINE)) {
                log.fine("DB read code system complete. executionTime=" + dbCallDuration + "ms");
            }
            
            if (resultSet.next()) {
                result = resultSet.getInt(1);
            }
            else {
                result = null;
            }
        }
        catch (Throwable e) {
            throw new FHIRPersistenceDataAccessException(errMsg,e);
        }
        finally {
            log.exiting(CLASSNAME, METHODNAME);
        }
                
        return result;
    }
}