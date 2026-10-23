package ptiny.load;
import ptiny.load.HttpConnection.Response;

class PostTask extends HttpTask {
    private final MainActivity main;
    private final String text;

    PostTask(MainActivity activity, String text){
        this.main = activity;
        this.text = text;
    }

    @Override
    public void run() {
        L.d("PostTask");
        HttpConnection http = main.http;

        try{
            main.refreshTask.show();
            L.d("http.post");
            Response ret = http.post(text);
            String msg = String.format("Post > %s", ret.getStatus());
            new ToastTask(msg).show(); 
        }catch(Exception e){
            L.e(e);
            handleException(e);
        }finally{
            main.refreshTask.dismiss();
            main.finish();
        }
    }
}
