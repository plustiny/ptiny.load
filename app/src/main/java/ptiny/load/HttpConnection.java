package ptiny.load;

import android.net.Uri;
import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.IOException;

import java.nio.charset.StandardCharsets; 
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.net.MalformedURLException;
import java.net.ConnectException; 
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public final class HttpConnection{
    public static final class Response{
        public final int code;
        public final String message;
        public final String body;
        Response(int code, String msg, String body){
            this.code = code;
            this.message = msg;
            this.body = body;
        }
        public String getStatus(){
            return code +":"+ message;
        }
    }
    final int timeout;//[ms]
    final String baseUrl;
    final String auth;

    HttpConnection(String baseUrl, String user, String password){
        this.baseUrl = baseUrl;
        String auth = user +":"+ password;
        this.auth = Base64.encodeToString(
                auth.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
        this.timeout = Pref.instance.I(R.string.pref_timeout, 20)*1000;
    }

    public Response post(String post) throws IOException, ConnectException, MalformedURLException {
        return post(baseUrl, post); 
    }

    private Response post(String url_str, String post) throws IOException, ConnectException, MalformedURLException {
        L.d("post", url_str, post);
        HttpURLConnection conn = null;
        try {
            URL url = new URL(url_str);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "text/plain; charset=UTF-8");
            conn.setRequestProperty("Authorization", "Basic " + auth);
            conn.setRequestProperty("Content-Encoding", "gzip");
            conn.setConnectTimeout(timeout);
            conn.setReadTimeout(timeout);
            L.d("conn", conn);
            
            byte[] data = post.getBytes(StandardCharsets.UTF_8);
            try(GZIPOutputStream os = new GZIPOutputStream(conn.getOutputStream())){
                os.write(data);
            }catch(Exception e){
                L.d(e);
            }
           
            byte[] buf = new byte[4096];
            try(InputStream in = conn.getInputStream()) {
                while (in.read(buf) != -1){}//ignore
            } catch (IOException e) {
                InputStream err = conn.getErrorStream();
                if(err != null){
                    try(err){
                        while(err.read(buf) != -1){}//ignore
                    }
                }
            }            
            
            int code = conn.getResponseCode();
            String msg = conn.getResponseMessage();
            L.d("response code", code, msg);
            return new Response(code, msg, null) ;
        }catch(Exception e){
            L.e(e);
            throw e;
        }
    }

    private Response get(String url_str) throws IOException, ConnectException, MalformedURLException {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(url_str);
            L.d("url", url);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Basic " + auth);
            conn.setRequestProperty("Accept-Encoding", "gzip");
            conn.setConnectTimeout(timeout);
            conn.setReadTimeout(timeout);
            L.d("conn", conn);

            StringBuilder sb = new StringBuilder();
            InputStream in = conn.getInputStream();
            if ("gzip".equalsIgnoreCase(conn.getContentEncoding())) {
                in = new GZIPInputStream(in);
            }
            try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            } catch (IOException e) {
                InputStream err = conn.getErrorStream();
                if(err != null){
                    try(err){
                        byte[] buf = new byte[4096];
                        while(err.read(buf) != -1){}//ignore
                    }
                }
            }  
            L.d("sb", sb);
            int code = conn.getResponseCode();
            String msg = conn.getResponseMessage();
            L.d("response code", code, msg);
            return new Response(code, msg, sb.toString());
        }catch(Exception e){
            L.e(e);
            throw e;
        }
    }

    private Response _query(String query, int max, String id, int status ) throws IOException, ConnectException, MalformedURLException {
        Uri.Builder b = Uri.parse(baseUrl).buildUpon();
        b = b.appendQueryParameter("query", query);
        if(0 < max){ 
            b = b.appendQueryParameter("max", String.valueOf(max));
        }
        if(id != null){ 
            b = b.appendQueryParameter("id", id);
        }
        if(0 <= status){
            b= b.appendQueryParameter("status", String.valueOf(status));
        }
        Uri uri = b.build();
        return get(uri.toString());
    }

    public Response queryMetadata() throws IOException, ConnectException, MalformedURLException {
        return _query("meta", -1, null, -1);
    }

    public Response querySelect(int max, int status ) throws IOException, ConnectException, MalformedURLException {
        return _query("select", max, null, status);
    }

    public Response queryDelete(String id, int status ) throws IOException, ConnectException, MalformedURLException {
        return _query("delete", -1, id, status);
    }

    public Response queryCancel(String id, int status ) throws IOException, ConnectException, MalformedURLException {
        return _query("cancel", -1, id, status);
    }

    public Response queryRestart(String id, int status ) throws IOException, ConnectException, MalformedURLException {
        return _query("restart", -1, id, status); 
    }
}
