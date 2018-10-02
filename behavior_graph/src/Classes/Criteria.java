/**
 * 
 */
package Classes;

/**
 * @author omido
 *
 */
public class Criteria {

	private String FieldName;
	private String op;
	private String value;

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

	/**
	 * ceate a criteria object from parts
	 * 
	 * @param fieldName
	 *            name of the fied in which criteioa will apply
	 * @param op
	 *            the operator of criteria validation
	 * @param value
	 *            the value against which the field will be evaluted
	 */
	public Criteria(String fieldName, String op, String value) {
		super();
		FieldName = fieldName;
		this.op = op;
		this.value = value;
	}

}
