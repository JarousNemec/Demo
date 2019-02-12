import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class SimplePostalCall {

    private static final char TAB = '\t';
    private static final char LF = '\n';

    private static final int SATORI_TEMPLATE_NAME_TO_USE = 151;
    private static final int SATORI_FIELD_LIST_IN = 200;
    private static final int SATORI_FIELD_LIST_OUT = 201;
    private static final int SATORI_RECORD_COUNT = 301;
    private static final int SATORI_INPUT_BLOCK_RECORD_COUNT = 202;
    private static final int SATORI_RECORD_COUNT_PER_RECEIVE = 310;
    private static final int SATORI_SHOW_SORT_PROGRESS = 303;
    private static final int SATORI_MAIL_OWNER_MAILER_ID = 6828;
    private static final int SATORI_IM_BARCODE_MAILER_ID_CODE = 6831;



    private static final int RECORD_COUNT = 205;
    private String mailerId = "999999999";
//    private String mailerId = "12345";

    private InputStream in;
    private OutputStreamWriter out;

    public SimplePostalCall() {
    }

    public void simpleCall() throws UnknownHostException, IOException {
        Socket socket = new Socket("mailroom1.satorisoftware.com", 5150);
        in = socket.getInputStream();
        out = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");

        //Prepaire
        String reg_key = "1Y5SSLT-DQX9DV-7BTWL";
        sendAndReceive("STI1", "0" + TAB + reg_key);

        //SetProperties
        sendAndReceive("STI8", "" + SATORI_SHOW_SORT_PROGRESS + TAB + "FALSE");
        sendAndReceive("STI8", "" + SATORI_TEMPLATE_NAME_TO_USE + TAB + "demo");
        sendAndReceive("STI8", "" + SATORI_RECORD_COUNT + TAB + RECORD_COUNT);
        sendAndReceive("STI8", "" + SATORI_INPUT_BLOCK_RECORD_COUNT + TAB + "1");
        sendAndReceive("STI8", "" + SATORI_RECORD_COUNT_PER_RECEIVE + TAB + "1");
        String inFields = "FLD_ADDRESSLINE1" + TAB + "FLD_CITY" + TAB + "FLD_STATE" + TAB + "FLD_ZIPCODE";
        sendAndReceive("STI8", "" + SATORI_FIELD_LIST_IN + TAB + inFields);
        String outFields = "FLD_ADDRESSLINE1" + TAB + "FLD_CITY" + TAB + "FLD_STATE" + TAB + "FLD_ZIPCODE" + TAB + "FLD_IM_BARCODE";
        sendAndReceive("STI8", "" + SATORI_FIELD_LIST_OUT + TAB + outFields);

        sendAndReceive("STI8", "" + SATORI_MAIL_OWNER_MAILER_ID + TAB + mailerId);
        sendAndReceive("STI8", "" + SATORI_IM_BARCODE_MAILER_ID_CODE + TAB + "0");


        //ValidateProperties
        sendAndReceive("STI2", "");

        //Send records
        for (int i = 0; i < RECORD_COUNT; i++) {
            sendAndReceive("STI9", "416 Lake Street" + TAB + "Antioch" + TAB + "IL" + TAB + "60002");
        }

        //Sort records
        sendAndReceive("STI3", "");

        //Receive records
        for (int i = 0; i < RECORD_COUNT; i++) {
            sendAndReceive("STIA", "");
        }
        sendAndReceive("STIA", "");


        //EndTask
        sendAndReceive("STI5", "");
        socket.close();

    }

    private void sendAndReceive(String header, String body) throws IOException {
        int bodySize = body.length();//All chars are encoded in one byte, so it is correct for this example.
        bodySize += 2;//first TAB + last LF
        String h = header + "=" + bodySize;
        String b = TAB + body + LF;
        String toSend = h + b;
        System.out.println("GMC: \"" + formatForHuman(toSend) + "\"");
        out.append(toSend);
        out.flush();
        readResponse();
    }

    private void readResponse() throws IOException {
        ByteArrayOutputStream headRead = new ByteArrayOutputStream();
        while (true) {//read until first tab
            int i = in.read();//HERE IT FREEZE WHEN YOU WANT RETRIEVE LAST RECORD - YOU WILL GET NO RESPONSE
            headRead.write(i);
            if (i == '\t') {
                break;
            }
        }
        headRead.flush();
        headRead.close();
        String header = headRead.toString("UTF-8");
        String bodySizeStr = header.split("=")[1].trim();

        int bodySize = Integer.valueOf(bodySizeStr).intValue();
        bodySize--;//First Is <TAB> that was already readed

        ByteArrayOutputStream bodyRead = new ByteArrayOutputStream();
        for (int i = 0; i < bodySize; i++) {
            int b = in.read();
            bodyRead.write(b);
        }

        bodyRead.flush();
        bodyRead.close();
        String body = bodyRead.toString("UTF-8");
        String satoriResponse = header + body;
        System.out.println("SATORI: \"" + formatForHuman(satoriResponse) + "\"");
    }

    private String formatForHuman(String textToFormat) {
        String result = textToFormat.replaceAll(Character.toString(TAB), "<TAB>");
//        result = result.replaceAll(" ", "<SPACE>");
        result = result.replaceAll("\\"+Character.toString(LF), "<LF>");
        return result;
    }

    public static void main(String[] args) throws Exception {
        SimplePostalCall app = new SimplePostalCall();
        app.simpleCall();
    }
}