/**
 * (C) Copyright IBM Corp. 2017,2018,2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.watsonhealth.fhir.persistence.jdbc.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.datatype.XMLGregorianCalendar;

import com.ibm.watsonhealth.fhir.core.FHIRUtilities;
import com.ibm.watsonhealth.fhir.model.Address;
import com.ibm.watsonhealth.fhir.model.Annotation;
import com.ibm.watsonhealth.fhir.model.Attachment;
import com.ibm.watsonhealth.fhir.model.Base64Binary;
import com.ibm.watsonhealth.fhir.model.Boolean;
import com.ibm.watsonhealth.fhir.model.Code;
import com.ibm.watsonhealth.fhir.model.CodeableConcept;
import com.ibm.watsonhealth.fhir.model.Coding;
import com.ibm.watsonhealth.fhir.model.ContactPoint;
import com.ibm.watsonhealth.fhir.model.Date;
import com.ibm.watsonhealth.fhir.model.DateTime;
import com.ibm.watsonhealth.fhir.model.Decimal;
import com.ibm.watsonhealth.fhir.model.HumanName;
import com.ibm.watsonhealth.fhir.model.Id;
import com.ibm.watsonhealth.fhir.model.Identifier;
import com.ibm.watsonhealth.fhir.model.Instant;
import com.ibm.watsonhealth.fhir.model.Integer;
import com.ibm.watsonhealth.fhir.model.LocationPosition;
import com.ibm.watsonhealth.fhir.model.Markdown;
import com.ibm.watsonhealth.fhir.model.Meta;
import com.ibm.watsonhealth.fhir.model.Oid;
import com.ibm.watsonhealth.fhir.model.Period;
import com.ibm.watsonhealth.fhir.model.PositiveInt;
import com.ibm.watsonhealth.fhir.model.Quantity;
import com.ibm.watsonhealth.fhir.model.Range;
import com.ibm.watsonhealth.fhir.model.Ratio;
import com.ibm.watsonhealth.fhir.model.Reference;
import com.ibm.watsonhealth.fhir.model.SampledData;
import com.ibm.watsonhealth.fhir.model.SearchParameter;
import com.ibm.watsonhealth.fhir.model.Signature;
import com.ibm.watsonhealth.fhir.model.Time;
import com.ibm.watsonhealth.fhir.model.Timing;
import com.ibm.watsonhealth.fhir.model.UnsignedInt;
import com.ibm.watsonhealth.fhir.model.Uri;
import com.ibm.watsonhealth.fhir.model.Uuid;
import com.ibm.watsonhealth.fhir.persistence.exception.FHIRPersistenceProcessorException;
import com.ibm.watsonhealth.fhir.persistence.jdbc.dto.Parameter;
import com.ibm.watsonhealth.fhir.persistence.util.AbstractProcessor;

/**
 * This class is the JDBC persistence layer implementation for transforming SearchParameters into Parameter DTOs for persistence.
 * @author markd
 *
 */
public class JDBCParameterBuilder extends AbstractProcessor<List<Parameter>> {
	private static final Logger log = Logger.getLogger(JDBCParameterBuilder.class.getName());
	private static final String className = JDBCParameterBuilder.class.getName();

	@Override
	public List<Parameter> process(SearchParameter parameter, String value) throws FHIRPersistenceProcessorException {
		String methodName = "process(SearchParameter, String)";
		log.entering(className, methodName);
		List<Parameter> parameters = new ArrayList<Parameter>();
		try {
			Parameter p = new Parameter();
			p.setName(parameter.getName().getValue());
			if ("token".equals(parameter.getType().getValue())) {
				p.setValueCode(value);
			} else {
				p.setValueString(value);
			}
			parameters.add(p);
			return parameters;
		} catch (Throwable e) {
			throw new FHIRPersistenceProcessorException(e);
		} finally {
			log.exiting(className, methodName);
		}
	}

	@Override
	public List<Parameter> process(SearchParameter parameter, com.ibm.watsonhealth.fhir.model.String value) throws FHIRPersistenceProcessorException {
		String methodName = "process(SearchParameter,String)";
		log.entering(className, methodName);
		List<Parameter> parameters = new ArrayList<Parameter>();
		try {

			Parameter p = new Parameter();
			p.setName(parameter.getName().getValue());
			p.setValueString(value.getValue());
			parameters.add(p);
			return parameters;
		} catch (Throwable e) {
			throw new FHIRPersistenceProcessorException(e);
		} finally {
			log.exiting(className, methodName);
		}
	}

	@Override
	public List<Parameter> process(SearchParameter parameter, Address value) throws FHIRPersistenceProcessorException {
		String methodName = "process(SearchParameter,Address)";
		log.entering(className, methodName);
		List<Parameter> parameters = new ArrayList<Parameter>();
		try {
			Parameter p = new Parameter();

			String paramName = parameter.getName().getValue();

			if(value.getCity() != null){
				p = new Parameter();
				p.setName(paramName);
				p.setValueString(value.getCity().getValue());
				parameters.add(p);
			}
			if(value.getCountry() != null){
				p = new Parameter();
				p.setName(paramName);
				p.setValueString(value.getCountry().getValue());
				parameters.add(p);
			}

			if(value.getDistrict() != null){
				p = new Parameter();
				p.setName(paramName);
				p.setValueString(value.getDistrict().getValue());
				parameters.add(p);
			}

			if(value.getLine() != null){
				for (com.ibm.watsonhealth.fhir.model.String aLine : value.getLine()) {
					p = new Parameter();
					p.setName(paramName);
					p.setValueString(aLine.getValue());
					parameters.add(p);
				}

			}

			if(value.getPostalCode() != null){
				p = new Parameter();
				p.setName(paramName);
				p.setValueString(value.getPostalCode().getValue());
				parameters.add(p);
			}

			if(value.getState() != null){
				p = new Parameter();
				p.setName(paramName);
				p.setValueString(value.getState().getValue());
				parameters.add(p);
			}


			if(value.getUse() != null){
				p = new Parameter();
				p.setName(paramName);
				p.setValueString(value.getUse().getValue().value());
				parameters.add(p);
			}

			if(value.getType() != null){
				p = new Parameter();
				p.setName(paramName);
				p.setValueString(value.getType().getValue().value());
				parameters.add(p);
			}

			if(value.getText() != null){
				p = new Parameter();
				p.setName(paramName);
				p.setValueString(value.getText().getValue());
				parameters.add(p);
			}

			return parameters;
		} catch (Throwable e) {
			throw new FHIRPersistenceProcessorException(e);
		} finally {
			log.exiting(className, methodName);
		}
	}

	@Override
	public List<Parameter> process(SearchParameter parameter, Annotation value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Parameter> process(SearchParameter parameter, Attachment value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Parameter> process(SearchParameter parameter, Base64Binary value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Parameter> process(SearchParameter parameter, Boolean value) throws FHIRPersistenceProcessorException {
		String methodName = "process(SearchParameter,Boolean)";
		log.entering(className, methodName);
		List<Parameter> parameters = new ArrayList<Parameter>();
		try {
			Parameter p = new Parameter();
			p.setName(parameter.getName().getValue());
			if (value.isValue()) {
				p.setValueCode("true");
			} else {
				p.setValueCode("false");
			}
			parameters.add(p);
			return parameters;
		} catch (Throwable e) {
			throw new FHIRPersistenceProcessorException(e);
		} finally {
			log.exiting(className, methodName);
		}
	}

	@Override
	public List<Parameter> process(SearchParameter parameter, Code value) throws FHIRPersistenceProcessorException {
		String methodName = "process(SearchParameter,Code)";
		log.entering(className, methodName);
		List<Parameter> parameters = new ArrayList<Parameter>();
		try {
			Parameter p = new Parameter();
			p.setName(parameter.getName().getValue());
			p.setValueCode(value.getValue());
			parameters.add(p);
			return parameters;
		} catch (Throwable e) {
			throw new FHIRPersistenceProcessorException(e);
		} finally {
			log.exiting(className, methodName);
		}
	}

	@Override
	public List<Parameter> process(SearchParameter parameter, CodeableConcept value) throws FHIRPersistenceProcessorException {
		String methodName = "process(SearchParameter,CodeableConcept)";
		log.entering(className, methodName);
		List<Parameter> parameters = new ArrayList<Parameter>();
		try {
			for (Coding c : value.getCoding()) {
				parameters.addAll(this.process(parameter, c));
			}
			return parameters;
		}
		finally {
			log.exiting(className, methodName);
		}
	}

	@Override
	public List<Parameter> process(SearchParameter parameter, Coding value) throws FHIRPersistenceProcessorException {
		String methodName = "process(SearchParameter,Coding)";
		log.entering(className, methodName);
		List<Parameter> parameters = new ArrayList<Parameter>();
		try {
			if (value.getCode() != null || value.getSystem() != null) {
				Parameter p = new Parameter();
				p.setName(parameter.getName().getValue());
				if (value.getSystem() != null) {
					p.setValueSystem(value.getSystem().getValue());
				}
				if (value.getCode() != null) {
					p.setValueCode(value.getCode().getValue());
				}
				parameters.add(p);
			}
			return parameters;
		} catch (Throwable e) {
			throw new FHIRPersistenceProcessorException(e);
		} finally {
			log.exiting(className, methodName);
		}
	}


	@Override
	public List<Parameter> process(SearchParameter parameter, ContactPoint value) throws FHIRPersistenceProcessorException {
		String methodName = "process(SearchParameter,ContactPoint)";
		log.entering(className, methodName);
		List<Parameter> parameters = new ArrayList<Parameter>();
		try {
			if (value.getValue() != null) {
				Parameter telecom = new Parameter();
				telecom.setName(parameter.getName().getValue());
				telecom.setValueCode(value.getValue().getValue());
				if (value.getSystem() != null) {
					telecom.setValueSystem(value.getSystem().getValue().value());
				}
				parameters.add(telecom);
			}
						
			if (value.getSystem() != null) {
				Parameter phone = new Parameter();
				phone.setValueCode(value.getValue().getValue());
				phone.setName(value.getSystem().getValue().value());
				phone.setValueSystem(value.getSystem().getValue().value());
				parameters.add(phone);
			}
			
			return parameters;
		} catch (Throwable e) {
			throw new FHIRPersistenceProcessorException(e);
		} finally {
			log.exiting(className, methodName);
		}
	}

	@Override
	public List<Parameter> process(SearchParameter parameter, Date value) throws FHIRPersistenceProcessorException {
		String methodName = "process(SearchParameter,Date)";
		log.entering(className, methodName);
		List<Parameter> parameters = new ArrayList<Parameter>();
		try {
			Parameter p = new Parameter();
			p.setName(parameter.getName().getValue());
			XMLGregorianCalendar calendar = FHIRUtilities.parseDateTime(value.getValue(), true);
			p.setValueDate(FHIRUtilities.convertToTimestamp(calendar));
			parameters.add(p);
			return parameters;
		} catch (Throwable e) {
			throw new FHIRPersistenceProcessorException(e);
		} finally {
			log.exiting(className, methodName);
		}
	}

	@Override
	public List<Parameter> process(SearchParameter parameter, DateTime value) throws FHIRPersistenceProcessorException {
		String methodName = "process(SearchParameter,DateTime)";
		log.entering(className, methodName);
		List<Parameter> parameters = new ArrayList<Parameter>();
		try {
			Parameter p = new Parameter();
			p.setName(parameter.getName().getValue());
			XMLGregorianCalendar calendar = FHIRUtilities.parseDateTime(value.getValue(), true);
			p.setValueDate(FHIRUtilities.convertToTimestamp(calendar));
			parameters.add(p);
			return parameters;
		} catch (Throwable e) {
			throw new FHIRPersistenceProcessorException(e);
		} finally {
			log.exiting(className, methodName);
		}
	}

	@Override
	public List<Parameter> process(SearchParameter parameter, Decimal value) throws FHIRPersistenceProcessorException {
		String methodName = "process(SearchParameter,Decimal)";
		log.entering(className, methodName);
		List<Parameter> parameters = new ArrayList<Parameter>();
		try {
			Parameter p = new Parameter();
			p.setName(parameter.getName().getValue());
			p.setValueNumber(value.getValue().doubleValue());
			parameters.add(p);
			return parameters;
		} catch (Throwable e) {
			throw new FHIRPersistenceProcessorException(e);
		} finally {
			log.exiting(className, methodName);
		}
	}

	@Override
	public List<Parameter> process(SearchParameter parameter, HumanName value) throws FHIRPersistenceProcessorException {
		String methodName = "process(SearchParameter,HumanName)";
		log.entering(className, methodName);
		List<Parameter> parameters = new ArrayList<Parameter>();
		try {
			String paramname = parameter.getName().getValue();
			
			Parameter p = new Parameter();
			if (value.getFamily() != null) {
				for(com.ibm.watsonhealth.fhir.model.String family : value.getFamily()) {
					p = new Parameter();
					p.setName(paramname);
					p.setValueString(family.getValue());
					parameters.add(p);
        		}
			}
			if (value.getGiven() != null) {
				for(com.ibm.watsonhealth.fhir.model.String given : value.getGiven()) {
					p = new Parameter();
					p.setName(paramname);
					p.setValueString(given.getValue());
					parameters.add(p);
        		}
			}
			
			if (value.getText() != null) {
				p = new Parameter();
				p = new Parameter();
				p.setName(paramname);
				p.setValueString(value.getText().getValue());
        		
				parameters.add(p);
        	}
			
			if (value.getUse() != null) {
				p = new Parameter();
				p = new Parameter();
				p.setName(paramname);
				p.setValueString(value.getUse().getValue().value());
        		
				parameters.add(p);
        	}
			
			return parameters;
			
		} catch (Throwable e) {
			throw new FHIRPersistenceProcessorException(e);
		} finally {
			log.exiting(className, methodName);
		}
	}

	@Override
	public List<Parameter> process(SearchParameter parameter, Id value) throws FHIRPersistenceProcessorException {
		String methodName = "process(SearchParameter,Id)";
		log.entering(className, methodName);
		List<Parameter> parameters = new ArrayList<Parameter>();
		try {
			Parameter p = new Parameter();
			p.setName(parameter.getName().getValue());
			p.setValueCode(value.getValue());
			parameters.add(p);
			return parameters;
		} catch (Throwable e) {
			throw new FHIRPersistenceProcessorException(e);
		} finally {
			log.exiting(className, methodName);
		}
	}

	@Override
	public List<Parameter> process(SearchParameter parameter, Identifier value) throws FHIRPersistenceProcessorException {
		String methodName = "process(SearchParameter,Identifier)";
		log.entering(className, methodName);
		List<Parameter> parameters = new ArrayList<Parameter>();
		try {
			Parameter p = new Parameter();
			p.setName(parameter.getName().getValue());
			if (value.getSystem() != null) {
				p.setValueSystem(value.getSystem().getValue());
			}
			p.setValueCode(value.getValue().getValue());
			parameters.add(p);
			return parameters;
		} catch (Throwable e) {
			throw new FHIRPersistenceProcessorException(e);
		} finally {
			log.exiting(className, methodName);
		}
	}

	@Override
	public List<Parameter> process(SearchParameter parameter, Instant value) throws FHIRPersistenceProcessorException {
		String methodName = "process(SearchParameter,Instant)";
		log.entering(className, methodName);
		List<Parameter> parameters = new ArrayList<Parameter>();
		try {
			Parameter p = new Parameter();
			p.setName(parameter.getName().getValue());
			XMLGregorianCalendar calendar = value.getValue();
			calendar = calendar.normalize();
			p.setValueDate(FHIRUtilities.convertToTimestamp(calendar));
			parameters.add(p);
			return parameters;
		} catch (Throwable e) {
			throw new FHIRPersistenceProcessorException(e);
		} finally {
			log.exiting(className, methodName);
		}
	}

	@Override
	public List<Parameter> process(SearchParameter parameter, Integer value) throws FHIRPersistenceProcessorException {
		String methodName = "process(SearchParameter,Integer)";
		log.entering(className, methodName);
		List<Parameter> parameters = new ArrayList<Parameter>();
		try {
			Parameter p = new Parameter();
			p.setName(parameter.getName().getValue());
			p.setValueNumber(value.getValue().doubleValue());
			parameters.add(p);
			return parameters;
		} catch (Throwable e) {
			throw new FHIRPersistenceProcessorException(e);
		} finally {
			log.exiting(className, methodName);
		}
	}

	/**
	 * 
	 *  Parameter Name = postition
	 *  Value = System|code = Longitude|Latitude
	 * @throws FHIRPersistenceProcessorException 
	 */
	@Override
	public List<Parameter> process(SearchParameter parameter, LocationPosition value) throws FHIRPersistenceProcessorException {

		String methodName = "process(SearchParameter,LocationPosition)";
		log.entering(className, methodName);
		List<Parameter> parameters = new ArrayList<Parameter>();
		try {
			Parameter p = new Parameter();
			p.setName(parameter.getName().getValue());
			if (value.getLatitude() != null) {
				p.setValueLatitude(value.getLatitude().getValue().doubleValue());
			}
			if (value.getLongitude() != null) {
				p.setValueLongitude(value.getLongitude().getValue().doubleValue());
			}
			parameters.add(p);
			return parameters;
		} catch (Throwable e) {
			throw new FHIRPersistenceProcessorException(e);
		} finally {
			log.exiting(className, methodName);
		}
	}

	@Override
	public List<Parameter> process(SearchParameter parameter, Markdown value) throws FHIRPersistenceProcessorException {
		String methodName = "process(SearchParameter,Markdown)";
		log.entering(className, methodName);
		List<Parameter> parameters = new ArrayList<Parameter>();
		try {
			Parameter p = new Parameter();
			p.setName(parameter.getName().getValue());
			p.setValueString(value.getValue());
			parameters.add(p);
			return parameters;
		} catch (Throwable e) {
			throw new FHIRPersistenceProcessorException(e);
		} finally {
			log.exiting(className, methodName);
		}
	}

	@Override
	public List<Parameter> process(SearchParameter parameter, Meta value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Parameter> process(SearchParameter parameter, Oid value) throws FHIRPersistenceProcessorException {
		String methodName = "process(SearchParameter,Oid)";
		log.entering(className, methodName);
		List<Parameter> parameters = new ArrayList<Parameter>();
		try {
			Parameter p = new Parameter();
			p.setName(parameter.getName().getValue());
			p.setValueString(value.getValue());
			parameters.add(p);
			return parameters;
		} catch (Throwable e) {
			throw new FHIRPersistenceProcessorException(e);
		} finally {
			log.exiting(className, methodName);
		}
	}

	@Override
	public List<Parameter> process(SearchParameter parameter, Period value) throws FHIRPersistenceProcessorException {
		String methodName = "process(SearchParameter,Period)";
		log.entering(className, methodName);
		List<Parameter> parameters = new ArrayList<Parameter>();
		try {
			Parameter p = new Parameter();
			p.setName(parameter.getName().getValue());
			if (value.getStart() != null) {
				XMLGregorianCalendar calendar = FHIRUtilities.parseDateTime(value.getStart().getValue(), true);
				p.setValueDateStart(FHIRUtilities.convertToTimestamp(calendar));
			}
			if (value.getEnd() != null) {
				XMLGregorianCalendar calendar1 = FHIRUtilities.parseDateTime(value.getEnd().getValue(), true);
				p.setValueDateEnd(FHIRUtilities.convertToTimestamp(calendar1));
			}
			parameters.add(p);
			return parameters;
		} catch (Throwable e) {
			throw new FHIRPersistenceProcessorException(e);
		} finally {
			log.exiting(className, methodName);
		}
	}

	@Override
	public List<Parameter> process(SearchParameter parameter, PositiveInt value) throws FHIRPersistenceProcessorException {
		String methodName = "process(SearchParameter,PositiveInt)";
		log.entering(className, methodName);
		List<Parameter> parameters = new ArrayList<Parameter>();
		try {
			Parameter p = new Parameter();
			p.setName(parameter.getName().getValue());
			p.setValueNumber(value.getValue().doubleValue());
			parameters.add(p);
			return parameters;
		} catch (Throwable e) {
			throw new FHIRPersistenceProcessorException(e);
		} finally {
			log.exiting(className, methodName);
		}
	}

	@Override
	public List<Parameter> process(SearchParameter parameter, Quantity value) throws FHIRPersistenceProcessorException {
		String methodName = "process(SearchParameter,Quantity)";
		log.entering(className, methodName);
		List<Parameter> parameters = new ArrayList<Parameter>();
		try {
			Parameter p = new Parameter();
			p.setName(parameter.getName().getValue());
			BigDecimal bd = value.getValue().getValue();
			p.setValueNumber(bd.doubleValue());
			if (value.getCode() != null) {
				p.setValueCode(value.getCode().getValue());
			} else if (value.getUnit() != null) {
				p.setValueCode(value.getUnit().getValue());

			}
			if (value.getSystem() != null) {
				p.setValueSystem(value.getSystem().getValue());
			}
			parameters.add(p);
			return parameters;
		} catch (Throwable e) {
			throw new FHIRPersistenceProcessorException(e);
		} finally {
			log.exiting(className, methodName);
		}
	}

	@Override
	public List<Parameter> process(SearchParameter parameter, Range value) throws FHIRPersistenceProcessorException {
		String methodName = "process(SearchParameter,Range)";
		log.entering(className, methodName);
		List<Parameter> parameters = new ArrayList<Parameter>();
		try {
			Parameter p = new Parameter();
			p.setName(parameter.getName().getValue());
			if(value.getLow().getSystem() != null){
				p.setValueSystem(value.getLow().getSystem().getValue());
			}
			if(value.getLow().getCode() != null ){
				p.setValueCode(value.getLow().getCode().getValue());
			}else if(value.getLow().getUnit() != null ){
				p.setValueCode(value.getLow().getUnit().getValue());
			}
			p.setValueNumberLow(value.getLow().getValue().getValue().doubleValue());
			p.setValueNumberHigh(value.getHigh().getValue().getValue().doubleValue());
			parameters.add(p);
			return parameters;
		} catch (Throwable e) {
			throw new FHIRPersistenceProcessorException(e);
		} finally {
			log.exiting(className, methodName);
		}
	}
	@Override
	public List<Parameter> process(SearchParameter parameter, Ratio value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Parameter> process(SearchParameter parameter, Reference value) throws FHIRPersistenceProcessorException {
		String methodName = "process(SearchParameter,Reference)";
		log.entering(className, methodName);
		List<Parameter> parameters = new ArrayList<Parameter>();
		try {
			if (value.getReference() != null) {
				Parameter p = new Parameter();
				p.setName(parameter.getName().getValue());
				p.setValueString(value.getReference().getValue());
				parameters.add(p);
			}
			return parameters;
		} catch (Throwable e) {
			throw new FHIRPersistenceProcessorException(e);
		} finally {
			log.exiting(className, methodName);
		}
	}

	@Override
	public List<Parameter> process(SearchParameter parameter, SampledData value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Parameter> process(SearchParameter parameter, Signature value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Parameter> process(SearchParameter parameter, Time value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Parameter> process(SearchParameter parameter, Timing value) throws FHIRPersistenceProcessorException {
		return process( parameter, value.getRepeat().getPeriod());
	}

	@Override
	public List<Parameter> process(SearchParameter parameter, UnsignedInt value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Parameter> process(SearchParameter parameter, Uri value) throws FHIRPersistenceProcessorException {
		String methodName = "process(SearchParameter,Uri)";
		log.entering(className, methodName);
		List<Parameter> parameters = new ArrayList<Parameter>();
		try {
			Parameter p = new Parameter();
			p.setName(parameter.getName().getValue());
			p.setValueString(value.getValue());
			parameters.add(p);
			return parameters;
		} catch (Throwable e) {
			throw new FHIRPersistenceProcessorException(e);
		} finally {
			log.exiting(className, methodName);
		}
	}

	@Override
	public List<Parameter> process(SearchParameter parameter, Uuid value) {
		// TODO Auto-generated method stub
		return null;
	}
}