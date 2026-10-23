package ptiny.load;
import ptiny.load.HttpConnection.Response;

class CancelTask extends HttpTask {
    private final MainActivity main;
    private final String id;
    private final int status;

    CancelTask(MainActivity activity, String id, int status){
        this.main = activity;
        this.id   = id;
        this.status = status;
    }

    @Override
    public void run() {
        HttpConnection http = main.http;
        try{
            main.refreshTask.show();
            Response res = http.queryCancel(id, status);
            String s = main.jobStatusFrom(status);
            String msg = String.format("Cancel %s > %s", toStr(s,id), res.getStatus());
            new InfoTask(main, msg).show();
            main.reloadTask.run();
        }catch(Exception e){
            handleException(e);
        }finally{
            main.refreshTask.dismiss();
        }
    }
}
