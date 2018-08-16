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
	public Criteria(String fieldName, String op, String value) {
		super();
		FieldName = fieldName;
		this.op = op;
		this.value = value;
	}
	
	
	
}
