package Classes;

public  class ResourceItem {

	public String id; 
	public String Title;
	public String Path;
	public String Number;
	public String Description;
	public ResourceType Type;	
	
	public boolean isEqual( ResourceItem tmp ){
		return tmp.Title.equals(this.Title) &&
				tmp.Number.equals(this.Number) && 
				tmp.Type.equals(this.Type);
		
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return id + " | number : " + Number + " | type :" + Type.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		if( obj ==null || obj.getClass() != this.getClass() )
			return false;
		
		return this.id.equals(( (ResourceItem )obj).id);
	}
}
