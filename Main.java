

public class Main {

	private static Main instance;
	private TimeBroadcaster timeBroadcaster;
	
	private Main(){
		//Honor the Singleton pattern
	}
	
	public void Start(){
		timeBroadcaster = TimeBroadcaster.GetInstance();
		timeBroadcaster.BroadcastTime(15*60);//TimePattern.SECONDS_OF_DAY);
	}

	
	public static Main GetInstance(){
		if(instance == null)
			instance = new Main();
		return instance;
	}
	
	public static void main(String[] args) {
		Main main = Main.GetInstance();
		main.Start();
	}
	
}

