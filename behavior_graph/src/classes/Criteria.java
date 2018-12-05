/**
 * 
 */
package classes;

import java.util.ArrayList;

/**
 * @author omido
 *
 */
public class Criteria {

	private String FieldName;
	private String op;
	private String value;
	private ArrayList<ResourceType> FieldType;

	public String getFieldName() {
		return FieldName.toLowerCase();
	}

	public void setFieldName(String fieldName) {
		FieldName = fieldName;
	}

	public String getOp() {
		return op.toLowerCase();
	}

	public void setOp(String op) {
		this.op = op;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public ArrayList<ResourceType> getFieldType() {
		return this.FieldType;
	}

	public void addFieldType(ResourceType fieldType) {
		this.FieldType.add(fieldType);
	}

	/**
	 * creates a criteria object from parts
	 * 
	 * @param fieldType sets the field type on which the criteria applies to
	 * @param fieldName name of the field in which criteria will apply
	 * @param op        the operator of criteria validation
	 * @param value     the value against which the field will be evaluated
	 */
	public Criteria(ArrayList<ResourceType> fieldType, String fieldName, String op, String value) {
		super();
		this.FieldName = fieldName;
		this.op = op;
		this.value = value;
		this.FieldType = fieldType;
	}

	/**
	 * creates a criteria object from parts
	 * 
	 * @param fieldName name of the field in which criteria will apply
	 * @param op        the operator of criteria validation
	 * @param value     the value against which the field will be evaluated
	 */
	public Criteria(String fieldName, String op, String value) {
		super();
		this.FieldName = fieldName;
		this.op = op;
		this.value = value;
		this.FieldType = new ArrayList<ResourceType>();
	}

}
