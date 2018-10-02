class Packet{
	private int number;
	private byte[] data;

	public Packet(){
		this(0);
	}

	public Packet(int number){
		this(number, null);
	}


	public Packet (int number, byte[] data){
		this.number = number;
		this.data = data;
	}

	public int getNumber(){
		return number;
	}

	public byte[] getData(){
		return data;
	}
}
