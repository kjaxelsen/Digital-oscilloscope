import javax.swing.*;
import java.nio.channels.UnresolvedAddressException;
import java.util.ArrayList;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
/**
 * Created by Kevin on 17/05/16.
 * Thread for reading and sending to ethernet
 */
@SuppressWarnings("All")
public class EthernetReader implements Runnable{
    OscilloscopeController oc;
    ArrayList<ArrayList<Double>> dataSets;
    ArrayList<ArrayList<Double>> dataSets2;
    SocketChannel channel = null;
    EthernetReader(OscilloscopeController oc) {
        this.oc = oc;
    }
    public void run() {
        while(true) {
            String theIP = oc.getModel().getIP();
            try {
                Thread.sleep(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(!theIP.equals("")) {
                int port = 19000;
                String message = "";
                //outBox is outBox from model
                ArrayList<ArrayList<Byte>> outBox = oc.getModel().getOutBox();
                try {
                    channel = SocketChannel.open();
                    // we open this channel in non blocking mode
                    channel.configureBlocking(false);
                    channel.connect(new InetSocketAddress(theIP, port));
                    oc.getModel().setConnection(true);
                    oc.getView().connecter.setText("Disconnect");
                    oc.getView().netElement.setText("Connected");
                    ArrayList<Byte> bytes = new ArrayList<>();
                    bytes.add((byte) 0b01111111);
                    bytes.add((byte) 0b01111111);
                    oc.getModel().addOutBox(bytes);

                    while (!channel.finishConnect()) {
                        // still connecting
                    }
                    while (true) {
                        //if disconnect
                        if(oc.getModel().isBreak()) {
                            oc.getModel().setConnection(false);
                            oc.getModel().setBreaker(false);
                            oc.getView().netElement.setText("Disconnected");
                            oc.getModel().setIP("");
                            System.out.println("::");
                            channel.close();
                            channel = null;
                            oc.getView().connecter.setText("Connect");
                            break;
                        }
                        //if something in outbox
                        if (outBox.size() > 0) {
                            // write some data into the channel
                            byte[] byter = new byte[2];
                            byter[0] = outBox.get(0).get(0);
                            byter[1] = outBox.get(0).get(1);
                            ByteBuffer buffer = ByteBuffer.wrap(byter);
                            while (buffer.hasRemaining()) {
                                channel.write(buffer);
                            }
                            outBox.remove(0);
                        }
                        // see if any message has been received
                        ByteBuffer bufferA = ByteBuffer.allocate(2);
                        while (channel.read(bufferA) > 0) {
                            // flip the buffer to start reading
                            bufferA.flip();
                            System.out.println(Integer.toBinaryString(bufferA.get(0)) + " -- " + Integer.toBinaryString(bufferA.get(1)));
                            //time to sort
                            byte test = (byte)(bufferA.get(0) & 0x0F);
                            byte test2 = (byte)((bufferA.get(0) & 0xFF));
                            int tester = test2 & 0xFF;
                            System.out.println("gabba " + Integer.toBinaryString(test));
                            System.out.println("gabba2 " + Integer.toBinaryString(tester));
                            //if beginning message sending
                            if(((bufferA.get(0) & 0xFF) >> 3) == 0b11111) {
                                boolean breakit = false;
                                System.out.println("yessir");
                                //if A beginning
                                if((bufferA.get(1) & 0b00000011) == 0) {
                                    ArrayList<ArrayList<Double>> daSet = new ArrayList<>();
                                    int i = 0;
                                    while(true) {
                                        //receive A points
                                        ByteBuffer bufferAA = ByteBuffer.allocate(2);
                                        //receive B points
                                        while (channel.read(bufferAA) > 0) {
                                            // flip the buffer to start reading
                                            bufferAA.flip();
                                            System.out.println("000");
                                            int byt1 = bufferAA.get(0) & 0xFF;
                                            int byt2 = bufferAA.get(1) & 0xFF;
                                            int bytTotal = oc.combineBytes(byt1, byt2);
                                            String[] tscale;
                                            String[] vscale;
                                            double timemult = 1.0;
                                            double voltmult = 1.0;
                                            double sampleRate = Double.parseDouble(oc.getView().currSampleElement.getText());
                                            if(oc.getIdBytes(byt1) == 0b11111) {
                                                breakit = true;
                                                break;
                                            } else {
                                                ArrayList<Double> da = new ArrayList<>();
                                                //get samplerate from model
                                                tscale = oc.getModel().getHorizRange().split(" ");
                                                if(tscale[1].equals("us")) {
                                                    timemult = 1000000.0;
                                                } else if(tscale[1].equals("ms")) {
                                                    timemult = 1000.0;
                                                }
                                                int k = 0;
                                                vscale = oc.getModel().getVertRange().split(" ");
                                                if(vscale[1] == "mV") {
                                                    voltmult = 1000.0;
                                                }
                                                double time = i/(sampleRate*(Double.parseDouble(tscale[0])/timemult));
                                                double voltDiv = Double.parseDouble(vscale[0])/voltmult;
                                                double voltage = -5*voltDiv + bytTotal * voltDiv*10/4095;
                                                da.add(time);
                                                da.add(voltage);
                                                daSet.add(da);
                                                System.out.println("x " + da.get(0) + " y " + da.get(1));
                                                i++;

                                            }
                                            double converted = 0;
                                            //dataSets.add()
                                        }
                                        if(breakit) {
                                            oc.resetMultipliers();
                                            oc.setSeries(daSet, "A");
                                            oc.redrawPlot();
                                            break;
                                        }
                                    }
                                //if B beginning
                                } else if((bufferA.get(1) & 0b00000011) == 1) {
                                    ArrayList<ArrayList<Double>> dbSet = new ArrayList<>();
                                    int i = 0;
                                    while(true) {
                                        ByteBuffer bufferB = ByteBuffer.allocate(2);
                                        //receive B points
                                        while (channel.read(bufferB) > 0) {
                                            // flip the buffer to start reading
                                            bufferB.flip();
                                            System.out.println("000");
                                            int byt1 = bufferB.get(0) & 0xFF;
                                            int byt2 = bufferB.get(1) & 0xFF;
                                            int bytTotal = oc.combineBytes(byt1, byt2);
                                            String[] tscale;
                                            String[] vscale;
                                            double timemult = 1.0;
                                            double voltmult = 1.0;
                                            double sampleRate = Double.parseDouble(oc.getView().currSampleElement.getText());
                                            if(oc.getIdBytes(byt1) == 0b11111) {
                                                breakit = true;
                                                break;
                                            } else {
                                                ArrayList<Double> db = new ArrayList<>();
                                                //get samplerate from model
                                                tscale = oc.getModel().getHorizRange().split(" ");
                                                if(tscale[1].equals("us")) {
                                                    timemult = 1000000.0;
                                                } else if(tscale[1].equals("ms")) {
                                                    timemult = 1000.0;
                                                }
                                                vscale = oc.getModel().getVertRange().split(" ");
                                                if(vscale[1] == "mV") {
                                                    voltmult = 1000.0;
                                                }
                                                double time = i/(sampleRate*(Double.parseDouble(tscale[0])/timemult));
                                                double voltDiv = Double.parseDouble(vscale[0])/voltmult;
                                                double voltage = -5*voltDiv + bytTotal * voltDiv*10/4095;
                                                db.add(time);
                                                db.add(voltage);
                                                dbSet.add(db);
                                                System.out.println("x " + db.get(0) + " y " + db.get(1));
                                                i++;

                                            }
                                            System.out.println("doublecheck " + Integer.toBinaryString(byt1) + " - " + Integer.toBinaryString(byt2));
                                            double converted = 0;
                                        }
                                        if(breakit) {
                                            oc.resetMultipliers();
                                            oc.setSeries(dbSet, "B");
                                            oc.redrawPlot();
                                            break;
                                        }
                                    }
                                } else {
                                    //err
                                }
                            } else {
                                oc.messageHandler(bufferA.get(0), bufferA.get(1));
                            }

                        }

                    }
                } catch (IOException e) {
                    oc.getModel().setIP("");
                    oc.getView().netElement.setText("Disconnected");
                    oc.getView().connecter.setText("Connect");
                    JOptionPane.showMessageDialog(oc.getView(), "No Server Found");
                    //e.printStackTrace();
                } catch (UnresolvedAddressException ex) {
                    oc.getModel().setIP("");
                    oc.getView().netElement.setText("Disconnected");
                    oc.getView().connecter.setText("Connect");
                    JOptionPane.showMessageDialog(oc.getView(), "No Server Found");
                }
            }
        }
    }


}
