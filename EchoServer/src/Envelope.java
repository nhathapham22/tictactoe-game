
import java.io.Serializable;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Huy
 */
public class Envelope implements Serializable {
    private String id;
    private String arg;
    private Object contents;
    
    public Envelope() {
    }

    public Envelope(String id, String arg, Object contents) {
        this.setId(id);
        this.setArg(arg);
        this.setContents(contents);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getArg() {
        return arg;
    }

    public void setArg(String arg) {
        this.arg = arg;
    }

    public Object getContents() {
        return contents;
    }

    public void setContents(Object contents) {
        this.contents = contents;
    }
}
