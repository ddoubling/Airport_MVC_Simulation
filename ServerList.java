import java.util.ArrayList;

public class ServerList {
	private ArrayList<Server> servers;
	
	public ServerList() {
		this.servers = new ArrayList<Server>();
	}
	
	public void add(Server i) {
		servers.add(i);
	}
	
	public int getSize() {
		return servers.size();
	}
	
	public Server get(int i) {
		return servers.get(i);
	}
}
