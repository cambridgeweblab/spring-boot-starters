package ucles.weblab.common.webapi.resource;

/**
 * Used as the body of error responses.
 *
 * @since 23/03/15
 */
public class ErrorResource {
    private String summary;
    private String detail;

    public ErrorResource(String summary, String detail) {
        this.summary = summary;
        this.detail = detail;
    }

    public String getSummary() {
        return summary;
    }

    public String getDetail() {
        return detail;
    }
}
