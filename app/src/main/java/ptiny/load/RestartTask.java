package ptiny.load;
import ptiny.load.HttpConnection.Response;

class RestartTask extends HttpTask {
    private final MainActivity main;
    private final String id;
    private final int status;

    RestartTask(MainActivity activity, String id, int status){
        this.main = activity;
        this.id   = id;
        this.status = status;
    }

    @Override
    public void run() {
        HttpConnection http = main.http;
        try{
            main.refreshTask.show();
            Response ret = http.queryRestart(id, status);
            String s = main.jobStatusFrom(status);
            if( s == "all") s = null;
            String msg = String.format("Restart %s > %s", toStr(s,id), ret.getStatus());
            new InfoTask(main, msg).show();
            main.reloadTask.run();
        }catch(Exception e){
            handleException(e);
        }finally{
            main.refreshTask.dismiss();
        }
    }
}
