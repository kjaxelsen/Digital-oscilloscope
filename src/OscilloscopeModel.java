import java.util.ArrayList;

/**
 * Created by Kevin on 06/04/16.
 */
@SuppressWarnings("All")
public class OscilloscopeModel {
    private ArrayList<ArrayList<Double>> chanAData = null, chanBData = null, chanMData = null, chanFData = null, filterData = null;
    private String expression = "", filtFile = "", IPAddr = "", vertRange = "", horizRange = "", mathChan = "", filtChan = "";
    private ArrayList<ArrayList<Byte>> outBox = new ArrayList<>();
    private boolean connected = false, breaker = false, toSend = false;
    //set and get chan a dataset
    public synchronized void setChanA(ArrayList<ArrayList<Double>> dataSets) {
        chanAData = dataSets;
    }
    public synchronized ArrayList<ArrayList<Double>> getChanA() {
        return chanAData;
    }

    //set and get chan b dataset
    public synchronized void setChanB(ArrayList<ArrayList<Double>> dataSets) {
        chanBData = dataSets;
    }
    public synchronized ArrayList<ArrayList<Double>> getChanB() { return chanBData; }

    //set and get math channel dataset
    public synchronized void setChanM(ArrayList<ArrayList<Double>> dataSets) {
        chanMData = dataSets;
    }
    public synchronized ArrayList<ArrayList<Double>> getChanM() {
        return chanMData;
    }

    //set and get filter channel dataset
    public synchronized void setChanF(ArrayList<ArrayList<Double>> dataSets) {
        chanFData = dataSets;
    }
    public synchronized ArrayList<ArrayList<Double>> getChanF() {
        return chanFData;
    }

    //set and get math expression as string
    public synchronized void setExpression(String exp) {
        expression = exp;
    }
    public synchronized String getExpression() {
        return expression;
    }

    //set and get filter file path as string
    public synchronized void setFiltFile(String filt) {
        filtFile = filt;
    }
    public synchronized String getFiltFile() { return filtFile; }

    //set and get ip as string
    public synchronized void setIP(String ip) {
        IPAddr = ip;
    }
    public synchronized String getIP() { return IPAddr; }

    //set and get filters data
    public synchronized void setFiltData(ArrayList<ArrayList<Double>> filtData) {filterData = filtData;}
    public synchronized ArrayList<ArrayList<Double>> getFiltData() { return filterData; }

    //add to and get outbox for communication
    public synchronized void addOutBox(ArrayList<Byte> oBox) {
        outBox.add(oBox);
    }
    public synchronized ArrayList<ArrayList<Byte>> getOutBox() { return outBox; }

    //set and check connection
    public synchronized void setConnection(boolean con) { connected = con;}
    public synchronized boolean isConnected() { return connected;}

    //set and check need for sending data
    public synchronized void setToSend(boolean tos) { toSend = tos;}
    public synchronized boolean toSend() { return toSend;}

    //set and check if break needed in communication
    public synchronized void setBreaker(boolean bre) { breaker = bre;}
    public synchronized boolean isBreak() { return breaker;}

    //sets and gets vertical range volts per division val
    public synchronized void setVertRange(String vr) {
        vertRange = vr;
    }
    public synchronized String getVertRange() { return vertRange; }

    //sets and gets horizontal time per devision val
    public synchronized void setHorizRange(String hr) {
        horizRange = hr;
    }
    public synchronized String getHorizRange() { return horizRange; }

    //sets and gets the selected channel for filter
    public synchronized void setFiltChan(String fc) {
        filtChan = fc;
    }
    public synchronized String getFiltChan() { return filtChan; }

    //sets and gets math string
    public synchronized void setMathChan(String mc) {
        mathChan = mc;
    }
    public synchronized String getMathChan() { return mathChan; }
}
