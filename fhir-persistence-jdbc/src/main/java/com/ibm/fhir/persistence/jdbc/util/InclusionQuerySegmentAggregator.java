/*
 * (C) Copyright IBM Corp. 2018, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.persistence.jdbc.util;

import static com.ibm.fhir.persistence.jdbc.JDBCConstants.AND;
import static com.ibm.fhir.persistence.jdbc.JDBCConstants.COMBINED_RESULTS;
import static com.ibm.fhir.persistence.jdbc.JDBCConstants.COMMA;
import static com.ibm.fhir.persistence.jdbc.JDBCConstants.LEFT_PAREN;
import static com.ibm.fhir.persistence.jdbc.JDBCConstants.QUOTE;
import static com.ibm.fhir.persistence.jdbc.JDBCConstants.RIGHT_PAREN;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.ibm.fhir.persistence.exception.FHIRPersistenceException;
import com.ibm.fhir.persistence.jdbc.dao.api.ParameterDAO;
import com.ibm.fhir.persistence.jdbc.dao.api.ResourceDAO;
import com.ibm.fhir.search.parameters.InclusionParameter;

/**
 * This class assists the JDBCQueryBuilder. It extends the
 * QuerySegmentAggregator to build a FHIR Resource query
 * that processes _include and _revinclude search result parameters. Using the
 * query segments built by the query builder,
 * this class augments those query segments to result in a query that includes
 * other resources as determined by
 * InclusionParameter objects.
 */
public class InclusionQuerySegmentAggregator extends QuerySegmentAggregator {
    private static final String CLASSNAME = InclusionQuerySegmentAggregator.class.getName();
    private static final Logger log = java.util.logging.Logger.getLogger(CLASSNAME);

    private static final String SELECT_COUNT_ROOT = "SELECT COUNT(RESOURCE_ID) FROM ";
    private static final String SELECT_ROOT =
            "SELECT RESOURCE_ID, LOGICAL_RESOURCE_ID, VERSION_ID, LAST_UPDATED, IS_DELETED, DATA, LOGICAL_ID FROM ";
    private static final String UNION_ALL = " UNION ALL ";
    private static final String REVINCLUDE_JOIN_START =
            "JOIN ";
    private static final String REVINCLUDE_JOIN_END =
            "_STR_VALUES P1 ON P1.LOGICAL_RESOURCE_ID = R.LOGICAL_RESOURCE_ID ";
    private static final String ORDERING = " ORDER BY R.LOGICAL_RESOURCE_ID ASC ";

    private List<InclusionParameter> includeParameters;
    private List<InclusionParameter> revIncludeParameters;

    protected InclusionQuerySegmentAggregator(Class<?> resourceType, int offset, int pageSize,
            ParameterDAO parameterDao, ResourceDAO resourceDao,
            List<InclusionParameter> includeParameters, List<InclusionParameter> revIncludeParameters) {
        super(resourceType, offset, pageSize, parameterDao, resourceDao);
        this.includeParameters    = includeParameters;
        this.revIncludeParameters = revIncludeParameters;
    }

    /**
     * This methods builds a query to return the count of resources matching the
     * search. This count will encompass resources
     * represented by _include and _revinclude parameters.
     * <p>
     * The following generated SQL is an example. It is based on this REST query
     * string:
     * {@code /Patient?id=some-id&_include=Patient:organization&_revinclude=Observation:patient}
     * <p>
     * See comments embedded in the SQL for explanation:
     * 
     * <pre>
       SELECT COUNT(RESOURCE_ID) FROM 
       -- The following SELECT is the query for the "target" Patient resource. It is generated by superclass methods.
           (SELECT R.RESOURCE_ID, R.LOGICAL_RESOURCE_ID, R.VERSION_ID, R.LAST_UPDATED, R.IS_DELETED, R.DATA, LR.LOGICAL_ID FROM 
          Patient_RESOURCES R JOIN 
          Patient_LOGICAL_RESOURCES LR ON R.LOGICAL_RESOURCE_ID=LR.LOGICAL_RESOURCE_ID JOIN 
          Patient_TOKEN_VALUES P1 ON P1.LOGICAL_RESOURCE_ID=LR.LOGICAL_RESOURCE_ID WHERE 
          R.IS_DELETED <> 'Y' AND 
          P1.RESOURCE_ID = R.RESOURCE_ID AND 
          (P1.PARAMETER_NAME_ID=1 AND ((P1.TOKEN_VALUE = ?))) 
       
       UNION ALL 
       
       -- The following part of the overall query is built by the processInlcudeParameters() method.
       -- The following SELECT is generated in order to retrieve the desired included Organization resources that are
       -- referenced by the target Patient resources.
       SELECT R.RESOURCE_ID, R.LOGICAL_RESOURCE_ID, R.VERSION_ID, R.LAST_UPDATED, R.IS_DELETED, R.DATA, LR.LOGICAL_ID FROM 
         Organization_RESOURCES R JOIN 
         Organization_LOGICAL_RESOURCES LR ON R.LOGICAL_RESOURCE_ID=LR.LOGICAL_RESOURCE_ID WHERE 
         R.IS_DELETED <> 'Y' AND 
         R.RESOURCE_ID = LR.CURRENT_RESOURCE_ID AND 
         ('Organization/' || LR.LOGICAL_ID IN 
           (SELECT P1.STR_VALUE FROM 
            Patient_STR_VALUES P1 WHERE 
            P1.PARAMETER_NAME_ID=19 AND 
            P1.RESOURCE_ID IN 
            (SELECT R.RESOURCE_ID FROM 
             Patient_RESOURCES R JOIN 
             Patient_LOGICAL_RESOURCES LR ON R.LOGICAL_RESOURCE_ID=LR.LOGICAL_RESOURCE_ID JOIN 
             Patient_TOKEN_VALUES P1 ON P1.LOGICAL_RESOURCE_ID=R.LOGICAL_RESOURCE_ID WHERE 
             R.IS_DELETED <> 'Y' AND 
             P1.RESOURCE_ID = R.RESOURCE_ID AND 
             (P1.PARAMETER_NAME_ID=1 AND ((P1.TOKEN_VALUE = ?)))
            )
           )
         ) 
         
         UNION ALL 
    
         -- The following part of the overall query is built by the processRevInlcudeParameters() method.
         -- The following SELECT is generated in order to retrieve the desired reverse included Observation resources 
         -- that reference the target Patient resources.
         SELECT R.RESOURCE_ID, R.LOGICAL_RESOURCE_ID, R.VERSION_ID, R.LAST_UPDATED, R.IS_DELETED, R.DATA, LR.LOGICAL_ID FROM 
          Observation_RESOURCES R JOIN 
          Observation_LOGICAL_RESOURCES LR ON R.LOGICAL_RESOURCE_ID=LR.LOGICAL_RESOURCE_ID JOIN  
          Observation_STR_VALUES P1 ON P1.RESOURCE_ID = R.RESOURCE_ID WHERE 
          R.IS_DELETED <> 'Y' AND 
          P1.PARAMETER_NAME_ID=29 AND 
          P1.STR_VALUE IN 
           (SELECT 'Patient/' || LR.LOGICAL_ID FROM 
            Patient_RESOURCES R JOIN 
            Patient_LOGICAL_RESOURCES LR ON R.LOGICAL_RESOURCE_ID=LR.LOGICAL_RESOURCE_ID JOIN 
            Patient_TOKEN_VALUES P1 ON P1.RESOURCE_ID=R.RESOURCE_ID WHERE 
            R.IS_DELETED <> 'Y' AND 
            P1.RESOURCE_ID = R.RESOURCE_ID AND 
            (P1.PARAMETER_NAME_ID=1 AND ((P1.TOKEN_VALUE = ?)))
           )
         ) 
       COMBINED_RESULTS
     * </pre>
     */
    @Override
    protected SqlQueryData buildCountQuery() throws Exception {
        final String METHODNAME = "buildCountQuery";
        log.entering(CLASSNAME, METHODNAME);

        StringBuilder queryString = new StringBuilder();
        queryString.append(SELECT_COUNT_ROOT);
        queryString.append(LEFT_PAREN);
        queryString.append(QuerySegmentAggregator.SELECT_ROOT);
        buildFromClause(queryString, resourceType.getSimpleName());

        // An important step here is to add _id and _lastUpdated and then
        // the regular bind variables. 
        List<Object> allBindVariables = new ArrayList<>();
        allBindVariables.addAll(idsObjects);
        allBindVariables.addAll(lastUpdatedObjects);
        this.addBindVariables(allBindVariables);

        // Add the Where Clause
        buildWhereClause(queryString, null);
        queryString.append(COMBINED_RESULTS);

        SqlQueryData queryData = new SqlQueryData(queryString.toString(), allBindVariables);
        log.exiting(CLASSNAME, METHODNAME);
        return queryData;
    }

    /**
     * This methods builds a query to return the resources which are the target of
     * the search, along with other resources
     * specified by _include and rev_include parameters. The SQL generated by this
     * method is the same as that generated
     * by the buildCountQuery() method with the following exceptions:
     * 1. The "root" SELECT selects individual columns instead of a COUNT.
     * 2. Pagination clauses are added to the end of the query.
     * 
     * @see The javadoc for the buildCountQuery() method for an example of the
     *      generated SQL along with a detailed
     *      explanation.
     */
    @Override
    protected SqlQueryData buildQuery() throws Exception {
        final String METHODNAME = "buildQuery";
        log.entering(CLASSNAME, METHODNAME);

        StringBuilder queryString = new StringBuilder();
        queryString.append(InclusionQuerySegmentAggregator.SELECT_ROOT).append(LEFT_PAREN);
        queryString.append(InclusionQuerySegmentAggregator.SELECT_ROOT).append(LEFT_PAREN);
        queryString.append(QuerySegmentAggregator.SELECT_ROOT);

        buildFromClause(queryString, resourceType.getSimpleName());

        // An important step here is to add _id and _lastUpdated
        // then add the regular bind variables.
        List<Object> allBindVariables = new ArrayList<>();
        allBindVariables.addAll(idsObjects);
        allBindVariables.addAll(lastUpdatedObjects);
        this.addBindVariables(allBindVariables);

        buildWhereClause(queryString, null);

        // Add ordering
        queryString.append(ORDERING);
        this.addPaginationClauses(queryString);
        queryString.append(") RESULT ");
        this.processIncludeParameters(queryString, allBindVariables);
        this.processRevIncludeParameters(queryString, allBindVariables);

        queryString.append(COMBINED_RESULTS);

        SqlQueryData queryData = new SqlQueryData(queryString.toString(), allBindVariables);
        log.exiting(CLASSNAME, METHODNAME);
        return queryData;
    }

    /**
     * Appends values like
     * ({@code ('Patient/<resource_id>', 'Patient/<resource_id>' ...)}) to the
     * queryString
     */
    private void executeIncludeSubQuery(StringBuilder queryString, InclusionParameter includeParm,
            List<Object> bindVariables) throws Exception {
        StringBuilder subQueryString = new StringBuilder();
        // SELECT P1.STR_VALUE FROM OBSERVATION_STR_VALUES P1 WHERE
        subQueryString.append("SELECT P1.STR_VALUE FROM ")
                .append(this.resourceType.getSimpleName())
                .append("_STR_VALUES P1 WHERE ");

        // P1.PARAMETER_NAME_ID=xx AND 
        subQueryString.append("P1.PARAMETER_NAME_ID=")
                .append(this.getParameterNameId(includeParm.getSearchParameter()))
                .append(AND);

        // P1.LOGICAL_RESOURCE_ID IN 
        subQueryString.append("P1.LOGICAL_RESOURCE_ID IN ");
        // (SELECT R.LOGICAL_RESOURCE_ID  
        subQueryString.append("(SELECT R.LOGICAL_RESOURCE_ID ");

        // Add FROM clause for "root" resource type
        buildFromClause(subQueryString, resourceType.getSimpleName());

        // Add WHERE clause for "root" resource type
        buildWhereClause(subQueryString, null);

        // ORDER BY R.LOGICAL_RESOURCE_ID ASC
        subQueryString.append(ORDERING);
        // Only include resources related to the required page of the main resources.
        this.addPaginationClauses(subQueryString);
        subQueryString.append(RIGHT_PAREN);

        queryString.append(LEFT_PAREN);
        //The subquery should return a list of strings in the FHIR Reference String value format 
        //(e.g. {@code "Patient/<resource_id>"})
        SqlQueryData subQueryData = new SqlQueryData(subQueryString.toString(), bindVariables);

        boolean isFirstItem = true;
        for (String strValue : this.resourceDao.searchStringValues(subQueryData)) {
            if (!isFirstItem) {
                queryString.append(COMMA);
            }
            if (strValue != null) {
                queryString.append(QUOTE).append(SqlParameterEncoder.encode(strValue)).append(QUOTE);
                isFirstItem = false;
            }
        }

        // if nothing added so far, then need to add '', otherwise sql will fail. 
        if (isFirstItem) {
            queryString.append(QUOTE).append(QUOTE);
        }
        queryString.append(RIGHT_PAREN);
    }

    /*
     * Formats the FROM clause instead of assembling a String MessageFormat
     * The code here is just building as part of the StringBuilder.
     * 
     * @param queryString the non-null StringBuilder
     * 
     * @param target is the Target Type for the search
     */
    private void processFromClause(StringBuilder queryString, String target) {
        queryString.append("FROM ");
        queryString.append(target);
        queryString.append("_RESOURCES R JOIN ");
        queryString.append(target);
        queryString.append(
                "_LOGICAL_RESOURCES LR ON R.LOGICAL_RESOURCE_ID=LR.LOGICAL_RESOURCE_ID AND R.RESOURCE_ID = LR.CURRENT_RESOURCE_ID ");

    }

    private void processIncludeParameters(StringBuilder queryString, List<Object> bindVariables) throws Exception {
        final String METHODNAME = "processIncludeParameters";
        log.entering(CLASSNAME, METHODNAME);

        for (InclusionParameter includeParm : this.includeParameters) {
            // UNION ALL
            queryString.append(UNION_ALL);
            // SELECT R.RESOURCE_ID, R.LOGICAL_RESOURCE_ID, R.VERSION_ID, R.LAST_UPDATED, R.IS_DELETED, R.DATA, LR.LOGICAL_ID 
            queryString.append(QuerySegmentAggregator.SELECT_ROOT);
            // FROM Organization_RESOURCES R JOIN Organization_LOGICAL_RESOURCES LR ON R.LOGICAL_RESOURCE_ID=LR.LOGICAL_RESOURCE_ID
            processFromClause(queryString, includeParm.getSearchParameterTargetType());
            // WHERE R.IS_DELETED <> 'Y' AND
            queryString.append(QuerySegmentAggregator.WHERE_CLAUSE_ROOT).append(" AND ");
            // R.RESOURCE_ID = LR.CURRENT_RESOURCE_ID AND
            queryString.append("R.RESOURCE_ID = LR.CURRENT_RESOURCE_ID AND ");
            // ('Organization/' || LR.LOGICAL_ID IN 
            queryString.append("('").append(includeParm.getSearchParameterTargetType())
                    .append("/' || LR.LOGICAL_ID IN ");

            // Execute sub query to get the string values for constructing the query string.
            // This avoids DB engine to run this sub query once for each record in the previously joined tables.
            executeIncludeSubQuery(queryString, includeParm, bindVariables);
            queryString.append(RIGHT_PAREN);
        }
        log.exiting(CLASSNAME, METHODNAME);
    }

    private void processRevIncludeParameters(StringBuilder queryString, List<Object> bindVariables) throws Exception {
        final String METHODNAME = "processRevIncludeParameters";
        log.entering(CLASSNAME, METHODNAME);

        for (InclusionParameter includeParm : this.revIncludeParameters) {
            // UNION ALL
            queryString.append(UNION_ALL);
            // SELECT R.RESOURCE_ID, R.LOGICAL_RESOURCE_ID, R.VERSION_ID, R.LAST_UPDATED, R.IS_DELETED, R.DATA, LR.LOGICAL_ID 
            queryString.append(QuerySegmentAggregator.SELECT_ROOT);
            // FROM Observation_RESOURCES R JOIN Observation_LOGICAL_RESOURCES LR ON R.LOGICAL_RESOURCE_ID=LR.LOGICAL_RESOURCE_ID
            processFromClause(queryString, includeParm.getJoinResourceType());
            // JOIN Observation_STR_VALUES P1 ON P1.RESOURCE_ID = R.RESOURCE_ID
            queryString.append(REVINCLUDE_JOIN_START);
            queryString.append(includeParm.getJoinResourceType());
            queryString.append(REVINCLUDE_JOIN_END);
            // WHERE R.IS_DELETED <> 'Y' AND
            queryString.append(QuerySegmentAggregator.WHERE_CLAUSE_ROOT).append(" AND ");
            // P1.PARAMETER_NAME_ID=xx AND 
            queryString.append("P1.PARAMETER_NAME_ID=")
                    .append(this.getParameterNameId(includeParm.getSearchParameter())).append(" AND ");
            // P1.STR_VALUE IN 
            queryString.append("P1.STR_VALUE IN ");
            // (SELECT 'Patient/' || LR.LOGICAL_ID
            queryString.append("(SELECT '").append(includeParm.getSearchParameterTargetType())
                    .append("/' || LR.LOGICAL_ID ");

            // Add FROM clause for "root" resource type
            buildFromClause(queryString, resourceType.getSimpleName());

            // An important step here is to add _id and _lastUpdated
            bindVariables.addAll(this.idsObjects);
            bindVariables.addAll(this.lastUpdatedObjects);

            // Add WHERE clause for "root" resource type
            buildWhereClause(queryString, null);

            // ORDER BY R.LOGICAL_RESOURCE_ID ASC
            queryString.append(ORDERING);
            // Only include resources related to the required page of the main resources.
            this.addPaginationClauses(queryString);

            queryString.append(RIGHT_PAREN);

            this.addBindVariables(bindVariables);
        }
        log.exiting(CLASSNAME, METHODNAME);
    }

    /**
     * Returns the integer id that corresponds to the passed search parameter name.
     * 
     * @param searchParameterName
     * @return Integer
     * @throws FHIRPersistenceException
     */
    private Integer getParameterNameId(String searchParameterName) throws FHIRPersistenceException {
        final String METHODNAME = "getParameterNameId";
        log.entering(CLASSNAME, METHODNAME);

        Integer parameterNameId;

        parameterNameId = ParameterNamesCache.getParameterNameId(searchParameterName);
        if (parameterNameId == null) {
            parameterNameId = this.parameterDao.readParameterNameId(searchParameterName);
            if (parameterNameId != null) {
                this.parameterDao.addParameterNamesCacheCandidate(searchParameterName, parameterNameId);
            } else {
                parameterNameId = -1; // need a value to keep query syntax valid
            }
        }

        log.exiting(CLASSNAME, METHODNAME);
        return parameterNameId;

    }

    /**
     * Adds the bind variables contained in all of the query segments contained in
     * this instance to the passed collection.
     * 
     * @param bindVariables
     */
    private void addBindVariables(List<Object> bindVariables) {
        final String METHODNAME = "addBindVariables";
        log.entering(CLASSNAME, METHODNAME);

        for (SqlQueryData querySegment : this.querySegments) {
            bindVariables.addAll(querySegment.getBindVariables());
        }
        log.exiting(CLASSNAME, METHODNAME);
    }
}