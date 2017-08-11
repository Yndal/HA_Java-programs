import com.pi4j.io.gpio.exception.UnsupportedBoardType;
import com.pi4j.io.serial.*;
import com.pi4j.util.CommandArgumentParser;
import com.pi4j.util.Console;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class TimeBroadcaster {
	
	private static final char TIME_CHAR = 't';
	private static final char END_CHAR = ';';
	
	public enum TimePattern{
		SECONDS_OF_DAY
	}
	
	private class SerialListener implements SerialDataEventListener {
		
		private final Console console;
		
		private SerialListener(){
			console = new Console();

		     // print program title/header
		     console.title("<-- Yndal HA project -->", "Yndal HA header");

		     // allow for user to exit program using CTRL-C
		     console.promptForExit();
		}
		@Override
        public void dataReceived(SerialDataEvent event) {
			// NOTE! - It is extremely important to read the data received from the
            // serial port.  If it does not get read from the receive buffer, the
            // buffer will continue to grow and consume memory.

            // get the received data and print them...
            try {
            	
            	String data = event.getAsciiString();
            	console.println("Received from serial: " + data);
                //console.println("[HEX DATA]   " + event.getHexByteString());
                //console.println("[ASCII DATA] " + event.getAsciiString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
	}
	
	private class RunnableBroadcaster implements Runnable {
		
		private final Serial serial;
		
		RunnableBroadcaster(Serial serial) throws InstantiationException{
			if(serial == null)
				throw new InstantiationException("Serial is null - it has to be instatiated!");
			this.serial = serial;
			
		}
		
		@SuppressWarnings("deprecation")
		@Override
		public void run() {
			
			Date now = new Date();
			Date d = new Date(now.getYear(), now.getMonth(), now.getDay());
			
			long secondsOfDay = (now.getHours() - d.getHours()) * 60 * 60; 
			secondsOfDay += (now.getMinutes() - d.getMinutes()) * 60;
			secondsOfDay += now.getSeconds() - d.getSeconds();
			
			String str = TIME_CHAR + String.valueOf(secondsOfDay) + END_CHAR;
			
			try {
				serial.write(str);
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println(str);
			
		}
	}
	private RunnableBroadcaster runnableBroadcaster;
	
	private static TimeBroadcaster instance;
	private ScheduledExecutorService scheduledExecutor;
	private ScheduledFuture<?> scheduledFuture;
	private boolean isBroadcasting = false;
	private TimeBroadcaster(){
		
	}
	
	private void initializeRunnableBroadcaster() throws UnsupportedBoardType, IOException, InterruptedException, InstantiationException{
		Serial serial = SerialFactory.createInstance();
		
		SerialConfig config = new SerialConfig();

        // set default serial settings (device, baud rate, flow control, etc)
        //
        // by default, use the DEFAULT com port on the Raspberry Pi (exposed on GPIO header)
        // NOTE: this utility method will determine the default serial port for the
        //       detected platform and board/model.  For all Raspberry Pi models
        //       except the 3B, it will return "/dev/ttyAMA0".  For Raspberry Pi
        //       model 3B may return "/dev/ttyS0" or "/dev/ttyAMA0" depending on
        //       environment configuration.
        config.device(SerialPort.getDefaultPort())
              .baud(Baud._9600)
              .dataBits(DataBits._8)
              .parity(Parity.NONE)
              .stopBits(StopBits._1)
              .flowControl(FlowControl.NONE);
		serial.open(config);
		
		this.runnableBroadcaster = new RunnableBroadcaster(serial);
		
	}
	
	public void BroadcastTime(int perSeconds){//TimePattern pattern){
		try{
			initializeRunnableBroadcaster();
			
			scheduledExecutor = Executors.newScheduledThreadPool(1);
			scheduledFuture =  scheduledExecutor.scheduleAtFixedRate(runnableBroadcaster, 0, perSeconds, TimeUnit.SECONDS);
			isBroadcasting = true;
		} catch (Exception e){
			e.printStackTrace();
		}
		
				
	}
	
	public void StopBroadcasting(){
		if(scheduledFuture != null)
			scheduledFuture.cancel(false);
		isBroadcasting = false;
	}
	
	
	public static TimeBroadcaster GetInstance(){
		if(instance == null){
			instance = new TimeBroadcaster();
		}
		return instance;
	}
	
}

