package classes;

import controlClasses.RuntimeVariables;

public class ResourceItem {

	public String id;
	public String Title;
	public String Path;
	public String Number;
	public String Description;
	public ResourceType Type;
	public String computer_id;

	public boolean isEqual(ResourceItem tmp) {
		return tmp.Title.equals(this.Title) && tmp.Number.equals(this.Number) && tmp.Type.equals(this.Type);

	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return id + " | number : " + Number + " | type :" + Type.toString();
	}

	/**
	 * returns a string representation of the object in the Neo4J insertion
	 * format
	 * 
	 * @return the formated to string
	 */
	public String toN4JObjectString() {

		return String.format(
				"%s{id:\"%s\", title:\"%s\" , path:\"%s\", " + "number: \"%s\", description:\"%s\" , type:\"%s\", computer_id:\"%s\"  }",
				Type.toString(), Type == ResourceType.File ? getID() : String.valueOf(id), String.valueOf(Title),
				String.valueOf(Path), Type == ResourceType.Process ? String.valueOf(Number) : "", "", // String.valueOf(Description),
				Type.toString(), String.valueOf(computer_id)).replace("\\", "");
	}

	public String toPGInsertString() {
		return String.format("INSERT Into resources (id,title,path,number,description,type,computer_id) values ('%s','%s','%s','%s','%s','%s','%s');"
				
				,
				Type.toString(),
				Type == ResourceType.File ? getID() : String.valueOf(id), 
						String.valueOf(Title),
				String.valueOf(Path),
				Type == ResourceType.Process ? String.valueOf(Number) : "", 
						"", // String.valueOf(Description),
				Type.toString(), 
				String.valueOf(computer_id)
				).replace("\\", "");
	}
	
	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		if (obj == null || obj.getClass() != this.getClass())
			return false;

		return this.id.equals(((ResourceItem) obj).id);
	}

	public String getHashID() {
		return this.id + "|" + this.Number + "|" + this.Title;
	}

	public String getID() {
		if (RuntimeVariables.getInstance().getIgnoreFDNumber() && this.Type == ResourceType.File)
			return this.Title;
		else
			return this.Number + "|" + this.Title;
	}
}
