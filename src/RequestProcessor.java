import java.io.*;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

class RequestProcessor implements Runnable {
    private Socket socket = null;
    private OutputStream os = null;
    private BufferedReader in = null;
    private DataInputStream dis = null;
    private String msgToClient = "HTTP/1.1 200 OK\n"
            + "Server: HTTP server/0.1\n"
            + "Access-Control-Allow-Origin: *\n\n";
    private JSONObject jsonObject = new JSONObject();
    public RequestProcessor(Socket Socket) {
        super();
        try {
            socket = Socket;
            in = new BufferedReader(new
                    InputStreamReader(socket.getInputStream()));

            os = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void run() {
        //write your code here
        String request; //variable to hold the request or URL
        Pattern numpattern = Pattern.compile("\\=\\-?\\d+"); //pattern to get left and right operands from HTTP request (numbers are found after = symbol)
        Pattern oppattern = Pattern.compile("\\=\\W\\h"); //pattern to get operation from HTTP request (operation is found between = symbol and a whitespace)
        Matcher nummatcher = null;
        Matcher opmatcher = null;
        int leftOp = 0, rightOp = 0, result = 0; //variables to store operands and result
        char operation = 0; //variable to hold operation symbol

        try {
            request = in.readLine();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        nummatcher = numpattern.matcher(request);
        opmatcher = oppattern.matcher(request);

        if(nummatcher.find()){
            leftOp = Integer.parseInt(nummatcher.group().substring(1)); //assign the left operand if found
        }
        if(nummatcher.find()){
            rightOp = Integer.parseInt(nummatcher.group().substring(1)); //assign right operand if found
        }
        if(opmatcher.find()) {
            operation = opmatcher.group().charAt(1); //assign operation if found
            switch (operation) { //performs arithmetic based on operation
                case '+':
                    result = leftOp + rightOp;
                    break;
                case '-':
                    result = leftOp - rightOp;
                    break;
                case '*':
                    result = leftOp * rightOp;
                    break;
                case '/':
                    result = leftOp / rightOp;
                    break;
                case '%':
                    result = leftOp % rightOp;
                    break;
            }
        }
        //add result to json object for it to be sent back to user
        try {
            jsonObject.put("Expression", ""+leftOp+" "+operation+" "+rightOp);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        try {
            jsonObject.put("Result", result);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        System.out.println(jsonObject);
        //end of your code
        String response = msgToClient + jsonObject.toString();

        try {
            os.write(response.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            os.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}