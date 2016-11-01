package ucles.weblab.common.webapi.resource;

import java.util.List;

/**
 * Used as the body of error responses.
 *
 * @since 23/03/15
 */
public class ErrorResource {
    private String summary;
    private String detail;
    
    /*a useful data structure to hold seperate object that may relate the the error being thrown*/
    private List<Object> items;

    public ErrorResource(String summary, String detail) {
        this.summary = summary;
        this.detail = detail;
    }    
    
    public ErrorResource(String summary, String detail, List<Object> items) {
        this.summary = summary;
        this.detail = detail;
        this.items = items;
    }

    public String getSummary() {
        return summary;
    }

    public String getDetail() {
        return detail;
    }

    public List<Object> getItems() {
        return items;
    }
   
}
