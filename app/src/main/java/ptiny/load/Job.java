package ptiny.load;

public class Job {
    public int status;
    public int ec;
    public String id;
    public String name;
    public String post;
    public String body;
    public String error;

    @Override
    public String toString(){
        return String.format("%d: %.5s: %.5s %.5s :%.5s",status, name, post, body, error);
    }
}
