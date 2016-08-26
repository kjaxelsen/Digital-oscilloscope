/**
 * Created by Kevin on 30/03/16.
 * Oscilloscope main method
 */

public class Oscilloscope {

    public static void main(String args[]) {
        OscilloscopeView ov = new OscilloscopeView();
        OscilloscopeModel om = new OscilloscopeModel();
        OscilloscopeController oc = new OscilloscopeController(om, ov);
        Thread er = new Thread(new EthernetReader(oc));
        er.start();

    }
}

